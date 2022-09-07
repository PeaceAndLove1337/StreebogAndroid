package com.mpei.vkr.streebog_hashing.presentation

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.mpei.vkr.streebog_hashing.R

class MainActivity : AppCompatActivity() {

    private lateinit var buttonChooseFile: Button
    private lateinit var buttonGetHashOfFile: Button
    private lateinit var buttonGetHashOfText: Button
    private lateinit var editTextHashingText: EditText
    private lateinit var currentFileTextView: TextView
    private lateinit var currentFileNameTextView: TextView
    private lateinit var editTextResultOfHashing: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var radioButton256: RadioButton
    private lateinit var radioButton512: RadioButton

    private lateinit var mainViewModel: MainViewModel

    private lateinit var byteArrayOfSelectedFile: ByteArray

    private val FILE_SELECT_CODE = 1
    private val EXTERNAL_STORAGE_PERMISSION_CODE = 23

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViews()

        initButtons()
        initResultEditText()
        requestPermissions()
        initViewModel()
        initObservers()
        makeFileNameVisible(false)
    }

    private fun findViews() {
        buttonChooseFile = findViewById(R.id.button_choose_file)
        buttonGetHashOfFile = findViewById(R.id.button_hash_file)
        buttonGetHashOfText = findViewById(R.id.button_hash_text)
        editTextHashingText = findViewById(R.id.editTextHashingText)
        editTextResultOfHashing = findViewById(R.id.editTextResultOfHashing)
        radioButton256 = findViewById(R.id.radioButton256)
        radioButton512 = findViewById(R.id.radioButton512)
        progressBar = findViewById(R.id.progressBar)
        currentFileTextView = findViewById(R.id.file_name_text_view)
        currentFileNameTextView = findViewById(R.id.current_file_text_view)
    }

    private fun initViewModel() {
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
    }

    private fun initObservers() {
        mainViewModel.getHashLiveData().observe(this){
            editTextResultOfHashing.text = it
            makeToastHashIsComplete()
            makeElementsIsEnabled(true)
            progressBar.progress = 0
        }
    }

    private fun createProgressHelper()
         = ProgressHelper(progressBar, Handler())


    private fun initButtons() {
        buttonChooseFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(Intent.createChooser(intent, "Select a file to Encode"), FILE_SELECT_CODE)
            buttonGetHashOfFile.isEnabled = true
        }

        buttonGetHashOfFile.setOnClickListener {
            when {
                radioButton256.isChecked -> {
                    if (!this::byteArrayOfSelectedFile.isInitialized) {
                        makeToastNotChoosenFile()
                    } else {
                        makeElementsIsEnabled(false)
                        mainViewModel.hash256InThread(byteArrayOfSelectedFile, createProgressHelper())
                    }
                }
                radioButton512.isChecked -> {
                    if (!this::byteArrayOfSelectedFile.isInitialized) {
                        makeToastNotChoosenFile()
                    } else {
                        makeElementsIsEnabled(false)
                        mainViewModel.hash512InThread(byteArrayOfSelectedFile, createProgressHelper())
                    }
                }
                else -> {
                    makeToastHashTypeNotSelected()
                }
            }
        }

        buttonGetHashOfText.setOnClickListener {
            val textToHashing = editTextHashingText.text.toString()

            if (textToHashing.isEmpty()) {
                makeToastEmptyEditText()
            } else {
                when {
                    radioButton256.isChecked -> {
                        val textToHash = editTextHashingText.text.toString()
                        val textInByteArray = textToHash.toByteArray()
                        makeElementsIsEnabled(false)
                        mainViewModel.hash256InThread(textInByteArray, createProgressHelper())
                    }
                    radioButton512.isChecked -> {
                        val textToHash = editTextHashingText.text.toString()
                        val textInByteArray = textToHash.toByteArray()
                        makeElementsIsEnabled(false)
                        mainViewModel.hash512InThread(textInByteArray, createProgressHelper())
                    }
                    else -> {
                        makeToastHashTypeNotSelected()
                    }
                }
            }

        }
    }

    private fun initResultEditText() {
        editTextResultOfHashing.setOnClickListener {
            val clipboard: ClipboardManager = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("", editTextResultOfHashing.text.toString())
            clipboard.setPrimaryClip(clip)
            makeToastHashWasCopied()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_SELECT_CODE) {
            if (resultCode == RESULT_OK) {
                Log.i("FILE READING:", "File was correctly read")
                val uri: Uri = data?.data!!
                byteArrayOfSelectedFile = readBytes(this, uri)!!
                makeFileNameVisible(true)
                currentFileNameTextView.text = ""
                currentFileNameTextView.text = getFileName(uri)
            } else {
                Log.e("FILE READING:", "Failed to read file")
            }
        }
    }

    private fun makeElementsIsEnabled(isEnabled: Boolean) {
        buttonGetHashOfFile.isEnabled = isEnabled
        buttonGetHashOfText.isEnabled = isEnabled
        buttonChooseFile.isEnabled = isEnabled
        editTextHashingText.isEnabled = isEnabled
        editTextResultOfHashing.isEnabled = isEnabled
    }

    private fun makeFileNameVisible(isEnabled: Boolean) {
        currentFileTextView.isVisible = isEnabled
        currentFileNameTextView.isVisible = isEnabled
    }

    private fun readBytes(context: Context, uri: Uri): ByteArray? =
        context.contentResolver.openInputStream(uri)?.buffered()?.use { it.readBytes() }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            EXTERNAL_STORAGE_PERMISSION_CODE
        )
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            cursor.use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }

    private fun makeToastEmptyEditText() {
        Toast.makeText(this, "Текст для хеширования пуст!", Toast.LENGTH_LONG).show()
    }

    private fun makeToastNotChoosenFile() {
        Toast.makeText(this, "Не выбран файл для хеширования!", Toast.LENGTH_LONG).show()
    }

    private fun makeToastHashTypeNotSelected() {
        Toast.makeText(this, "Не выбран режим хеширования!", Toast.LENGTH_LONG).show()
    }

    private fun makeToastHashWasCopied() {
        Toast.makeText(this, "Скопировано в буфер обмена!", Toast.LENGTH_SHORT).show()
    }

    private fun makeToastHashIsComplete() {
        Toast.makeText(this, "Хеширование произведено!", Toast.LENGTH_SHORT).show()
    }
}
package com.mpei.vkr.streebog_hashing.presentation.views

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
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.mpei.vkr.streebog_hashing.R
import com.mpei.vkr.streebog_hashing.presentation.ProgressHelper
import com.mpei.vkr.streebog_hashing.presentation.viewmodels.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var buttonChooseFile: Button
    private lateinit var buttonGetHashOfFile: Button
    private lateinit var currentFileTextView: TextView
    private lateinit var currentFileNameTextView: TextView
    private lateinit var editTextResultOfHashing: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var radioButton256: RadioButton
    private lateinit var radioButton512: RadioButton

    private lateinit var radioButtonNonOptimized: RadioButton
    private lateinit var radioButtonOptimized: RadioButton
    private lateinit var radioButtonOnBack: RadioButton
    private lateinit var radioButtonOnBackSteganography: RadioButton

    private lateinit var waitBar: ProgressBar

    private lateinit var mainViewModel: MainViewModel

    private lateinit var byteArrayOfSelectedFile: ByteArray

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
        editTextResultOfHashing = findViewById(R.id.editTextResultOfHashing)
        radioButton256 = findViewById(R.id.radioButton256)
        radioButton512 = findViewById(R.id.radioButton512)
        progressBar = findViewById(R.id.progressBar)
        currentFileTextView = findViewById(R.id.file_name_text_view)
        currentFileNameTextView = findViewById(R.id.current_file_text_view)
        radioButtonNonOptimized = findViewById(R.id.radioButtonNonOptimizedRealization)
        radioButtonOptimized = findViewById(R.id.radioButtonOptimizedRealization)
        radioButtonOnBack = findViewById(R.id.radioButtonBackendOptimizedRealization)
        radioButtonOnBackSteganography = findViewById(R.id.radioButtonBackendOptimizedSteganography)
        waitBar = findViewById(R.id.wait_bar)
    }

    private fun initViewModel() {
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
    }

    private fun initObservers() {
        mainViewModel.getHashLiveData().observe(this) {
            waitBar.visibility = View.GONE
            editTextResultOfHashing.text = it
            makeToastHashIsComplete()
            makeElementsIsEnabled(true)
            progressBar.progress = 0
        }
    }

    private fun createProgressHelper() = ProgressHelper(progressBar, Handler())

    private fun initButtons() {
        buttonChooseFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(Intent.createChooser(intent, "Select a file to Encode"), FILE_SELECT_CODE)
            buttonGetHashOfFile.isEnabled = true
        }
        buttonGetHashOfFile.setOnClickListener {
            if (!this::byteArrayOfSelectedFile.isInitialized) {
                makeToastNotChosenFile()
            } else {
                if (!radioButton256.isChecked && !radioButton512.isChecked) {
                    makeToastModeNotSelected()
                } else {
                    makeElementsIsEnabled(false)
                    waitBar.visibility = View.VISIBLE
                    when {
                        radioButtonNonOptimized.isChecked -> {
                            when {
                                radioButton256.isChecked -> mainViewModel.hash256NonOptimized(
                                    byteArrayOfSelectedFile,
                                    createProgressHelper()
                                )
                                radioButton512.isChecked -> mainViewModel.hash512NonOptimized(
                                    byteArrayOfSelectedFile,
                                    createProgressHelper()
                                )
                            }
                        }
                        radioButtonOptimized.isChecked -> {
                            when {
                                radioButton256.isChecked -> mainViewModel.hash256Optimized(
                                    byteArrayOfSelectedFile,
                                    createProgressHelper()
                                )
                                radioButton512.isChecked -> mainViewModel.hash512Optimized(
                                    byteArrayOfSelectedFile,
                                    createProgressHelper()
                                )
                            }
                        }
                        radioButtonOnBack.isChecked -> {
                            when {
                                radioButton256.isChecked -> mainViewModel.hash256DefaultByBackend(
                                    byteArrayOfSelectedFile
                                )
                                radioButton512.isChecked -> mainViewModel.hash512DefaultByBackend(
                                    byteArrayOfSelectedFile
                                )
                            }
                        }
                        radioButtonOnBackSteganography.isChecked -> {
                            when {
                                radioButton256.isChecked -> mainViewModel.hash256SteganographyByBackend(
                                    byteArrayOfSelectedFile
                                )
                                radioButton512.isChecked -> mainViewModel.hash512SteganographyByBackend(
                                    byteArrayOfSelectedFile
                                )
                            }
                        }
                        else -> {
                            makeToastHashTypeNotSelected()
                        }
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
        buttonChooseFile.isEnabled = isEnabled
        radioButtonOnBackSteganography.isEnabled = isEnabled
        radioButtonOnBack.isEnabled = isEnabled
        radioButton256.isEnabled = isEnabled
        radioButton512.isEnabled = isEnabled
        radioButtonOptimized.isEnabled = isEnabled
        radioButtonNonOptimized.isEnabled = isEnabled
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

    private fun makeToastNotChosenFile() {
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

    private fun makeToastModeNotSelected() {
        Toast.makeText(this, "Не выбрана реализация хеширования!", Toast.LENGTH_LONG).show()
    }

    private companion object {
        const val FILE_SELECT_CODE = 1
        const val EXTERNAL_STORAGE_PERMISSION_CODE = 23
    }
}
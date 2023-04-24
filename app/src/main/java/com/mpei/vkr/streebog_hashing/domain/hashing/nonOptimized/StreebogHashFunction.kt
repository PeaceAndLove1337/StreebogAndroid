import StreebogAdditionalFunctions.Functions.addTwoULongArraysMod512
import StreebogAdditionalFunctions.Functions.convertFromUByteArrayToUInt
import StreebogAdditionalFunctions.Functions.convertFromUByteArrayToULong
import StreebogAdditionalFunctions.Functions.convertFromUIntArrayToULong
import StreebogAdditionalFunctions.Functions.convertFromUIntToUByteArray
import StreebogAdditionalFunctions.Functions.convertFromULongToUByteArray
import StreebogAdditionalFunctions.Functions.convertFromULongToUIntArray
import StreebogAdditionalFunctions.Functions.convertHexToBin
import StreebogAdditionalFunctions.Functions.xorTwoULongArrays
import java.util.Arrays

// Один блок входных данных - 512 бит.
// Размер выходного хэш кода 256 бит или 512 бит (на выбор).
class StreebogHashFunction {
    companion object Hashing {

        //region constants boxes etc..
        private val sBox: Array<UByte> = arrayOf(
            0xFCU, 0xEEU, 0xDDU, 0x11U, 0xCFU, 0x6EU, 0x31U, 0x16U,
            0xFBU, 0xC4U, 0xFAU, 0xDAU, 0x23U, 0xC5U, 0x04U, 0x4DU,
            0xE9U, 0x77U, 0xF0U, 0xDBU, 0x93U, 0x2EU, 0x99U, 0xBAU,
            0x17U, 0x36U, 0xF1U, 0xBBU, 0x14U, 0xCDU, 0x5FU, 0xC1U,
            0xF9U, 0x18U, 0x65U, 0x5AU, 0xE2U, 0x5CU, 0xEFU, 0x21U,
            0x81U, 0x1CU, 0x3CU, 0x42U, 0x8BU, 0x01U, 0x8EU, 0x4FU,
            0x05U, 0x84U, 0x02U, 0xAEU, 0xE3U, 0x6AU, 0x8FU, 0xA0U,
            0x06U, 0x0BU, 0xEDU, 0x98U, 0x7FU, 0xD4U, 0xD3U, 0x1FU,
            0xEBU, 0x34U, 0x2CU, 0x51U, 0xEAU, 0xC8U, 0x48U, 0xABU,
            0xF2U, 0x2AU, 0x68U, 0xA2U, 0xFDU, 0x3AU, 0xCEU, 0xCCU,
            0xB5U, 0x70U, 0x0EU, 0x56U, 0x08U, 0x0CU, 0x76U, 0x12U,
            0xBFU, 0x72U, 0x13U, 0x47U, 0x9CU, 0xB7U, 0x5DU, 0x87U,
            0x15U, 0xA1U, 0x96U, 0x29U, 0x10U, 0x7BU, 0x9AU, 0xC7U,
            0xF3U, 0x91U, 0x78U, 0x6FU, 0x9DU, 0x9EU, 0xB2U, 0xB1U,
            0x32U, 0x75U, 0x19U, 0x3DU, 0xFFU, 0x35U, 0x8AU, 0x7EU,
            0x6DU, 0x54U, 0xC6U, 0x80U, 0xC3U, 0xBDU, 0x0DU, 0x57U,
            0xDFU, 0xF5U, 0x24U, 0xA9U, 0x3EU, 0xA8U, 0x43U, 0xC9U,
            0xD7U, 0x79U, 0xD6U, 0xF6U, 0x7CU, 0x22U, 0xB9U, 0x03U,
            0xE0U, 0x0FU, 0xECU, 0xDEU, 0x7AU, 0x94U, 0xB0U, 0xBCU,
            0xDCU, 0xE8U, 0x28U, 0x50U, 0x4EU, 0x33U, 0x0AU, 0x4AU,
            0xA7U, 0x97U, 0x60U, 0x73U, 0x1EU, 0x00U, 0x62U, 0x44U,
            0x1AU, 0xB8U, 0x38U, 0x82U, 0x64U, 0x9FU, 0x26U, 0x41U,
            0xADU, 0x45U, 0x46U, 0x92U, 0x27U, 0x5EU, 0x55U, 0x2FU,
            0x8CU, 0xA3U, 0xA5U, 0x7DU, 0x69U, 0xD5U, 0x95U, 0x3BU,
            0x07U, 0x58U, 0xB3U, 0x40U, 0x86U, 0xACU, 0x1DU, 0xF7U,
            0x30U, 0x37U, 0x6BU, 0xE4U, 0x88U, 0xD9U, 0xE7U, 0x89U,
            0xE1U, 0x1BU, 0x83U, 0x49U, 0x4CU, 0x3FU, 0xF8U, 0xFEU,
            0x8DU, 0x53U, 0xAAU, 0x90U, 0xCAU, 0xD8U, 0x85U, 0x61U,
            0x20U, 0x71U, 0x67U, 0xA4U, 0x2DU, 0x2BU, 0x09U, 0x5BU,
            0xCBU, 0x9BU, 0x25U, 0xD0U, 0xBEU, 0xE5U, 0x6CU, 0x52U,
            0x59U, 0xA6U, 0x74U, 0xD2U, 0xE6U, 0xF4U, 0xB4U, 0xC0U,
            0xD1U, 0x66U, 0xAFU, 0xC2U, 0x39U, 0x4BU, 0x63U, 0xB6U
        )

        private val permutationBox: Array<UByte> = arrayOf(
            0x00U, 0x08U, 0x10U, 0x18U, 0x20U, 0x28U, 0x30U, 0x38U,
            0x01U, 0x09U, 0x11U, 0x19U, 0x21U, 0x29U, 0x31U, 0x39U,
            0x02U, 0x0aU, 0x12U, 0x1aU, 0x22U, 0x2aU, 0x32U, 0x3aU,
            0x03U, 0x0bU, 0x13U, 0x1bU, 0x23U, 0x2bU, 0x33U, 0x3bU,
            0x04U, 0x0cU, 0x14U, 0x1cU, 0x24U, 0x2cU, 0x34U, 0x3cU,
            0x05U, 0x0dU, 0x15U, 0x1dU, 0x25U, 0x2dU, 0x35U, 0x3dU,
            0x06U, 0x0eU, 0x16U, 0x1eU, 0x26U, 0x2eU, 0x36U, 0x3eU,
            0x07U, 0x0fU, 0x17U, 0x1fU, 0x27U, 0x2fU, 0x37U, 0x3fU
        )

        private val matrixA: Array<ULong> = arrayOf(
            0x8e20faa72ba0b470U, 0x47107ddd9b505a38U, 0xad08b0e0c3282d1cU,
            0xd8045870ef14980eU, 0x6c022c38f90a4c07U, 0x3601161cf205268dU, 0x1b8e0b0e798c13c8U, 0x83478b07b2468764U,
            0xa011d380818e8f40U, 0x5086e740ce47c920U, 0x2843fd2067adea10U, 0x14aff010bdd87508U, 0x0ad97808d06cb404U,
            0x05e23c0468365a02U, 0x8c711e02341b2d01U, 0x46b60f011a83988eU, 0x90dab52a387ae76fU, 0x486dd4151c3dfdb9U,
            0x24b86a840e90f0d2U, 0x125c354207487869U, 0x092e94218d243cbaU, 0x8a174a9ec8121e5dU, 0x4585254f64090fa0U,
            0xaccc9ca9328a8950U, 0x9d4df05d5f661451U, 0xc0a878a0a1330aa6U, 0x60543c50de970553U, 0x302a1e286fc58ca7U,
            0x18150f14b9ec46ddU, 0x0c84890ad27623e0U, 0x0642ca05693b9f70U, 0x0321658cba93c138U, 0x86275df09ce8aaa8U,
            0x439da0784e745554U, 0xafc0503c273aa42aU, 0xd960281e9d1d5215U, 0xe230140fc0802984U, 0x71180a8960409a42U,
            0xb60c05ca30204d21U, 0x5b068c651810a89eU, 0x456c34887a3805b9U, 0xac361a443d1c8cd2U, 0x561b0d22900e4669U,
            0x2b838811480723baU, 0x9bcf4486248d9f5dU, 0xc3e9224312c8c1a0U, 0xeffa11af0964ee50U, 0xf97d86d98a327728U,
            0xe4fa2054a80b329cU, 0x727d102a548b194eU, 0x39b008152acb8227U, 0x9258048415eb419dU, 0x492c024284fbaec0U,
            0xaa16012142f35760U, 0x550b8e9e21f7a530U, 0xa48b474f9ef5dc18U, 0x70a6a56e2440598eU, 0x3853dc371220a247U,
            0x1ca76e95091051adU, 0x0edd37c48a08a6d8U, 0x07e095624504536cU, 0x8d70c431ac02a736U, 0xc83862965601dd1bU,
            0x641c314b2b8ee083U
        )


        private val constants: Array<Array<ULong>> = arrayOf(
            arrayOf(
                0xb1085bda1ecadae9U, 0xebcb2f81c0657c1fU, 0x2f6a76432e45d016U,
                0x714eb88d7585c4fcU, 0x4b7ce09192676901U, 0xa2422a08a460d315U, 0x05767436cc744d23U, 0xdd806559f2a64507U
            ),
            arrayOf(
                0x6fa3b58aa99d2f1aU, 0x4fe39d460f70b5d7U, 0xf3feea720a232b98U,
                0x61d55e0f16b50131U, 0x9ab5176b12d69958U, 0x5cb561c2db0aa7caU,
                0x55dda21bd7cbcd56U, 0xe679047021b19bb7U
            ),
            arrayOf(
                0xf574dcac2bce2fc7U, 0x0a39fc286a3d8435U, 0x06f15e5f529c1f8bU,
                0xf2ea7514b1297b7bU, 0xd3e20fe490359eb1U, 0xc1c93a376062db09U,
                0xc2b6f443867adb31U, 0x991e96f50aba0ab2U
            ),
            arrayOf(
                0xef1fdfb3e81566d2U, 0xf948e1a05d71e4ddU, 0x488e857e335c3c7dU,
                0x9d721cad685e353fU, 0xa9d72c82ed03d675U, 0xd8b71333935203beU,
                0x3453eaa193e837f1U, 0x220cbebc84e3d12eU
            ),
            arrayOf(
                0x4bea6bacad474799U, 0x9a3f410c6ca92363U, 0x7f151c1f1686104aU, 0x359e35d7800fffbdU,
                0xbfcd1747253af5a3U, 0xdfff00b723271a16U,
                0x7a56a27ea9ea63f5U, 0x601758fd7c6cfe57U
            ),
            arrayOf(
                0xae4faeae1d3ad3d9U, 0x6fa4c33b7a3039c0U, 0x2d66c4f95142a46cU,
                0x187f9ab49af08ec6U, 0xcffaa6b71c9ab7b4U, 0x0af21f66c2bec6b6U,
                0xbf71c57236904f35U, 0xfa68407a46647d6eU
            ),
            arrayOf(
                0xf4c70e16eeaac5ecU, 0x51ac86febf240954U, 0x399ec6c7e6bf87c9U, 0xd3473e33197a93c9U,
                0x0992abc52d822c37U, 0x06476983284a0504U, 0x3517454ca23c4af3U, 0x8886564d3a14d493U
            ),
            arrayOf(
                0x9b1f5b424d93c9a7U, 0x03e7aa020c6e4141U, 0x4eb7f8719c36de1eU, 0x89b4443b4ddbc49aU,
                0xf4892bcb929b0690U, 0x69d18d2bd1a5c42fU, 0x36acc2355951a8d9U, 0xa47f0dd4bf02e71eU
            ),
            arrayOf(
                0x378f5a541631229bU, 0x944c9ad8ec165fdeU, 0x3a7d3a1b25894224U, 0x3cd955b7e00d0984U,
                0x800a440bdbb2ceb1U, 0x7b2b8a9aa6079c54U, 0x0e38dc92cb1f2a60U, 0x7261445183235adbU
            ),
            arrayOf(
                0xabbedea680056f52U, 0x382ae548b2e4f3f3U, 0x8941e71cff8a78dbU, 0x1fffe18a1b336103U,
                0x9fe76702af69334bU, 0x7a1e6c303b7652f4U, 0x3698fad1153bb6c3U, 0x74b4c7fb98459cedU
            ),
            arrayOf(
                0x7bcd9ed0efc889fbU, 0x3002c6cd635afe94U, 0xd8fa6bbbebab0761U, 0x2001802114846679U,
                0x8a1d71efea48b9caU, 0xefbacd1d7d476e98U, 0xdea2594ac06fd85dU, 0x6bcaa4cd81f32d1bU,
            ),
            arrayOf(
                0x378ee767f11631baU, 0xd21380b00449b17aU, 0xcda43c32bcdf1d77U,
                0xf82012d430219f9bU, 0x5d80ef9d1891cc86U, 0xe71da4aa88e12852U, 0xfaf417d5d9b21b99U, 0x48bc924af11bd720U,
            )

        )

        val initVector256: Array<ULong> = arrayOf(
            0x0101010101010101U,
            0x0101010101010101U,
            0x0101010101010101U,
            0x0101010101010101U,
            0x0101010101010101U,
            0x0101010101010101U,
            0x0101010101010101U,
            0x0101010101010101U
        )
        val initVector512: Array<ULong> = arrayOf(
            0x0000000000000000U,
            0x0000000000000000U,
            0x0000000000000000U,
            0x0000000000000000U,
            0x0000000000000000U,
            0x0000000000000000U,
            0x0000000000000000U,
            0x0000000000000000U
        )
        val initControlSum = initVector512
        val initLengthOfControlSum = initVector512
        //endregion

        //region S-Преобразование(Подстановка)
        //S-преобразование (принимает 64 байта (или 512 бит), 16 UInt'ов) (Подстановка)
        fun substitutionTransformation(inputArray: Array<UByte>): Array<UByte> {
            val result = Array<UByte>(inputArray.size) { 0U }
            for (i in inputArray.indices) {
                result[i] = sBox[inputArray[i].toInt()]
            }
            return result
        }

        fun substitutionTransformation(inputArray: Array<UInt>): Array<UInt> {
            val result = Array(inputArray.size) { 0U }
            for (i in inputArray.indices) {
                val uIntInArrayOfUByte = convertFromUIntToUByteArray(inputArray[i])
                val intermediateResult = substitutionTransformation(uIntInArrayOfUByte)
                result[i] = convertFromUByteArrayToUInt(intermediateResult)
            }
            return result
        }

        fun substitutionTransformation(inputArray: Array<ULong>): Array<ULong> {
            val result = Array<ULong>(inputArray.size) { 0U }
            for (i in inputArray.indices) {
                val uLongInArrayOfUByte = convertFromULongToUByteArray(inputArray[i])
                val intermediateResult = substitutionTransformation(uLongInArrayOfUByte)
                result[i] = convertFromUByteArrayToULong(intermediateResult)
            }
            return result
        }
        //endregion

        //region P-Преобразование(Перестановка)
        //Перестановка ПРОИЗВОДИТСЯ ИМЕННО В UINT!!!
        fun permutationTransformation(inputArray: Array<UInt>): Array<UInt> {
            val result = Array(inputArray.size) { 0U }
            var inUByteArray = Array<UByte>(0) { 0U }
            for (i in inputArray.indices) {
                inUByteArray = inUByteArray.plus(convertFromUIntToUByteArray(inputArray[i]))
            }

            for (i in inputArray.indices) {
                result[i] = convertFromUByteArrayToUInt(
                    arrayOf(
                        inUByteArray[permutationBox[i * 4].toInt()],
                        inUByteArray[permutationBox[i * 4 + 1].toInt()],
                        inUByteArray[permutationBox[i * 4 + 2].toInt()],
                        inUByteArray[permutationBox[i * 4 + 3].toInt()]
                    )
                )
            }
            return result
        }

        fun permutationTransformation(inputArray: Array<ULong>): Array<ULong> {
            var inUInt: Array<UInt> = Array(0) { 0U }
            inputArray.forEach { inUInt = inUInt.plus(convertFromULongToUIntArray(it)) }

            val resultOfPermutation = permutationTransformation(inUInt)

            var result: Array<ULong> = Array(0) { 0U }
            for (i in resultOfPermutation.indices step 2) {
                result = result.plus(convertFromUIntArrayToULong(resultOfPermutation.sliceArray(i..i + 1)))
            }
            return result
        }
        //endregion


        //region L-Преобразование(Линейное преобразование)
        //L-Преобразование (принимает 64 байта (или 512 бит)) (Перемножение на матрицу в Поле Галуа)
        fun linearTransformation(inputArray: Array<ULong>): Array<ULong> {
            val result: Array<ULong> = Array(inputArray.size) { 0U }

            for (i in inputArray.indices) {
                val uLongInBinaryString = convertHexToBin(inputArray[i])
                var intermediateResult: ULong = 0U
                uLongInBinaryString.forEachIndexed { index, elem ->
                    if (elem == '1') {
                        intermediateResult = intermediateResult xor matrixA[index]
                    }
                }
                result[i] = intermediateResult
            }
            return result
        }
        //endregion


        //region Функция расширения ключей
        //Функция расширения ключей(Генерация временного ключа)
        fun keySchedule(inputArray: Array<ULong>, numberOfConst: Int): Array<ULong> {
            var result = xorTwoULongArrays(inputArray, constants[numberOfConst])

            result = substitutionTransformation(result)


            result = permutationTransformation(result)

            result = linearTransformation(result)

            return result
        }
        //endregion


        //region Функция E(K,M)
        fun e_TransformationV2(inputArray: Array<ULong>, mArray: Array<ULong>): Array<ULong> {
            var state = mArray
            var k: Array<ULong> = inputArray

            for (i in 0..11) {
                state = xorTwoULongArrays(state, k)
                state = substitutionTransformation(state)
                state = permutationTransformation(state)
                state = linearTransformation(state)
                k = keySchedule(k, i)

            }
            state = xorTwoULongArrays(state, k)
            return state
        }
        //endregion


        //region Функция сжатия g(h,m,N)
        // inputArray - иницилизирующий вектор, mArray- вектор данных, arrayN - длина сообщения
        fun compressionFunction(inputArray: Array<ULong>, mArray: Array<ULong>, arrayN: Array<ULong>): Array<ULong> {
            var result = xorTwoULongArrays(inputArray, arrayN)
            result = substitutionTransformation(result)
            result = permutationTransformation(result)
            result = linearTransformation(result)
            val resultOfE_Function = e_TransformationV2(result, mArray)
            result = xorTwoULongArrays(resultOfE_Function, inputArray)
            result = xorTwoULongArrays(result, mArray)

            return result
        }

        //endregion

        //region Хэш Функция
        private fun getHash(inputArray: Array<UByte>, initVectorH: Array<ULong>): Array<UByte> {
            var message = inputArray
            var h = initVectorH
            var nLength = initLengthOfControlSum
            var sigmaSum = initControlSum
            lateinit var currentBlock: Array<ULong>
            var blockNo =0
            while (message.size > 64) {
                val currentSize = message.size
                currentBlock = Array<ULong>(0) { 0U }
                val inUByteCurrentBlock = message.sliceArray(currentSize - 64 until currentSize)

                //Преобразование из 64 UByte в 8 ULong
                for (i in inUByteCurrentBlock.indices step 8) {
                    val currentUnderBlock = convertFromUByteArrayToULong(inUByteCurrentBlock.sliceArray(i..i + 7))
                    currentBlock = currentBlock.plus(currentUnderBlock)
                }

                h = compressionFunction(h, currentBlock, nLength)
                message = message.sliceArray(0..currentSize - 65)
                nLength = addTwoULongArraysMod512(
                    nLength,
                    arrayOf(0x0U, 0x0U, 0x0U, 0x0U, 0x0U, 0x0U, 0x0U, 0x200U)
                )
                sigmaSum = addTwoULongArraysMod512(sigmaSum, currentBlock)
                blockNo+=1
                // блок если сообщение больще 512 бит
            }
            if (message.isNotEmpty()) {
                val currentLength = (message.size * 8).toULong()
                message = supplementBlock(message)
                currentBlock = Array<ULong>(0) { 0U }
                //Преобразование из 64 UByte в 8 ULong
                for (i in message.indices step 8) {
                    val currentUnderBlock = convertFromUByteArrayToULong(message.sliceArray(i..i + 7))
                    currentBlock = currentBlock.plus(currentUnderBlock)
                }
                h = compressionFunction(h, currentBlock, nLength)

                val newLength = arrayOf<ULong>(0x0U, 0x0U, 0x0U, 0x0U, 0x0U, 0x0U, 0x0U, currentLength)
                nLength = addTwoULongArraysMod512(
                    nLength,
                    newLength
                )

                sigmaSum = addTwoULongArraysMod512(sigmaSum, currentBlock)
                h = compressionFunction(h, nLength, initVector512)
                h = compressionFunction(h, sigmaSum, initVector512)
                // обработка недополненного блока
            }

            var result = Array<UByte>(0) { 0U }

            h.forEach {
                result = result.plus(convertFromULongToUByteArray(it))
            }


            return result
        }

        fun supplementBlock(inputArray: Array<UByte>): Array<UByte> {
            var result = arrayOf<UByte>(0x01U).plus(inputArray)
            while (result.size != 64) {
                result = arrayOf<UByte>(0x00U).plus(result)
            }
            return result
        }

        //Принимает UByte array, они конвертятся и все отправляется в getCurrentHash
        fun getHash512(inputArray: Array<UByte>): Array<UByte> {
            return getHash(inputArray, initVector512)
        }



        fun getHash256(inputArray: Array<UByte>): Array<UByte> {
            val hash = getHash(inputArray, initVector256)
            //hash.forEach { print(it.toString(16)) }
            return hash.sliceArray(0 until 32)
        }
        //endregion

    }
}
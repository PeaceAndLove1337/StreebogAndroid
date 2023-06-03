import java.math.BigInteger

class StreebogAdditionalFunctions {
    companion object Functions {

        //Конвертация из ULong в Array<UByte>, т.е. исходное число из 8 байт разбивается на 8 однобайтовых массивов
        fun convertFromULongToUByteArray(inputUInt: ULong): Array<UByte> {
            var inString = inputUInt.toString(16)
            while (inString.length != 16) {
                inString = "0$inString"
            }
            val result = inString.chunked(2).map { it.toUByte(16) }.toTypedArray()
            return result
        }

        //Конвертация из ULong в Array<UInt>, т.е. исходное число из 8 байт разбивается на два четырёхбайтных массива
        fun convertFromULongToUIntArray(inputULong: ULong): Array<UInt> {
            var inString = inputULong.toString(16)
            while (inString.length != 16) {
                inString = "0$inString"
            }
            val result = inString.chunked(8).map { it.toUInt(16) }.toTypedArray()
            return result
        }

        //Конвертация из Array<Uint> в ULong
        fun convertFromUIntArrayToULong(inputUInt: Array<UInt>): ULong {
            var result = ""
            inputUInt.forEach {
                var inString = it.toString(16)
                while (inString.length != 8) {
                    inString = "0$inString"
                }
                result += inString
            }
            return result.toULong(16)
        }

        //Конвертация из  Array<UByte> в ULong
        fun convertFromUByteArrayToULong(inputUInt: Array<UByte>): ULong {
            var result = ""
            inputUInt.forEach {
                var inString = it.toString(16)
                while (inString.length != 2) {
                    inString = "0$inString"
                }
                result += inString
            }
            return result.toULong(16)
        }

        //Конвертация из UInt в Array<UByte>
        fun convertFromUIntToUByteArray(inputUInt: UInt): Array<UByte> {
            var inString = inputUInt.toString(16)
            while (inString.length != 8) {
                inString = "0$inString"
            }
            val result = inString.chunked(2).map { it.toUByte(16) }.toTypedArray()
            return result
        }

        fun convertFromUByteArrayToUIntArray(uByteArray: Array<UByte>): Array<UInt> {
            var result = Array<UInt>(0) { 0U }
            for (i in uByteArray.indices step 4) {
                val currentBlock = uByteArray.sliceArray(i..i + 3)
                result = result.plus(convertFromUByteArrayToUInt(currentBlock))
            }
            return result
        }

        //Конвертация из Array<UByte> в UInt
        fun convertFromUByteArrayToUInt(inputUInt: Array<UByte>): UInt {
            var result = ""
            inputUInt.forEach {
                var inString = it.toString(16)
                while (inString.length != 2) {
                    inString = "0$inString"
                }
                result += inString
            }
            return result.toUInt(16)
        }

        //Сложение по модулю 2^512
        fun addTwoULongArraysMod512(firstArray: Array<ULong>, secondArray: Array<ULong>): Array<ULong> {

            var inUByteFirstArray: Array<UByte> = Array(0) { 0U }
            firstArray.map { convertFromULongToUByteArray(it) }.forEach {
                inUByteFirstArray = inUByteFirstArray.plus(it)
            }

            var inUByteSecondArray: Array<UByte> = Array(0) { 0U }
            secondArray.map { convertFromULongToUByteArray(it) }.forEach {
                inUByteSecondArray = inUByteSecondArray.plus(it)
            }

            val resultOfUByteAdding = addArraysMod512(inUByteFirstArray.toUByteArray().toByteArray(),
                inUByteSecondArray.toUByteArray().toByteArray()).toUByteArray().toTypedArray()

            var result: Array<ULong> = Array(0) { 0U }

            for (i in 0..63 step 8) {
                val convertedToULong = convertFromUByteArrayToULong(resultOfUByteAdding.sliceArray(i..i + 7))
                result = result.plus(convertedToULong)
            }
            return result
        }

        private fun addArraysMod512(array1: ByteArray, array2: ByteArray): ByteArray {
            val bigInt1 = BigInteger(1, array1)
            val bigInt2 = BigInteger(1, array2)
            val mod = BigInteger("2").pow(512)
            val sum = bigInt1.add(bigInt2).mod(mod)
            val res = sum.toByteArray()
            if (res.size < 64) {
                val res1 = ByteArray(64)
                System.arraycopy(res, 0, res1, 64 - res.size, res.size)
                return res1
            }
            return res
        }

        fun addTwoUByteArraysMod512(firstArray: Array<UByte>, secondArray: Array<UByte>): Array<UByte> {
            val result: Array<UByte> = Array(firstArray.size) { 0U }
            var t: UInt = 0U
            for (i in 63 downTo 0) {
                t = firstArray[i] + secondArray[i] + (t shr 8)
                result[i] = (t and 0xFFU).toUByte()
            }

            return result
        }

        fun xorTwoUByteArrays(firstArray: Array<UByte>, secondArray: Array<UByte>): Array<UByte> {
            val result: Array<UByte> = Array(firstArray.size) { 0U }
            for (i in firstArray.indices)
                result[i] = firstArray[i] xor secondArray[i]
            return result
        }

        fun xorTwoULongArrays(firstArray: Array<ULong>, secondArray: Array<ULong>): Array<ULong> {
            val result: Array<ULong> = Array(firstArray.size) { 0U }
            for (i in firstArray.indices)
                result[i] = firstArray[i] xor secondArray[i]
            return result
        }

        //Конвертация из 16 системы в двоичную без потери нулей вначале
        fun convertHexToBin(inputUByte: UByte): String {
            var result = inputUByte.toString(2)
            while (result.length != 8) {
                result = "0$result"
            }
            return result
        }

        fun convertHexToBin(inputULong: ULong): String {
            var result = inputULong.toString(2)
            while (result.length != 64) {
                result = "0$result"
            }
            return result
        }

        fun convertHexToStringWithoutLosingZeros(inputUByte: UByte): String {
            var result = inputUByte.toString(16)
            while (result.length != 2) {
                result = "0$result"
            }
            return result
        }
    }
}
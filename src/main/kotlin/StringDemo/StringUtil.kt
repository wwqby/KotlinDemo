package StringDemo

/**
 * 测试函数入口
 */
fun main(args: Array<String>) {
    StringUtil().testStringNull()
}

/**
 * 字符串处理类
 * 功能1:演示String.format功能{@see getDoubleWithPlus()}
 * 功能2:演示了分解处理TLV功能{@see getTLV()}
 */
class StringUtil {


    /**
     * 测试了如果是空对象,会打印null字符串
     */
    fun testStringNull() {
        val data: String? = null
        val data2: Int? = data?.length
        println("data2=$data2")
    }


    /**
     * 测试String的trim()方法
     * trim()方法会去掉开头和结尾连续的空格,文字间隔的空格不处理
     */
    fun testStringTrim() {
        println("    SUCCESS AND FAIL.    ".trim())
    }

    /**
     * 演示format用法
     * 打印带正负符号/带分隔符/带指定长度的字符串
     */
    fun getDoubleWithPlus() {
        val str = String.format("%+10f/+10%f", 1.20394857, 2.10394857)
        println(str)
    }

    fun getTlV() {
        var data =
            "00020101021229300012D156000000000510A93FO3230Q31280012D15600000001030812345678520441115802CN5914BEST TRANSPORT6007BEIJING64200002ZH0104最佳运输0202北京540523.7253031565502016233030412340603***0708A60086670902ME91320016A0112233449988770708123456786304A13A"
        while (data.length > 0) {
            val tag = data.substring(0, 2)
            val length = data.substring(2, 4).toInt()
            val value = data.substring(4, 4 + length)
            println("读取数据\n[tag]=$tag\n[length]=$length\n[value]=$value")
            data = data.substring(4 + length)
        }
    }

    /**
     * 演示格式化字符串效果
     * 汉字计算length时与英文字符一样,但是实际占用宽度,大致相当于两个英文字符
     */
    fun formatString() {
        val source = "汉字"
        val len = "1234567890"
        println(String.format("%10s", source))
        println(String.format("%-10s", source))
        println(len)
    }

    fun formatStringWithLength() {
        val len = "1234567890"
        println(len)
        println(String.format("%8s", len))
        println(String.format("%15s", len))
        println(String.format("%-15s", len))
    }
}


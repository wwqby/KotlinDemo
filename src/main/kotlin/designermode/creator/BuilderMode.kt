package designermode.creator


/**
 * 建造者模式
 * 利用具名的可选参数来实现建造者特性
 */
class Robot private constructor(
    //名称
    val code: String,
    //电量
    var battery: String?,
    //高度
    var height: Int?,
    //宽度
    var weight: Int?,
) {

    override fun toString(): String {
        return """
            robot参数
            code:[$code]
            battery:[$battery]
            height:[$height]
            weight:[$weight]
        """.trimIndent()
    }

    data class RobotBuilder(
        val code: String,
    ) {
        var battery: String? = null
        var height: Int? = null
        var weight: Int? = null

        fun build(): Robot {
            return Robot(
                code = code,
                battery = battery,
                height = height,
                weight = weight
            )
        }
    }
}

/**
 * 利用具名可选参数来模拟建造者
 * 在构造器里,属性赋值可选值,比如null,实际初始化时,会优先初始化传入值,没有传入值时,会用可选值初始化.val只会初始化一次
 * 在类body里为val属性赋值初始值,代表该属性已经初始化,不能重新赋值,如果需要支持赋值,就要放到构造器里
 */
class Robot2(
    //名称
    val code: String,
    //电量
    val battery: String? = null,
    //高度
    val height: Int? = null,
    //宽度
    val weight: Int? = null,
) {
    init {
        /**
         * 通过require关键字对初始化参数进行条件约束
         * 类似java里得assert
         */
        require(weight != null || height != null) {
            "weight[$weight] and height[$height] 不能为null"
        }
    }

    override fun toString(): String {
        return """
            robot参数
            code:[$code]
            battery:[$battery]
            height:[$height]
            weight:[$weight]
        """.trimIndent()
    }
}


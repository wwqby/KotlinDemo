package `class`

/**
 * 验证到底是构造器先运行还是init块先运行
 * 结论
 * 先运行init块再运行构造器
 * 同一个文件里的多个init块,按照上下顺序执行
 */
class ClassB constructor(
    a: String,
    val b: String,
) : ClassA(a) {

    var c: String? = null

    constructor(a: String) : this(a, "100") {
        println("这里是ClassB构造器1")
    }

    init {
        println("这里是ClassB初始化:init块1")
        c = "100"
        println(a.toString())
        println(b.toString())
        println(c.toString())
    }

    init {
        println("这里是ClassB初始化:init块2")
        c = "200"
        println(b.toString())
    }
}
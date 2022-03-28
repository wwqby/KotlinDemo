package `class`


/**
 * 验证继承后是先运行B的init块还是A
 * 结论,先运行父类的init块-构造器,再运行子类的init-构造器
 */
abstract class ClassA {

    var a: String? = null

    constructor(a: String) {
        this.a = a
        println("这里是ClassA构造器")
    }

    init {
        println("这里是ClassA初始化")
    }
}
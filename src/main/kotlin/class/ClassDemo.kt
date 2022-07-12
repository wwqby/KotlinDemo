package `class`


fun main(args: Array<String>) {
    ClassDemo.testPerson()
}


/**
 * 测试与类/属性相关的问题
 */
object ClassDemo {

    /**
     * 测试val和var关键字
     * 有这么一种用法:
     *  var _a:String?=null
     *  val a
     *  get()=_a
     * 上面的代码里,a到底是null还是其他string,_a变化了,a会变吗
     * 不会变
     */
    fun testValVar() {
        //验证a的值什么时候确定
        var _dataA: String? = null
        val dataA = _dataA
        println("1. dataA=$dataA")
        var _dataB: String? = null
        val dataB = _dataB
        _dataB = "hello world"
        println("2. dataB=$dataB")
    }

    /**
     * 写法A
     * var _a:String?=null
     * val a:String
     * get()=_a!!
     * 达到了对val非空属性延迟赋值的效果相当于by lazy,比by lazy的初始化更自由
     * by lazy实际上还是要在声明时初始化,没办法注入其他属性
     * 实现原理
     * val a=_a!! ==>java public final String a=_a;
     * 反编译java发现是声明了一个final属性
     * val a
     * get()=_a!! ==>java public final getA(){return _a;}
     * 反编译发现声明了一个final方法
     * 不过需要注意用法
     * 1._a需要提前赋值,不然a的读取会空指针
     * 2._a不能重复赋值,不然a的值会变化,val失效
     *
     */
    fun testPerson() {
        //name只会存取_name第一次的赋值
        val person = Person()
        person._name = "hello world1"
        person._name = "hello world2"
        println("1. ${person.name}")
        person._name = "hello world3"
        println("2. ${person.name}")
        //如果_name没有赋值就读取name,就会空指针
        val person2 = Person()
        println("3. ${person2.name}")
    }


    class Person {
        var _name: String? = null
        val name: String
            get() = _name!!
    }
}
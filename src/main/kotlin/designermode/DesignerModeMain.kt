package designermode

import designermode.actormode.*
import designermode.creator.*


/**
 * 设计者模式测试类入口
 */
fun main(args: Array<String>) {

    testDelegateObservable()
}

/**
 * 测试属性委托实现观察者模式
 */
fun testDelegateObservable() {
    //测试观察者模式
    println("测试观察者模式")
    val stock = StockUpdate()
    val stockDisplay = StockDisplay()
    stock.observers.add(stockDisplay)
    stock.setStockPriceChanged(101)

    //通过属性委托实现观察者模式
    val stockDisplayListener = StockDisplayListener()
    val stockUpdate2 = StockUpdate2()
    stockUpdate2.listener.add(stockDisplayListener)
    stockUpdate2.price = 100
    stockUpdate2.price = 200

    //通过属性委托实现对属性的控制
    value = 100
    println(value)
    value = -100
    println(value)
}

/**
 * 测试建造者模式
 */
fun testBuildMode() {
    //测试建造者模式
    println("测试建造者模式")
    val robot = Robot.RobotBuilder("SkyWalker").apply {
        battery = "100"
        height = 80
        weight = 40
    }.build()
    println(robot.toString())

    //测试可选参数初始化
    val robot2 = Robot2(
        code = "BlackWarrior",
        battery = "100",
        height = 80,
        weight = 40
    )
    println(robot2.toString())

    //测试对参数初始化约束
    val robot3 = Robot2(
        code = "BlackWarrior",
        battery = "100",
    )
    println(robot2.toString())
}

/**
 * 抽象工厂测试函数
 */
fun testAbstractFactory() {
    //测试抽象工厂类
    println("测试抽象工厂类")
    val dellFactory = AbstractFactory(DellFactory())
    val dell = dellFactory.produce()
    println(dell)
    //运用内联函数优化后
    val asusFactory = AbstractFactory.invoke2<Asus>()
    val asus = asusFactory.produce()
    println(asus)
}

/**
 * 伴生工厂测试函数
 */
fun testFactory() {
    println("测试工厂模式")
//测试标准简易工厂
    val pc = ComputerFactory().produce(ComputerType.PC)
    val server = ComputerFactory().produce(ComputerType.Server)
    println(pc.cpu)
    println(server.cpu)
//    测试单例工厂
    val pc1 = ComputerFactoryObject.produce(ComputerType.PC)
    val server1 = ComputerFactoryObject.produce(ComputerType.Server)
    println(pc1.cpu)
    println(server1.cpu)
//    测试伴生对象简单工厂
    val pc2 = Computer.Factory(ComputerType.PC)
    val server2 = Computer.Factory(ComputerType.Server)
    println(pc2.cpu)
    println(server2.cpu)
//    测试扩展函数增加伴生工厂功能
    println(Computer.Factory.fromCpu(pc2.cpu))
    println(Computer.Factory.fromCpu(server2.cpu))
}

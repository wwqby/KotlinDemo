package designermode

import designermode.creator.*


/**
 * 设计者模式测试类入口
 */
fun main(args: Array<String>) {

    testFactory()
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

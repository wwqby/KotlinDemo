package designermode.creator

//工厂类文件
/**
 * 电脑接口
 */
interface Computer {
    val cpu: String

    /**
     * 伴生对象实现简易工厂
     * 伴生对象可以命名
     */
    companion object Factory {
        /**
         * 利用operator关键字,重载invoke方法
         * invoke方法允许直接调用class.invoke()
         * 或者简易调用class()
         */
        operator fun invoke(computerType: ComputerType): Computer {
            return when (computerType) {
                ComputerType.PC -> PC()
                ComputerType.Server -> Server()
            }
        }
    }


}

//电脑实现类
class PC(override val cpu: String = "Core") : Computer
class Server(override val cpu: String = "Xeon") : Computer

//代表电脑类型的枚举
enum class ComputerType {
    PC, Server
}

/**
 * 标准简单工厂类
 */
class ComputerFactory {
    fun produce(type: ComputerType): Computer {
        return when (type) {
            ComputerType.PC -> PC()
            ComputerType.Server -> Server()
        }
    }
}

/**
 * 单例模式实现简易工厂
 */
object ComputerFactoryObject {
    fun produce(type: ComputerType): Computer {
        return when (type) {
            ComputerType.PC -> PC()
            ComputerType.Server -> Server()
        }
    }
}


/**
 * 利用扩展函数为伴生工厂增加新功能
 */
fun Computer.Factory.fromCpu(cpu: String): ComputerType? = when (cpu) {
    "Core" -> ComputerType.PC
    "Xeon" -> ComputerType.Server
    else -> null
}

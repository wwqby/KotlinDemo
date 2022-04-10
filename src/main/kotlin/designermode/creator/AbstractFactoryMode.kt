package designermode.creator

/**
 *  抽象工厂模式
 *  利用内联函数来优化抽象工厂的使用
 */
//电脑品牌
interface ComputerBrand
class Dell : ComputerBrand
class Asus : ComputerBrand
class Acer : ComputerBrand

//抽象工厂
abstract class AbstractFactory {
    //工厂生产方法
    abstract fun produce(): ComputerBrand

    companion object {
        /**
         * 通过重载invoke方法来返回具体的工厂
         * todo 不理解,为啥要多此一举
         */
        operator fun invoke(factory: AbstractFactory): AbstractFactory = factory

        /**
         * 通过内联函数,省去了对抽象工厂实现类的直接依赖
         * class 获取到的是KClass 类似的方法还有javaClass.kotlin
         * class.java 获取到的是Class
         * inline可以获取运行时的参数类型
         * reified联合inline使用,可以指定具体化参数类型
         */
        inline fun <reified T : ComputerBrand> invoke2(): AbstractFactory = when (T::class) {
            Dell::class -> DellFactory()
            Asus::class -> AsusFactory()
            Acer::class -> AcerFactory()
            else -> throw IllegalArgumentException()
        }
    }
}

//抽象工厂实现类
class DellFactory : AbstractFactory() {
    override fun produce(): ComputerBrand {
        return Dell()
    }
}

class AsusFactory : AbstractFactory() {
    override fun produce(): ComputerBrand {
        return Asus()
    }
}

class AcerFactory : AbstractFactory() {
    override fun produce(): ComputerBrand {
        return Acer()
    }
}
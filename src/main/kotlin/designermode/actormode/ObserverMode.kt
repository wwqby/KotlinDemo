package designermode.actormode

import java.util.*
import kotlin.properties.Delegates

/**
 * 观察者模式
 * Observable已经被标记为deprecated,一个是不是线程安全,第二个没有实现序列化,最后可以响应的方法太少
 * 新的版本用PropertyChangeEvent和PropertyChangeListener来代替
 */
//java版本的被观察者
class StockUpdate : Observable() {

    val observers = mutableListOf<Observer>()

    fun setStockPriceChanged(price: Int) {
        observers.forEach {
            it.update(this, price)
        }
    }

}

//java版本的观察者
class StockDisplay : Observer {
    override fun update(p0: Observable?, p1: Any?) {
        if (p0 is StockUpdate) {
            println("The last stock price is $p1")
        }
    }
}

/**
 * 通过属性委托来实现更强大的订阅功能
 */
interface StockUpdateListener {
    fun onFall(price: Int)
    fun onRise(price: Int)
}

class StockDisplayListener : StockUpdateListener {
    override fun onFall(price: Int) {
        println("The latest price is $price")
    }

    override fun onRise(price: Int) {
        println("The latest price is $price")
    }

}

class StockUpdate2 {
    //订阅者集合
    val listener = mutableListOf<StockUpdateListener>()

    //price通过属性委托,by Delegates.observable来实现订阅者StockUpdateListener
    var price: Int by Delegates.observable(0) { _, old, new ->
        listener.onEach {
            if (old > new) it.onRise(price)
            else it.onFall(price)
        }
    }
}

/**
 * 通过vetoable属性委托来实现对赋值的限制
 */
var value: Int by Delegates.vetoable(0) { _, old, new ->
    new > 0
}



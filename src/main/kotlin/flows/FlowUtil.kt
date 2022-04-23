package flows

import com.sun.javafx.application.LauncherImpl
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


fun main(args: Array<String>) {
    runBlocking {
        val util=FlowUtil()
        util.testFlowTriggerIsBlock()
        util.testFlowTriggerIsIndependence()
    }
}

/**
 * flow演示类
 */
class FlowUtil {

    /**
     * 证明onStart和onCompletion在哪个线程
     * 证明了执行顺序 onStart->emit->collect->onCompletion
     * 并且两者与collect总是在同一个协程域
     */
    suspend fun testFlowOnStartOnCompletion() = flow{
        println("emit thread:${currentCoroutineContext()}")
        emit(100)
    }.map {
        "this value is $it"
    }
        .flowOn(Dispatchers.IO)
        .onStart {
        println("on start thread:${currentCoroutineContext()}")
    }.onCompletion {
        println("on complete thread:${currentCoroutineContext()}")
    }.collect {
        println("collect thread:${currentCoroutineContext()}")
        println(it)
    }

    /**
     * 测试catch捕捉异常处理后，重新抛出能不能正常结束
     * 结果证明
     * 1.抛出异常后，仍然可以触发onCompletion.应该这么说，上游的异常，最后会被onCompletion收集到
     * 2.catch尽量放在onCompletion之前，不要在onCompletion中处理异常
     * 3.collect中的异常，只能在下游处理或者通过onEach加catch组合作为前置处理
     */
    suspend fun testFlowCatch()= flow{
        println("emit thread:${currentCoroutineContext()}")
        emit(100)
    }.map {
        throw NullPointerException()
        "100"
    }.catch{
        println("catch1 thread:${currentCoroutineContext()}")
        println("${it.message}")
        throw NullPointerException()
        "100"
    }
        .flowOn(Dispatchers.IO)
        .onStart {
            println("on start thread:${currentCoroutineContext()}")
        }.onCompletion {
            println("on complete thread:${currentCoroutineContext()}")
            //收集异常
            it?.let {
                println("收集到异常：${it.message}")
                //这里无法继续执行，因为抛出异常中断执行链
                emit("100")
            }
        }
//        .catch {
//            println("catch2 thread:${currentCoroutineContext()}")
//            println("${it.message}")
//            emit("000")
//        }
        .collect {
            println("collect thread:${currentCoroutineContext()}")
            println(it)
        }

    /**
     * 测试catch捕捉异常处理后，重新抛出能不能正常结束
     * 结果证明可以正常结束
     */
    suspend fun testFlowCatchNormal()= flow{
        println("emit thread:${currentCoroutineContext()}")
        emit(100)
    }.map {
        throw NullPointerException()
        "100"
    }.catch{
        println("catch thread:${currentCoroutineContext()}")
        println("${it.message}")
        emit("000")
    }
        .flowOn(Dispatchers.IO)
        .onStart {
            println("on start thread:${currentCoroutineContext()}")
        }.onCompletion {
            println("on complete thread:${currentCoroutineContext()}")
        }.collect {
            println("collect thread:${currentCoroutineContext()}")
            println(it)
        }

    /**
     * 测试flow作为冷流的触发机制
     * 证明flow的执行会阻塞当前位置
     * 两个串行的flow会依次执行，执行完flow1才会执行flow2
     */
    suspend fun testFlowTriggerIsBlock() {
        /**
         * 定义一个flow对象
         * 该flow对象会每隔一秒发送一次，共发送10次
         */
        val flow=flow {
            val flowName = System.currentTimeMillis()
            repeat(5) {
                delay(1000)
                emit("$flowName:$it")
            }
        }
        //触发第一次
        flow.collect{
            println("obj1-$it")
        }
        //延迟1.5s以后，触发第二次
        delay(1500)
        flow.collect{
            println("obj2-$it")
        }
    }

    /**
     * 测试flow作为冷流的触发机制
     * 证明冷流每次被collect就会重新触发
     * 两个并行的flow彼此互不干扰，完整触发整个flow的emit
     */
    suspend fun testFlowTriggerIsIndependence(){
        /**
         * 定义一个flow对象
         * 该flow对象会每隔一秒发送一次，共发送10次
         */
        val flow=flow {
            val flowName = System.currentTimeMillis()
            repeat(5) {
                delay(1000)
                emit("$flowName:$it")
            }
        }
        //发现两个flow是串行执行，第一个flow阻塞执行完毕，才执行到第二flow
        /**
         * 打开两个协程，分别触发两次flow
         */
        CoroutineScope(Dispatchers.Default).launch{
            flow.collect{
                println("obj3-$it")
            }
        }
        delay(1500)
        CoroutineScope(Dispatchers.Default).launch{
            flow.collect{
                println("obj4-$it")
            }
        }
    }
}
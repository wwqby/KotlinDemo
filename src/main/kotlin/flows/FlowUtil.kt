package flows

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


fun main(args: Array<String>) {
    runBlocking {
        FlowUtil().testFlowCatch()
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
     * 3.collect中的异常，只能在下游处理或者通过onEach加catch组合处理
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
}
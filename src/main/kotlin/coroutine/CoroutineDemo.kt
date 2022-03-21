package coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CoroutineDemo {


    /**
     * 演示并测试channel的性能
     */
    fun TestChannel() {
        runBlocking {
            println("TestChannel start")
            val channel = Channel<String>()
            launch {
                var count = 0
                while (isActive) {
                    val value = channel.receive()
                    println("count=$count,value=$value")
                    count++
                    delay(2000)
                }
            }
            launch {
                for (i in 1..10) {
                    channel.send("this is $i")
                }
            }

        }
    }

    /**
     * 测试SuspendCoroutine函数
     * 验证了该函数是阻塞函数
     */
    fun TestSuspendCoroutine() {
        runBlocking {
            println("start")
            val result = async {
                suspendCoroutine<Boolean> {
                    taskCallBack(object : CallBackDemo {
                        override fun onSuc() {
                            it.resume(true)
                        }

                        override fun onFail() {
                            it.resume(false)
                        }
                    })
                }
            }
            println("end")
            if (result.await()) println("suc")
        }
    }

    fun taskCallBack(callBack: CallBackDemo) {
        thread {
            Thread.sleep(1000)
            println("task")
            callBack.onSuc()
        }
    }

    interface CallBackDemo {
        fun onSuc()
        fun onFail()
    }
}
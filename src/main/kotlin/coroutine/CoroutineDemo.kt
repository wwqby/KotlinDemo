package coroutine

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CoroutineDemo {

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
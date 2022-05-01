@file:OptIn(ExperimentalTime::class, FlowPreview::class)

package flows

import designermode.actormode.value
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

fun main(args: Array<String>) = runBlocking {

    FlowStudy.testFlowSharedFlow()
}

/**
 * 演示flow的相关知识
 * 1.flow的开始和结束操作符onStart,onCompletion
 * 2.flow的取消
 * 3.flow背压
 * 4.flow的操作符
 */
object FlowStudy {

    /**
     * 验证onStart,onCompletion的执行机制
     * 1.两者执行顺序。onStart->emit->...->onCompletion
     * 2.两者的执行协程是定义时所在协程，不受flowOn影响，即使在flowOn切换线程之后
     */
    suspend fun testFlowBeforeAndAfter() {
        val job = CoroutineScope(Dispatchers.Default).launch(Dispatchers.Default) {
            flow {
                repeat(5) {
                    emit("${currentCoroutineContext()}:$it")
                }
            }.flowOn(Dispatchers.IO)
                .onStart {
                    println("Before:onStart:${currentCoroutineContext()}")
                }.onCompletion {
                    println("After:onCompletion:${currentCoroutineContext()}")
                }.collect {
                    println(it)
                }
        }
        job.join()
    }

    /**
     * 测试取消flow
     * 1.取消collect操作符所在的协程，会取消flow的emit
     * 2.取消flow不会触发onCompletion
     */
    suspend fun testFlowCancel() {
        val flowSub = flow {
            repeat(5) {
                delay(1000)
                emit("${currentCoroutineContext()}:$it")
            }
        }.onStart {
            println("flow执行开始:${currentCoroutineContext()}")
        }.onCompletion {
            println("flow执行结束:${currentCoroutineContext()}")
        }
        //在新的协程启动flow并获取job句柄
        val job = CoroutineScope(Dispatchers.Default).launch {
            flowSub.collect {
                println(it)
            }
        }
        //延时3s取消协程
        delay(3000)
        job.cancel()
    }

    /**
     * 测试flow的背压操作符
     * buffer 缓冲上游的结果，并等待下游响应依次发射处理
     * conflate 缓冲上游结果，等待下游响应合并所有缓存发射处理，下游只处理缓存中的最后一个，放弃旧的数据
     * collectLatest 上游每次发送会直接取消下游的响应，只处理当前emit
     */
    suspend fun testFlowBackpressure() {
        //新建一个flow，每隔100ms发送一次，共发送5次
        val flowSub = flow {
            repeat(5) {
                delay(100)
                emit("${currentCoroutineContext()}:$it")
                println("发送完毕")
            }
        }

        /**
         * 无背压测试
         * 100-300-100-300.。。
         * 每一次的collect需要100ms+300ms
         * 5次执行一共需要2000ms
         */
        val time = measureTime {
            flowSub.onStart {
                println("无背压测试start")
            }.onCompletion {
                println("无背压测试complete")
            }.collect {
                //每隔300ms处理一次emit
                delay(300)
                println(it)
            }
        }
        //大于等于2000ms
        println("无背压测试总耗时:$time")

        /**
         * buffer操作符
         * 切断了上游和下游
         * flow的间隔emit结果发送到buffer，然后由buffer依次emit。
         * 这样隔离了上游emit的生产速度和下游的collect消费速度
         * buffer需要考虑缓冲容量大小
         */
        val timeBuffer = measureTime {
            flowSub
                .buffer()
                .onStart {
                    println("buffer操作符测试start")
                }.onCompletion {
                    println("buffer操作符测试complete")
                }.collect {
                    //每隔300ms处理一次emit
                    delay(300)
                    println(it)
                }
        }
        //耗时大于等于1500+100ms
        println("背压测试buffer总耗时:$timeBuffer")

        /**
         * conflate操作符
         * 切断了上游和下游
         * 类似buffer，不同的是每次下游接受数据，会接受缓冲池里的所有数据，并取最新的值，放弃旧值
         * 处理0时，1和2已经发射出来，所以合并当前收到的结果，只处理最新的发射，合并1和2，只处理2
         * 相当于放弃了上一次处理和当前最新的结果之间发送的数据
         * 相比buffer，相当于只处理同时产生的数据，放弃了没有来得及处理的数据
         * 想象一下变成两个并行处理的生产线，下游只处理上游产生的最新的数据，没有来得及处理的数据被放弃
         */
        val timeConflate = measureTime {
            flowSub
                .conflate()
                .onStart {
                    println("conflate操作符测试start")
                }.onCompletion {
                    println("conflate操作符测试complete")
                }.collect {
                    //每隔300ms处理一次emit
                    delay(300)
                    println(it)
                }
        }
        //耗时大于等于1500ms
        println("背压测试conflate总耗时:$timeConflate")

        /**
         * collectLatest操作符
         * 切断了上游和下游
         * 每次上游发送数据，下游会取消当前响应(如果响应还没处理完)，发起新的响应
         * 这样造成的结果是下游接受到了所有发射，但是由于延时，造成新的发射会取消上一次的响应，最后只有最新的响应输出
         */
        val timeCollectLatest = measureTime {
            flowSub
                .onStart {
                    println("collectLatest操作符测试start")
                }.onCompletion {
                    println("collectLatest操作符测试complete")
                }.collectLatest {
                    //每隔300ms处理一次emit
                    println("接收到的值:$it")
                    delay(300)
                    println(it)
                }
        }
        //耗时大于等于1500ms
        println("背压测试collectLatest总耗时:$timeCollectLatest")
    }

    /**
     * flow基本操作符
     * 1.map 把发送的数据单独转换为另一种数据
     * 2.take 只处理不多于规定数量的flow事件，
     * 3.filter 过滤满足指定条件的事件
     */
    suspend fun testFlowBasicOperator() {
        val flowStub = flow {
            repeat(5) {
                emit(it)
            }
        }

        /**
         * map操作符
         * 把emit发射的数据处理转化为另一种数据
         */
        flowStub.map {
            "This is $it"
        }.onStart {
            println("map操作符演示开始")
            println("把int转换为String")
        }.onCompletion {
            println("map操作符演示结束")
        }.collect {
            println(it)
        }

        /**
         * take操作符
         * 只处理指定数量的flow事件
         */
        flowStub.take(3)
            .onStart {
                println("take操作符演示开始")
                println("只处理前3个flow事件")
            }.onCompletion {
                println("take操作符演示结束")
            }.collect {
                println(it)
            }

        /**
         * filter操作符
         * 通过指定条件过滤flow发送值
         */
        flowStub.filter {
            it > 3
        }
            .onStart {
                println("filter操作符演示开始")
                println("只处理大于3的发射事件")
            }.onCompletion {
                println("filter操作符演示结束")
            }.collect {
                println(it)
            }
    }

    /**
     * flow功能性操作符
     * 1.retry  重试机制，异常时重新执行
     * 2.cancellable flow取消异常，取消时抛出异常
     * 3.debounce 防抖动，指定时间内只接受最新的数据
     */
    suspend fun testFlowRetry() {
        val flowStub = flow {
            repeat(5) {
                emit(it)
            }
        }
        /**
         * retry
         * retry(count){}
         * count次数，包括retry成功的次数和失败的次数
         * 即使成功以后又触发retry，次数超过规定限制也会直接抛出异常终止
         */
        flowStub.map {
            if (it > 3) throw RuntimeException("测试时引发异常1")
            it
        }.retry(2) {
            if (it is RuntimeException) {
                println("retry判断成功触发，重新执行flow")
                return@retry true
            }
            println("retry判断失败，直接异常")
            false
        }.onStart {
            println("retry测试开始")
            println("测试触发成功情况")
        }.onCompletion {
            println("retry测试结束")
        }.collect {
            println(it)
        }

        flowStub.map {
            if (it > 3) throw RuntimeException("测试时引发异常1")
            it
        }.retry(2) {
            if (it is NullPointerException) {
                println("retry判断成功触发，重新执行flow")
                return@retry true
            }
            println("retry判断失败，直接异常")
            false
        }.onStart {
            println("retry测试开始")
            println("测试触发失败情况")
        }.onCompletion {
            println("retry测试结束")
        }.collect {
            println(it)
        }
    }

    /**
     * cancellable操作符
     * 允许一些原本不可以取消的flow可以取消
     * 相当于
     *  .onEach { currentCoroutineContext().ensureActive() }
     */
    suspend fun testFlowCancellable() {
        val flowStub = flow {
            repeat(10) {
                println("repeat $it")
                emit(it)
            }
        }
        val job = CoroutineScope(Dispatchers.Default).launch {
            flowStub
                .cancellable()
                .collect {
                    if (it == 3) cancel()
                    println(it)
                }
        }
        job.join()
    }

    /**
     * debounce操作符
     * 下游只允许timeOut时间内接收一次emit
     */
    suspend fun testFlowDebounce() {
        //新建一个延时逐渐增长的flow
        //100 200 300 400 500 600 ...
        val flowStub = flow {
            repeat(10) {
                val timeMillis = it * 100.toLong()
                delay(timeMillis)
                println("time:$timeMillis and int:$it")
                emit(it)
            }
        }
        //debounce防抖动节流，只返回限制的时间里的最新emit
        //下游只允许timeOut时间内接收一次emit
        flowStub.debounce(500)
            .collect {
                println(it)
            }
    }

    /**
     * onEach操作符
     * 1.中间操作符,执行顺序emit->onEach->collect
     * 2.不会改变flow的数据类型，可以对flow的数据操作处理，然后继续发出
     */
    suspend fun testFlowOnEach() {
        val flowStub = flow {
            repeat(10) {
                println("emit:$it")
                emit(it)
            }
        }
        flowStub.onEach {
            println("onEach:$it")
        }.collect {
            println(it)
        }
    }

    /**
     * onEmpty操作符
     * 当flow没有发送任何数据或者通过其他操作符，没有符合条件的数据产生时，触发onEmpty内的默认数据
     */
    suspend fun testFlowOnEmpty() {
        val flowStub = flow {
            repeat(10) {
                println("emit:$it")
                emit(it)
            }
        }
        flowStub.filter {
            it > 10
        }.onEmpty {
            println("onEmpty:")
            emit(-1)
        }.collect {
            println(it)
        }
    }

    /**
     * zip操作符
     * 1.flow1组合flow2，并对数据组合新的数据
     * 2.任何一个flow结束，整体zip过程结束
     * 3.先发送的flow的发射会等待另外一个flow的发射才会触发zip及以后的流程
     */
    suspend fun testFlowZip() {
        val flowStub1 = flow {
            repeat(10) {
                delay(100)
                emit(it)
            }
        }

        val flowStub2 = flow {
            repeat(5) {
                emit(it)
            }
        }
        flowStub1.zip(flowStub2) { data1, data2 ->
            "zip:$data1>>$data2"
        }.collect {
            println(it)
        }
    }

    /**
     * combine操作符
     * 1.类似zip，可以对两个flow进行组合
     * 2.不同的地方，所有flow开始之后才开始，所有flow发射结束才算结束
     * 3.不同的地方，两个flow发射之间不会等待，每次组合都是当前flow的最新的emit值
     * 期待结果
     *  combine:2>>a
     *  combine:2>>b
     *  combine:2>>c
     */
    suspend fun testFlowCombine() {
        val flowStub1 = flowOf(1, 2)
            .onEach {
                delay(10)
            }

        val flowStub2 = flowOf("a", "b", "c")
            .onEach {
                delay(15)
            }
        flowStub1.combine(flowStub2) { data1, data2 ->
            "combine:$data1>>$data2"
        }.collect {
            println(it)
        }
    }

    /**
     * flatMapConcat操作符
     * 作用：把Flow<T>->Flow<R>
     *     拿到emit的数据后，处理数据转换为新的flow类型
     */
    suspend fun testFlowFlatMapConcat(){
        val flowStub = flowOf(1, 2,3,4,5)
            .onEach {
                delay(10)
            }

        flowStub.flatMapConcat {
            flow { emit("String:$it") }
        }.collect {
            println(it)
        }
    }

    /**
     * 测试使用stateflow，验证stateflow特性
     * 1.stateflow是接口，可以自己通过mutableStateFlow或者通过flow().stateIn()来获得
     * 2.flow().stateIn()来获取，需要配置stateFlow的参数
     * 3.scope，代表生命周期作用域，当该协程结束，stateFlow结束
     * 4.started，热流开始策略。WhileSubscribed：开始于第一个订阅者，结束于最后一个订阅者取消。Eagerly：立即开始并永不停止。Lazily：当第一个订阅者出现时开始并永不停止
     * 5.initialValue，初始值。stateFlow必须有一个默认值
     */
    suspend fun testFlowStateFlow(){

        val core=CoroutineScope(Dispatchers.Default)
        val state= flow {
            repeat(10){
                delay(500)
                emit(it*100)
            }
        }.stateIn(
            core,
            SharingStarted.WhileSubscribed(),
            -1
        )
        val job1=core.launch {
            state.collect {
                println("job:value=$it")
            }
        }
        delay(2000)
        val job2=core.launch {
            state.collect {
                println("job2:value=$it")
            }
        }
        repeat(10){
            delay(1000)
            if (it==5)job1.cancel()
        }
        core.cancel()
    }

    /**
     * 测试MutableStateFlow
     * 可以在外部改动，StateIn得到的StateFlow只能在Flow内部改变
     * MutableStateFlow没有额外配置的参数
     * 参考特性，类似于scope=
     */
    suspend fun testFlowMutableStateFlow(){
        val core=CoroutineScope(Dispatchers.Default)
        val state0= MutableStateFlow(100)
        val state=state0
        //job1从默认值100开始
        val job1=core.launch {
            state.collect {
                println("job:value=$it")
            }
        }
        //对比下方没有延时，发现stateFlow默认背压，只显示最新值
        repeat(5){
            delay(100)
            state.value=it*100
        }
        //只显示400，抛弃了前面的旧值
        repeat(5){
            state.value=it*100
        }
        //job2从400开始
        val job2=core.launch {
            state.collect {
                println("job2:value=$it")
            }
        }
        //stateFlow下游是可以取消的，取消job或者协程
        repeat(10){
            delay(500)
            state.value=it*100
            if (it==5)job1.cancel()
        }
        core.cancel()
    }

    /**
     * MutableSharedFlow
     * 与MutableStateFlow类似
     * 1.可以在外部emit发送新值
     * 2.初始化时，没有默认值
     * 3.可以在初始化时配置更多得参数
     *  replay，缓存数量
     *  onBufferOverflow，缓存策略 SUSPEND：挂起。DROP_OLDEST：放弃最旧得数据。DROP_LATEST：放弃最新得数据
     */
    suspend fun testFlowMutableSharedFlow(){
        val core=CoroutineScope(Dispatchers.Default)
        val state0= MutableSharedFlow<Int>(1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        val state=state0
        //job1从默认值100开始
        val job1=core.launch {
            state.collect {
                println("job:value=$it")
            }
        }
        //对比下方没有延时，发现stateFlow默认背压，只显示最新值
        repeat(5){
            delay(100)
            state.emit(100)
        }
        //只显示400，抛弃了前面的旧值
        repeat(5){
            state.emit(it*100)
        }
        //job2从400开始
        val job2=core.launch {
            state.collect {
                println("job2:value=$it")
            }
        }
        //stateFlow下游是可以取消的，取消job或者协程
        repeat(10){
            delay(500)
            state.emit(it*100)
            if (it==5)job1.cancel()
        }
        core.cancel()
    }

    /**
     * 测试SharedFlow
     * SharedIn配置参数
     * 1.scope，生命周期上下文
     * 2.开始策略，参考StateIn
     * 3.replay 缓存池大小
     */
    suspend fun testFlowSharedFlow(){
        val core=CoroutineScope(Dispatchers.Default)
        val state= flow {
            repeat(10){
                delay(500)
                emit(it*100)
            }
        }.shareIn(
            core,
            SharingStarted.WhileSubscribed(),
            //缓存数量，会把旧值一起发送
            5
        )
        //job1从默认值100开始
        val job1=core.launch {
            state.collect {
                println("job:value=$it")
            }
        }
        delay(2000)
        //job2从400开始
        val job2=core.launch {
            state.collect {
                println("job2:value=$it")
            }
        }
        delay(3000)
        core.cancel()
    }

}
package rope

import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

abstract class Benchmark<T>(private val name: String) {
    abstract fun prepare(input: String): T
    abstract fun execute(input: T)

    private val memoryDivider: Long = 1024 * 1024
    private fun getHeapSize(): Long = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()

    fun run(warmups: Int, iterations: Int, input: String): BenchmarkResult {
        val benchInput = prepare(input)

        repeat(warmups) { execute(benchInput) }

        val runtime = Runtime.getRuntime()

        val times = mutableListOf<Double>()
        val memoryUsages = mutableListOf<Long>()
        (times as ArrayList<Double>).ensureCapacity(iterations)
        (memoryUsages as ArrayList<Long>).ensureCapacity(iterations)

        repeat(iterations) {
            System.gc()
            Thread.sleep(10)

            val beforeUsedMem = runtime.totalMemory() - runtime.freeMemory()

            val time = measureTimeMillis {
                execute(benchInput)
            }.toDouble()

            val afterUsedMem = runtime.totalMemory() - runtime.freeMemory()
            val usedMem = max(0L, afterUsedMem - beforeUsedMem)

            times += time
            memoryUsages += usedMem
        }

        val meanTime = times.average()
        val stdDevTime = sqrt(times.sumOf { (it - meanTime).pow(2) } / (times.size - 1))
        val stdErrTime = stdDevTime / sqrt(times.size.toDouble())
        val ci75Time = 1.15 * stdErrTime

        val meanMemoryKb = memoryUsages.average() / 1024
        val stdDevMemoryKb = sqrt(memoryUsages.sumOf { (it / 1024 - meanMemoryKb).pow(2) } / (memoryUsages.size - 1))
        val stdErrMemoryKb = stdDevMemoryKb / sqrt(memoryUsages.size.toDouble())
        val ci75MemoryKb = 1.15 * stdErrMemoryKb

        return BenchmarkResult(
            name,
            meanTime, meanTime - ci75Time, meanTime + ci75Time,
            meanMemoryKb, meanMemoryKb - ci75MemoryKb, meanMemoryKb + ci75MemoryKb,
            input.length
        )
    }
}

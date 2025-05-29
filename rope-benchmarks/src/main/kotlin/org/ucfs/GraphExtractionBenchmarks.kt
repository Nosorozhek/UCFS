package org.ucfs

import kotlinx.benchmark.*
import org.ucfs.input.rope.Rope
import java.util.concurrent.TimeUnit


@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
open class GraphExtractionBenchmarks : RopeBenchmark() {
    @Param("1000000", "4000000", "7000000", "10000000")
    override var benchmarkSize = 0

    @Benchmark
    fun createGraphTwice(bh: Blackhole) {
        val result = Rope(stringInput)
        val result2 = Rope(stringInput + stringInput)
        bh.consume(result)
        bh.consume(result2)
    }

    @Benchmark
    fun createGraphIncrementally(bh: Blackhole) {
        val result = Rope(stringInput)
        val result2 = result + Rope(stringInput)
        bh.consume(result)
        bh.consume(result2)
    }
}
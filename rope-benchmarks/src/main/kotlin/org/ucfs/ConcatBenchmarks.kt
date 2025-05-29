package org.ucfs

import kotlinx.benchmark.*
import java.util.concurrent.TimeUnit


@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
open class ConcatRopesBenchmark : RopeBenchmark() {
    @Param("10000000", "40000000", "70000000", "100000000")
    override var benchmarkSize = 0

    @Benchmark
    fun testRope(bh: Blackhole) {
        val result = (ropeInput + ropeInput).rebalance()
        bh.consume(result)
    }

    @Benchmark
    fun testString(bh: Blackhole) {
        val result = stringInput + stringInput
        bh.consume(result)
    }
}

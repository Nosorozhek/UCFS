package org.ucfs

import kotlinx.benchmark.*
import org.ucfs.input.rope.MAX_DEPTH
import java.util.concurrent.TimeUnit


@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
open class SubRopeBenchmark : RopeBenchmark() {
    @Param("1000000", "4000000", "7000000", "10000000")
    override var benchmarkSize = 0

    @Benchmark
    fun testRope(bh: Blackhole) {
        repeat(MAX_DEPTH) {
            val result = ropeInput.substring(2 * ropeInput.length / 7, 5 * ropeInput.length / 7)
            bh.consume(result)
        }
    }

    @Benchmark
    fun testString(bh: Blackhole) {
        repeat(MAX_DEPTH) {
            val result = stringInput.substring(2 * stringInput.length / 7, 5 * stringInput.length / 7)
            bh.consume(result)
        }
    }
}

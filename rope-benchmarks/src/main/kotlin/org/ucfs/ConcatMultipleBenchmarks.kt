package org.ucfs

import kotlinx.benchmark.*
import org.ucfs.input.rope.MAX_DEPTH
import java.util.concurrent.TimeUnit


@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
open class ConcatMultipleRopesBenchmark : RopeBenchmark() {
    @Param("1000000", "4000000", "7000000", "10000000")
    override var benchmarkSize = 0

    @Benchmark
    fun testRope(bh: Blackhole) {
        var result = ropeInput
        repeat(MAX_DEPTH) { result += ropeInput }
        bh.consume(result)
    }

    @Benchmark
    fun testString(bh: Blackhole) {
        var result = stringInput
        repeat(MAX_DEPTH) { result += stringInput }
        bh.consume(result)
    }
}

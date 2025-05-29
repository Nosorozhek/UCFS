package org.ucfs

import kotlinx.benchmark.*
import java.util.concurrent.TimeUnit


@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
open class IterateRopeBenchmark : RopeBenchmark() {
    @Param("1000000", "4000000", "7000000", "10000000")
    override var benchmarkSize = 0

    @Benchmark
    fun testRope(bh: Blackhole) {
        val result = ropeInput.asIterable().count()
        bh.consume(result)
    }

    @Benchmark
    fun testRopeGraph(bh: Blackhole) {
        val result = sequence {
            var currentEdge = ropeInput.inputGraph.getEdges(ropeInput.inputGraph.getInputStartVertices().first()).firstOrNull()
            while (currentEdge != null) {
                yieldAll(currentEdge.label.toString().toList())
                currentEdge = ropeInput.inputGraph.getEdges(currentEdge.targetVertex).firstOrNull()
            }
        }.count()
        bh.consume(result)
    }

    @Benchmark
    fun testString(bh: Blackhole) {
        val result = stringInput.asIterable().count()
        bh.consume(result)
    }
}

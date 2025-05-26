package org.ucfs

import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import org.ucfs.input.rope.Rope


@State(Scope.Benchmark)
open class RopeBenchmark {
    protected open var benchmarkSize = 0

    protected lateinit var ropeInput: Rope
    protected lateinit var stringInput: String

    @Setup
    fun setup() {
        val testData = testString(benchmarkSize)
        ropeInput = Rope(testData)
        stringInput = testData
    }

    private fun testString(n: Int) = generateSequence { "test".asSequence() }.flatten().take(n).joinToString("")
}

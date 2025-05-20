package rope

import org.ucfs.input.rope.Rope

class ConcatRopesBenchmark : Benchmark<Rope>("RopeConcat") {
    override fun prepare(input: String): Rope {
        return Rope(input)
    }

    override fun execute(input: Rope) {
        (input + input).rebalance()
    }
}

class ConcatStringsBenchmark : Benchmark<String>("StringConcat") {
    override fun prepare(input: String): String {
        return input
    }

    override fun execute(input: String) {
        input + input
    }
}

class ConcatMultipleRopesBenchmark(private val times: Int) : Benchmark<Rope>("ConcatMultipleRopes") {
    override fun prepare(input: String): Rope {
        return Rope(input)
    }

    override fun execute(input: Rope) {
        var result = input
        repeat(times) { result += input }
    }
}

class ConcatMultipleStringsBenchmark(private val times: Int) : Benchmark<String>("ConcatMultipleStrings") {
    override fun prepare(input: String): String {
        return input
    }

    override fun execute(input: String) {
        var result = input
        repeat(times) { result += input }
    }
}

class SubRopeBenchmark(private val times: Int) : Benchmark<Rope>("SubRope") {
    override fun prepare(input: String): Rope {
        return Rope(input)
    }

    override fun execute(input: Rope) {
        repeat(times) {
            input.substring(2 * input.length / 7, 5 * input.length / 7)
        }
    }
}

class SubStringBenchmark(private val times: Int) : Benchmark<String>("SubString") {
    override fun prepare(input: String): String {
        return input
    }

    override fun execute(input: String) {
        repeat(times) {
            input.substring(2 * input.length / 7, 5 * input.length / 7)
        }
    }
}

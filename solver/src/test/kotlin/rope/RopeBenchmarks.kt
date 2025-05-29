package rope

import org.ucfs.input.rope.Rope

class ConcatRopesBenchmark : Benchmark<Rope<Char>>("RopeConcat") {
    override fun prepare(input: String): Rope<Char> = Rope(input)

    override fun execute(input: Rope<Char>) {
        (input + input).rebalance()
    }
}

class ConcatStringsBenchmark : Benchmark<String>("StringConcat") {
    override fun prepare(input: String): String = input

    override fun execute(input: String) {
        input + input
    }
}

class ConcatMultipleRopesBenchmark(private val times: Int) : Benchmark<Rope<Char>>("ConcatMultipleRopes") {
    override fun prepare(input: String): Rope<Char> = Rope(input)

    override fun execute(input: Rope<Char>) {
        var result = input
        repeat(times) { result += input }
    }
}

class ConcatMultipleStringsBenchmark(private val times: Int) : Benchmark<String>("ConcatMultipleStrings") {
    override fun prepare(input: String): String = input

    override fun execute(input: String) {
        var result = input
        repeat(times) { result += input }
    }
}

class SubRopeBenchmark(private val times: Int) : Benchmark<Rope<Char>>("SubRope") {
    override fun prepare(input: String): Rope<Char> = Rope(input)

    override fun execute(input: Rope<Char>) {
        repeat(times) {
            input.substring(2 * input.length / 7, 5 * input.length / 7)
        }
    }
}

class SubStringBenchmark(private val times: Int) : Benchmark<String>("SubString") {
    override fun prepare(input: String): String = input

    override fun execute(input: String) {
        repeat(times) {
            input.substring(2 * input.length / 7, 5 * input.length / 7)
        }
    }
}

class IterateRopeBenchmark : Benchmark<Rope<Char>>("SubRope") {
    override fun prepare(input: String): Rope<Char> = Rope(input)

    override fun execute(input: Rope<Char>) {
        input.asIterable().count()
    }
}

class IterateStringBenchmark : Benchmark<String>("SubString") {
    override fun prepare(input: String): String = input

    override fun execute(input: String) {
        input.asIterable().count()
    }
}

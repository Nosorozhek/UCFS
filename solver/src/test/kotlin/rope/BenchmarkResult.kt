package rope

data class BenchmarkResult(
    val name: String,
    val meanTime: Double,
    val minTime75: Double,
    val maxTime75: Double,
    val meanMemoryKb: Double,
    val minMemoryKb75: Double,
    val maxMemoryKb75: Double,
    val size: Int
)

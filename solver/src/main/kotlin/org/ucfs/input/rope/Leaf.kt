package org.ucfs.input.rope

internal fun Leaf(text: String): Leaf<Char> {
    return Leaf(text.toList())
}

internal class Leaf<T>(var text: List<T> = listOf()) : RopeNode<T> {
    override val firstLeaf: Leaf<T> = this
    override val lastLeaf: Leaf<T> = this
    override val length: Int get() = text.size
    override val depth: Int = 0

    val nodes: List<LinearGraphVertex<T>> = text.map { LinearGraphVertex(it) } + LinearGraphVertex(null)

    init {
        require(text.size <= MAX_NODE_SIZE)
        for (i in 0 until nodes.lastIndex) {
            nodes[i].next = nodes[i + 1]
        }
    }

    override fun get(index: Int): T {
        return text[index]
    }

    override fun substring(startIndex: Int, length: Int): Leaf<T> {
        val start = startIndex.coerceIn(0, this.length)
        val end = (start + length).coerceIn(start, this.length)
        return Leaf(text.subList(start, end))
    }


    override fun isShortLeaf(): Boolean = this.text.size < MAX_NODE_SIZE
    override fun flattenLeaves(): Sequence<Leaf<T>> = sequenceOf(this)
}
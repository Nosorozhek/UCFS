package org.ucfs.input.rope

class Leaf(var text: String) : RopeNode {
    override val length: Int get() = text.length
    override val depth: Int = 0

    init {
        require(text.length <= MAX_NODE_SIZE)
    }

    override fun get(index: Int): Char {
        return text[index]
    }

    override fun substring(startIndex: Int, length: Int): Leaf {
        val start = startIndex.coerceIn(0, this.length)
        val end = (start+ length).coerceIn(start, this.length)
        return Leaf(text.substring(start, end))
    }


    override fun isShortLeaf(): Boolean = this.text.length < MAX_NODE_SIZE
    override fun flattenLeaves(): Sequence<Leaf> = sequenceOf(this)
}
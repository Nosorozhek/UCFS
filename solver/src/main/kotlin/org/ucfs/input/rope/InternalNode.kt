package org.ucfs.input.rope

import kotlin.math.max

internal class InternalNode<T>(
    val left: RopeNode<T>,
    val right: RopeNode<T>,
) : RopeNode<T> {
    override val firstLeaf: Leaf<T> = left.firstLeaf
    override val lastLeaf: Leaf<T> = right.lastLeaf
    override val length: Int = left.length + right.length
    override val depth: Int = max(left.depth, right.depth) + 1

    init {
        left.lastLeaf.nodes[left.lastLeaf.nodes.lastIndex - 1].next = right.firstLeaf.nodes[0]
    }

    override fun get(index: Int): T {
        return if (left.length > index) {
            left.get(index)
        } else {
            right.get(index - left.length)
        }
    }


    override fun substring(startIndex: Int, length: Int): RopeNode<T> {
        val newLeft = if (startIndex <= 0 && length >= left.length) left else left.substring(startIndex, length)
        val newRight = if (startIndex <= left.length && startIndex + length >= this.length) right
        else right.substring(startIndex - left.length, length - newLeft.length)

        return concat(newLeft, newRight)
    }


    override fun isShortLeaf(): Boolean = false


    override fun flattenLeaves(): Sequence<Leaf<T>> = sequence {
        yieldAll(left.flattenLeaves())
        yieldAll(right.flattenLeaves())
    }
}
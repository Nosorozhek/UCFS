package org.ucfs.input.rope

import kotlin.math.max

class InternalNode(
    val left: RopeNode,
    val right: RopeNode,
) : RopeNode {
    override val length: Int = left.length + right.length
    override val depth: Int = max(left.depth, right.depth) + 1

    override fun get(index: Int): Char {
        return if (left.length > index) {
            left.get(index)
        } else {
            right.get(index - left.length)
        }
    }


    override fun substring(startIndex: Int, length: Int): RopeNode {
        val newLeft = if (startIndex <= 0 && length >= left.length) left else left.substring(startIndex, length)
        val newRight = if (startIndex <= left.length && startIndex + length >= this.length) right
        else right.substring(startIndex - left.length, length - newLeft.length)

        return concat(newLeft, newRight)
    }


    override fun isShortLeaf(): Boolean = false


    override fun flattenLeaves(): Sequence<Leaf> = sequence {
        yieldAll(left.flattenLeaves())
        yieldAll(right.flattenLeaves())
    }
}
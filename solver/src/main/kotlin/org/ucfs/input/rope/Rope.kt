package org.ucfs.input.rope

import java.util.*

const val MAX_NODE_SIZE: Int = 1024
const val MAX_DEPTH: Int = 32

fun Rope(text: String = ""): Rope<Char> = Rope(initTree(text, 0, text.length))

/**
 * A structure that represents a string and provides efficient addition and substring operations.
 * Implementation is based on the article:
 * "Ropes: An Alternative to Strings" by Hans-J. Boehm, Russ Atkinson, and Michael Plass.
 * Published in Software—Practice & Experience, Volume 25, Issue 12, 1995, pages 1315–1330.
 * DOI: https://doi.org/10.1002/spe.4380251203
 */
class Rope<T> internal constructor(private val rootNode: RopeNode<T>) : Iterable<T> {
    /**
     * Returns a new rope obtained by concatenating two ropes.
     * Usually, this simply adds a new node.
     * If the depth of the resulting rope exceeds the [MAX_DEPTH] limit,
     * a new rebalanced rope is returned.
     */
    operator fun plus(rope: Rope<T>): Rope<T> = Rope(concat(rootNode, rope.rootNode))

    override fun iterator(): Iterator<T> = object : Iterator<T> {
        private var currentNode: RopeNode<T>?
        private val nodesStack: Stack<RopeNode<T>>
        private var currentLeaf: Leaf<T>?
        private var posInLeaf: Int

        init {
            currentNode = this@Rope.rootNode
            nodesStack = Stack<RopeNode<T>>()
            currentLeaf = null
            posInLeaf = 0

            if (currentNode!!.length > 0) {
                advanceToNextNonEmptyLeaf()
            } else {
                currentNode = null
            }
        }

        override fun hasNext(): Boolean {
            return currentLeaf != null && posInLeaf < currentLeaf!!.length
        }

        override fun next(): T {
            if (!hasNext()) throw NoSuchElementException()

            val char = currentLeaf!!.text[posInLeaf++]

            if (posInLeaf >= currentLeaf!!.text.size) {
                advanceToNextNonEmptyLeaf()
            }
            return char
        }

        private fun advanceToNextNonEmptyLeaf() {
            while (true) {
                if (currentNode != null) {
                    while (currentNode != null) {
                        val node = currentNode!!
                        nodesStack.push(node)
                        currentNode = if (node is InternalNode) node.left else null
                    }
                } else if (nodesStack.isEmpty()) {
                    currentLeaf = null
                    posInLeaf = 0
                    return
                }

                if (nodesStack.isEmpty()) {
                    currentLeaf = null
                    posInLeaf = 0
                    return
                }

                val poppedNode = nodesStack.pop()

                if (poppedNode is Leaf) {
                    if (poppedNode.text.isNotEmpty()) {
                        posInLeaf = 0
                        currentLeaf = poppedNode
                        currentNode = null
                        return
                    } else {
                        currentNode = null
                    }
                } else {
                    currentNode = (poppedNode as InternalNode).right
                }
            }
        }
    }

    /**
     * Returns the length of the string, represented by this rope.
     */
    val length: Int
        get() = rootNode.length


    /**
     * Returns the character at the specified [index] in this rope.
     *
     * @throws [IndexOutOfBoundsException] if the [index] is out of bounds of this rope.
     */
    operator fun get(index: Int): T = rootNode.get(index)


    /**
     * Returns a new rope representing a subsequence of characters in this rope,
     * starting at the specified [startIndex] and ending right before the specified [endIndex].
     *
     * @param startIndex the start index (inclusive).
     * @param endIndex the end index (exclusive).
     */
    fun substring(startIndex: Int, endIndex: Int): Rope<T> =
        Rope(rootNode.substring(startIndex, endIndex - startIndex))


    /**
     * Returns a new, rebalanced rope. Runs in O(n) time. The original rope remains unchanged.
     *
     * A rope of depth n is considered balanced if its length is at least F(n+2).
     * For example, a balanced rope of depth 1 must have a length of at least 2.
     * Note that balanced ropes may still contain unbalanced subropes.
     */
    fun rebalance(): Rope<T> = Rope(rootNode.rebalance())

    /**
     * Returns a new rope with the specified rope [text] inserted at the given [offset].
     *
     * @param offset the position in the rope where the new text will be inserted.
     * @param text the rope to insert.
     */
    fun insert(offset: Int, text: Rope<T>): Rope<T> = substring(0, offset) + text + substring(offset, length)


    /**
     * Returns a new rope with a substring of length [length] removed,
     * starting from the given [offset].
     *
     * @param offset the start position of the substring to delete.
     * @param length the number of characters to delete.
     */
    fun delete(offset: Int, length: Int): Rope<T> = substring(0, offset) + substring(offset + length, this.length)


    /**
     * Returns a new rope with a substring of [length] replaced by the specified rope [text],
     * starting at the given [offset].
     *
     * @param offset the start position where replacement begins.
     * @param length the number of characters to replace.
     * @param text the rope to insert in place of the removed content.
     */
    fun replace(offset: Int, length: Int, text: Rope<T>): Rope<T> =
        substring(0, offset) + text + substring(offset + length, this.length)

    val inputGraph = RopeInputGraph(this.rootNode.firstLeaf.nodes.first())
}

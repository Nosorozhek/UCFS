package org.ucfs.input.rope

import org.ucfs.input.Edge
import org.ucfs.input.InputGraph
import org.ucfs.input.TerminalInputLabel
import org.ucfs.rsm.symbol.Term
import java.util.*

const val MAX_NODE_SIZE: Int = 1024
const val MAX_DEPTH: Int = 32

fun Rope(text: String = ""): Rope = Rope(initTree(text, 0, text.length))

/**
 * A structure that represents a string and provides efficient addition and substring operations.
 * Implementation is based on the article:
 * "Ropes: An Alternative to Strings" by Hans-J. Boehm, Russ Atkinson, and Michael Plass.
 * Published in Software—Practice & Experience, Volume 25, Issue 12, 1995, pages 1315–1330.
 * DOI: https://doi.org/10.1002/spe.4380251203
 */
class Rope(private val rootNode: RopeNode) : Iterable<Char> {
    /**
     * Returns a new rope obtained by concatenating two ropes.
     * Usually, this simply adds a new node.
     * If the depth of the resulting rope exceeds the [MAX_DEPTH] limit,
     * a new rebalanced rope is returned.
     */
    operator fun plus(rope: Rope): Rope = Rope(concat(rootNode, rope.rootNode))

    operator fun plus(string: String): Rope = plus(Rope(string))

    override fun iterator(): Iterator<Char> = object : Iterator<Char> {
        private var currentNode: RopeNode?
        private val nodesStack: Stack<RopeNode>
        private var currentLeaf: Leaf?
        private var posInLeaf: Int

        init {
            currentNode = this@Rope.rootNode
            nodesStack = Stack<RopeNode>()
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

        override fun next(): Char {
            if (!hasNext()) throw NoSuchElementException()

            val char = currentLeaf!!.text[posInLeaf++]

            if (posInLeaf >= currentLeaf!!.text.length) {
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

                if (nodesStack.isEmpty()){
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
    operator fun get(index: Int): Char = rootNode.get(index)


    /**
     * Returns a new rope representing a subsequence of characters in this rope,
     * starting at the specified [startIndex] and ending right before the specified [endIndex].
     *
     * @param startIndex the start index (inclusive).
     * @param endIndex the end index (exclusive).
     */
    fun substring(startIndex: Int, endIndex: Int): Rope =
        Rope(rootNode.substring(startIndex, endIndex - startIndex))


    /**
     * Returns a new, rebalanced rope. Runs in O(n) time. The original rope remains unchanged.
     *
     * A rope of depth n is considered balanced if its length is at least F(n+2).
     * For example, a balanced rope of depth 1 must have a length of at least 2.
     * Note that balanced ropes may still contain unbalanced subropes.
     */
    fun rebalance(): Rope = Rope(rootNode.rebalance())

    /**
     * Returns a new rope with the specified rope [text] inserted at the given [offset].
     *
     * @param offset the position in the rope where the new text will be inserted.
     * @param text the rope to insert.
     */
    fun insert(offset: Int, text: Rope): Rope = substring(0, offset) + text + substring(offset, length)

    /**
     * Returns a new rope with the specified string [text] inserted at the given [offset].
     *
     * @param offset the position in the rope where the new text will be inserted.
     * @param text the string to insert.
     */
    fun insert(offset: Int, text: String): Rope = insert(offset, Rope(text))

    /**
     * Returns a new rope with a substring of length [length] removed,
     * starting from the given [offset].
     *
     * @param offset the start position of the substring to delete.
     * @param length the number of characters to delete.
     */
    fun delete(offset: Int, length: Int): Rope = substring(0, offset) + substring(offset + length, this.length)

    /**
     * Returns a new rope with a substring of [length] replaced by the specified rope [text],
     * starting at the given [offset].
     *
     * @param offset the start position where replacement begins.
     * @param length the number of characters to replace.
     * @param text the rope to insert in place of the removed content.
     */
    fun replace(offset: Int, length: Int, text: Rope): Rope =
        substring(0, offset) + text + substring(offset + length, this.length)

    /**
     * Returns a new rope with a substring of [length] replaced by the specified string [text],
     * starting at the given [offset].
     *
     * @param offset the start position where replacement begins.
     * @param length the number of characters to replace.
     * @param text the string to insert in place of the removed content.
     */
    fun replace(offset: Int, length: Int, text: String): Rope =
        replace(offset, length, Rope(text))


    fun getGraph(): InputGraph<IteratorGraphVertex, TerminalInputLabel> =
        object : InputGraph<IteratorGraphVertex, TerminalInputLabel>() {
            override fun getInputStartVertices(): MutableSet<IteratorGraphVertex> =
                mutableSetOf(IteratorGraphVertex(PersistentRopeIterator(this@Rope)))

            override fun isFinal(vertex: IteratorGraphVertex): Boolean = !vertex.iterator.hasNext()

            override fun isStart(vertex: IteratorGraphVertex): Boolean = vertex.iterator.isStart()

            override fun getEdges(from: IteratorGraphVertex): MutableList<Edge<IteratorGraphVertex, TerminalInputLabel>> {
                if (!from.iterator.hasNext()) return mutableListOf()
                val newVertexIterator = from.iterator.copy()
                val charLabel = newVertexIterator.next()
                return mutableListOf(Edge(TerminalInputLabel(Term(charLabel)), IteratorGraphVertex(newVertexIterator)))
            }
        }


    internal fun ropeIterator(): PersistentRopeIterator = PersistentRopeIterator(this)

    internal class PersistentRopeIterator private constructor(
        private var currentNode: RopeNode?,
        private var nodes: PersistentStack<RopeNode>,
        private var currentLeaf: Leaf?,
        private var posInLeaf: Int,
        private var isStart: Boolean,
    ) : Iterator<Char> {
        constructor(rope: Rope) : this(
            rope.rootNode,
            PersistentStack<RopeNode>(),
            null,
            0,
            true
        ) {
            if (this.currentNode != null && this.currentNode!!.length > 0) {
                advanceToNextNonEmptyLeaf()
            } else {
                this.currentNode = null
                this.currentLeaf = null
            }
        }

        fun isStart(): Boolean = isStart

        fun copy(): PersistentRopeIterator {
            return PersistentRopeIterator(
                currentNode,
                nodes,
                currentLeaf,
                posInLeaf,
                isStart
            )
        }

        override fun hasNext(): Boolean {
            return currentLeaf != null && posInLeaf < currentLeaf!!.length
        }

        override fun next(): Char {
            if (!hasNext()) throw NoSuchElementException()
            isStart = false

            val char = currentLeaf!!.text[posInLeaf++]

            if (posInLeaf >= currentLeaf!!.length) {
                advanceToNextNonEmptyLeaf()
            }
            return char
        }

        private fun advanceToNextNonEmptyLeaf() {
            while (true) {
                if (currentNode != null) {
                    while (currentNode != null) {
                        val node = currentNode!!
                        nodes = nodes.push(node)
                        currentNode = if (node is InternalNode) node.left else null
                    }
                } else if (nodes.isEmpty()) {
                    currentLeaf = null
                    posInLeaf = 0
                    return
                }
                if (nodes.isEmpty()) {
                    currentLeaf = null
                    posInLeaf = 0
                    return
                }

                val poppedNode = nodes.peek()!!
                nodes = nodes.pop()

                if (poppedNode is Leaf) {
                    if (poppedNode.text.isNotEmpty()) {
                        currentLeaf = poppedNode
                        posInLeaf = 0
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
}

data class IteratorGraphVertex internal constructor(internal val iterator: Rope.PersistentRopeIterator)


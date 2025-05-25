package org.ucfs.input.rope

import org.ucfs.input.IInputGraph
import org.ucfs.input.ILabel
import org.ucfs.input.TerminalInputLabel
import org.ucfs.rsm.symbol.Term
import java.util.*

internal const val MAX_NODE_SIZE: Int = 1024
internal const val MAX_DEPTH: Int = 32

class CharLabel(char: Char) : ILabel by TerminalInputLabel(Term(char))

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

    override fun iterator() = object : Iterator<Char> {
        private val nodes = Stack<RopeNode>()
        private var leafPos: Int

        init {
            leafPos = 0
            if (rootNode.length > 0) {
                var current = rootNode
                while (current is InternalNode) {
                    nodes.push(current)
                    current = current.left
                }
                nodes.push(current)

                moveToNextLeafIfNeeded()
            }
        }

        override fun hasNext(): Boolean {
            return nodes.isNotEmpty()
        }

        override fun next(): Char {
            if (!hasNext()) throw NoSuchElementException()

            val currentLeaf = nodes.peek() as Leaf
            val char = currentLeaf.text[leafPos++]
            moveToNextLeafIfNeeded()
            return char
        }

        private fun moveToNextLeafIfNeeded() {
            var currentLeaf = nodes.peek() as Leaf
            var current: RopeNode = currentLeaf
            while (nodes.isNotEmpty() && leafPos >= currentLeaf.text.length) {
                nodes.pop() // Pop current Leaf
                if (nodes.empty()) {
                    return
                }
                var parent = nodes.peek() as InternalNode
                while (parent.right === current) { // It is essential to compare nodes by reference
                    current = nodes.pop()
                    if (nodes.isEmpty()) {
                        return
                    }
                    parent = nodes.peek() as InternalNode
                }

                current = parent.right
                nodes.push(current)
                while (current is InternalNode) {
                    current = (current as InternalNode).left
                    nodes.push(current)
                }
                currentLeaf = current as Leaf
                leafPos = 0
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


    fun getGraph(): IInputGraph<Int, TerminalInputLabel> = TODO()
}

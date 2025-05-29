package rope

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.ucfs.input.rope.*
import kotlin.test.assertEquals

class TestRope {
    private fun Rope<Char>.content() = (this as Iterable<Char>).joinToString(separator = "")
    private fun Rope<Char>.graphContent()= sequence {
        var currentEdge = inputGraph.getEdges(inputGraph.getInputStartVertices().first()).firstOrNull()
        while (currentEdge != null) {
            yieldAll(currentEdge.label.toString().toList())
            currentEdge = inputGraph.getEdges(currentEdge.targetVertex).firstOrNull()
        }
    }.joinToString(separator = "")

    private val testString = "Test string.".repeat(MAX_NODE_SIZE)

    @Test
    fun `init empty rope test`() {
        val rope = Rope()
        assertEquals(0, rope.length)
        assertThrows<IndexOutOfBoundsException> { rope[0] }
    }

    @Test
    fun `init rope test`() {
        val rope = Rope(testString)
        testString.forEachIndexed { index, char ->
            assertEquals(char, rope.get(index))
        }
    }

    @Test
    fun `rope iterator test`() {
        val rope = Rope(testString)
        assertEquals(testString, rope.content())
        assertEquals(testString, rope.graphContent())
    }

    @Test
    fun `empty rope iterator test`() {
        val rope = Rope()
        assertEquals("", rope.content())
        assertEquals("", rope.graphContent())
    }

    @Test
    fun `add to an empty rope test`() {
        val sumRope = Rope("") + Rope(testString)
        assertEquals(testString, sumRope.content())
        assertEquals(testString, sumRope.graphContent())
    }

    @Test
    fun `add empty rope test`() {
        val sumRope = Rope(testString) + Rope("")
        assertEquals(testString, sumRope.content())
        assertEquals(testString, sumRope.graphContent())
    }

    @Test
    fun `add rope test`() {
        val firstString = testString
        val secondString = testString.reversed()
        val firstRope = Rope(firstString)
        val secondRope = Rope(secondString)
        val sumRope = firstRope + secondRope
        assertEquals(firstString + secondString, sumRope.content())
        assertEquals(firstString + secondString, sumRope.graphContent())
    }

    @Test
    fun `sum of multiple ropes test`() {
        var expected = testString
        var rope = Rope(expected)
        for (i in 0..MAX_DEPTH * 20) {
            val str = ('a' + i % 26).toString().repeat(MAX_NODE_SIZE)
            rope += Rope(str)
            expected += str
        }

        assertEquals(expected, rope.content())
        assertEquals(expected, rope.graphContent())
    }

    @Test
    fun `rebalancing rope test`() {
        var expected = ""
        var leftBranch : RopeNode<Char> = Leaf("left")
        var rightBranch : RopeNode<Char> = Leaf("right")
        for (i in 0..MAX_DEPTH * 10) {
            val str = ('a' + i % 26).toString().repeat(i % MAX_NODE_SIZE + 1)
            leftBranch = InternalNode(leftBranch, Leaf(str))
            rightBranch = InternalNode(Leaf(str), rightBranch)
            expected += str
        }
        expected = "left" + expected + expected.reversed() + "right"
        val rope = Rope(InternalNode(leftBranch, rightBranch))

        assertEquals(expected, rope.rebalance().content())
        assertEquals(expected, rope.rebalance().graphContent())
    }

    @Test
    fun `substring test`() {
        for (i in testString.indices step MAX_NODE_SIZE / 5) {
            for (j in i + 1..testString.length step MAX_NODE_SIZE / 5) {
                val subRope = Rope(testString).substring(i, j)
                assertEquals(testString.substring(i, j), subRope.content())
                assertEquals(testString.substring(i, j).length, subRope.graphContent().length)
            }
        }
    }

    @Test
    fun `insert in the middle test`() {
        val rope = Rope(testString)
        val insert = Rope(testString.reversed())
        val offset = testString.length / 2
        val result = rope.insert(offset, insert)

        val expected = testString.substring(0, offset) +
                insert.content() +
                testString.substring(offset)
        assertEquals(expected, result.content())
        assertEquals(expected, result.graphContent())
    }

    @Test
    fun `insert at the start test`() {
        val rope = Rope(testString)
        val insert = testString.reversed()
        val result = rope.insert(0, Rope(insert))

        val expected = insert + testString
        assertEquals(expected, result.content())
        assertEquals(expected, result.graphContent())
    }

    @Test
    fun `insert at the end test`() {
        val rope = Rope(testString)
        val insert = testString.reversed()
        val result = rope.insert(rope.length, Rope(insert))

        val expected = testString + insert
        assertEquals(expected, result.content())
        assertEquals(expected, result.graphContent())
    }

    @Test
    fun `delete from the middle test`() {
        val rope = Rope(testString)
        val offset = testString.length / 3
        val deleteLength = MAX_NODE_SIZE * 2
        val result = rope.delete(offset, deleteLength)

        val expected = testString.removeRange(offset, offset + deleteLength)
        assertEquals(expected, result.content())
        assertEquals(expected, result.graphContent())
    }

    @Test
    fun `delete from start to near end test`() {
        val rope = Rope(testString)
        val result = rope.delete(0, testString.length - 10)
        val expected = testString.takeLast(10)
        assertEquals(expected, result.content())
        assertEquals(expected, result.graphContent())
    }

    @Test
    fun `delete from almost start to the end test`() {
        val rope = Rope(testString)
        val result = rope.delete(10, testString.length)
        val expected = testString.take(10)
        assertEquals(expected, result.content())
        assertEquals(expected, result.graphContent())
    }

    @Test
    fun `replace in the middle test`() {
        val rope = Rope(testString)
        val replacement = Rope(testString.reversed())
        val offset = testString.length / 4
        val lengthToReplace = MAX_NODE_SIZE * 2

        val result = rope.replace(offset, lengthToReplace, replacement)

        val expected = testString.substring(0, offset) +
                replacement.content() +
                testString.substring(offset + lengthToReplace)
        assertEquals(expected, result.content())
        assertEquals(expected, result.graphContent())
    }

    @Test
    fun `replace near the end test`() {
        val rope = Rope(testString)
        val replacement = testString.reversed()
        val offset = testString.length - MAX_NODE_SIZE
        val lengthToReplace = MAX_NODE_SIZE

        val result = rope.replace(offset, lengthToReplace, Rope(replacement))

        val expected = testString.substring(0, offset) +
                replacement +
                testString.substring(offset + lengthToReplace)
        assertEquals(expected, result.content())
        assertEquals(expected, result.graphContent())
    }

    @Test
    fun `replace beginning test`() {
        val rope = Rope(testString)
        val replacement = Rope("replacement")
        val offset = 0
        val lengthToReplace = MAX_NODE_SIZE / 3

        val result = rope.replace(offset, lengthToReplace, replacement)

        val expected = replacement.content() + testString.substring(lengthToReplace)
        assertEquals(expected, result.content())
        assertEquals(expected, result.graphContent())
    }

    @Test
    fun `replace entire rope test`() {
        val replacement = Rope(testString.reversed().take(testString.length / 2))
        val result = Rope(testString).replace(0, testString.length, replacement)

        assertEquals(replacement.content(), result.content())
        assertEquals(replacement.content(), result.graphContent())
    }

    @Test
    fun `replace with empty string test`() {
        val rope = Rope(testString)
        val offset = testString.length / 2 - 100
        val lengthToReplace = 200

        val result = rope.replace(offset, lengthToReplace, Rope(""))
        val expected = testString.removeRange(offset, offset + lengthToReplace)
        assertEquals(expected, result.content())
        assertEquals(expected, result.graphContent())
    }
}

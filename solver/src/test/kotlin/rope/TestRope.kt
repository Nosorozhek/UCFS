package rope

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.ucfs.input.rope.*
import kotlin.test.assertEquals

class TestRope {
    private fun Rope.content() = (this as Iterable<Char>).joinToString(separator = "")
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
    }

    @Test
    fun `empty rope iterator test`() {
        val rope = Rope()
        assertEquals("", rope.content())
    }

    @Test
    fun `add to an empty rope test`() {
        val sumRope = Rope("") + Rope(testString)
        assertEquals(testString, sumRope.content())
    }

    @Test
    fun `add empty rope test`() {
        val sumRope = Rope(testString) + Rope("")
        assertEquals(testString, sumRope.content())
    }

    @Test
    fun `add rope test`() {
        val firstString = testString
        val secondString = testString.reversed()
        val firstRope = Rope(firstString)
        val secondRope = Rope(secondString)
        val sumRope = firstRope + secondRope
        assertEquals(firstString + secondString, sumRope.content())
    }

    @Test
    fun `sum of multiple ropes test`() {
        var expected = testString
        var rope = Rope(expected)
        for (i in 0..MAX_DEPTH * 20) {
            val str = ('a' + i % 26).toString().repeat(MAX_NODE_SIZE)
            rope += str
            expected += str
        }

        assertEquals(expected, rope.content())
    }

    @Test
    fun `rebalancing rope test`() {
        var expected = ""
        var leftBranch: RopeNode = Leaf("left")
        var rightBranch: RopeNode = Leaf("right")
        for (i in 0..MAX_DEPTH * 10) {
            val str = ('a' + i % 26).toString().repeat(i % MAX_NODE_SIZE + 1)
            leftBranch = InternalNode(leftBranch, Leaf(str))
            rightBranch = InternalNode(Leaf(str), rightBranch)
            expected += str
        }
        expected = "left" + expected + expected.reversed() + "right"
        val rope = Rope(InternalNode(leftBranch, rightBranch))

        assertEquals(expected, rope.rebalance().content())
    }

    @Test
    fun `substring test`() {
        val string = "Test string.".repeat(100).take(300)
        val rope = Rope(string)

        for (i in string.indices) {
            for (j in i + 1..string.length) {
                assertEquals(string.substring(i, j), rope.substring(i, j).content())
            }
        }

    }
}
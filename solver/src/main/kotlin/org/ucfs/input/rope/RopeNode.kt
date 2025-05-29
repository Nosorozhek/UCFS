package org.ucfs.input.rope


// F_2, ..., F_{MAX_DEPTH+2}
private val fibonacci: List<Int> =
    generateSequence(Pair(0, 1)) { Pair(it.second, it.first + it.second) }
        .map { it.second }
        .drop(1)
        .take(MAX_DEPTH + 1)
        .toList()

private fun determineSlotIndex(length: Int): Int {
    if (length < fibonacci[0]) {
        return 0
    }
    val searchResult = fibonacci.binarySearch(length)

    var slotIdx = if (searchResult >= 0) searchResult else (-searchResult - 1 - 1)

    slotIdx = slotIdx.coerceIn(0, fibonacci.size - 1)
    return slotIdx
}

internal sealed interface RopeNode<T> {

    val length: Int

    val depth: Int

    val firstLeaf: Leaf<T>

    val lastLeaf: Leaf<T>

    fun get(index: Int): T


    fun substring(startIndex: Int, length: Int): RopeNode<T>


    fun isShortLeaf(): Boolean


    fun flattenLeaves(): Sequence<Leaf<T>>

    fun rebalance(): RopeNode<T> {
        if (length == 0) return Leaf()

        val slots = MutableList<RopeNode<T>?>(fibonacci.size) { null }
        var minOccupiedSlot = Int.MAX_VALUE
        flattenLeaves()
            .filter { it.length != 0 }
            .forEach { currentLeaf ->
                val targetSlotIdx = determineSlotIndex(currentLeaf.length)

                var ropeToPlace: RopeNode<T> = currentLeaf
                if (minOccupiedSlot > targetSlotIdx) {
                    slots[targetSlotIdx] = ropeToPlace
                    minOccupiedSlot = targetSlotIdx
                } else {
                    var currentRope: RopeNode<T>? = null
                    for (i in minOccupiedSlot until targetSlotIdx) {
                        if (slots[i] != null) {
                            currentRope = if (currentRope == null) slots[i] else InternalNode(slots[i]!!, currentRope)
                            slots[i] = null
                        }
                    }

                    ropeToPlace = if (currentRope == null) currentLeaf else InternalNode(currentRope, currentLeaf)

                    var ropeIsPlaced = false
                    for (i in targetSlotIdx until slots.size) {
                        if (slots[i] != null) {
                            ropeToPlace = InternalNode(slots[i]!!, ropeToPlace)
                            slots[i] = null
                        } else {
                            slots[i] = ropeToPlace
                            ropeIsPlaced = true
                            minOccupiedSlot = i
                            break
                        }
                    }
                    require(ropeIsPlaced)
                }
            }

        var finalRope: RopeNode<T>? = null
        for (kFinal in 0 until slots.size) {
            if (slots[kFinal] != null) {
                finalRope = if (finalRope == null) slots[kFinal] else InternalNode(slots[kFinal]!!, finalRope)
            }
        }

        return finalRope ?: Leaf()
    }
}

internal fun initTree(string: String, beginIndex: Int, length: Int): RopeNode<Char> =
    if (length <= MAX_NODE_SIZE) {
        Leaf(string.substring(beginIndex, beginIndex + length).toList())
    } else {
        InternalNode(
            initTree(string, beginIndex, length / 2),
            initTree(string, beginIndex + length / 2, length - length / 2)
        )
    }

internal fun <T> initTree(string: List<T>, beginIndex: Int, length: Int): RopeNode<T> =
    if (length <= MAX_NODE_SIZE) {
        Leaf(string.subList(beginIndex, beginIndex + length).toList())
    } else {
        InternalNode(
            initTree(string, beginIndex, length / 2),
            initTree(string, beginIndex + length / 2, length - length / 2)
        )
    }

internal fun <T> concat(left: RopeNode<T>, right: RopeNode<T>): RopeNode<T> {
    if(left.length == 0) return right
    if(right.length == 0) return left
    if (left.isShortLeaf() && right.isShortLeaf()) {
        val combined = (left as Leaf).text + (right as Leaf).text
        return if (combined.size <= MAX_NODE_SIZE) {
            Leaf(combined)
        } else {
            initTree(combined, 0, combined.size)
        }
    }

    if (left is InternalNode && left.right.isShortLeaf() && right.isShortLeaf()) {
        val leftLeaf = left.right as Leaf
        val rightLeaf = right as Leaf
        val mergedText = leftLeaf.text + rightLeaf.text
        return if (mergedText.size <= MAX_NODE_SIZE) {
            val newRight = Leaf(mergedText)
            val newLeft = left.left
            InternalNode(newLeft, newRight)
        } else {
            InternalNode(left.left, initTree(mergedText, 0, mergedText.size))
        }
    }

    val concatenated = InternalNode(left, right)
    return if (concatenated.depth > MAX_DEPTH) {
        concatenated.rebalance()
    } else {
        concatenated
    }
}






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

sealed interface RopeNode {

    val length: Int

    val depth: Int


    fun get(index: Int): Char


    fun substring(startIndex: Int, length: Int): RopeNode


    fun isShortLeaf(): Boolean


    fun flattenLeaves(): Sequence<Leaf>


    fun rebalance(): RopeNode {
        if (length == 0) return Leaf("")

        val slots = MutableList<RopeNode?>(fibonacci.size) { null }
        var minOccupiedSlot = Int.MAX_VALUE
        flattenLeaves()
            .filter { it.length != 0 }
            .forEach { currentLeaf ->
                val targetSlotIdx = determineSlotIndex(currentLeaf.length)

                var ropeToPlace: RopeNode = currentLeaf
                if (minOccupiedSlot > targetSlotIdx) {
                    slots[targetSlotIdx] = ropeToPlace
                    minOccupiedSlot = targetSlotIdx
                } else {
                    var currentRope: RopeNode? = null
                    for (i in minOccupiedSlot until  targetSlotIdx) {
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

        var finalRope: RopeNode? = null
        for (kFinal in 0 until slots.size) {
            if (slots[kFinal] != null) {
                finalRope = if (finalRope == null) slots[kFinal] else InternalNode(slots[kFinal]!!, finalRope)
            }
        }

        return finalRope ?: Leaf("")
    }
}

internal fun initTree(string: String, beginIndex: Int, length: Int): RopeNode =
    if (length <= MAX_NODE_SIZE) {
        Leaf(string.substring(beginIndex, beginIndex + length))
    } else {
        InternalNode(
            initTree(string, beginIndex, length / 2),
            initTree(string, beginIndex + length / 2, length - length / 2)
        )
    }

internal fun concat(left: RopeNode, right: RopeNode): RopeNode {
    if (left.isShortLeaf() && right.isShortLeaf()) {
        val combined = (left as Leaf).text + (right as Leaf).text
        return if (combined.length <= MAX_NODE_SIZE) {
            Leaf(combined)
        } else {
            initTree(combined, 0, combined.length)
        }
    }

    if (left is InternalNode && left.right.isShortLeaf() && right.isShortLeaf()) {
        val leftLeaf = left.right as Leaf
        val rightLeaf = right as Leaf
        val mergedText = leftLeaf.text + rightLeaf.text
        return if (mergedText.length <= MAX_NODE_SIZE) {
            val newRight = Leaf(mergedText)
            val newLeft = left.left
            InternalNode(newLeft, newRight)
        } else {
            InternalNode(left.left, initTree(mergedText, 0, mergedText.length))
        }
    }

    val concatenated = InternalNode(left, right)
    return if (concatenated.depth > MAX_DEPTH) {
        concatenated.rebalance()
    } else {
        concatenated
    }
}






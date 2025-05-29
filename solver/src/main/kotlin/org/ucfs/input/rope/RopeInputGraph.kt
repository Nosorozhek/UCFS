package org.ucfs.input.rope

import org.ucfs.input.Edge
import org.ucfs.input.InputGraph
import org.ucfs.input.TerminalInputLabel
import org.ucfs.rsm.symbol.Term

class RopeInputGraph<T>(
    private val startVertex: LinearGraphVertex<T>,
) : InputGraph<LinearGraphVertex<T>, TerminalInputLabel>() {

    override fun getInputStartVertices(): MutableSet<LinearGraphVertex<T>> =
        mutableSetOf(startVertex)

    override fun isFinal(vertex: LinearGraphVertex<T>): Boolean = vertex.next == null

    override fun isStart(vertex: LinearGraphVertex<T>): Boolean = vertex == startVertex

    override fun getEdges(from: LinearGraphVertex<T>): MutableList<Edge<LinearGraphVertex<T>, TerminalInputLabel>> {
        return if (from.next != null) {
            mutableListOf(
                Edge(
                    TerminalInputLabel(Term(from.char)),
                    from.next!!
                )
            )
        } else mutableListOf()
    }
}

class LinearGraphVertex<T> internal constructor(
    val char: T?,
    internal var next: LinearGraphVertex<T>? = null
)
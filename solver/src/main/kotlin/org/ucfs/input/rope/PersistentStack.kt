package org.ucfs.input.rope

/**
 * A persistent stack implementation.
 * Each operation returns a new stack instance without modifying the original one.
 */
class PersistentStack<T> private constructor(
    private val head: Node<T>?,
    private val size: Int
) : Iterable<T> {
    private class Node<T>(val value: T, val next: Node<T>?)

    constructor() : this(null, 0)

    /**
     * Adds an element to the top of the stack and returns a new stack instance.
     *
     * @param element The element to add
     */
    fun push(element: T): PersistentStack<T> = PersistentStack(Node(element, head), size + 1)

    /**
     * Removes the top element from the stack and returns a new stack instance.
     */
    fun pop(): PersistentStack<T> = if (isEmpty()) this else PersistentStack(head?.next, size - 1)

    /**
     * Returns the top element of the stack without removing it.
     */
    fun peek(): T? = head?.value

    /**
     * Returns true if the stack is empty, false otherwise.
     */
    fun isEmpty(): Boolean = head == null

    /**
     * Returns the number of elements in the stack.
     */
    fun size(): Int = size

    /**
     * Returns an iterator over the elements in the stack, from top to bottom.
     */
    override fun iterator(): Iterator<T> = object : Iterator<T> {
        private var current: Node<T>? = head

        override fun hasNext(): Boolean = current != null

        override fun next(): T {
            val value = current?.value ?: throw NoSuchElementException("No more elements in the stack")
            current = current!!.next
            return value
        }
    }

    /**
     * Returns a persistent copy of the stack. No new nodes are allocated.
     */
    fun copy(): PersistentStack<T> = PersistentStack(head, size)
}

/**
 * Creates a new persistent stack from the given elements.
 *
 * @param elements The elements to add to the stack
 */
fun <T> persistentStackOf(vararg elements: T): PersistentStack<T> {
    var stack = PersistentStack<T>()
    for (i in elements.size - 1 downTo 0) {
        stack = stack.push(elements[i])
    }
    return stack
}
package com.sjn.stamp.utils

interface DiskCache<T> {

    /**
     * Returns all values from cache directory if all files are same type
     *
     * @return
     */
    val all: List<T>

    /**
     * Sets the value to `value`.
     */
    operator fun set(key: String, value: T)

    /**
     * Returns a value by `key`, or null if it doesn't
     * exist is not currently readable. If a value is returned, it is moved to
     * the head of the LRU queue.
     */
    operator fun get(key: String): T

    /**
     * Drops the entry for `key` if it exists and can be removed. Entries
     * actively being edited cannot be removed.
     *
     * @return true if an entry was removed.
     */
    fun remove(key: String): Boolean

    /**
     * Deletes all file from cache directory
     */
    fun clear()

    /**
     * Returns true if there is file by `key` in cache folder
     *
     * @param key
     * @return
     */
    fun exists(key: String): Boolean

    /**
     * Closes this cache. Stored values will remain on the filesystem.
     */
    fun close()

}
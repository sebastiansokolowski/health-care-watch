package com.selastiansokolowski.healthcarewatch.util

/**
 * Created by Sebastian Sokołowski on 06.07.19.
 */
open class SingleEvent<out T>(private val content: T) {

    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content
}
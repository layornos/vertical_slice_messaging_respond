package edu.kit.ipd.sdq.respond.mqtt

import kotlin.text.Regex

typealias MatchCallback = (List<String>) -> Unit
typealias DefaultCallback = () -> Unit

class PathMatcher(prefix: String = "", initializer: PathMatcherBody.() -> Unit) {
    private val body = PathMatcherBody(prefix)

    init {
        body.apply(initializer)
    }

    fun match(path: String) {
        body.options.forEach {
            val matches = it.key.matchEntire(path)
            if (matches != null) {
                it.value.invoke(matches.groupValues.drop(1))
                return
            }
        }
        body.defaultCallback?.invoke()
    }

    class PathMatcherBody(private val prefix: String) {
        val options = mutableMapOf<Regex, MatchCallback>()
        var defaultCallback: DefaultCallback? = null

        object Default
        val default = Default

        operator fun Default.invoke(callback: DefaultCallback) {
            defaultCallback = callback
        }

        operator fun String.invoke(callback: MatchCallback) {
            options[Regex(prefix + this)] = callback
        }
    }
}

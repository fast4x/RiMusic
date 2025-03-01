package it.fast4x.environment.models

import kotlinx.serialization.Serializable

@Serializable
data class Run(
    val text: String,
    val navigationEndpoint: NavigationEndpoint?,
)

@Serializable
data class Runs(
    val runs: List<Run> = listOf()
) {
    val text: String
        get() = runs.joinToString("") { it.text ?: "" }

    fun splitBySeparator(): List<List<Run>> {
        return runs.flatMapIndexed { index, run ->
            when {
                index == 0 || index == runs.lastIndex -> listOf(index)
                run.text == " • " -> listOf(index - 1, index + 1)
                else -> emptyList()
            }
        }.windowed(size = 2, step = 2) { (from, to) -> runs.slice(from..to) }.let {
            it.ifEmpty {
                listOf(runs)
            }
        }
    }

    @Serializable
    data class Run(
        val text: String?,
        val navigationEndpoint: NavigationEndpoint?,
    )
}

fun List<Runs.Run>.splitBySeparator(): List<List<Runs.Run>> {
    val res = mutableListOf<List<Runs.Run>>()
    var tmp = mutableListOf<Runs.Run>()
    forEach { run ->
        if (run.text == " • ") {
            res.add(tmp)
            tmp = mutableListOf()
        } else {
            tmp.add(run)
        }
    }
    res.add(tmp)
    return res
}

fun <T> List<T>.oddElements() = filterIndexed { index, _ ->
    index % 2 == 0
}
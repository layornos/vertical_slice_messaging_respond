package edu.kit.ipd.sdq.respond.utils

fun <T> listOfLambda(count: Int, lambda: () -> T): List<T> {
    val list = mutableListOf<T>()
    repeat(count) {
        list.add(lambda())
    }

    return list.toList()
}
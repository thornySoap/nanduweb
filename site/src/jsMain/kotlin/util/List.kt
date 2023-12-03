package util

fun <T> List<T>.minusElementAt(index: Int) = toMutableList().apply { removeAt(index) } as List<T>

val <T> List<T>.permutations: List<List<T>>
    get() = if (size <= 1) listOf(this) else
        mapIndexed { index, item ->
            minusElementAt(index).permutations.map { it.plusElement(item) }
        }.flatten()

val <T> List<T>.dimensions: List<Int>
    get() = (firstOrNull()?.let { if (it is List<*>) it.dimensions else emptyList() } ?: listOf(0)).plusElement(size)

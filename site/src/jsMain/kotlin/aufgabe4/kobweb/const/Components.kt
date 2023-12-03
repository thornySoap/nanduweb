package aufgabe4.kobweb.const

const val COMPONENTS_SQUARE_SIZE = 20

enum class Components(
    val imageSrc: String,
    val representation: List<String>,
) {
    WHITE(imageSrc = "white_component.svg", representation = listOf("W", "W")),
    BLUE(imageSrc = "blue_component.svg", representation = listOf("B", "B")),
    RED(imageSrc = "red_component.svg", representation = listOf("R", "r")),
    RED_INVERT(imageSrc = "red_component_invert.svg", representation = listOf("r", "R")),
    EMITTER(imageSrc = "emitter_component.svg", representation = listOf("Q")),
    RECEIVER(imageSrc = "receiver_component.svg", representation = listOf("L")),
    ;

    companion object {
        const val BLANK = "X"

        fun fromChar(char: Char) = entries.find { char == it.representation.getOrNull(0)?.getOrNull(0) }
    }
}

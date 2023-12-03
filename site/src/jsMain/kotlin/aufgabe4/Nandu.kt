package aufgabe4

import util.dimensions
import util.minusElementAt

/**
 * Modified to work with Nandu web editor
 */
class Nandu(private val input: () -> String) {
    constructor(input: String): this(input::toString)

    private val components: List<List<NanduComponent>> by lazy {
        val (dimensions, field) = input()
            .split("\n")
            .map { it.split(" ").filterNot(String::isEmpty) }.filterNot(List<*>::isEmpty)
            .run {
                first().map(String::toInt) to
                        minusElementAt(0).toMutableList()
            }

        if (field.dimensions != dimensions) throw IllegalArgumentException("Dimensions $dimensions doesn't match")

        val components = List(dimensions.last()) { mutableListOf<NanduComponent>() }
        val sensors = List(dimensions.last()) { MutableList<LightSensor?>(dimensions.first()) { null } }

        // From bottom to top
        for ((rowIInverse, row) in field.reversed().withIndex()) {
            val rowI = dimensions.last() - 1 - rowIInverse
            var columnI = 0
            while (columnI < row.size) {
                val name = row[columnI]
                val connectingSensor = sensors.getOrNull(rowI + 1)?.get(columnI)
                val rightConnectingSensor = sensors.getOrNull(rowI + 1)?.getOrNull(columnI + 1)

                if (name.matches(Regex("X|[LQ].*"))) {
                    if (name.startsWith("L")) Receiver(name).let {
                        components[rowI].add(it)
                        sensors[rowI][columnI] = it.sensor
                        receivers.add(it)
                    } else if (name.startsWith("Q")) Emitter(name, output = connectingSensor).let {
                        components[rowI].add(it)
                        emitters.add(it)
                    }
                    columnI += 1
                } else if (name.matches(Regex("[rRWB]"))) {
                    if (name.uppercase() == "R") RedComponent(
                        leftOutput = connectingSensor,
                        rightOutput = rightConnectingSensor,
                    ).let {
                        components[rowI].add(it)
                        sensors[rowI][columnI + if (name == "r") 1 else 0] = it.sensor
                    } else if (name == "W") WhiteComponent(
                        leftOutput = connectingSensor,
                        rightOutput = rightConnectingSensor,
                    ).let {
                        components[rowI].add(it)
                        sensors[rowI][columnI] = it.leftSensor
                        sensors[rowI][columnI + 1] = it.rightSensor
                    } else if (name == "B") BlueComponent(
                        leftOutput = connectingSensor,
                        rightOutput = rightConnectingSensor,
                    ).let {
                        components[rowI].add(it)
                        sensors[rowI][columnI] = it.leftSensor
                        sensors[rowI][columnI + 1] = it.rightSensor
                    }
                    columnI += 2
                } else {
                    throw IllegalArgumentException("$name not understandable")
                }
            }
        }

        components
    }
    private val emitters: MutableList<Emitter> = mutableListOf()
        get() = field.apply { sortBy(Emitter::name) }
    private val receivers: MutableList<Receiver> = mutableListOf()
        get() = field.apply { sortBy(Receiver::name) }

    val emitterNames get() = emitters.map(Emitter::name)
    val receiverNames get() = receivers.map(Receiver::name)

    fun configure(config: (NanduConfiguration) -> Unit) {
        components
        val configuration = emitters.associate { it.name to it.emitting }.toMutableMap()
        config(configuration)
        for ((name, emitting) in configuration) emitters.find { it.name == name }?.emitting = emitting
    }

    fun doCalculation() {
        // From top to bottom
        for (row in components) {
            for (component in row) {
                component.doLogic()
            }
        }
    }

    fun getResults() = receivers.associate { it.name to it.receiving }

    fun getTable(): Map<String, List<Boolean>> {
        components
        val table = (emitters.map(Emitter::name) + receivers.map(Receiver::name))
            .associateWith { mutableListOf<Boolean>() }
        for (i in 0..<(1 shl emitters.size)) {
            var k = i
            for (emitter in emitters) {
                emitter.emitting = k and 1 == 1
                k = k shr 1
            }
            doCalculation()
            emitters.forEach { table[it.name]!!.add(it.emitting) }
            receivers.forEach { table[it.name]!!.add(it.receiving) }
        }
        return table
    }

    private interface NanduComponent {
        fun doLogic()
    }

    private data class LightSensor(var detectsLight: Boolean)

    private class Emitter(val name: String, val output: LightSensor?): NanduComponent {
        var emitting = false

        override fun doLogic() {
            output?.detectsLight = emitting
        }
    }

    private class Receiver(val name: String): NanduComponent {
        val sensor = LightSensor(detectsLight = false)
        var receiving = false

        override fun doLogic() {
            receiving = sensor.detectsLight
        }
    }

    private abstract class LogicComponent(val leftOutput: LightSensor?, val rightOutput: LightSensor?): NanduComponent

    private class RedComponent(
        leftOutput: LightSensor?,
        rightOutput: LightSensor?,
    ): LogicComponent(leftOutput, rightOutput) {
        val sensor = LightSensor(detectsLight = false)

        override fun doLogic() {
            leftOutput?.detectsLight = !sensor.detectsLight
            rightOutput?.detectsLight = !sensor.detectsLight
        }
    }

    private class WhiteComponent(
        leftOutput: LightSensor?,
        rightOutput: LightSensor?,
    ): LogicComponent(leftOutput, rightOutput) {
        val leftSensor = LightSensor(detectsLight = false)
        val rightSensor = LightSensor(detectsLight = false)

        override fun doLogic() {
            leftOutput?.detectsLight = !(leftSensor.detectsLight && rightSensor.detectsLight)
            rightOutput?.detectsLight = !(leftSensor.detectsLight && rightSensor.detectsLight)
        }
    }

    private class BlueComponent(
        leftOutput: LightSensor?,
        rightOutput: LightSensor?,
    ): LogicComponent(leftOutput, rightOutput) {
        val leftSensor = LightSensor(detectsLight = false)
        val rightSensor = LightSensor(detectsLight = false)

        override fun doLogic() {
            leftOutput?.detectsLight = leftSensor.detectsLight
            rightOutput?.detectsLight = rightSensor.detectsLight
        }
    }
}

typealias NanduConfiguration = MutableMap<String, Boolean>

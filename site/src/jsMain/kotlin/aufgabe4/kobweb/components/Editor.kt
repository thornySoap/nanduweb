package aufgabe4.kobweb.components

import androidx.compose.runtime.*
import aufgabe4.Nandu
import aufgabe4.kobweb.const.COMPONENTS_SQUARE_SIZE
import aufgabe4.kobweb.const.Components
import aufgabe4.kobweb.const.DRAG_OFFSET
import aufgabe4.kobweb.const.TARGET_CONSTRUCTION
import aufgabe4.kobweb.util.variant
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.css.functions.url
import com.varabyte.kobweb.compose.file.readBytes
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.icons.fa.*
import com.varabyte.kobweb.silk.components.layout.SimpleGrid
import com.varabyte.kobweb.silk.components.layout.numColumns
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.palette.background
import com.varabyte.kobweb.silk.theme.colors.palette.color
import com.varabyte.kobweb.silk.theme.colors.palette.toPalette
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.download
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.*
import util.dimensions
import kotlin.experimental.and
import kotlin.math.round


@Composable
fun Editor(modifier: Modifier = Modifier) {
    var colorMode by ColorMode.currentState
    val colorPalette = colorMode.toPalette()

    var showHelp by remember { mutableStateOf(false) }
    var showFileChooser by remember { mutableStateOf(false) }
    var showTablePanel by remember { mutableStateOf(false) }

    var construction by remember { mutableStateOf("4 6\nX  Q1 Q2 X\nX  W  W  X\nr  R  R  r\nX  B  B  X\nX  W  W  X\nX  L1 L2 X".asConstruction()) }
    val nandu by remember(construction) { mutableStateOf(Nandu(input = construction.asString())) }

    var scrollOffsetX by remember { mutableStateOf(0) }
    var scrollOffsetY by remember { mutableStateOf(0) }
    var dragging by remember { mutableStateOf<String?>(null) }

    var table by remember(nandu) { mutableStateOf<Map<String, List<Boolean>>?>(null) }
    LaunchedEffect(nandu) {
        table = nandu.getTable()
    }

    Column(modifier = modifier.userSelect(UserSelect.None)) {
        SimpleGrid(
            numColumns = numColumns(8),
            modifier = Modifier
                .backgroundColor(colorPalette.background.variant())
                .fillMaxWidth()
                .padding(8.px)
                .zIndex(20),
        ) {
            Components.entries.forEach { component ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .onDragStart { dragEvent ->
                            dragEvent.dataTransfer?.setData(
                                DRAG_OFFSET,
                                listOf(dragEvent.offsetX, dragEvent.offsetY).joinToString(","),
                            )
                            dragEvent.dataTransfer?.setData(TARGET_CONSTRUCTION, "")
                            dragging = when (component) {
                                Components.EMITTER -> (1..Int.MAX_VALUE).asSequence()
                                    .map { "${Components.EMITTER.representation.first()}$it" }.find {
                                        it !in nandu.emitterNames
                                    }

                                Components.RECEIVER -> (1..Int.MAX_VALUE).asSequence()
                                    .map { "${Components.RECEIVER.representation.first()}$it" }.find {
                                        it !in nandu.receiverNames
                                    }

                                else -> component.representation.first()
                            }
                        }
                        .onDragEnd { dragging = null },
                    contentAlignment = Alignment.Center,
                ) {
                    Image(src = component.imageSrc)
                }
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Button(
                    onClick = { showHelp = !showHelp },
                ) {
                    FaQuestion()
                }
                Box(modifier = Modifier.width(4.px))
                Button(
                    onClick = { colorMode = colorMode.opposite },
                ) {
                    if (colorMode.isLight) FaSun()
                    else FaMoon()
                }
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                A(
                    href = "data:application/text;charset=utf-8,${construction.asString().replace("\n", "%0A")}",
                    attrs = { download("new_document.txt") }
                ) {
                    Button(
                        onClick = { },
                    ) {
                        FaDownload()
                    }
                }
                Box(modifier = Modifier.width(4.px))
                Button(
                    onClick = { showFileChooser = true },
                ) {
                    FaUpload()
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .backgroundImage(url("dot.png"))
                .backgroundRepeat(BackgroundRepeat.Repeat)
                .backgroundPosition(
                    BackgroundPosition.of(
                        CSSPosition(
                            x = (scrollOffsetX % COMPONENTS_SQUARE_SIZE).px,
                            y = (scrollOffsetY % COMPONENTS_SQUARE_SIZE).px,
                        )
                    )
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10)
                    .onMouseMove {
                        if (dragging == null && it.buttons and 1 == 1.toShort()) {
                            scrollOffsetX += it.movementX
                            scrollOffsetY += it.movementY
                        }
                    }
                    .onDragOver { it.preventDefault() }
                    .onDrop { dragEvent ->
                        dragEvent.preventDefault()

                        val (dx, dy) = dragEvent.dataTransfer?.getData(DRAG_OFFSET)?.split(",")?.map(String::toDouble)
                            ?: listOf(0.0, 0.0)
                        var x = round((dragEvent.offsetX - dx - scrollOffsetX) / COMPONENTS_SQUARE_SIZE).toInt()
                        var y = round((dragEvent.offsetY - dy - scrollOffsetY) / COMPONENTS_SQUARE_SIZE).toInt()

                        val constr = (dragEvent.dataTransfer?.getData(TARGET_CONSTRUCTION)
                            ?.takeIf(String::isNotBlank)?.asConstruction()
                            ?: construction).map(List<String>::toMutableList).toMutableList()
                        val component = dragging?.first()?.let { Components.fromChar(it) }

                        fun w() = constr.dimensions[0]
                        fun h() = constr.dimensions[1]

                        if (constr.isEmpty()) {
                            component?.representation?.size
                                ?.let { MutableList(it) { dragging!! } }
                                ?.let { constr.add(it) }
                            scrollOffsetX = (dragEvent.offsetX - dx).toInt()
                            scrollOffsetY = (dragEvent.offsetY - dy).toInt()
                        } else {
                            while (x < 0) {
                                constr.forEach { it.add(index = 0, Components.BLANK) }
                                scrollOffsetX -= 20
                                x++
                            }
                            while (y < 0) {
                                constr.add(index = 0, MutableList(size = w()) { Components.BLANK })
                                scrollOffsetY -= 20
                                y++
                            }
                            while (x + (component?.representation?.size ?: 1) > w()) {
                                constr.forEach { it.add(Components.BLANK) }
                            }
                            while (y + 1 > h()) {
                                constr.add(MutableList(size = w()) { Components.BLANK })
                            }

                            if (component != null) {
                                if (component.representation
                                        .size < (Components.fromChar(constr[y][x].first())?.representation?.size ?: 0)
                                ) {
                                    return@onDrop
                                }

                                for (i in 0..<component.representation.size)
                                    constr[y][x + i] = dragging!!
                            }

                            while (constr.firstOrNull()?.all { it == Components.BLANK } != false) {
                                constr.removeFirst()
                                scrollOffsetY += 20
                            }
                            while (constr.all { it.firstOrNull() == Components.BLANK }) {
                                constr.forEach { it.removeFirst() }
                                scrollOffsetX += 20
                            }
                            while (constr.lastOrNull()?.all { it == Components.BLANK } != false) {
                                constr.removeLast()
                            }
                            while (constr.all { it.lastOrNull() == Components.BLANK }) {
                                constr.forEach { it.removeLast() }
                            }
                        }

                        println(constr)

                        construction = constr
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (showHelp)
                    Help(
                        onDismiss = { showHelp = false },
                        modifier = Modifier.zIndex(20),
                    )

                if (showFileChooser)
                    FileChooser(
                        onOk = {
                            CoroutineScope(Dispatchers.Default).launch {
                                try {
                                    construction = it.readBytes().decodeToString().asConstruction()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            showFileChooser = false
                        },
                        onCancel = { showFileChooser = false },
                        modifier = Modifier.zIndex(20),
                    )
            }

            Column(
                modifier = Modifier
                    .position(Position.Relative)
                    .left(scrollOffsetX.px)
                    .top(scrollOffsetY.px),
            ) {
                for ((y, row) in construction.withIndex()) Row {
                    var w = 0
                    for (x in row.indices) {
                        if (w > 0) {
                            w--
                            continue
                        }

                        val component = construction[y][x].firstOrNull()?.let { Components.fromChar(it) }
                        if (component != null) {
                            Box(
                                modifier = Modifier
                                    .maxWidth((component.representation.size * COMPONENTS_SQUARE_SIZE).px)
                                    .maxHeight(COMPONENTS_SQUARE_SIZE.px)
                                    .zIndex(if (dragging != null) 9 else 11)
                                    .onDragStart { dragEvent ->
                                        val constr = construction.map(List<String>::toMutableList).toMutableList()
                                        for (i in 0..<component.representation.size) {
                                            constr[y][x + i] = Components.BLANK
                                        }

                                        dragEvent.dataTransfer?.setData(
                                            DRAG_OFFSET,
                                            listOf(dragEvent.offsetX, dragEvent.offsetY).joinToString(","),
                                        )
                                        dragEvent.dataTransfer?.setData(TARGET_CONSTRUCTION, constr.asString())

                                        dragging = construction[y][x]
                                    }
                                    .onDragEnd { dragging = null }
                                    .onDoubleClick {
                                        val constr = construction.map(List<String>::toMutableList).toMutableList()
                                        for (i in 0..<component.representation.size) {
                                            constr[y][x + i] = Components.BLANK
                                        }
                                        construction = constr
                                    },
                            ) {
                                Image(
                                    modifier = Modifier,
                                    src = component.imageSrc,
                                )

                                if (component == Components.EMITTER || component == Components.RECEIVER)
                                    Div(attrs = Modifier.pointerEvents(PointerEvents.None).toAttrs()) {
                                        Text(construction[y][x])
                                    }
                            }

                            w = component.representation.size - 1
                        } else {
                            Box(modifier = Modifier.size(COMPONENTS_SQUARE_SIZE.px))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Bottom,
            ) {
                FaTrash(
                    modifier = Modifier
                        .padding(12.px)
                        .zIndex(20)
                        .onDragOver { it.preventDefault() }
                        .onDrop {
                            it.preventDefault()
                            val constr = it.dataTransfer?.getData(TARGET_CONSTRUCTION)?.asConstruction()
                                ?: return@onDrop
                            construction = constr
                        }
                        .onDoubleClick {
                            construction = emptyList()
                        },
                    size = IconSize.X2,
                )

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        horizontalAlignment = Alignment.End,
                    ) {
                        Box(modifier = Modifier.height(8.px))
                        Button(
                            modifier = Modifier.zIndex(20),
                            onClick = { showTablePanel = !showTablePanel },
                        ) {
                            if (showTablePanel) FaArrowRight()
                            else FaTableColumns()
                        }
                        Spacer()

                        Box(
                            modifier = Modifier
                                .padding(8.px)
                                .zIndex(20),
                        ) {
                            Button(
                                onClick = {
                                    scrollOffsetX = 0
                                    scrollOffsetY = 0
                                },
                            ) {
                                FaLocationCrosshairs()
                            }
                        }
                    }

                    if (showTablePanel) Box(
                        modifier = Modifier
                            .width(40.percent)
                            .fillMaxHeight()
                            .backgroundColor(colorPalette.background.variant().toRgb().copyf(alpha = 0.9f))
                            .zIndex(20),
                    ) {
                        if (table != null)
                            Table(
                                attrs = Modifier
                                    .padding(8.px)
                                    .fillMaxWidth()
                                    .toAttrs()
                            ) {
                                Tr { table!!.keys.forEach { Th { Text(it) } } }
                                for (i in 0..<table!!.size) {
                                    Tr {
                                        table!!.values.forEach {
                                            Td(
                                                attrs = Modifier
                                                    .border(1.px, LineStyle.Solid, colorPalette.color.variant())
                                                    .toAttrs(),
                                            ) {
                                                Text(if (it[i]) "1" else "0")
                                            }
                                        }
                                    }
                                }
                            }
                    }
                }
            }
        }
    }
}

private fun String.asConstruction() =
    split('\n').drop(1).map { it.split(' ').filter(String::isNotBlank) }

private fun List<List<String>>.asString() =
    "${dimensions.joinToString(" ")}\n${joinToString("\n") { it.joinToString(" ") }}"

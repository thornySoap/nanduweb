package aufgabe4.kobweb.util

import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.isBright
import com.varabyte.kobweb.compose.ui.graphics.lightened

fun Color.variant() = if (isBright) darkened() else lightened()

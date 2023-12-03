package aufgabe4.kobweb.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import aufgabe4.kobweb.util.variant
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.boxShadow
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.icons.fa.FaXmark
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.palette.background
import com.varabyte.kobweb.silk.theme.colors.palette.toPalette
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.*

@Composable
fun Help(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorMode by ColorMode.currentState
    val colorPalette = colorMode.toPalette()

    Box(
        modifier = modifier
            .backgroundColor(colorPalette.background.variant())
            .boxShadow(4.px, 2.px),
    ) {
        Column(
            modifier = Modifier.padding(8.px),
            horizontalAlignment = Alignment.End,
        ) {
            Button(onClick = { onDismiss() }) { FaXmark() }
            H4 { Text("Nandu Web Editor") }
            H6 { Text("Entry for extra task 4 of the 1st round of 42th Bundeswettbewerb Informatik") }
            P {
                B { Text("Drag and drop components from above into the field") }; Br()
                Hr()
                Text("Red: Emits light if it's one sensor doesn't detect light"); Br()
                Text("White: Emits light if both it's sensors don't detect light"); Br()
                Text("Blue: Emits light on the left if its left sensor detects light"); Br()
                Text("      Emits light on the right if its right sensor detects light"); Br()
                Hr()
                Text("Emitter: Emits light"); Br()
                Text("Receiver: Receives light (open side panel to see table)"); Br()
                Hr()
                I { Text("Double-click to delete component. Double-click trash can to delete all") }
            }
        }
    }
}

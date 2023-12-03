package aufgabe4.kobweb.components

import androidx.compose.runtime.*
import aufgabe4.kobweb.util.variant
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.palette.background
import com.varabyte.kobweb.silk.theme.colors.palette.color
import com.varabyte.kobweb.silk.theme.colors.palette.toPalette
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.accept
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.FileInput
import org.jetbrains.compose.web.dom.H4
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Text
import org.w3c.files.File
import org.w3c.files.get

@Composable
fun FileChooser(
    onOk: (File) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var colorMode by ColorMode.currentState
    val colorPalette = colorMode.toPalette()
    var selected by remember { mutableStateOf<File?>(null) }

    Box(
        modifier = modifier
            .backgroundColor(colorPalette.background.variant())
            .boxShadow(4.px, 2.px)
            .onDragOver {
                val file = it.dataTransfer?.files?.get(0)
                if (file != null) selected = file
            },
    ) {
        Column(
            modifier = Modifier.padding(8.px),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            H4 { Text("Choose File") }
            Input(type = InputType.File) {
                accept("text/plain")
                onChange { selected = it.target.files?.get(0) }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onOk(selected!!) },
                    enabled = selected != null,
                ) {
                    Text("OK")
                }
                Spacer()
                Button(
                    onClick = { onCancel() },
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

package aufgabe4.kobweb.pages

import androidx.compose.runtime.Composable
import aufgabe4.kobweb.components.Editor
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.core.Page

@Page
@Composable
fun HomePage() {
    Editor(modifier = Modifier.fillMaxSize())
}

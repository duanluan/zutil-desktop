
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit

/**
 * 简单的链接组件
 *
 * @param text 链接显示的文本
 * @param url 点击后跳转的URL
 * @param modifier Compose修饰符
 * @param fontSize 文字大小
 * @param linkColor 链接颜色
 */
@Composable
fun ZLink(
  text: String,
  url: String,
  modifier: Modifier = Modifier,
  fontSize: TextUnit = TextUnit.Unspecified,
  linkColor: Color = Color.Blue
) {
  val uriHandler = LocalUriHandler.current

  val annotatedString = buildAnnotatedString {
    append(text)
    addStyle(
      style = SpanStyle(
        color = linkColor,
        textDecoration = TextDecoration.Underline
      ),
      start = 0,
      end = text.length
    )
  }

  Text(
    text = annotatedString,
    modifier = modifier.pointerInput(url) {
      detectTapGestures {
        uriHandler.openUri(url)
      }
    },
    style = TextStyle(fontSize = fontSize)
  )
}

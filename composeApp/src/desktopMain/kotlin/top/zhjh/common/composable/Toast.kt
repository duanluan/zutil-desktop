package top.zhjh.common.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.AlertCircle
import compose.icons.feathericons.Check
import compose.icons.feathericons.X
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Toast类型枚举
 *
 * 使用前要先调用 top.zhjh.zutil.composable.NotifyKt.ToastContainer
 */
enum class ToastType {
  SUCCESS, WARN, ERROR, NORMAL
}

// 持续时间
private const val DEFAULT_DURATION = 1000L

/**
 * Toast管理器 - 用于在应用中显示临时通知
 */
object ToastManager {
  // 内部状态
  private val _visible = mutableStateOf(false)
  private val _message = mutableStateOf("")
  private val _duration = mutableStateOf(DEFAULT_DURATION)
  private val _type = mutableStateOf(ToastType.NORMAL)

  // 显示普通Toast
  fun show(message: String, duration: Long = DEFAULT_DURATION) {
    _message.value = message
    _duration.value = duration
    _type.value = ToastType.NORMAL
    _visible.value = true
  }

  // 显示成功Toast
  fun success(message: String, duration: Long = DEFAULT_DURATION) {
    _message.value = message
    _duration.value = duration
    _type.value = ToastType.SUCCESS
    _visible.value = true
  }

  // 显示警告Toast
  fun warn(message: String, duration: Long = DEFAULT_DURATION) {
    _message.value = message
    _duration.value = duration
    _type.value = ToastType.WARN
    _visible.value = true
  }

  // 显示错误Toast
  fun error(message: String, duration: Long = DEFAULT_DURATION) {
    _message.value = message
    _duration.value = duration
    _type.value = ToastType.ERROR
    _visible.value = true
  }

  // 隐藏Toast
  internal fun hide() {
    _visible.value = false
  }

  // 内部状态访问
  internal val visible: State<Boolean> get() = _visible
  internal val message: State<String> get() = _message
  internal val duration: State<Long> get() = _duration
  internal val type: State<ToastType> get() = _type
}

/**
 * Toast容器组件 - 需要在应用的顶层添加此组件
 */
@Composable
fun ToastContainer() {
  val visible = ToastManager.visible.value
  val message = ToastManager.message.value
  val duration = ToastManager.duration.value
  val type = ToastManager.type.value

  if (visible) {
    ToastDialog(
      message = message,
      dismissTimeMillis = duration,
      toastType = type,
      onDismiss = { ToastManager.hide() }
    )
  }
}

/**
 * 获取对应类型的图标
 */
@Composable
private fun getIconForType(type: ToastType): ImageVector? {
  return when (type) {
    ToastType.SUCCESS -> FeatherIcons.Check
    ToastType.WARN -> FeatherIcons.AlertCircle
    ToastType.ERROR -> FeatherIcons.X
    ToastType.NORMAL -> null
  }
}

/**
 * 获取对应类型的颜色
 */
@Composable
private fun getColorForType(type: ToastType): Color {
  return when (type) {
    ToastType.SUCCESS -> Color.Green
    ToastType.WARN -> Color(0xFFFFA500) // 橙色
    ToastType.ERROR -> Color.Red
    ToastType.NORMAL -> MaterialTheme.colors.onSurface
  }
}

/**
 * Toast对话框 - 内部使用的实际显示组件
 */
@Composable
fun ToastDialog(
  message: String,
  onDismiss: () -> Unit,
  dismissTimeMillis: Long = DEFAULT_DURATION,
  isVisible: Boolean = true,
  toastType: ToastType = ToastType.NORMAL
) {
  // 使用remember保存状态
  var visible by remember { mutableStateOf(isVisible) }
  val scope = rememberCoroutineScope()

  // 获取当前类型的图标
  val icon = getIconForType(toastType)
  val iconTint = getColorForType(toastType)

  // 设置自动关闭效果
  LaunchedEffect(key1 = message, key2 = visible) {
    if (visible) {
      scope.launch {
        delay(dismissTimeMillis)
        visible = false
        onDismiss()
      }
    }
  }

  // 仅在可见状态下显示
  if (visible) {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.BottomEnd // 右下角对齐
    ) {

      Card(
        modifier = Modifier.widthIn(min = 250.dp).padding(16.dp),
        elevation = 8.dp,
      ) {
        Row(
          modifier = Modifier.padding(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // 根据类型显示图标
          if (icon != null) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = iconTint,
              modifier = Modifier.padding(end = 12.dp).size(24.dp)
            )
          }

          Text(
            text = message,
            style = MaterialTheme.typography.body1
          )
        }
      }
    }
  }
}

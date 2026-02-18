package top.zhjh.zui.composable

import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.zhjh.zui.enums.ZColorType
import top.zhjh.zui.theme.isAppInDarkTheme

/**
 * ZText 的底层实现函数。
 *
 * 这个函数集中处理三件核心事情：
 * 1. 文本渲染参数透传：把外部传入的排版参数统一交给 Compose `Text`。
 * 2. 标题样式增强：当 `style` 是 `MaterialTheme.typography.h1~h6` 时，
 *    使用组件内定义的字号和加粗规则，并自动添加上下留白。
 * 3. 语义颜色计算：当 `color` 没有显式指定时，根据 `type` 和当前主题计算文本颜色。
 *
 * 颜色优先级：
 * - 第一优先级：调用方传入的 `color`（只要不是 `Color.Unspecified`）
 * - 第二优先级：`type` 推导的语义色（见 [getZTextTypeColor]）
 * - 第三优先级：`Color.Unspecified` 交由外层 `LocalContentColor`/主题系统兜底
 *
 * @param text 文本内容，使用 `AnnotatedString` 以支持富文本样式。
 * @param modifier 修饰符，用于布局、间距、点击等能力组合。
 * @param type 语义类型（默认、主色、成功、信息、警告、危险）。
 * @param color 显式文本颜色；传入时会覆盖 `type` 自动颜色策略。
 * @param fontSize 字号。
 * @param fontStyle 字体样式（例如斜体）。
 * @param fontWeight 字重（例如粗体）。
 * @param fontFamily 字体家族。
 * @param letterSpacing 字间距。
 * @param textDecoration 文本装饰（如下划线、删除线）。
 * @param textAlign 文本对齐方式。
 * @param lineHeight 行高。
 * @param overflow 文本超出范围时的处理策略。
 * @param softWrap 是否自动换行。
 * @param maxLines 最大显示行数。
 * @param minLines 最小显示行数。
 * @param onTextLayout 文本布局完成回调，可读取布局结果做二次处理。
 * @param style 基础文本样式，默认继承 `LocalTextStyle.current`。
 */
@Composable
fun _ZTextImpl(
  text: AnnotatedString,
  modifier: Modifier = Modifier,
  type: ZColorType = ZColorType.DEFAULT,
  color: Color = Color.Unspecified,
  fontSize: TextUnit = TextUnit.Unspecified,
  fontStyle: FontStyle? = null,
  fontWeight: FontWeight? = null,
  fontFamily: FontFamily? = null,
  letterSpacing: TextUnit = TextUnit.Unspecified,
  textDecoration: TextDecoration? = null,
  textAlign: TextAlign? = null,
  lineHeight: TextUnit = TextUnit.Unspecified,
  overflow: TextOverflow = TextOverflow.Clip,
  softWrap: Boolean = true,
  maxLines: Int = Int.MAX_VALUE,
  minLines: Int = 1,
  onTextLayout: (TextLayoutResult) -> Unit = {},
  style: TextStyle = LocalTextStyle.current
) {
  // 当前是否暗黑主题：用于语义色在明暗主题下的差异化计算（例如 INFO）。
  val isDarkTheme = isAppInDarkTheme()

  // 标题匹配池：仅当 style 正好是 h1~h6 之一时，才套用“标题增强”策略。
  val headingStyles = listOf(
    MaterialTheme.typography.h1,
    MaterialTheme.typography.h2,
    MaterialTheme.typography.h3,
    MaterialTheme.typography.h4,
    MaterialTheme.typography.h5,
    MaterialTheme.typography.h6,
  )

  // 标题增强映射：
  // - 字号映射：为组件统一视觉，不直接依赖 Material 默认标题字号。
  // - 间距映射：为标题添加上下呼吸感，提升层级辨识度。
  val headingFontSizes = listOf(34.sp, 26.sp, 22.sp, 16.sp, 14.sp, 10.sp)
  val headingPaddings = listOf(23.dp, 21.dp, 19.dp, 23.dp, 24.dp, 28.dp)

  // 查找当前 style 是否命中标题样式。
  val headingIndex = headingStyles.indexOfFirst { it == style }

  // 最终样式：
  // - 命中标题时，统一替换为“组件定义字号 + Bold”；
  // - 非标题场景，完全沿用调用方 style。
  val finalStyle = if (headingIndex != -1) {
    TextStyle(
      fontSize = headingFontSizes[headingIndex],
      fontWeight = FontWeight.Bold
    )
  } else {
    style
  }

  // 最终修饰符：
  // - 标题场景自动追加上下间距；
  // - 普通场景保持调用方原始 modifier。
  val finalModifier = if (headingIndex != -1) {
    modifier.padding(
      top = headingPaddings[headingIndex],
      bottom = headingPaddings[headingIndex]
    )
  } else {
    modifier
  }

  // 最终颜色：
  // - 显式 color 优先级最高；
  // - 未显式传色时，按 type + 主题计算语义色。
  val finalColor = if (color != Color.Unspecified) {
    color
  } else {
    getZTextTypeColor(type = type, isDarkTheme = isDarkTheme)
  }

  Text(
    text = text,
    modifier = finalModifier,
    color = finalColor,
    fontSize = fontSize,
    fontStyle = fontStyle,
    fontWeight = fontWeight,
    fontFamily = fontFamily,
    letterSpacing = letterSpacing,
    textDecoration = textDecoration,
    textAlign = textAlign,
    lineHeight = lineHeight,
    overflow = overflow,
    softWrap = softWrap,
    maxLines = maxLines,
    minLines = minLines,
    onTextLayout = onTextLayout,
    style = finalStyle
  )
}

/**
 * `AnnotatedString` 版本的 `ZText`。
 *
 * 适合需要富文本（局部高亮、局部样式差异、带注解文本）场景。
 * 所有参数直接转发到 [_ZTextImpl]。
 */
@Composable
fun ZText(
  text: AnnotatedString,
  modifier: Modifier = Modifier,
  type: ZColorType = ZColorType.DEFAULT,
  color: Color = Color.Unspecified,
  fontSize: TextUnit = TextUnit.Unspecified,
  fontStyle: FontStyle? = null,
  fontWeight: FontWeight? = null,
  fontFamily: FontFamily? = null,
  letterSpacing: TextUnit = TextUnit.Unspecified,
  textDecoration: TextDecoration? = null,
  textAlign: TextAlign? = null,
  lineHeight: TextUnit = TextUnit.Unspecified,
  overflow: TextOverflow = TextOverflow.Clip,
  softWrap: Boolean = true,
  maxLines: Int = Int.MAX_VALUE,
  minLines: Int = 1,
  onTextLayout: (TextLayoutResult) -> Unit = {},
  style: TextStyle = LocalTextStyle.current
) {
  _ZTextImpl(
    text = text,
    modifier = modifier,
    type = type,
    color = color,
    fontSize = fontSize,
    fontStyle = fontStyle,
    fontWeight = fontWeight,
    fontFamily = fontFamily,
    letterSpacing = letterSpacing,
    textDecoration = textDecoration,
    textAlign = textAlign,
    lineHeight = lineHeight,
    overflow = overflow,
    softWrap = softWrap,
    maxLines = maxLines,
    minLines = minLines,
    onTextLayout = onTextLayout,
    style = style
  )
}

/**
 * `String` 版本的 `ZText`。
 *
 * 这是日常最常用的入口。内部会将 `String` 包装为 `AnnotatedString`，
 * 再统一交给 [_ZTextImpl] 执行完整渲染逻辑。
 */
@Composable
fun ZText(
  text: String,
  modifier: Modifier = Modifier,
  type: ZColorType = ZColorType.DEFAULT,
  color: Color = Color.Unspecified,
  fontSize: TextUnit = TextUnit.Unspecified,
  fontStyle: FontStyle? = null,
  fontWeight: FontWeight? = null,
  fontFamily: FontFamily? = null,
  letterSpacing: TextUnit = TextUnit.Unspecified,
  textDecoration: TextDecoration? = null,
  textAlign: TextAlign? = null,
  lineHeight: TextUnit = TextUnit.Unspecified,
  overflow: TextOverflow = TextOverflow.Clip,
  softWrap: Boolean = true,
  maxLines: Int = Int.MAX_VALUE,
  minLines: Int = 1,
  onTextLayout: (TextLayoutResult) -> Unit = {},
  style: TextStyle = LocalTextStyle.current
) {
  _ZTextImpl(
    text = AnnotatedString(text),
    modifier = modifier,
    type = type,
    color = color,
    fontSize = fontSize,
    fontStyle = fontStyle,
    fontWeight = fontWeight,
    fontFamily = fontFamily,
    letterSpacing = letterSpacing,
    textDecoration = textDecoration,
    textAlign = textAlign,
    lineHeight = lineHeight,
    overflow = overflow,
    softWrap = softWrap,
    maxLines = maxLines,
    minLines = minLines,
    onTextLayout = onTextLayout,
    style = style
  )
}

/**
 * 根据 `ZColorType` 与当前主题，计算文本语义色。
 *
 * 约定：
 * - `DEFAULT` 返回 `Color.Unspecified`，表示“不要强制指定颜色”，
 *   由外部主题或父级 `LocalContentColor` 决定最终颜色。
 * - 其余类型返回固定语义色；其中 `INFO` 在暗黑主题下使用更亮灰色，保证对比度。
 *
 * @param type 文本语义类型。
 * @param isDarkTheme 是否暗黑主题。
 */
private fun getZTextTypeColor(type: ZColorType, isDarkTheme: Boolean): Color {
  return when (type) {
    ZColorType.DEFAULT -> Color.Unspecified
    ZColorType.PRIMARY -> Color(0xff409eff)
    ZColorType.SUCCESS -> Color(0xff67c23a)
    ZColorType.INFO -> if (isDarkTheme) Color(0xffa6a9ad) else Color(0xff909399)
    ZColorType.WARNING -> Color(0xffe6a23c)
    ZColorType.DANGER -> Color(0xfff56c6c)
  }
}

/**
 * 段落文本快捷函数。
 *
 * 功能：
 * - 可选首行缩进（使用两个全角空格 `\u3000\u3000`）。
 * - 默认追加段后间距，方便多段连续排版时保持视觉节奏。
 * - 默认使用 `body1 + 24.sp lineHeight`，更适合阅读型正文。
 *
 * @param text 段落内容。
 * @param modifier 修饰符。
 * @param indent 是否首行缩进，默认开启。
 */
@Composable
fun ZParagraph(
  text: String,
  modifier: Modifier = Modifier,
  indent: Boolean = true
) {
  // 根据 indent 开关决定是否添加首行缩进。
  val finalText = if (indent) "\u3000\u3000$text" else text

  ZText(
    text = finalText,
    // 统一段后间距，避免段落黏连。
    modifier = modifier.padding(bottom = 8.dp),
    // 正文建议样式：body1 + 较舒适的行高。
    style = MaterialTheme.typography.body1,
    lineHeight = 24.sp
  )
}
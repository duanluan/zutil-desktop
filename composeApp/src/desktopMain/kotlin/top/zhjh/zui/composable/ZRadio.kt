package top.zhjh.zui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.zhjh.zui.theme.isAppInDarkTheme

/**
 * ZRadio 尺寸枚举。
 *
 * 设计目标：
 * 1. 与 Element Plus 的 Large / Default / Small 语义对齐，降低设计和开发沟通成本。
 * 2. 尺寸不仅影响圆形指示器，还会联动文字大小、文字与指示器间距，确保整体比例一致。
 * 3. 将“尺寸策略”收敛到一个枚举，避免业务层散落魔法值（magic number）。
 */
enum class ZRadioSize {
  /** 大号尺寸：适合强调场景或触控区域更大的界面。 */
  Large,
  /** 默认尺寸：最常用，视觉密度与可读性平衡。 */
  Default,
  /** 小号尺寸：用于紧凑布局或信息密度较高的区域。 */
  Small
}

/**
 * 通用单选框组件（支持 String / Number / Boolean 三类值）。
 *
 * 核心行为说明：
 * 1. 通过 `value` 与 `selectedValue` 的相等性判断选中状态，不保存内部选中状态。
 * 2. 点击已选中项不会重复触发 `onValueChange`，避免无意义的状态写入和重组。
 * 3. 启用状态下支持 hover 与 click；禁用状态下关闭交互并切换到禁用配色。
 * 4. 会根据全局主题自动切换明暗色板，保证夜间模式可读性。
 *
 * @param value 当前单选项绑定值，仅支持 `String`、`Number`、`Boolean`。
 * @param selectedValue 当前组的已选值。`selectedValue == value` 时此项为选中态。
 * @param onValueChange 用户点击并发生选中变化时回调新值。
 * @param modifier 外部布局修饰符，最后拼接，便于调用方覆盖布局约束。
 * @param label 单选项右侧文本；为 null 或空字符串时仅渲染圆形指示器。
 * @param size 尺寸档位，联动圆环尺寸、内点尺寸、间距和字号。
 * @param enabled 是否可交互。false 时禁用 hover/click 且采用禁用色。
 */
@Composable
fun <T : Any> ZRadio(
  value: T,
  selectedValue: T?,
  onValueChange: (T) -> Unit,
  modifier: Modifier = Modifier,
  label: String? = null,
  size: ZRadioSize = ZRadioSize.Default,
  enabled: Boolean = true
) {
  // 在入口处做值类型约束，快速暴露错误使用方式。
  requireSupportedValue(value)
  selectedValue?.let(::requireSupportedValue)

  // 尺寸指标统一从 defaults 获取，避免不同分支出现尺寸不一致。
  val metrics = ZRadioDefaults.metrics(size)
  val isSelected = selectedValue == value

  // 交互源统一服务于 hover 与 click，保证状态来源一致。
  val interactionSource = remember { MutableInteractionSource() }
  val isHovered by interactionSource.collectIsHoveredAsState()
  val isDarkTheme = isAppInDarkTheme()
  // 样式由“主题 + 启用状态 + 选中状态 + 悬停状态”共同决定。
  val style = getZRadioStyle(
    isDarkTheme = isDarkTheme,
    isEnabled = enabled,
    isSelected = isSelected,
    isHovered = isHovered
  )

  Row(
    modifier = Modifier
      .then(
        if (enabled) {
          // 仅在可用状态下挂载交互，禁用态不响应 hover/click。
          Modifier
            .hoverable(interactionSource = interactionSource)
            .clickable(
              interactionSource = interactionSource,
              indication = null
            ) {
              // 已选中时不重复回调，避免上层状态“同值写入”。
              if (!isSelected) {
                onValueChange(value)
              }
            }
        } else {
          Modifier
        }
      )
      .heightIn(min = metrics.indicatorSize)
      .then(modifier),
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.CenterVertically
    ) {
      // 外圆：背景 + 边框，模拟 Element 风格的 radio ring。
      Box(
        modifier = Modifier
          .size(metrics.indicatorSize)
        .background(style.indicatorBackgroundColor, CircleShape)
        .border(ZRadioDefaults.IndicatorBorderWidth, style.indicatorBorderColor, CircleShape),
      contentAlignment = Alignment.Center
      ) {
        if (isSelected) {
          // 内圆点：仅在选中态渲染。
          Box(
            modifier = Modifier
              .size(metrics.dotSize)
            .background(style.dotColor, CircleShape)
        )
      }
    }

    if (!label.isNullOrEmpty()) {
      // 标签文本与圆环保持固定间距，避免不同字体大小下贴边。
      Spacer(modifier = Modifier.width(metrics.labelSpacing))
      ZText(
        text = label,
        color = style.labelColor,
        fontSize = metrics.fontSize,
        fontWeight = if (isSelected) FontWeight.Medium else null
      )
    }
  }
}

/**
 * 限制 ZRadio 可接受的数据类型。
 *
 * 约束原因：
 * 1. 与组件需求保持一致，单选值通常是字符串、数字或布尔值。
 * 2. 减少复杂对象比较带来的歧义（例如 equals/hashCode 不稳定）。
 * 3. 让类型不符合预期的问题在开发阶段立即暴露，而不是运行中静默失败。
 */
private fun requireSupportedValue(value: Any) {
  require(value is String || value is Number || value is Boolean) {
    "ZRadio only supports String, Number or Boolean values."
  }
}

@Immutable
/**
 * 单选框最终渲染色板。
 *
 * 说明：
 * - `indicatorBackgroundColor`：外圆内部背景色。
 * - `indicatorBorderColor`：外圆边框色，通常体现 hover/selected 差异。
 * - `dotColor`：选中态内圆点颜色，未选中通常为透明。
 * - `labelColor`：右侧文本颜色，与状态联动。
 */
private data class ZRadioStyle(
  val indicatorBackgroundColor: Color,
  val indicatorBorderColor: Color,
  val dotColor: Color,
  val labelColor: Color
)

@Immutable
/**
 * 单选框尺寸指标集合。
 *
 * 通过集中定义尺寸指标，保证同一尺寸档位下：
 * - 圆环尺寸
 * - 内圆点尺寸
 * - 标签间距
 * - 标签字号
 * 始终保持视觉比例一致。
 */
data class ZRadioMetrics(
  val indicatorSize: Dp,
  val dotSize: Dp,
  val labelSpacing: Dp,
  val fontSize: TextUnit
)

/**
 * 根据运行状态计算最终样式。
 *
 * 状态优先级：
 * 1. 禁用态优先级最高，直接返回禁用配色（忽略 hover/selected 的强调色）。
 * 2. 其次按明暗主题选择色板。
 * 3. 在同一主题色板下再根据 selected/hover 调整边框与标签颜色。
 */
private fun getZRadioStyle(
  isDarkTheme: Boolean,
  isEnabled: Boolean,
  isSelected: Boolean,
  isHovered: Boolean
): ZRadioStyle {
  if (!isEnabled) {
    // 禁用态：弱化对比度，传达不可交互语义。
    return if (isDarkTheme) {
      ZRadioStyle(
        indicatorBackgroundColor = Color(0xff1d1e1f),
        indicatorBorderColor = Color(0xff4c4d4f),
        dotColor = Color(0xff6c6e72),
        labelColor = Color(0xff6c6e72)
      )
    } else {
      ZRadioStyle(
        indicatorBackgroundColor = Color.White,
        indicatorBorderColor = Color(0xffdcdfe6),
        dotColor = Color(0xffc0c4cc),
        labelColor = Color(0xffc0c4cc)
      )
    }
  }

  if (isDarkTheme) {
    // 深色主题：基础色偏深，选中/悬停用亮蓝提升可见性。
    return ZRadioStyle(
      indicatorBackgroundColor = Color(0xff1d1e1f),
      indicatorBorderColor = when {
        isSelected -> Color(0xff409eff)
        isHovered -> Color(0xff79bbff)
        else -> Color(0xff4c4d4f)
      },
      dotColor = if (isSelected) Color(0xff409eff) else Color.Transparent,
      labelColor = if (isSelected) Color(0xff409eff) else Color(0xffcfd3dc)
    )
  }

  // 浅色主题：背景白色，边框与文本遵循常规灰阶 + 品牌蓝强调。
  return ZRadioStyle(
    indicatorBackgroundColor = Color.White,
    indicatorBorderColor = when {
      isSelected -> Color(0xff409eff)
      isHovered -> Color(0xff409eff)
      else -> Color(0xffdcdfe6)
    },
    dotColor = if (isSelected) Color(0xff409eff) else Color.Transparent,
    labelColor = if (isSelected) Color(0xff409eff) else Color(0xff606266)
  )
}

/**
 * ZRadio 默认配置。
 *
 * 建议：
 * - 业务层优先使用这些默认值，保证全局组件视觉一致。
 * - 如需品牌化适配，可在此处统一调整，不建议在业务页面零散覆写。
 */
object ZRadioDefaults {
  /** 单选圆环边框宽度。 */
  val IndicatorBorderWidth = 1.dp

  /**
   * 根据尺寸档位返回对应的布局指标。
   *
   * 对应关系：
   * - Large：18 / 8 / 8 / 16
   * - Default：14 / 6 / 8 / 14
   * - Small：12 / 5 / 6 / 12
   *
   * 四个数值分别表示：
   * `indicatorSize` / `dotSize` / `labelSpacing` / `fontSize`
   */
  fun metrics(size: ZRadioSize): ZRadioMetrics {
    return when (size) {
      ZRadioSize.Large -> ZRadioMetrics(
        indicatorSize = 18.dp,
        dotSize = 8.dp,
        labelSpacing = 8.dp,
        fontSize = 16.sp
      )
      ZRadioSize.Default -> ZRadioMetrics(
        indicatorSize = 14.dp,
        dotSize = 6.dp,
        labelSpacing = 8.dp,
        fontSize = 14.sp
      )
      ZRadioSize.Small -> ZRadioMetrics(
        indicatorSize = 12.dp,
        dotSize = 5.dp,
        labelSpacing = 6.dp,
        fontSize = 12.sp
      )
    }
  }
}

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
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
import compose.icons.FeatherIcons
import compose.icons.feathericons.Check
import top.zhjh.zui.theme.isAppInDarkTheme

enum class ZCheckboxSize {
  Large,
  Default,
  Small
}

@Composable
fun ZCheckbox(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  label: String? = null,
  size: ZCheckboxSize = ZCheckboxSize.Default,
  enabled: Boolean = true
) {
  val metrics = ZCheckboxDefaults.metrics(size)
  val interactionSource = remember { MutableInteractionSource() }
  val isHovered by interactionSource.collectIsHoveredAsState()
  val isDarkTheme = isAppInDarkTheme()
  val style = getZCheckboxStyle(
    isDarkTheme = isDarkTheme,
    isEnabled = enabled,
    isChecked = checked,
    isHovered = isHovered
  )

  Row(
    modifier = Modifier
      .then(
        if (enabled) {
          Modifier
            .hoverable(interactionSource = interactionSource)
            .clickable(
              interactionSource = interactionSource,
              indication = null
            ) {
              onCheckedChange(!checked)
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
    Box(
      modifier = Modifier
        .size(metrics.indicatorSize)
        .background(style.indicatorBackgroundColor, RoundedCornerShape(metrics.cornerRadius))
        .border(1.dp, style.indicatorBorderColor, RoundedCornerShape(metrics.cornerRadius)),
      contentAlignment = Alignment.Center
    ) {
      if (checked) {
        Icon(
          imageVector = FeatherIcons.Check,
          contentDescription = null,
          tint = style.checkColor,
          modifier = Modifier.size(metrics.checkIconSize)
        )
      }
    }

    if (!label.isNullOrEmpty()) {
      Spacer(modifier = Modifier.width(metrics.labelSpacing))
      ZText(
        text = label,
        color = style.labelColor,
        fontSize = metrics.fontSize,
        fontWeight = if (checked) FontWeight.Medium else null
      )
    }
  }
}

@Immutable
private data class ZCheckboxStyle(
  val indicatorBackgroundColor: Color,
  val indicatorBorderColor: Color,
  val checkColor: Color,
  val labelColor: Color
)

@Immutable
data class ZCheckboxMetrics(
  val indicatorSize: Dp,
  val checkIconSize: Dp,
  val cornerRadius: Dp,
  val labelSpacing: Dp,
  val fontSize: TextUnit
)

private fun getZCheckboxStyle(
  isDarkTheme: Boolean,
  isEnabled: Boolean,
  isChecked: Boolean,
  isHovered: Boolean
): ZCheckboxStyle {
  if (!isEnabled) {
    return if (isDarkTheme) {
      ZCheckboxStyle(
        indicatorBackgroundColor = if (isChecked) Color(0xff2a598a) else Color(0xff1d1e1f),
        indicatorBorderColor = if (isChecked) Color(0xff2a598a) else Color(0xff4c4d4f),
        checkColor = Color(0xffcfd3dc),
        labelColor = Color(0xff6c6e72)
      )
    } else {
      ZCheckboxStyle(
        indicatorBackgroundColor = if (isChecked) Color(0xffa0cfff) else Color.White,
        indicatorBorderColor = if (isChecked) Color(0xffa0cfff) else Color(0xffdcdfe6),
        checkColor = Color.White,
        labelColor = Color(0xffc0c4cc)
      )
    }
  }

  if (isDarkTheme) {
    return ZCheckboxStyle(
      indicatorBackgroundColor = if (isChecked) {
        if (isHovered) Color(0xff66b1ff) else Color(0xff409eff)
      } else {
        Color(0xff1d1e1f)
      },
      indicatorBorderColor = if (isChecked) {
        if (isHovered) Color(0xff66b1ff) else Color(0xff409eff)
      } else {
        if (isHovered) Color(0xff409eff) else Color(0xff4c4d4f)
      },
      checkColor = Color.White,
      labelColor = if (isChecked) Color(0xff409eff) else Color(0xffcfd3dc)
    )
  }

  return ZCheckboxStyle(
    indicatorBackgroundColor = if (isChecked) {
      if (isHovered) Color(0xff79bbff) else Color(0xff409eff)
    } else {
      Color.White
    },
    indicatorBorderColor = if (isChecked) {
      if (isHovered) Color(0xff79bbff) else Color(0xff409eff)
    } else {
      if (isHovered) Color(0xff409eff) else Color(0xffdcdfe6)
    },
    checkColor = Color.White,
    labelColor = if (isChecked) Color(0xff409eff) else Color(0xff606266)
  )
}

object ZCheckboxDefaults {
  fun metrics(size: ZCheckboxSize): ZCheckboxMetrics {
    return when (size) {
      ZCheckboxSize.Large -> ZCheckboxMetrics(
        indicatorSize = 18.dp,
        checkIconSize = 13.dp,
        cornerRadius = 2.dp,
        labelSpacing = 10.dp,
        fontSize = 16.sp
      )
      ZCheckboxSize.Small -> ZCheckboxMetrics(
        indicatorSize = 12.dp,
        checkIconSize = 9.dp,
        cornerRadius = 2.dp,
        labelSpacing = 8.dp,
        fontSize = 12.sp
      )
      ZCheckboxSize.Default -> ZCheckboxMetrics(
        indicatorSize = 14.dp,
        checkIconSize = 10.dp,
        cornerRadius = 2.dp,
        labelSpacing = 8.dp,
        fontSize = 14.sp
      )
    }
  }
}

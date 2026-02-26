package top.zhjh.zui.composable

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.zhjh.zui.theme.isAppInDarkTheme

@Composable
fun ZSwitch(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  activeColor: Color? = null,
  inactiveColor: Color? = null
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isHovered by interactionSource.collectIsHoveredAsState()
  val isDarkTheme = isAppInDarkTheme()
  val metrics = ZSwitchDefaults.Metrics
  val style = resolveZSwitchStyle(
    checked = checked,
    enabled = enabled,
    isHovered = isHovered,
    isDarkTheme = isDarkTheme,
    activeColor = activeColor,
    inactiveColor = inactiveColor
  )
  val trackShape = RoundedCornerShape(percent = 50)
  val thumbOffsetTarget = if (checked) {
    metrics.trackWidth - metrics.thumbSize - metrics.trackPadding * 2
  } else {
    0.dp
  }
  val thumbOffsetX by animateDpAsState(
    targetValue = thumbOffsetTarget,
    animationSpec = tween(durationMillis = 140),
    label = "z_switch_thumb_offset"
  )
  val trackColor by animateColorAsState(
    targetValue = style.trackColor,
    animationSpec = tween(durationMillis = 140),
    label = "z_switch_track_color"
  )
  val borderColor by animateColorAsState(
    targetValue = style.trackBorderColor,
    animationSpec = tween(durationMillis = 140),
    label = "z_switch_track_border_color"
  )

  Box(
    modifier = Modifier
      .then(
        if (enabled) {
          Modifier
            .hoverable(interactionSource)
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
      .size(metrics.trackWidth, metrics.trackHeight)
      .background(trackColor, trackShape)
      .border(1.dp, borderColor, trackShape)
      .padding(metrics.trackPadding)
      .then(modifier),
    contentAlignment = Alignment.CenterStart
  ) {
    Box(
      modifier = Modifier
        .offset(x = thumbOffsetX)
        .size(metrics.thumbSize)
        .background(style.thumbColor, CircleShape)
    )
  }
}

@Immutable
private data class ZSwitchStyle(
  val trackColor: Color,
  val trackBorderColor: Color,
  val thumbColor: Color
)

@Immutable
data class ZSwitchMetrics(
  val trackWidth: Dp,
  val trackHeight: Dp,
  val thumbSize: Dp,
  val trackPadding: Dp
)

private fun resolveZSwitchStyle(
  checked: Boolean,
  enabled: Boolean,
  isHovered: Boolean,
  isDarkTheme: Boolean,
  activeColor: Color?,
  inactiveColor: Color?
): ZSwitchStyle {
  val resolvedActiveColor = activeColor ?: Color(0xff409eff)
  val resolvedInactiveColor = inactiveColor ?: if (isDarkTheme) Color(0xff4c4d4f) else Color(0xffdcdfe6)
  val baseTrackColor = if (checked) resolvedActiveColor else resolvedInactiveColor
  val hoveredTrackColor = when {
    isHovered && checked -> lerp(baseTrackColor, Color.White, if (isDarkTheme) 0.10f else 0.18f)
    isHovered && !checked -> lerp(baseTrackColor, Color.White, if (isDarkTheme) 0.06f else 0.12f)
    else -> baseTrackColor
  }
  val resolvedTrackColor = if (enabled) {
    hoveredTrackColor
  } else {
    baseTrackColor.copy(alpha = if (isDarkTheme) 0.55f else 0.65f)
  }
  val resolvedBorderColor = when {
    checked -> resolvedTrackColor
    !enabled -> resolvedTrackColor
    else -> if (isDarkTheme) Color(0xff4c4d4f) else Color(0xffdcdfe6)
  }
  val resolvedThumbColor = if (enabled) {
    Color.White
  } else {
    if (isDarkTheme) Color(0xffa3a6ad) else Color(0xfff5f7fa)
  }
  return ZSwitchStyle(
    trackColor = resolvedTrackColor,
    trackBorderColor = resolvedBorderColor,
    thumbColor = resolvedThumbColor
  )
}

object ZSwitchDefaults {
  val Metrics = ZSwitchMetrics(
    trackWidth = 40.dp,
    trackHeight = 20.dp,
    thumbSize = 16.dp,
    trackPadding = 1.dp
  )
}

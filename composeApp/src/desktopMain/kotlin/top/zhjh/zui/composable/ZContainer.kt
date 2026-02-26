package top.zhjh.zui.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class ZContainerDirection {
  Horizontal,
  Vertical
}

@Composable
fun ZContainer(
  modifier: Modifier = Modifier,
  direction: ZContainerDirection? = null,
  content: @Composable () -> Unit
) {
  Layout(
    content = content,
    modifier = modifier
  ) { measurables, constraints ->
    if (measurables.isEmpty()) {
      return@Layout layout(constraints.minWidth, constraints.minHeight) {}
    }

    val childData = measurables.map { measurable ->
      (measurable.parentData as? ZContainerChildParentData) ?: ZContainerChildParentData()
    }
    val resolvedDirection = direction ?: if (
      childData.any { it.slot == ZContainerChildSlot.Header || it.slot == ZContainerChildSlot.Footer }
    ) {
      ZContainerDirection.Vertical
    } else {
      ZContainerDirection.Horizontal
    }
    val placeables = arrayOfNulls<Placeable>(measurables.size)

    when (resolvedDirection) {
      ZContainerDirection.Vertical -> {
        val mainIndices = mutableListOf<Int>()
        var occupiedHeight = 0
        var maxWidth = 0

        fun measureVerticalChild(
          index: Int,
          fixedHeightPx: Int? = null
        ): Placeable {
          val boundedWidth = constraints.hasBoundedWidth
          val maxWidthForChild = constraints.maxWidth
          val minWidthForChild = if (boundedWidth) maxWidthForChild else 0
          val maxHeightForChild = when {
            fixedHeightPx != null -> fixedHeightPx
            constraints.hasBoundedHeight -> (constraints.maxHeight - occupiedHeight).coerceAtLeast(0)
            else -> Constraints.Infinity
          }
          val minHeightForChild = (fixedHeightPx ?: 0).coerceAtMost(maxHeightForChild)
          return measurables[index].measure(
            Constraints(
              minWidth = minWidthForChild.coerceAtMost(maxWidthForChild),
              maxWidth = maxWidthForChild,
              minHeight = minHeightForChild,
              maxHeight = maxHeightForChild
            )
          )
        }

        measurables.indices.forEach { index ->
          val child = childData[index]
          if (child.slot == ZContainerChildSlot.Main) {
            mainIndices += index
            return@forEach
          }
          val fixedHeightPx = if (child.slot == ZContainerChildSlot.Header || child.slot == ZContainerChildSlot.Footer) {
            child.size
              ?.roundToPx()
              ?.coerceAtLeast(0)
              ?.let { rawHeight ->
                if (constraints.hasBoundedHeight) {
                  rawHeight.coerceAtMost((constraints.maxHeight - occupiedHeight).coerceAtLeast(0))
                } else {
                  rawHeight
                }
              }
          } else {
            null
          }
          val placeable = measureVerticalChild(
            index = index,
            fixedHeightPx = fixedHeightPx
          )
          placeables[index] = placeable
          occupiedHeight += placeable.height
          maxWidth = maxWidth.coerceAtLeast(placeable.width)
        }

        if (mainIndices.isNotEmpty()) {
          val remainingHeight = if (constraints.hasBoundedHeight) {
            (constraints.maxHeight - occupiedHeight).coerceAtLeast(0)
          } else {
            Constraints.Infinity
          }
          val mainCount = mainIndices.size
          val eachHeight = if (remainingHeight == Constraints.Infinity) 0 else remainingHeight / mainCount
          var extraHeight = if (remainingHeight == Constraints.Infinity) 0 else remainingHeight % mainCount

          mainIndices.forEach { index ->
            val allocatedHeight = if (remainingHeight == Constraints.Infinity) {
              null
            } else {
              val current = eachHeight + if (extraHeight > 0) 1 else 0
              if (extraHeight > 0) extraHeight -= 1
              current
            }
            val placeable = measureVerticalChild(
              index = index,
              fixedHeightPx = allocatedHeight
            )
            placeables[index] = placeable
            occupiedHeight += placeable.height
            maxWidth = maxWidth.coerceAtLeast(placeable.width)
          }
        }

        val measuredHeight = placeables.filterNotNull().sumOf { it.height }
        val layoutWidth = if (constraints.hasBoundedWidth) {
          constraints.maxWidth
        } else {
          maxWidth.coerceIn(constraints.minWidth, constraints.maxWidth)
        }
        val layoutHeight = if (constraints.hasBoundedHeight && mainIndices.isNotEmpty()) {
          constraints.maxHeight.coerceAtLeast(constraints.minHeight)
        } else {
          measuredHeight.coerceIn(constraints.minHeight, constraints.maxHeight)
        }

        layout(layoutWidth, layoutHeight) {
          var y = 0
          placeables.forEach { placeable ->
            placeable ?: return@forEach
            placeable.placeRelative(0, y)
            y += placeable.height
          }
        }
      }

      ZContainerDirection.Horizontal -> {
        val mainIndices = mutableListOf<Int>()
        var occupiedWidth = 0
        var maxHeight = 0

        fun measureHorizontalChild(
          index: Int,
          fixedWidthPx: Int? = null
        ): Placeable {
          val boundedHeight = constraints.hasBoundedHeight
          val maxHeightForChild = constraints.maxHeight
          val minHeightForChild = if (boundedHeight) maxHeightForChild else 0
          val maxWidthForChild = when {
            fixedWidthPx != null -> fixedWidthPx
            constraints.hasBoundedWidth -> (constraints.maxWidth - occupiedWidth).coerceAtLeast(0)
            else -> Constraints.Infinity
          }
          val minWidthForChild = (fixedWidthPx ?: 0).coerceAtMost(maxWidthForChild)
          return measurables[index].measure(
            Constraints(
              minWidth = minWidthForChild,
              maxWidth = maxWidthForChild,
              minHeight = minHeightForChild.coerceAtMost(maxHeightForChild),
              maxHeight = maxHeightForChild
            )
          )
        }

        measurables.indices.forEach { index ->
          val child = childData[index]
          if (child.slot == ZContainerChildSlot.Main) {
            mainIndices += index
            return@forEach
          }
          val fixedWidthPx = if (child.slot == ZContainerChildSlot.Aside) {
            child.size
              ?.roundToPx()
              ?.coerceAtLeast(0)
              ?.let { rawWidth ->
                if (constraints.hasBoundedWidth) {
                  rawWidth.coerceAtMost((constraints.maxWidth - occupiedWidth).coerceAtLeast(0))
                } else {
                  rawWidth
                }
              }
          } else {
            null
          }
          val placeable = measureHorizontalChild(
            index = index,
            fixedWidthPx = fixedWidthPx
          )
          placeables[index] = placeable
          occupiedWidth += placeable.width
          maxHeight = maxHeight.coerceAtLeast(placeable.height)
        }

        if (mainIndices.isNotEmpty()) {
          val remainingWidth = if (constraints.hasBoundedWidth) {
            (constraints.maxWidth - occupiedWidth).coerceAtLeast(0)
          } else {
            Constraints.Infinity
          }
          val mainCount = mainIndices.size
          val eachWidth = if (remainingWidth == Constraints.Infinity) 0 else remainingWidth / mainCount
          var extraWidth = if (remainingWidth == Constraints.Infinity) 0 else remainingWidth % mainCount

          mainIndices.forEach { index ->
            val allocatedWidth = if (remainingWidth == Constraints.Infinity) {
              null
            } else {
              val current = eachWidth + if (extraWidth > 0) 1 else 0
              if (extraWidth > 0) extraWidth -= 1
              current
            }
            val placeable = measureHorizontalChild(
              index = index,
              fixedWidthPx = allocatedWidth
            )
            placeables[index] = placeable
            occupiedWidth += placeable.width
            maxHeight = maxHeight.coerceAtLeast(placeable.height)
          }
        }

        val measuredWidth = placeables.filterNotNull().sumOf { it.width }
        val layoutWidth = if (constraints.hasBoundedWidth && mainIndices.isNotEmpty()) {
          constraints.maxWidth
        } else {
          measuredWidth.coerceIn(constraints.minWidth, constraints.maxWidth)
        }
        val layoutHeight = if (constraints.hasBoundedHeight) {
          constraints.maxHeight.coerceAtLeast(constraints.minHeight)
        } else {
          maxHeight.coerceIn(constraints.minHeight, constraints.maxHeight)
        }

        layout(layoutWidth, layoutHeight) {
          var x = 0
          placeables.forEach { placeable ->
            placeable ?: return@forEach
            placeable.placeRelative(x, 0)
            x += placeable.width
          }
        }
      }
    }
  }
}

@Composable
fun ZHeader(
  modifier: Modifier = Modifier,
  height: Dp = 60.dp,
  content: @Composable BoxScope.() -> Unit
) {
  Box(
    modifier = modifier.then(ZContainerChildParentDataModifier(ZContainerChildSlot.Header, height)),
    contentAlignment = Alignment.Center,
    content = content
  )
}

@Composable
fun ZAside(
  modifier: Modifier = Modifier,
  width: Dp = 200.dp,
  content: @Composable BoxScope.() -> Unit
) {
  Box(
    modifier = modifier.then(ZContainerChildParentDataModifier(ZContainerChildSlot.Aside, width)),
    contentAlignment = Alignment.Center,
    content = content
  )
}

@Composable
fun ZMain(
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit
) {
  Box(
    modifier = modifier.then(ZContainerChildParentDataModifier(ZContainerChildSlot.Main, null)),
    contentAlignment = Alignment.Center,
    content = content
  )
}

@Composable
fun ZFooter(
  modifier: Modifier = Modifier,
  height: Dp = 60.dp,
  content: @Composable BoxScope.() -> Unit
) {
  Box(
    modifier = modifier.then(ZContainerChildParentDataModifier(ZContainerChildSlot.Footer, height)),
    contentAlignment = Alignment.Center,
    content = content
  )
}

private enum class ZContainerChildSlot {
  Header,
  Aside,
  Main,
  Footer,
  Default
}

private data class ZContainerChildParentData(
  val slot: ZContainerChildSlot = ZContainerChildSlot.Default,
  val size: Dp? = null
)

private class ZContainerChildParentDataModifier(
  private val slot: ZContainerChildSlot,
  private val size: Dp?
) : ParentDataModifier {
  override fun Density.modifyParentData(parentData: Any?): Any {
    return ZContainerChildParentData(
      slot = slot,
      size = size
    )
  }
}

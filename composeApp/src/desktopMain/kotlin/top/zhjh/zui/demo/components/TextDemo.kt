package top.zhjh.zui.demo.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.zhjh.zui.composable.ZText
import top.zhjh.zui.composable.ZTextSize
import top.zhjh.zui.enums.ZColorType

@Composable
fun textDemoContent() {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
      ZText("Default")
      ZText("Primary", type = ZColorType.PRIMARY)
      ZText("Success", type = ZColorType.SUCCESS)
      ZText("Info", type = ZColorType.INFO)
      ZText("Warning", type = ZColorType.WARNING)
      ZText("Danger", type = ZColorType.DANGER)
    }

    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
      ZText("Large", size = ZTextSize.Large)
      ZText("Default", size = ZTextSize.Default)
      ZText("Small", size = ZTextSize.Small)
    }

    ZText("这是一段 paragraph 文本示例（tag = p）。", tag = "p")

    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
      ZText("Bold", tag = "b")
      ZText("Italic", tag = "i")
      ZText("Inserted", tag = "ins")
      ZText("Deleted", tag = "del")
      ZText("Marked", tag = "mark")
    }

    Row(horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
      ZText("H", modifier = Modifier.alignByBaseline())
      ZText("2", tag = "sub", modifier = Modifier.alignByBaseline())
      ZText("O", modifier = Modifier.alignByBaseline())
      Spacer(modifier = Modifier.width(12.dp))
      ZText("X", modifier = Modifier.alignByBaseline())
      ZText("2", tag = "sup", modifier = Modifier.alignByBaseline())
    }

    ZText(
      text = "truncated 示例：这是一段超长文本，当容器宽度不足时会显示省略号而不是换行。",
      truncated = true,
      modifier = Modifier.width(350.dp)
    )

    ZText(
      text = "lineClamp 示例：这是一段用于演示多行省略的文本内容。设置 lineClamp 为 2 后，超过两行的部分会被截断，并在末尾显示省略号。",
      lineClamp = 2,
      modifier = Modifier.width(350.dp)
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
      ZText("H1", style = MaterialTheme.typography.h1, modifier = Modifier.border(1.dp, MaterialTheme.colors.primary))
      ZText("H2", style = MaterialTheme.typography.h2, modifier = Modifier.border(1.dp, MaterialTheme.colors.primary))
      ZText("H3", style = MaterialTheme.typography.h3, modifier = Modifier.border(1.dp, MaterialTheme.colors.primary))
      ZText("H4", style = MaterialTheme.typography.h4, modifier = Modifier.border(1.dp, MaterialTheme.colors.primary))
      ZText("H5", style = MaterialTheme.typography.h5, modifier = Modifier.border(1.dp, MaterialTheme.colors.primary))
      ZText("H6", style = MaterialTheme.typography.h6, modifier = Modifier.border(1.dp, MaterialTheme.colors.primary))
    }
  }
}

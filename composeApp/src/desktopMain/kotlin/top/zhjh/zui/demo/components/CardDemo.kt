package top.zhjh.zui.demo.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.zhjh.zui.composable.ZCard
import top.zhjh.zui.composable.ZText
import top.zhjh.zui.enums.ZCardShadow

@Composable
fun cardDemoContent() {
  Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
    ZCard(modifier = Modifier.weight(1f)) {
      ZText("Always")
    }
    ZCard(shadow = ZCardShadow.HOVER, modifier = Modifier.weight(1f)) {
      ZText("Hover")
    }
    ZCard(shadow = ZCardShadow.NEVER, modifier = Modifier.weight(1f)) {
      ZText("Never")
    }
  }
}

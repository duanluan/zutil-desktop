package top.zhjh.zui.demo.components

import ZLink
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.zhjh.zui.composable.ZText

@Composable
fun linkDemoContent() {
  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
    ZLink("人民网", "http://www.people.com.cn/")
    ZText("_网上的人民日报")
  }
}

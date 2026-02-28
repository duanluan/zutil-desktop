package top.zhjh.zui.demo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import top.zhjh.zui.composable.*

@Composable
fun containerDemoContent(isDarkTheme: Boolean) {
  val headerColor = if (isDarkTheme) Color(0xff213d5b) else Color(0xffc6e2ff)
  val footerColor = if (isDarkTheme) Color(0xff213d5b) else Color(0xffc6e2ff)
  val mainColor = if (isDarkTheme) Color(0xff18222b) else Color(0xffecf5ff)
  val asideColor = if (isDarkTheme) Color(0xff1d3043) else Color(0xffd9ecff)
  val textColor = if (isDarkTheme) Color(0xffe5eaf3) else Color(0xff303133)

  Column(
    verticalArrangement = Arrangement.spacedBy(10.dp),
    modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
  ) {
    ZText("header / main")
    ZContainerDemoFrame(
      modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
    ) {
      ZContainer(modifier = Modifier.fillMaxSize()) {
        ZHeader(height = 44.dp) {
          ZContainerDemoCell("Header", headerColor, textColor)
        }
        ZMain {
          ZContainerDemoCell("Main", mainColor, textColor)
        }
      }
    }

    ZText("header / main / footer")
    ZContainerDemoFrame(
      modifier = Modifier
        .fillMaxWidth()
        .height(210.dp)
    ) {
      ZContainer(modifier = Modifier.fillMaxSize()) {
        ZHeader(height = 44.dp) {
          ZContainerDemoCell("Header", headerColor, textColor)
        }
        ZMain {
          ZContainerDemoCell("Main", mainColor, textColor)
        }
        ZFooter(height = 48.dp) {
          ZContainerDemoCell("Footer", footerColor, textColor)
        }
      }
    }

    ZText("aside / main / aside")
    ZContainerDemoFrame(
      modifier = Modifier
        .fillMaxWidth()
        .height(170.dp)
    ) {
      ZContainer(modifier = Modifier.fillMaxSize()) {
        ZAside(width = 130.dp) {
          ZContainerDemoCell("Aside", asideColor, textColor)
        }
        ZMain {
          ZContainerDemoCell("Main", mainColor, textColor)
        }
        ZAside(width = 130.dp) {
          ZContainerDemoCell("Aside", asideColor, textColor)
        }
      }
    }

    ZText("aside / (header + main)")
    ZContainerDemoFrame(
      modifier = Modifier
        .fillMaxWidth()
        .height(210.dp)
    ) {
      ZContainer(modifier = Modifier.fillMaxSize()) {
        ZAside(width = 130.dp) {
          ZContainerDemoCell("Aside", asideColor, textColor)
        }
        ZContainer(modifier = Modifier.fillMaxSize()) {
          ZHeader(height = 44.dp) {
            ZContainerDemoCell("Header", headerColor, textColor)
          }
          ZMain {
            ZContainerDemoCell("Main", mainColor, textColor)
          }
        }
      }
    }

    ZText("aside / (header + main + footer)")
    ZContainerDemoFrame(
      modifier = Modifier
        .fillMaxWidth()
        .height(230.dp)
    ) {
      ZContainer(modifier = Modifier.fillMaxSize()) {
        ZAside(width = 130.dp) {
          ZContainerDemoCell("Aside", asideColor, textColor)
        }
        ZContainer(modifier = Modifier.fillMaxSize()) {
          ZHeader(height = 44.dp) {
            ZContainerDemoCell("Header", headerColor, textColor)
          }
          ZMain {
            ZContainerDemoCell("Main", mainColor, textColor)
          }
          ZFooter(height = 48.dp) {
            ZContainerDemoCell("Footer", footerColor, textColor)
          }
        }
      }
    }

    ZText("header / (aside + main)")
    ZContainerDemoFrame(
      modifier = Modifier
        .fillMaxWidth()
        .height(210.dp)
    ) {
      ZContainer(modifier = Modifier.fillMaxSize()) {
        ZHeader(height = 44.dp) {
          ZContainerDemoCell("Header", headerColor, textColor)
        }
        ZContainer(modifier = Modifier.fillMaxSize()) {
          ZAside(width = 130.dp) {
            ZContainerDemoCell("Aside", asideColor, textColor)
          }
          ZMain {
            ZContainerDemoCell("Main", mainColor, textColor)
          }
        }
      }
    }

    ZText("header / (aside + (main + footer))")
    ZContainerDemoFrame(
      modifier = Modifier
        .fillMaxWidth()
        .height(230.dp)
    ) {
      ZContainer(modifier = Modifier.fillMaxSize()) {
        ZHeader(height = 44.dp) {
          ZContainerDemoCell("Header", headerColor, textColor)
        }
        ZContainer(modifier = Modifier.fillMaxSize()) {
          ZAside(width = 130.dp) {
            ZContainerDemoCell("Aside", asideColor, textColor)
          }
          ZContainer(modifier = Modifier.fillMaxSize()) {
            ZMain {
              ZContainerDemoCell("Main", mainColor, textColor)
            }
            ZFooter(height = 48.dp) {
              ZContainerDemoCell("Footer", footerColor, textColor)
            }
          }
        }
      }
    }

    ZText("direction property")
    Row(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      ZContainerDemoFrame(
        modifier = Modifier
          .weight(1f)
          .height(140.dp)
      ) {
        ZContainer(
          modifier = Modifier.fillMaxSize(),
          direction = ZContainerDirection.Vertical
        ) {
          ZAside(
            width = 90.dp,
            modifier = Modifier.height(44.dp)
          ) {
            ZContainerDemoCell("Aside", asideColor, textColor)
          }
          ZMain {
            ZContainerDemoCell("Main", mainColor, textColor)
          }
        }
      }
      ZContainerDemoFrame(
        modifier = Modifier
          .weight(1f)
          .height(140.dp)
      ) {
        ZContainer(
          modifier = Modifier.fillMaxSize(),
          direction = ZContainerDirection.Horizontal
        ) {
          ZHeader(
            height = 44.dp,
            modifier = Modifier.width(110.dp)
          ) {
            ZContainerDemoCell("Header", headerColor, textColor)
          }
          ZMain {
            ZContainerDemoCell("Main", mainColor, textColor)
          }
        }
      }
    }
  }
}

@Composable
private fun ZContainerDemoFrame(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Box(
    modifier = modifier
  ) {
    content()
  }
}

@Composable
private fun ZContainerDemoCell(
  text: String,
  backgroundColor: Color,
  textColor: Color = Color(0xff303133)
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(backgroundColor),
    contentAlignment = Alignment.Center
  ) {
    ZText(
      text = text,
      color = textColor
    )
  }
}

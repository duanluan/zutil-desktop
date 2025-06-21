package top.zhjh.common.theme

import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import zutil_desktop.composeapp.generated.resources.*

@Composable
fun mapleMonoFontFamily(): FontFamily {
  return FontFamily(
    Font(Res.font.maple_mono_nf_cn_thin, weight = FontWeight.Thin),
    Font(Res.font.maple_mono_nf_cn_extra_light, weight = FontWeight.ExtraLight),
    Font(Res.font.maple_mono_nf_cn_light, weight = FontWeight.Light),
    Font(Res.font.maple_mono_nf_cn_regular, weight = FontWeight.Normal),
    Font(Res.font.maple_mono_nf_cn_medium, weight = FontWeight.Medium),
    Font(Res.font.maple_mono_nf_cn_semi_bold, weight = FontWeight.SemiBold),
    Font(Res.font.maple_mono_nf_cn_bold, weight = FontWeight.Bold),
    Font(Res.font.maple_mono_nf_cn_extra_bold, weight = FontWeight.ExtraBold)
  )
}

@Composable
fun mapleMonoTypography(): Typography {
  return Typography(
    defaultFontFamily = mapleMonoFontFamily()
  )
}

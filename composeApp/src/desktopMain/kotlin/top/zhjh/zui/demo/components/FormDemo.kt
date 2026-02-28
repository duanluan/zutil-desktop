package top.zhjh.zui.demo.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.zhjh.common.composable.ToastManager
import top.zhjh.zui.composable.*
import top.zhjh.zui.enums.ZColorType

@Composable
fun formDemoContent() {
  var username by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }

  val formState = rememberZFormState()
  val formModel = remember(username, email, password, confirmPassword) {
    mapOf(
      "username" to username,
      "email" to email,
      "password" to password,
      "confirmPassword" to confirmPassword
    )
  }
  val formRules = remember {
    mapOf(
      "username" to listOf(
        ZFormRule(required = true, message = "请输入用户名"),
        ZFormRule(minLength = 3, maxLength = 20, message = "用户名长度需在 3-20 之间")
      ),
      "email" to listOf(
        ZFormRule(required = true, message = "请输入邮箱"),
        ZFormRule(
          pattern = Regex(
            """^([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x22([^\x0d\x22\x5c\x80-\xff]|\x5c[\x00-\x7f])*\x22)(\x2e([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x22([^\x0d\x22\x5c\x80-\xff]|\x5c[\x00-\x7f])*\x22))*\x40([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x5b([^\x0d\x5b-\x5d\x80-\xff]|\x5c[\x00-\x7f])*\x5d)(\x2e([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x5b([^\x0d\x5b-\x5d\x80-\xff]|\x5c[\x00-\x7f])*\x5d))*${'$'}"""
          ),
          message = "邮箱格式不正确"
        )
      ),
      "password" to listOf(
        ZFormRule(required = true, message = "请输入密码"),
        ZFormRule(minLength = 6, message = "密码至少 6 位")
      ),
      "confirmPassword" to listOf(
        ZFormRule(required = true, message = "请再次输入密码"),
        ZFormRule(
          validator = { value, model ->
            val current = (value as? String).orEmpty()
            val target = (model["password"] as? String).orEmpty()
            if (current == target) null else "两次密码不一致"
          }
        )
      )
    )
  }

  Column(
    verticalArrangement = Arrangement.spacedBy(10.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    ZText("ZForm 验证示例")
    ZForm(
      state = formState,
      model = formModel,
      rules = formRules,
      labelWidth = 90.dp,
      statusIcon = true,
      modifier = Modifier.fillMaxWidth()
    ) {
      ZFormItem(label = "用户名", prop = "username", value = username) {
        ZTextField(
          value = username,
          onValueChange = { username = it },
          placeholder = "请输入用户名"
        )
      }
      ZFormItem(label = "邮箱", prop = "email", value = email) {
        ZTextField(
          value = email,
          onValueChange = { email = it },
          placeholder = "请输入邮箱"
        )
      }
      ZFormItem(label = "密码", prop = "password", value = password) {
        ZTextField(
          value = password,
          onValueChange = { password = it },
          type = ZTextFieldType.PASSWORD,
          showPassword = true,
          placeholder = "请输入密码"
        )
      }
      ZFormItem(label = "确认密码", prop = "confirmPassword", value = confirmPassword) {
        ZTextField(
          value = confirmPassword,
          onValueChange = { confirmPassword = it },
          type = ZTextFieldType.PASSWORD,
          showPassword = true,
          placeholder = "请再次输入密码"
        )
      }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      ZButton(type = ZColorType.PRIMARY, onClick = {
        if (formState.validate()) {
          ToastManager.success("表单校验通过")
        } else {
          ToastManager.error("表单校验失败")
        }
      }) {
        Text("提交校验")
      }
      ZButton(onClick = {
        formState.clearValidate()
        ToastManager.success("已清除校验状态")
      }) {
        Text("清除校验")
      }
      ZButton(onClick = {
        username = ""
        email = ""
        password = ""
        confirmPassword = ""
        formState.clearValidate()
        ToastManager.success("已重置表单")
      }) {
        Text("重置表单")
      }
    }
  }
}

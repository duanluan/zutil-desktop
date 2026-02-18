package top.zhjh.zui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.FeatherIcons
import compose.icons.feathericons.AlertCircle
import compose.icons.feathericons.CheckCircle
import java.util.UUID

/**
 * 表单尺寸枚举。
 *
 * 说明：
 * - 与 Element Plus 的 `large/default/small` 对齐；
 * - 当前用于表单容器和子项的尺寸上下文传播；
 * - `ZTextField/ZButton` 可读取该上下文，实现“表单级尺寸继承”。
 */
enum class ZFormSize {
  LARGE,
  DEFAULT,
  SMALL
}

/**
 * 表单标签位置。
 *
 * 语义对齐 Element Plus：
 * - LEFT：标签放在输入项左侧，文本左对齐；
 * - RIGHT：标签放在输入项左侧，文本右对齐（常用默认）；
 * - TOP：标签放在输入项上方。
 */
enum class ZFormLabelPosition {
  LEFT,
  RIGHT,
  TOP
}

/**
 * 校验触发时机。
 */
enum class ZFormValidateTrigger {
  CHANGE,
  BLUR,
  SUBMIT
}

/**
 * 字段值类型约束。
 */
enum class ZFormRuleType {
  STRING,
  NUMBER,
  INTEGER,
  BOOLEAN,
  ARRAY,
  OBJECT
}

/**
 * 表单项校验状态。
 */
enum class ZFormValidateStatus {
  NONE,
  SUCCESS,
  ERROR
}

/**
 * 自定义校验器。
 *
 * 返回值约定：
 * - `null`：校验通过；
 * - 非空字符串：校验失败，字符串作为错误提示。
 *
 * 注意：该回调是同步函数，便于在桌面端 Compose 事件里直接调用。
 */
typealias ZFormValidator = (value: Any?, model: Map<String, Any?>) -> String?

/**
 * 表单规则。
 *
 * 与 Element Plus 的核心能力保持同向：
 * - required
 * - type
 * - min/max（数值范围）
 * - minLength/maxLength（字符串/集合长度）
 * - pattern（正则）
 * - trigger（触发时机）
 * - validator（自定义规则）
 */
@Immutable
data class ZFormRule(
  val required: Boolean = false,
  val type: ZFormRuleType? = null,
  val min: Double? = null,
  val max: Double? = null,
  val minLength: Int? = null,
  val maxLength: Int? = null,
  val pattern: Regex? = null,
  val message: String? = null,
  val trigger: Set<ZFormValidateTrigger> = setOf(
    ZFormValidateTrigger.CHANGE,
    ZFormValidateTrigger.BLUR,
    ZFormValidateTrigger.SUBMIT
  ),
  val validator: ZFormValidator? = null
)

/**
 * 表单默认值与工具函数。
 */
object ZFormDefaults {
  const val DefaultLabelWidthDp = 100

  val ItemVerticalSpacing = 14.dp
  val InlineHorizontalSpacing = 12.dp
  val InlineVerticalSpacing = 10.dp
  val LabelContentSpacing = 8.dp
  val ErrorMessageTopSpacing = 4.dp

  val RequiredMarkColor = Color(0xFFF56C6C)
  val ErrorMessageColor = Color(0xFFF56C6C)
  val SuccessColor = Color(0xFF67C23A)
  val ErrorIconColor = Color(0xFFF56C6C)

  fun resolveControlHeight(size: ZFormSize?, fallback: Dp): Dp {
    return when (size) {
      ZFormSize.LARGE -> 38.dp
      ZFormSize.SMALL -> 24.dp
      ZFormSize.DEFAULT -> 30.dp
      null -> fallback
    }
  }
}

private data class ZFormContext(
  val state: ZFormState,
  val inline: Boolean,
  val rules: Map<String, List<ZFormRule>>,
  val labelPosition: ZFormLabelPosition,
  val labelWidth: Dp?,
  val showMessage: Boolean,
  val statusIcon: Boolean,
  val size: ZFormSize,
  val modelProvider: () -> Map<String, Any?>
)

/**
 * 供 ZTextField / ZButton 读取的“表单尺寸上下文”。
 *
 * 设计目标：
 * - 不破坏现有组件 API；
 * - 在 Form 场景中无需逐个组件手工传 size；
 * - 非 Form 场景保持原默认尺寸。
 */
internal val LocalZFormSize = compositionLocalOf<ZFormSize?> { null }

private val LocalZFormContext = compositionLocalOf<ZFormContext?> { null }

/**
 * 表单状态对象。
 *
 * 提供能力：
 * - `validate()`：校验全部字段；
 * - `validateField(prop)`：校验指定字段；
 * - `clearValidate()`：清空校验态；
 * - `resetFields()`：重置字段（若字段提供了 onReset 回调）。
 */
@Stable
class ZFormState {
  internal data class FieldController(
    val key: String,
    val validate: (ZFormValidateTrigger) -> Boolean,
    val clear: () -> Unit,
    val reset: () -> Unit
  )

  private val fields = mutableStateMapOf<String, FieldController>()

  internal fun registerField(prop: String, controller: FieldController) {
    fields[prop] = controller
  }

  internal fun unregisterField(prop: String, key: String) {
    val found = fields[prop] ?: return
    if (found.key == key) {
      fields.remove(prop)
    }
  }

  fun validate(): Boolean {
    if (fields.isEmpty()) return true
    var valid = true
    fields.values.forEach { controller ->
      if (!controller.validate(ZFormValidateTrigger.SUBMIT)) {
        valid = false
      }
    }
    return valid
  }

  fun validateField(prop: String, trigger: ZFormValidateTrigger = ZFormValidateTrigger.SUBMIT): Boolean {
    val controller = fields[prop] ?: return true
    return controller.validate(trigger)
  }

  fun validateFields(props: Collection<String>, trigger: ZFormValidateTrigger = ZFormValidateTrigger.SUBMIT): Boolean {
    if (props.isEmpty()) return true
    var valid = true
    props.forEach { prop ->
      if (!validateField(prop, trigger)) {
        valid = false
      }
    }
    return valid
  }

  fun clearValidate(vararg props: String) {
    if (props.isEmpty()) {
      fields.values.forEach { it.clear() }
      return
    }
    props.forEach { prop ->
      fields[prop]?.clear?.invoke()
    }
  }

  fun resetFields(vararg props: String) {
    if (props.isEmpty()) {
      fields.values.forEach { it.reset() }
      return
    }
    props.forEach { prop ->
      fields[prop]?.reset?.invoke()
    }
  }
}

@Composable
fun rememberZFormState(): ZFormState {
  return remember { ZFormState() }
}

/**
 * 表单容器。
 *
 * @param state 表单状态控制器。
 * @param model 当前表单模型（传给自定义 validator）。
 * @param rules 全局规则：key 对应 `ZFormItem.prop`。
 * @param inline 是否启用行内表单布局。
 * @param labelPosition 全局标签位置。
 * @param labelWidth 全局标签宽度（`TOP` 模式可忽略）。
 * @param showMessage 是否显示错误文本。
 * @param statusIcon 是否显示状态图标（成功/失败）。
 * @param size 表单尺寸，子组件默认继承。
 */
@Composable
@OptIn(ExperimentalLayoutApi::class)
fun ZForm(
  modifier: Modifier = Modifier,
  state: ZFormState = rememberZFormState(),
  model: Map<String, Any?> = emptyMap(),
  rules: Map<String, List<ZFormRule>> = emptyMap(),
  inline: Boolean = false,
  labelPosition: ZFormLabelPosition = ZFormLabelPosition.RIGHT,
  labelWidth: Dp? = ZFormDefaults.DefaultLabelWidthDp.dp,
  showMessage: Boolean = true,
  statusIcon: Boolean = false,
  size: ZFormSize = ZFormSize.DEFAULT,
  content: @Composable () -> Unit
) {
  val modelProvider = rememberUpdatedState(model)
  val formContext = remember(
    state,
    inline,
    rules,
    labelPosition,
    labelWidth,
    showMessage,
    statusIcon,
    size
  ) {
    ZFormContext(
      state = state,
      inline = inline,
      rules = rules,
      labelPosition = labelPosition,
      labelWidth = labelWidth,
      showMessage = showMessage,
      statusIcon = statusIcon,
      size = size,
      modelProvider = { modelProvider.value }
    )
  }

  CompositionLocalProvider(
    LocalZFormContext provides formContext,
    LocalZFormSize provides size
  ) {
    if (inline) {
      FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ZFormDefaults.InlineHorizontalSpacing),
        verticalArrangement = Arrangement.spacedBy(ZFormDefaults.InlineVerticalSpacing)
      ) {
        content()
      }
    } else {
      Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(ZFormDefaults.ItemVerticalSpacing)
      ) {
        content()
      }
    }
  }
}

/**
 * 表单项。
 *
 * 与 Element Plus 的 FormItem 职责一致：
 * - 承载 label + 输入控件；
 * - 绑定 prop 与 rules；
 * - 管理错误提示和状态图标；
 * - 向 `ZFormState` 注册字段，以支持全局 validate/reset。
 */
@Composable
fun ZFormItem(
  modifier: Modifier = Modifier,
  label: String? = null,
  prop: String? = null,
  value: Any? = null,
  required: Boolean = false,
  rules: List<ZFormRule> = emptyList(),
  labelPosition: ZFormLabelPosition? = null,
  labelWidth: Dp? = null,
  showMessage: Boolean? = null,
  statusIcon: Boolean? = null,
  size: ZFormSize? = null,
  onReset: (() -> Unit)? = null,
  content: @Composable () -> Unit
) {
  val context = LocalZFormContext.current
  val mergedLabelPosition = labelPosition ?: context?.labelPosition ?: ZFormLabelPosition.RIGHT
  val mergedLabelWidth = labelWidth ?: context?.labelWidth
  val mergedShowMessage = showMessage ?: context?.showMessage ?: true
  val mergedStatusIcon = statusIcon ?: context?.statusIcon ?: false
  val mergedSize = size ?: context?.size ?: ZFormSize.DEFAULT
  val isInline = context?.inline ?: false

  var errorMessage by remember(prop) { mutableStateOf<String?>(null) }
  var status by remember(prop) { mutableStateOf(ZFormValidateStatus.NONE) }
  var hasValidatedOnce by remember(prop) { mutableStateOf(false) }
  var hasObservedValue by remember(prop) { mutableStateOf(false) }

  val propKey = prop?.trim().orEmpty()
  val globalRules = remember(propKey, context?.rules) {
    if (propKey.isEmpty()) emptyList() else context?.rules?.get(propKey).orEmpty()
  }
  val allRules by remember(globalRules, rules, required, label, propKey) {
    derivedStateOf {
      val merged = mutableListOf<ZFormRule>()
      merged.addAll(globalRules)
      merged.addAll(rules)

      if (required && merged.none { it.required }) {
        merged.add(
          0,
          ZFormRule(
            required = true,
            message = "${label ?: propKey.ifEmpty { "该字段" }}不能为空"
          )
        )
      }
      merged
    }
  }

  val latestValue by rememberUpdatedState(value)
  val latestRules by rememberUpdatedState(allRules)
  val latestLabel by rememberUpdatedState(label)
  val latestModelProvider by rememberUpdatedState(context?.modelProvider ?: { emptyMap() })
  val latestOnReset by rememberUpdatedState(onReset)

  fun clearInternal() {
    errorMessage = null
    status = ZFormValidateStatus.NONE
    hasValidatedOnce = false
    hasObservedValue = false
  }

  fun validateInternal(trigger: ZFormValidateTrigger): Boolean {
    if (propKey.isEmpty() || latestRules.isEmpty()) {
      clearInternal()
      return true
    }

    val model = latestModelProvider.invoke()
    val fieldLabel = latestLabel ?: propKey
    val message = validateByRules(
      value = latestValue,
      label = fieldLabel,
      rules = latestRules,
      trigger = trigger,
      model = model
    )
    hasValidatedOnce = true

    return if (message == null) {
      errorMessage = null
      status = ZFormValidateStatus.SUCCESS
      true
    } else {
      errorMessage = message
      status = ZFormValidateStatus.ERROR
      false
    }
  }

  val formState = context?.state
  val controllerKey = remember(propKey) { UUID.randomUUID().toString() }
  DisposableEffect(formState, propKey, controllerKey) {
    if (formState != null && propKey.isNotEmpty()) {
      formState.registerField(
        propKey,
        ZFormState.FieldController(
          key = controllerKey,
          validate = ::validateInternal,
          clear = ::clearInternal,
          reset = {
            latestOnReset?.invoke()
            clearInternal()
          }
        )
      )
    }

    onDispose {
      if (formState != null && propKey.isNotEmpty()) {
        formState.unregisterField(propKey, controllerKey)
      }
    }
  }

  val shouldValidateOnChange = remember(latestRules) {
    latestRules.any { ZFormValidateTrigger.CHANGE in it.trigger }
  }
  LaunchedEffect(propKey, latestValue) {
    if (propKey.isEmpty() || !shouldValidateOnChange) return@LaunchedEffect
    if (!hasObservedValue) {
      hasObservedValue = true
      return@LaunchedEffect
    }
    validateInternal(ZFormValidateTrigger.CHANGE)
  }

  val fieldModifier = if (isInline) {
    modifier.wrapContentWidth()
  } else {
    modifier.fillMaxWidth()
  }

  val labelTextAlign = when (mergedLabelPosition) {
    ZFormLabelPosition.LEFT -> TextAlign.Left
    ZFormLabelPosition.RIGHT -> TextAlign.Right
    ZFormLabelPosition.TOP -> TextAlign.Left
  }

  CompositionLocalProvider(LocalZFormSize provides mergedSize) {
    key(propKey) {
      if (mergedLabelPosition == ZFormLabelPosition.TOP) {
        Column(modifier = fieldModifier) {
          if (!label.isNullOrBlank()) {
            ZFormItemLabel(
              label = label,
              required = required || allRules.any { it.required },
              textAlign = labelTextAlign
            )
            Spacer(Modifier.height(ZFormDefaults.LabelContentSpacing))
          }
          ZFormItemContent(content = content, status = status, showStatusIcon = mergedStatusIcon && hasValidatedOnce)
          if (mergedShowMessage && !errorMessage.isNullOrBlank()) {
            Spacer(Modifier.height(ZFormDefaults.ErrorMessageTopSpacing))
            ZText(text = errorMessage!!, color = ZFormDefaults.ErrorMessageColor, fontSize = 12.sp)
          }
        }
      } else {
        Row(
          modifier = fieldModifier,
          verticalAlignment = Alignment.Top
        ) {
          if (!label.isNullOrBlank()) {
            val labelModifier = if (mergedLabelWidth != null) {
              Modifier.width(mergedLabelWidth)
            } else {
              Modifier.wrapContentWidth()
            }
            ZFormItemLabel(
              modifier = labelModifier.padding(top = 6.dp),
              label = label,
              required = required || allRules.any { it.required },
              textAlign = labelTextAlign
            )
            Spacer(Modifier.width(ZFormDefaults.LabelContentSpacing))
          }

          Column(modifier = if (isInline) Modifier.wrapContentWidth() else Modifier.weight(1f, fill = true)) {
            ZFormItemContent(content = content, status = status, showStatusIcon = mergedStatusIcon && hasValidatedOnce)
            if (mergedShowMessage && !errorMessage.isNullOrBlank()) {
              Spacer(Modifier.height(ZFormDefaults.ErrorMessageTopSpacing))
              ZText(text = errorMessage!!, color = ZFormDefaults.ErrorMessageColor, fontSize = 12.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ZFormItemLabel(
  label: String,
  required: Boolean,
  textAlign: TextAlign,
  modifier: Modifier = Modifier
) {
  val labelArrangement = when (textAlign) {
    TextAlign.Right, TextAlign.End -> Arrangement.End
    else -> Arrangement.Start
  }

  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = labelArrangement
  ) {
    if (required) {
      ZText("*", color = ZFormDefaults.RequiredMarkColor, fontSize = 12.sp)
      Spacer(Modifier.width(2.dp))
    }
    ZText(
      text = label,
      textAlign = textAlign
    )
  }
}

@Composable
private fun ZFormItemContent(
  content: @Composable () -> Unit,
  status: ZFormValidateStatus,
  showStatusIcon: Boolean
) {
  Row(
    verticalAlignment = Alignment.CenterVertically
  ) {
    content()
    if (showStatusIcon) {
      val (icon, tint) = when (status) {
        ZFormValidateStatus.SUCCESS -> FeatherIcons.CheckCircle to ZFormDefaults.SuccessColor
        ZFormValidateStatus.ERROR -> FeatherIcons.AlertCircle to ZFormDefaults.ErrorIconColor
        ZFormValidateStatus.NONE -> null to Color.Unspecified
      }
      if (icon != null) {
        Spacer(Modifier.width(6.dp))
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = tint
        )
      }
    }
  }
}

private fun validateByRules(
  value: Any?,
  label: String,
  rules: List<ZFormRule>,
  trigger: ZFormValidateTrigger,
  model: Map<String, Any?>
): String? {
  for (rule in rules) {
    if (trigger !in rule.trigger) continue

    if (rule.required && isEmptyValue(value)) {
      return rule.message ?: "$label 不能为空"
    }

    if (!isEmptyValue(value)) {
      if (rule.type != null && !matchType(value, rule.type)) {
        return rule.message ?: "$label 类型不正确"
      }

      if (rule.min != null && value is Number && value.toDouble() < rule.min) {
        return rule.message ?: "$label 不能小于 ${rule.min}"
      }

      if (rule.max != null && value is Number && value.toDouble() > rule.max) {
        return rule.message ?: "$label 不能大于 ${rule.max}"
      }

      val length = measureLength(value)
      if (rule.minLength != null && length != null && length < rule.minLength) {
        return rule.message ?: "$label 长度不能小于 ${rule.minLength}"
      }

      if (rule.maxLength != null && length != null && length > rule.maxLength) {
        return rule.message ?: "$label 长度不能大于 ${rule.maxLength}"
      }

      if (rule.pattern != null) {
        val text = value.toString()
        if (!rule.pattern.matches(text)) {
          return rule.message ?: "$label 格式不正确"
        }
      }
    }

    if (rule.validator != null) {
      val customError = rule.validator.invoke(value, model)
      if (!customError.isNullOrBlank()) {
        return customError
      }
    }
  }
  return null
}

private fun isEmptyValue(value: Any?): Boolean {
  return when (value) {
    null -> true
    is String -> value.isBlank()
    is Collection<*> -> value.isEmpty()
    is Array<*> -> value.isEmpty()
    is Map<*, *> -> value.isEmpty()
    else -> false
  }
}

private fun measureLength(value: Any?): Int? {
  return when (value) {
    is String -> value.length
    is Collection<*> -> value.size
    is Array<*> -> value.size
    is Map<*, *> -> value.size
    else -> null
  }
}

private fun matchType(value: Any?, type: ZFormRuleType): Boolean {
  if (value == null) return false
  return when (type) {
    ZFormRuleType.STRING -> value is String
    ZFormRuleType.NUMBER -> value is Number
    ZFormRuleType.INTEGER -> value is Number && value.toDouble() % 1.0 == 0.0
    ZFormRuleType.BOOLEAN -> value is Boolean
    ZFormRuleType.ARRAY -> value is Collection<*> || value is Array<*>
    ZFormRuleType.OBJECT -> value is Map<*, *>
  }
}

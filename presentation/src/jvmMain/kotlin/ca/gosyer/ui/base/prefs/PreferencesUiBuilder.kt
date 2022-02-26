/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.prefs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Checkbox
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ca.gosyer.i18n.MR
import ca.gosyer.ui.base.components.VerticalScrollbar
import ca.gosyer.ui.base.components.rememberScrollbarAdapter
import ca.gosyer.ui.base.dialog.getMaterialDialogProperties
import ca.gosyer.uicore.components.keyboardHandler
import ca.gosyer.uicore.prefs.PreferenceMutableStateFlow
import ca.gosyer.uicore.resources.stringResource
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogButtons
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title

@Composable
fun PreferenceRow(
    title: String,
    icon: ImageVector? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    subtitle: String? = null,
    enabled: Boolean = true,
    action: @Composable (BoxScope.() -> Unit)? = null,
) {
    val height = if (subtitle != null) 72.dp else 56.dp

    var modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = height)
    if (enabled) {
        modifier = modifier.combinedClickable(
            onLongClick = onLongClick,
            onClick = onClick
        )
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                modifier = Modifier.padding(start = 16.dp).size(24.dp),
                tint = MaterialTheme.colors.primary,
                contentDescription = null
            )
        }
        Column(Modifier.padding(horizontal = 16.dp).weight(1f)) {
            Text(
                text = title,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.subtitle1,
                color = if (enabled) {
                    LocalContentColor.current
                } else {
                    LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
                }
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = if (enabled) {
                        LocalContentColor.current.copy(alpha = ContentAlpha.medium)
                    } else {
                        LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
                    },
                    style = MaterialTheme.typography.subtitle1
                )
            }
        }
        if (action != null) {
            Box(Modifier.widthIn(min = 56.dp)) {
                action()
            }
        }
    }
}

@Composable
fun SwitchPreference(
    preference: PreferenceMutableStateFlow<Boolean>,
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    changeListener: () -> Unit = {},
    enabled: Boolean = true,
) {
    PreferenceRow(
        title = title,
        subtitle = subtitle,
        icon = icon,
        action = {
            val prefValue by preference.collectAsState()
            Switch(checked = prefValue, onCheckedChange = null, enabled = enabled)
        },
        onClick = {
            preference.value = !preference.value
            changeListener()
        },
        enabled = enabled
    )
}

@Composable
fun EditTextPreference(
    preference: PreferenceMutableStateFlow<String>,
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    changeListener: () -> Unit = {},
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val dialogState = rememberMaterialDialogState()
    PreferenceRow(
        title = title,
        subtitle = subtitle,
        icon = icon,
        onClick = {
            dialogState.show()
        },
        enabled = enabled
    )
    val value by preference.collectAsState()
    var editText by remember(value) { mutableStateOf(TextFieldValue(preference.value)) }
    MaterialDialog(
        dialogState,
        buttons = {
            positiveButton(stringResource(MR.strings.action_ok)) {
                preference.value = editText.text
                changeListener()
            }
            negativeButton(stringResource(MR.strings.action_cancel))
        },
        properties = getMaterialDialogProperties(),
    ) {
        title(title)
        OutlinedTextField(
            editText,
            onValueChange = {
                editText = it
            },
            visualTransformation = visualTransformation,
            modifier = Modifier.keyboardHandler()
        )
    }
}

@Composable
fun <Key> ChoicePreference(
    preference: PreferenceMutableStateFlow<Key>,
    choices: Map<Key, String>,
    title: String,
    subtitle: String? = null,
    changeListener: () -> Unit = {},
    enabled: Boolean = true
) {
    val prefValue by preference.collectAsState()
    val dialogState = rememberMaterialDialogState()
    PreferenceRow(
        title = title,
        subtitle = subtitle ?: choices[prefValue],
        onClick = {
            dialogState.show()
        },
        enabled = enabled
    )
    ChoiceDialog(
        state = dialogState,
        items = choices.toList(),
        selected = prefValue,
        title = title,
        onSelected = { selected ->
            preference.value = selected
            changeListener()
        }
    )
}

@Composable
fun <T> ChoiceDialog(
    state: MaterialDialogState,
    items: List<Pair<T, String>>,
    selected: T?,
    onCloseRequest: () -> Unit = {},
    onSelected: (T) -> Unit,
    title: String,
    buttons: @Composable MaterialDialogButtons.() -> Unit = { }
) {
    MaterialDialog(
        state,
        buttons = buttons,
        properties = getMaterialDialogProperties(),
        onCloseRequest = {
            state.hide()
            onCloseRequest()
        }
    ) {
        title(title)
        Box {
            val listState = rememberLazyListState()
            LazyColumn(Modifier.fillMaxSize(), listState) {
                items(items) { (value, text) ->
                    Row(
                        modifier = Modifier.requiredHeight(48.dp).fillMaxWidth().clickable(
                            onClick = {
                                onSelected(value)
                                state.hide()
                            }
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = value == selected,
                            onClick = {
                                onSelected(value)
                                state.hide()
                            },
                        )
                        Text(text = text, modifier = Modifier.padding(start = 24.dp))
                    }
                }
            }
            VerticalScrollbar(
                rememberScrollbarAdapter(listState),
                Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun <T> MultiSelectDialog(
    state: MaterialDialogState,
    items: List<Pair<T, String>>,
    selected: List<T>?,
    onCloseRequest: () -> Unit = {},
    onFinished: (List<T>) -> Unit,
    title: String,
) {
    val checked = remember(selected) { selected.orEmpty().toMutableStateList() }
    MaterialDialog(
        state,
        buttons = {
            positiveButton(stringResource(MR.strings.action_ok)) {
                onFinished(checked)
            }
            negativeButton(stringResource(MR.strings.action_cancel))
        },
        properties = getMaterialDialogProperties(),
        onCloseRequest = {
            state.hide()
            onCloseRequest()
        }
    ) {
        title(title)
        val listState = rememberLazyListState()
        Box {
            LazyColumn(Modifier.fillMaxSize(), listState) {
                items(items) { (value, text) ->
                    Row(
                        modifier = Modifier.requiredHeight(48.dp).fillMaxWidth().clickable(
                            onClick = {
                                if (value in checked) {
                                    checked -= value
                                } else {
                                    checked += value
                                }
                            }
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = value in checked,
                            onCheckedChange = null,
                        )
                        Text(text = text, modifier = Modifier.padding(start = 24.dp))
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
            VerticalScrollbar(
                rememberScrollbarAdapter(listState),
                Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun ColorPreference(
    preference: PreferenceMutableStateFlow<Color>,
    title: String,
    subtitle: String? = null,
    enabled: Boolean = true,
    unsetColor: Color = Color.Unspecified
) {
    val initialColor = preference.value.takeOrElse { unsetColor }
    val dialogState = rememberMaterialDialogState()
    PreferenceRow(
        title = title,
        subtitle = subtitle,
        onClick = {
            dialogState.show()
        },
        onLongClick = { preference.value = Color.Unspecified },
        action = {
            val prefValue by preference.collectAsState()
            if (prefValue != Color.Unspecified || unsetColor != Color.Unspecified) {
                val borderColor = MaterialTheme.colors.onBackground.copy(alpha = 0.54f)
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color = initialColor)
                        .border(BorderStroke(1.dp, borderColor), CircleShape)
                )
            }
        },
        enabled = enabled
    )
    ColorPickerDialog(
        state = dialogState,
        title = title,
        onSelected = {
            preference.value = it
        },
        initialColor = initialColor
    )
}

const val EXPAND_ANIMATION_DURATION = 300
const val COLLAPSE_ANIMATION_DURATION = 300
const val FADE_IN_ANIMATION_DURATION = 350
const val FADE_OUT_ANIMATION_DURATION = 300

@Composable
fun ExpandablePreference(
    title: String,
    startExpanded: Boolean = false,
    onExpandedChanged: ((Boolean) -> Unit)? = null,
    expandedContent: @Composable ColumnScope.() -> Unit,
) {
    var expanded by remember { mutableStateOf(startExpanded) }
    LaunchedEffect(expanded) {
        if (onExpandedChanged != null) {
            onExpandedChanged(expanded)
        }
    }
    val transitionState = remember {
        MutableTransitionState(expanded).apply {
            targetState = !expanded
        }
    }
    val transition = updateTransition(transitionState)
    val elevation by transition.animateDp({
        tween(durationMillis = EXPAND_ANIMATION_DURATION)
    }) {
        if (expanded) 2.dp else 0.dp
    }
    val arrowRotationDegree by transition.animateFloat({
        tween(durationMillis = EXPAND_ANIMATION_DURATION)
    }) {
        if (expanded) 0f else 180f
    }

    Surface(
        elevation = elevation,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column {
            Box(Modifier.clickable { expanded = !expanded }) {
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = "Expandable Arrow",
                    modifier = Modifier.rotate(arrowRotationDegree)
                        .align(Alignment.CenterStart),
                )
                Text(
                    text = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                )
            }
            ExpandableContent(
                visible = expanded,
                initiallyVisible = expanded,
                expandedContent = expandedContent
            )
        }
    }
}

@Composable
private fun ExpandableContent(
    visible: Boolean = true,
    initiallyVisible: Boolean = false,
    expandedContent: @Composable ColumnScope.() -> Unit
) {
    val enterFadeIn = remember {
        fadeIn(
            animationSpec = TweenSpec(
                durationMillis = FADE_IN_ANIMATION_DURATION,
                easing = FastOutLinearInEasing
            )
        )
    }
    val enterExpand = remember {
        expandVertically(animationSpec = tween(EXPAND_ANIMATION_DURATION))
    }
    val exitFadeOut = remember {
        fadeOut(
            animationSpec = TweenSpec(
                durationMillis = FADE_OUT_ANIMATION_DURATION,
                easing = LinearOutSlowInEasing
            )
        )
    }
    val exitCollapse = remember {
        shrinkVertically(animationSpec = tween(COLLAPSE_ANIMATION_DURATION))
    }
    AnimatedVisibility(
        remember { MutableTransitionState(initialState = initiallyVisible) }
            .apply { targetState = visible },
        modifier = Modifier,
        enter = enterExpand + enterFadeIn,
        exit = exitCollapse + exitFadeOut
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            content = expandedContent
        )
    }
}
package co.joebirch.minimise.creation

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.*
import androidx.compose.animation.transition
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ColumnScope.gravity
import androidx.compose.foundation.layout.ColumnScope.weight
import androidx.compose.foundation.layout.RowScope.gravity
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ripple.RippleIndication
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.Top
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawOpacity
import androidx.compose.ui.focus.ExperimentalFocus
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import co.joebirch.minimise.common_ui.MinimiseTheme
import co.joebirch.minimise.dashboard.CreationState
import co.joebirch.minimise.dashboard.CreationStep
import androidx.lifecycle.LiveData

fun ViewGroup.composeDashboardContent(
    uiState: LiveData<CreationState>,
    creationEvents: (event: CreationEvent) -> Unit
): Any = setContent(Recomposer.current()) {
    ComposeInventoryContent(
        uiState, creationEvents
    )
}

@OptIn(ExperimentalLayout::class)
@Composable
private fun ComposeInventoryContent(
    uiState: LiveData<CreationState>,
    creationEvents: (event: CreationEvent) -> Unit
) {
    val viewState by uiState.observeAsState()
    viewState?.let {
        CreationContent(
            creationEvents = creationEvents, creationState = it
        )
    }
}

val sizeState = FloatPropKey()
val alphaState = FloatPropKey()
val contentAlphaState = FloatPropKey()

@SuppressLint("Range")
private val sizeTransitionDefinition = transitionDefinition<String> {
    state("A") {
        this[sizeState] = 0f
        this[alphaState] = 0f
        this[contentAlphaState] = 0f
    }
    state("B") {
        this[sizeState] = 80f
        this[alphaState] = 1f
        this[contentAlphaState] = 1f
    }

    transition(fromState = "A", toState = "B") {
        sizeState using tween(
            durationMillis = 200,
            easing = FastOutLinearInEasing
        )
        contentAlphaState using tween(
            durationMillis = 200,
            easing = FastOutLinearInEasing
        )
        alphaState using keyframes {
            durationMillis = 400
            0f at 0
            0.1f at 225
            1f at 400
        }
    }
}

@OptIn(ExperimentalFocus::class)
@ExperimentalLayout
@Composable
internal fun CreationContent(
    creationState: CreationState,
    creationEvents: (event: CreationEvent) -> Unit
) {
    MinimiseTheme {
        Scaffold(
            bodyContent = {
                Stack(modifier = Modifier.fillMaxSize()) {
                    Box(
                        backgroundColor = MaterialTheme.colors.primary,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (creationState.isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colors.secondary,
                            modifier = Modifier.gravity(Center)
                        )
                    } else {
                        val state = transition(
                            definition = sizeTransitionDefinition,
                            initState = "A",
                            toState = "B"
                        )
                        Column(
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.gravity(Center)
                                .fillMaxHeight()
                                .drawOpacity(state[contentAlphaState])
                        ) {
                            stepCounter(
                                creationState.currentStep
                            )
                            Box(modifier = Modifier.padding(16.dp)) {
                                Spacer(modifier = Modifier.height(16.dp))
                                when (creationState.currentStep) {
                                    CreationStep.NAME -> {
                                        nameStepComposable(
                                            creationState,
                                            creationEvents = creationEvents
                                        )
                                    }
                                    CreationStep.CATEGORY -> {
                                        categoriesStepComposable(
                                            creationState,
                                            creationEvents = creationEvents
                                        )
                                    }
                                    CreationStep.FREQUENCY -> {
                                        frequencyStepComposable(
                                            creationState,
                                            creationEvents = creationEvents
                                        )
                                    }
                                    CreationStep.REMIND -> {
                                        remindStepComposable(
                                            creationState,
                                            creationEvents = creationEvents
                                        )
                                    }
                                    CreationStep.POSITIVE -> {
                                        positiveStepComposable(
                                            creationState
                                        )
                                    }
                                    CreationStep.NEGATIVE -> {
                                        negativeStepComposable()
                                    }
                                    CreationStep.FINISHED -> {
                                        finishedComposable(
                                            creationEvents = creationEvents
                                        )
                                    }
                                }
                            }
                        }
                        if (creationState.currentStep != CreationStep.NAME &&
                            creationState.currentStep != CreationStep.FINISHED
                        ) {
                            Box(
                                shape = CircleShape,
                                modifier = Modifier.gravity(BottomStart).padding(16.dp)
                                    .clickable(
                                        onClick = {
                                            creationEvents(CreationEvent.PreviousStepRequested)
                                        }, indication = RippleIndication(
                                        bounded = true,
                                        radius = 16.dp,
                                        color = Color.Black
                                    )
                                    )
                            ) {
                                Icon(
                                    asset = vectorResource(id = R.drawable.ic_baseline_arrow_back_24),
                                    tint = Color.White
                                )
                            }
                        }

                        if (creationState.currentStep != CreationStep.FINISHED) {
                            Box(
                                shape = CircleShape,
                                modifier = Modifier.gravity(BottomEnd).padding(16.dp)
                                    .clickable(
                                        onClick = {
                                            creationEvents(CreationEvent.NextStepRequested)
                                        }, indication = RippleIndication(
                                        bounded = true,
                                        radius = 16.dp,
                                        color = Color.Black
                                    )
                                    )
                            ) {
                                Icon(
                                    asset = vectorResource(id = R.drawable.ic_baseline_arrow_forward_24),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            })
    }
}

@Composable
internal fun stepCounter(currentStep: CreationStep) {
    Row(modifier = Modifier.padding(top = 8.dp, start = 4.dp)) {
        CreationStep.values().forEachIndexed { index, creationStep ->
            val isStepViewed = index <= currentStep.ordinal
            Box(
                backgroundColor = MaterialTheme.colors.secondary,
                modifier = Modifier.height(6.dp).weight(1f)
                    .drawOpacity(if (isStepViewed) 1f else 0.5f)
                    .testTag(if (isStepViewed) creationStep.name else "")
                    .padding(end = 4.dp)
            )
        }
    }
}

@Composable
fun roundedBackgroundBox(
    modifier: Modifier = Modifier,
    children: @Composable () -> Unit = emptyContent(),
) {
    Box(
        shape = RoundedCornerShape(16.dp),
        backgroundColor = MaterialTheme.colors.secondary,
        modifier = modifier
    ) {
        children()
    }
}

@Composable
private fun creationStep(
    @StringRes title: Int,
    children: @Composable () -> Unit = emptyContent()
) {
    ScrollableColumn(
        modifier = Modifier.fillMaxSize().gravity(align = Top),
        horizontalGravity = CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        titleComposable(title = stringResource(id = title))

        Spacer(modifier = Modifier.height(48.dp))
        children()
    }
}

@ExperimentalFocus
@Composable
private fun nameStepComposable(
    creationState: CreationState,
    creationEvents: (name: CreationEvent) -> Unit
) {
    creationStep(title = R.string.hint_product_name) {
        roundedBackgroundBox(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Spacer(modifier = Modifier.width(16.dp))
                TextField(
                    value = creationState.name,
                    onValueChange = { value ->
                        creationEvents(CreationEvent.NameChanged(value))
                    },
                    label = {

                    },
                    imeAction = ImeAction.Next,
                    onImeActionPerformed = { imeAction, _ ->
                        if (imeAction == ImeAction.Next) {
                            creationEvents(CreationEvent.NextStepRequested)
                        }
                    },
                    activeColor = Color.White,
                    backgroundColor = Color.Transparent,
                    modifier = Modifier.padding(16.dp).fillMaxWidth().sizeIn(minHeight = 80.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@ExperimentalLayout
@Composable
private fun categoriesStepComposable(
    creationState: CreationState,
    creationEvents: (name: CreationEvent) -> Unit
) {
    creationStep(title = R.string.title_categories) {
        val items = stringArrayResource(id = R.array.category_options)
        val selectedItems = creationState.categories.toMutableList()
        FlowRow(
            mainAxisAlignment = MainAxisAlignment.Center,
            crossAxisSpacing = 16.dp,
            mainAxisSpacing = 16.dp,
            mainAxisSize = SizeMode.Expand
        ) {
            items.forEachIndexed { _, amenity ->

                val modifier = Modifier.clickable(onClick = {
                    if (selectedItems.contains(amenity)) {
                        selectedItems.remove(amenity)
                    } else {
                        selectedItems.add(amenity)
                    }
                    creationEvents(CreationEvent.CategoriesChanged(selectedItems))
                }).drawOpacity(if (selectedItems.contains(amenity)) 1f else 0.6f)

                roundedBackgroundBox(modifier = modifier) {
                    Text(
                        text = amenity,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@ExperimentalFocus
@Composable
private fun positiveStepComposable(
    creationState: CreationState
) {
    val focusModifiers = listOf(FocusRequester(), FocusRequester(), FocusRequester())

    creationStep(title = R.string.title_positive_reasons) {
        labelTextField(
            1,
            focusModifiers[0],
            focusModifiers[1]
        )
        Spacer(modifier = Modifier.height(36.dp))
        labelTextField(
            2,
            focusModifiers[1],
            focusModifiers[2]
        )
        Spacer(modifier = Modifier.height(36.dp))
        labelTextField(3, focusModifiers[2])
    }
}

@ExperimentalFocus
@Composable
internal fun negativeStepComposable(
) {
    val focusModifiers = listOf(FocusRequester(), FocusRequester(), FocusRequester())

    creationStep(title = R.string.title_negative_reasons) {
        labelTextField(
            1,
            focusModifiers[0],
            focusModifiers[1]
        )
        Spacer(modifier = Modifier.height(36.dp))
        labelTextField(
            2,
            focusModifiers[1],
            focusModifiers[2]
        )
        Spacer(modifier = Modifier.height(36.dp))
        labelTextField(3, focusModifiers[2])
    }
}

@Composable
private fun finishedComposable(
    creationEvents: (name: CreationEvent) -> Unit
) {
    creationStep(title = R.string.title_finished) {
        Text(
            text = stringResource(id = R.string.message_completed),
            textAlign = TextAlign.Center,
            fontSize = TextUnit.Companion.Sp(18),
            modifier = Modifier.fillMaxWidth().weight(4f).padding(16.dp),
            color = Color.White
        )

        Box(
            modifier = Modifier.gravity(align = CenterHorizontally).weight(1f),
            gravity = Center
        ) {
            Button(
                onClick = {
                    creationEvents(CreationEvent.FormCompleted)
                },
                backgroundColor = MaterialTheme.colors.secondary
            ) {
                Text(text = stringResource(id = R.string.label_completed) , color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalFocus::class)
@Composable
fun labelTextField(
    position: Int,
    requester: FocusRequester,
    nextModifier: FocusRequester? = null
) {
    val states = state { TextFieldValue() }
    val hasFocus = state { false }
    roundedBackgroundBox(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text(
                text = "$position. ", color = Color.White, fontWeight = FontWeight.Bold,
                fontSize = TextUnit.Companion.Sp(16)
            )
            TextField(
                value = states.value,
                onValueChange = { value ->
                    states.value = value
                },
                label = {

                },
                onImeActionPerformed = { _, _ ->
                    nextModifier?.requestFocus()
                },
                imeAction = if (position < 3) ImeAction.Next else ImeAction.Done,
                activeColor = Color.White,
                backgroundColor = Color.Transparent,
                modifier = Modifier.padding(16.dp).fillMaxWidth()
                    .focusRequester(requester)
            )
        }
    }
}

@Composable
private fun frequencyStepComposable(
    creationState: CreationState,
    creationEvents: (name: CreationEvent) -> Unit
) {
    creationStep(title = R.string.hint_frequency) {
        creationStepSlider(value = creationState.frequencyCount,
            options = R.array.frequency_options,
            onValueChanged = {
                creationEvents(CreationEvent.FrequencyChanged(it.toFloat()))
            })
    }
}

@Composable
internal fun titleComposable(title: String) {
    Text(
        text = title,
        style = TextStyle(
            textAlign = TextAlign.Center,
            fontSize = TextUnit.Companion.Sp(26),
            color = MaterialTheme.colors.onPrimary
        ),
        modifier = Modifier.padding(16.dp).fillMaxWidth()
    )
}

@Composable
private fun creationStepSlider(
    value: Float,
    @ArrayRes options: Int,
    onValueChanged: (value: Int) -> Unit
) {
    Slider(
        value = value,
        onValueChange = {
            onValueChanged(it.toInt())
        },
        thumbColor = Color.White,
        activeTrackColor = Color.White,
        activeTickColor = Color.White,
        valueRange = 0f..4f,
        steps = 3,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    )

    Text(
        text = stringArrayResource(id = options)[value.toInt()],
        style = currentTextStyle().merge(TextStyle(color = Color.White)),
        modifier = Modifier.wrapContentWidth(align = CenterHorizontally)
            .padding(16.dp)
    )
}

@Composable
private fun remindStepComposable(
    creationState: CreationState,
    creationEvents: (name: CreationEvent) -> Unit
) {
    creationStep(title = R.string.hint_remind_days) {
        creationStepSlider(value = creationState.daysToRemind.toFloat(),
            options = R.array.reminder_options,
            onValueChanged = {
                creationEvents(CreationEvent.ReminderLengthChanged(it))
            })
    }
}
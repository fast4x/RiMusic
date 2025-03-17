package it.fast4x.rimusic.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.fast4x.rimusic.R
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.enums.ThumbnailRoundness
import it.fast4x.rimusic.typography
import it.fast4x.rimusic.ui.components.themed.IconButton
import it.fast4x.rimusic.ui.styling.favoritesIcon
import it.fast4x.rimusic.utils.filterTokensForAutocomplete
import it.fast4x.rimusic.utils.secondary
import it.fast4x.rimusic.utils.semiBold

@Composable
fun FilterField(
    searching: Boolean,
    setSearching: (Boolean) -> Unit,
    filter: String?,
    setFilter: (String?) -> Unit,
    thumbnailRoundness: ThumbnailRoundness,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(visible = searching, modifier = Modifier
        .fillMaxWidth()
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .background(colorPalette().background0)
        ) {
            val focusRequester = remember { FocusRequester() }
            var autocompleteButtons by rememberSaveable {
                mutableStateOf(filterTokensForAutocomplete)
                //mutableStateOf<List<Pair<String, String>>>(emptyList())
            }
            var textState by remember { mutableStateOf(TextFieldValue("")) }
            fun onFilterChange(newState: TextFieldValue) {
                setFilter(newState.text)
                textState = newState
                // Update the autocomplete buttons
                val word = newState.text.substringAfterLast(" ")
                //if (word != "")
                autocompleteButtons = filterTokensForAutocomplete.filter {
                    it.first.startsWith(word, ignoreCase = true)
                            && !word.equals(it.first, ignoreCase = true)
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    //.requiredHeight(30.dp)
                    .padding(all = 10.dp)
                    .fillMaxWidth()
            ) {
                val focusManager = LocalFocusManager.current
                val keyboardController = LocalSoftwareKeyboardController.current

                LaunchedEffect(searching) {
                    focusRequester.requestFocus()
                }
                BasicTextField(
                    value = textState,
                    onValueChange = { onFilterChange(it) },
                    textStyle = typography().xs.semiBold,
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (filter.isNullOrBlank())
                            setFilter("")
                        focusManager.clearFocus()
                    }),
                    cursorBrush = SolidColor(colorPalette().text),
                    decorationBox = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp)
                        ) {
                            IconButton(
                                onClick = {},
                                icon = R.drawable.search,
                                color = colorPalette().favoritesIcon,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .size(16.dp)
                            )
                        }
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 30.dp)
                        ) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = filter?.isEmpty() ?: true,
                                enter = fadeIn(tween(100)),
                                exit = fadeOut(tween(100)),
                            ) {
                                BasicText(
                                    text = stringResource(R.string.search),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = typography().xs.semiBold.secondary.copy(
                                        color = colorPalette().textDisabled
                                    )
                                )
                            }

                            innerTextField()
                        }
                    },
                    modifier = Modifier
                        .height(30.dp)
                        .fillMaxWidth()
                        .background(
                            colorPalette().background4,
                            shape = thumbnailRoundness.shape()
                        )
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            if (!it.hasFocus) {
                                keyboardController?.hide()
                                if (filter?.isBlank() == true) {
                                    setFilter(null)
                                    setSearching(false)
                                }
                            }
                        }
                )
            }

            ButtonsRow(
                chips = autocompleteButtons,
                currentValue = null,
                onValueUpdate = {
                    // Remove what was being typed and replace it with selected
                    println("Setting filter: " + (filter?.split(" ")?.dropLast(1)?.joinToString(" ")
                            + " $it").trim() )
                    setFilter( (filter?.split(" ")?.dropLast(1)?.joinToString(" ")
                            + " $it").trim() )
                    val filterStr = filter ?: ""
                    focusRequester.requestFocus()
                    onFilterChange(TextFieldValue(filterStr,
                        selection=TextRange(filterStr.length)))
                },
                modifier = Modifier
                    .padding(all = 2.dp)
                    .padding(bottom = 8.dp)
                    .height(25.dp)
                    .fillMaxWidth()
            )
        }
    }
}
package com.palmah.cafe.amirtham.digitalSignage.ui

import amirtham.shared.generated.resources.Res
import amirtham.shared.generated.resources.board_name_label
import amirtham.shared.generated.resources.board_name_placeholder
import amirtham.shared.generated.resources.chef_special
import amirtham.shared.generated.resources.deal_of_the_day
import amirtham.shared.generated.resources.dish_name_placeholder
import amirtham.shared.generated.resources.extra_information_placeholder
import amirtham.shared.generated.resources.generate_ai_image
import amirtham.shared.generated.resources.generated_digital_signage
import amirtham.shared.generated.resources.price_placeholder
import amirtham.shared.generated.resources.scan
import amirtham.shared.generated.resources.selected_image
import amirtham.shared.generated.resources.signature_dishes
import amirtham.shared.generated.resources.something_went_wrong
import amirtham.shared.generated.resources.upload
import amirtham.shared.generated.resources.upload_dishes_label
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.palmah.cafe.amirtham.digitalSignage.model.MAX_SIGNAGE_IMAGES
import com.palmah.cafe.amirtham.digitalSignage.viewModel.DigitalSignageViewModel
import com.palmah.cafe.amirtham.platform.isCameraCaptureSupported
import com.palmah.cafe.amirtham.ui.theme.BorderTan
import com.palmah.cafe.amirtham.ui.theme.Terracotta
import com.palmah.cafe.amirtham.ui.theme.TerracottaContainer
import io.github.ismoy.imagepickerkmp.extensions.loadBytes
import io.github.ismoy.imagepickerkmp.picker.ImagePickerResult
import io.github.ismoy.imagepickerkmp.picker.rememberImagePickerKMP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DigitalSignageScreen(viewModel: DigitalSignageViewModel = koinViewModel<DigitalSignageViewModel>()) {
    val uiState by viewModel.uiState.collectAsState()
    val picker = rememberImagePickerKMP()
    val result = picker.result

    LaunchedEffect(result) {
        val index = uiState.activeDishIndex ?: return@LaunchedEffect
        val photo = (result as? ImagePickerResult.Success)?.first ?: return@LaunchedEffect
        val bytes = withContext(Dispatchers.Default) { photo.loadBytes() }
        val format = photo.mimeType?.substringAfterLast('/') ?: "jpeg"
        viewModel.onDishPhotoPicked(index, bytes, format)
        viewModel.onActiveDishIndexChange(null)
    }

    val boardNamePresets = listOf(
        stringResource(Res.string.chef_special),
        stringResource(Res.string.deal_of_the_day),
        stringResource(Res.string.signature_dishes),
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text(stringResource(Res.string.board_name_label), style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.boardName,
                onValueChange = viewModel::onBoardNameChange,
                placeholder = { Text(stringResource(Res.string.board_name_placeholder)) },
                supportingText = { Text("${uiState.boardName.length}/$BOARD_NAME_MAX_LENGTH") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                boardNamePresets.forEach { preset ->
                    FilterChip(
                        selected = uiState.boardName == preset,
                        onClick = { viewModel.onBoardNameChange(preset) },
                        label = { Text(preset) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Terracotta,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(Res.string.upload_dishes_label, MAX_SIGNAGE_IMAGES),
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.height(12.dp))

            val pagerState = rememberPagerState(pageCount = { MAX_SIGNAGE_IMAGES })
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().height(320.dp)) { page ->
                DishFormPage(
                    form = uiState.dishForms[page],
                    onScanClick = {
                        viewModel.onActiveDishIndexChange(page)
                        picker.launchCamera()
                    },
                    onUploadClick = {
                        viewModel.onActiveDishIndexChange(page)
                        picker.launchGallery()
                    },
                    onNameChange = { value -> viewModel.onDishNameChange(page, value) },
                    onPriceChange = { value -> viewModel.onDishPriceChange(page, value) },
                    onExtraInfoChange = { value -> viewModel.onDishExtraInfoChange(page, value) },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                repeat(MAX_SIGNAGE_IMAGES) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .height(4.dp)
                            .width(if (selected) 24.dp else 12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (selected) Terracotta else TerracottaContainer),
                    )
                }
            }

            if (result is ImagePickerResult.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = result.exception.message ?: stringResource(Res.string.something_went_wrong),
                    color = MaterialTheme.colorScheme.error,
                )
            }

            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            uiState.generatedImageBytes?.let { bytes ->
                Spacer(modifier = Modifier.height(16.dp))
                val generatedImage = remember(bytes) { bytes.decodeToImageBitmap() }
                Image(
                    bitmap = generatedImage,
                    contentDescription = stringResource(Res.string.generated_digital_signage),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                )
            }
        }

        Button(
            onClick = viewModel::generateDigitalSignageImage,
            enabled = !uiState.isLoading && uiState.dishForms.any { it.imageBytes != null },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Terracotta,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .height(52.dp),
        ) {
            Text(stringResource(Res.string.generate_ai_image))
        }
    }
}

@Composable
private fun DishFormPage(
    form: DishFormState,
    onScanClick: () -> Unit,
    onUploadClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onExtraInfoChange: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        var showCaptureOptions by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, BorderTan, RoundedCornerShape(16.dp))
                .clickable(onClick = { showCaptureOptions = true }),
            contentAlignment = Alignment.Center,
        ) {
            val imageBytes = form.imageBytes
            if (imageBytes != null) {
                val bitmap = remember(imageBytes) { imageBytes.decodeToImageBitmap() }
                Image(
                    bitmap = bitmap,
                    contentDescription = stringResource(Res.string.selected_image),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.AddCircle,
                    contentDescription = stringResource(Res.string.selected_image),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp),
                )
            }

            DropdownMenu(
                expanded = showCaptureOptions,
                onDismissRequest = { showCaptureOptions = false },
            ) {
                if (isCameraCaptureSupported) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.scan)) },
                        onClick = {
                            showCaptureOptions = false
                            onScanClick()
                        },
                    )
                }
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.upload)) },
                    onClick = {
                        showCaptureOptions = false
                        onUploadClick()
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = form.name,
            onValueChange = onNameChange,
            placeholder = { Text(stringResource(Res.string.dish_name_placeholder)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = form.price,
            onValueChange = onPriceChange,
            placeholder = { Text(stringResource(Res.string.price_placeholder)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = form.extraInfo,
            onValueChange = onExtraInfoChange,
            placeholder = { Text(stringResource(Res.string.extra_information_placeholder)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

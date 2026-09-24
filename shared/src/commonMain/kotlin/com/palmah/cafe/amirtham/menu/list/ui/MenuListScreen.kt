package com.palmah.cafe.amirtham.menu.list.ui

import amirtham.shared.generated.resources.Res
import amirtham.shared.generated.resources.add_menu
import amirtham.shared.generated.resources.all_categories
import amirtham.shared.generated.resources.clear_search
import amirtham.shared.generated.resources.description_placeholder
import amirtham.shared.generated.resources.dish_name_placeholder
import amirtham.shared.generated.resources.edit_dish
import amirtham.shared.generated.resources.menu_placeholder_item_2
import amirtham.shared.generated.resources.price_placeholder
import amirtham.shared.generated.resources.save
import amirtham.shared.generated.resources.scan
import amirtham.shared.generated.resources.scan_menu_card_subtitle
import amirtham.shared.generated.resources.scan_menu_card_title
import amirtham.shared.generated.resources.search_dishes_placeholder
import amirtham.shared.generated.resources.timings_placeholder
import amirtham.shared.generated.resources.upload
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.palmah.cafe.amirtham.menu.list.model.MenuItem
import com.palmah.cafe.amirtham.menu.list.viewmodel.MenuViewModel
import com.palmah.cafe.amirtham.platform.isCameraCaptureSupported
import com.palmah.cafe.amirtham.ui.theme.Terracotta
import com.palmah.cafe.amirtham.ui.theme.TerracottaContainer
import com.palmah.cafe.amirtham.utils.formatPrice
import io.github.ismoy.imagepickerkmp.picker.ImagePickerResult
import io.github.ismoy.imagepickerkmp.picker.rememberImagePickerKMP
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuListScreen(viewModel: MenuViewModel = koinViewModel<MenuViewModel>()) {
    val uiState by viewModel.uiState.collectAsState()
    val picker = rememberImagePickerKMP()

    val photo = (picker.result as? ImagePickerResult.Success)?.first
    LaunchedEffect(photo?.uri) {
        photo?.let { viewModel.onPhotoSelected(it) }
    }

    val editItemSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading && uiState.menuItems.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            uiState.menuItems.isEmpty() -> EmptyMenuContent()
            else -> MenuItemsContent(
                uiState = uiState,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onCategorySelected = viewModel::onCategorySelected,
                onItemClick = viewModel::onMenuItemClick,
            )
        }

        var showScanOptions by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SmallFloatingActionButton(
                onClick = { viewModel.generateDigitalSignageFromMenu() },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
            ) {
                Icon(Icons.Filled.Info, contentDescription = "Generate digital signage from menu")
            }
            Box {
                FloatingActionButton(
                    onClick = { showScanOptions = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(Res.string.add_menu))
                }
                DropdownMenu(
                    expanded = showScanOptions,
                    onDismissRequest = { showScanOptions = false },
                ) {
                    if (isCameraCaptureSupported) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.scan)) },
                            onClick = {
                                showScanOptions = false
                                picker.launchCamera()
                            },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.upload)) },
                        onClick = {
                            showScanOptions = false
                            picker.launchGallery()
                        },
                    )
                }
            }

            if (uiState.itemPendingEdit != null) {
                ModalBottomSheet(
                    onDismissRequest = viewModel::dismissEditMenuItem,
                    sheetState = editItemSheetState,
                ) {
                    EditMenuItemContent(
                        form = uiState.editForm,
                        onNameChange = viewModel::onEditNameChange,
                        onPriceChange = viewModel::onEditPriceChange,
                        onTimingsChange = viewModel::onEditTimingsChange,
                        onDescriptionChange = viewModel::onEditDescriptionChange,
                        onSave = viewModel::saveMenuItemEdits,
                    )
                }
            }
        }


    }
}


@Composable
private fun EmptyMenuContent() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(TerracottaContainer, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = null,
                tint = Terracotta,
                modifier = Modifier.size(56.dp),
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(Res.string.scan_menu_card_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.scan_menu_card_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MenuItemsContent(
    uiState: MenuUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onItemClick: (MenuItem) -> Unit,
) {
    val categories = remember(uiState.menuItems) {
        uiState.menuItems.map { it.categoryName }.filter { it.isNotBlank() }.distinct()
    }
    val filteredItems = remember(uiState.menuItems, uiState.searchQuery, uiState.selectedCategory) {
        uiState.menuItems.filter { item ->
            val matchesCategory = uiState.selectedCategory == null || item.categoryName == uiState.selectedCategory
            val matchesQuery = uiState.searchQuery.isBlank() ||
                    item.name.contains(uiState.searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            placeholder = { Text(stringResource(Res.string.search_dishes_placeholder)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(Res.string.clear_search))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = uiState.selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text(stringResource(Res.string.all_categories)) },
                leadingIcon = {
                    if (uiState.selectedCategory == null) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
            categories.forEach { category ->
                FilterChip(
                    selected = uiState.selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(filteredItems, key = { it.id.ifBlank { it.name } }) { item ->
                MenuItemCard(item, onClick = { onItemClick(item) })
            }
        }
    }
}

@Composable
private fun MenuItemCard(item: MenuItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(Res.drawable.menu_placeholder_item_2),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (item.description.isNotBlank()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formatPrice(item.price),
                style = MaterialTheme.typography.titleMedium,
                color = Terracotta,
            )
        }
    }
}

@Composable
private fun EditMenuItemContent(
    form: EditMenuItemFormState,
    onNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onTimingsChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Text(stringResource(Res.string.edit_dish), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

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
            value = form.timings,
            onValueChange = onTimingsChange,
            placeholder = { Text(stringResource(Res.string.timings_placeholder)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = form.description,
            onValueChange = onDescriptionChange,
            placeholder = { Text(stringResource(Res.string.description_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onSave,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Terracotta,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text(stringResource(Res.string.save))
        }
    }
}

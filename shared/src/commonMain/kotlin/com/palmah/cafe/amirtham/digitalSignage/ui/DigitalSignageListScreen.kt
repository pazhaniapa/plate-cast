package com.palmah.cafe.amirtham.digitalSignage.ui

import amirtham.shared.generated.resources.Res
import amirtham.shared.generated.resources.cancel
import amirtham.shared.generated.resources.created_on
import amirtham.shared.generated.resources.delete
import amirtham.shared.generated.resources.delete_board
import amirtham.shared.generated.resources.delete_board_confirm_message
import amirtham.shared.generated.resources.delete_board_confirm_title
import amirtham.shared.generated.resources.new_board
import amirtham.shared.generated.resources.no_digital_signage_boards
import amirtham.shared.generated.resources.no_digital_signage_boards_subtitle
import amirtham.shared.generated.resources.play_slideshow
import amirtham.shared.generated.resources.untitled_board
import amirtham.shared.generated.resources.unknown_date
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.palmah.cafe.amirtham.digitalSignage.model.DigitalSignageBoard
import com.palmah.cafe.amirtham.digitalSignage.viewModel.DigitalSignageListViewModel
import com.palmah.cafe.amirtham.digitalSignage.viewModel.DigitalSignageViewModel
import com.palmah.cafe.amirtham.ui.theme.Terracotta
import com.palmah.cafe.amirtham.ui.theme.TerracottaContainer
import com.palmah.cafe.amirtham.utils.formatDate
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalSignageListScreen(
    viewModel: DigitalSignageListViewModel = koinViewModel<DigitalSignageListViewModel>(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var boardPendingDelete by remember { mutableStateOf<DigitalSignageBoard?>(null) }
    var showCreateBoardSheet by remember { mutableStateOf(false) }
    val createBoardSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading && uiState.boards.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.error != null -> {
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                )
            }
            uiState.boards.isEmpty() -> EmptyBoardsContent()
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.boards, key = { it.id }) { board ->
                        DigitalSignageBoardCard(
                            board = board,
                            onDeleteClick = { boardPendingDelete = board },
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            /*SmallFloatingActionButton(
                onClick = {},
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(Res.string.play_slideshow))
            }*/
            FloatingActionButton(
                onClick = { showCreateBoardSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(Res.string.new_board))
            }
        }
    }

    if (showCreateBoardSheet) {
        val digitalSignageViewModel = koinViewModel<DigitalSignageViewModel>()
        ModalBottomSheet(
            onDismissRequest = {
                showCreateBoardSheet = false
                digitalSignageViewModel.reset()
                viewModel.loadBoards()
            },
            sheetState = createBoardSheetState,
        ) {
            DigitalSignageScreen(viewModel = digitalSignageViewModel)
        }
    }

    boardPendingDelete?.let { board ->
        AlertDialog(
            onDismissRequest = { boardPendingDelete = null },
            title = { Text(stringResource(Res.string.delete_board_confirm_title)) },
            text = { Text(stringResource(Res.string.delete_board_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBoard(board.id)
                        boardPendingDelete = null
                    },
                ) {
                    Text(stringResource(Res.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { boardPendingDelete = null }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun EmptyBoardsContent() {
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
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Terracotta,
                modifier = Modifier.size(56.dp),
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(Res.string.no_digital_signage_boards),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.no_digital_signage_boards_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DigitalSignageBoardCard(
    board: DigitalSignageBoard,
    onDeleteClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = board.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(TerracottaContainer),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = board.name.ifBlank { stringResource(Res.string.untitled_board) },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(
                        Res.string.created_on,
                        formatDate(board.createdAtMillis) ?: stringResource(Res.string.unknown_date),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(Res.string.delete_board),
                    tint = Terracotta,
                )
            }
        }
    }
}

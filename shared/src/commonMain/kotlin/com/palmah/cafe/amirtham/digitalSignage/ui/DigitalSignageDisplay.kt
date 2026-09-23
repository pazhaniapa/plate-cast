package com.palmah.cafe.amirtham.digitalSignage.ui

import amirtham.shared.generated.resources.Res
import amirtham.shared.generated.resources.close
import amirtham.shared.generated.resources.digital_signage_image_description
import amirtham.shared.generated.resources.failed_to_load_image
import amirtham.shared.generated.resources.no_digital_signage_images
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import com.palmah.cafe.amirtham.digitalSignage.viewModel.DigitalSignageDisplayViewModel
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.milliseconds

private val logger = Logger.withTag("DigitalSignageDisplay")
private const val SLIDE_DURATION_MILLIS = 5_000L

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DigitalSignageDisplay(
    viewModel: DigitalSignageDisplayViewModel = koinViewModel<DigitalSignageDisplayViewModel>(),
    onClose: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when {
            uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

            uiState.error != null -> Text(
                text = uiState.error ?: "",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
            )

            uiState.imageUrls.isEmpty() -> Text(
                text = stringResource(Res.string.no_digital_signage_images),
                color = Color.White,
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
            )

            else -> {
                val pageCount = uiState.imageUrls.size
                val pagerState = rememberPagerState(pageCount = { pageCount })

                LaunchedEffect(pageCount) {
                    if (pageCount <= 1) return@LaunchedEffect
                    while (true) {
                        delay(SLIDE_DURATION_MILLIS.milliseconds)
                        val nextPage = (pagerState.currentPage + 1) % pageCount
                        pagerState.animateScrollToPage(nextPage)
                    }
                }

                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    val url = uiState.imageUrls[page]
                    var loadError by remember(url) { mutableStateOf<String?>(null) }
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = url,
                            contentDescription = stringResource(Res.string.digital_signage_image_description),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize(),
                            onError = { state ->
                                val throwable = state.result.throwable
                                logger.e(throwable) { "Failed to load digital signage image: $url" }
                                loadError = throwable.message ?: throwable.toString()
                            },
                        )
                        loadError?.let { message ->
                            Text(
                                text = stringResource(Res.string.failed_to_load_image, message),
                                color = Color.Red,
                                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                            )
                        }
                    }
                }
            }
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
        ) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(Res.string.close), tint = Color.White)
        }
    }
}

package com.mobiledashboard.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.mobiledashboard.app.data.model.PageLayout
import com.mobiledashboard.app.data.model.TelemetryPayload
import com.mobiledashboard.app.ui.components.registry.RenderWidget
import com.mobiledashboard.app.ui.components.widgets.clock.PixelClockWidget
import com.mobiledashboard.app.ui.components.widgets.hardware.CpuWidget
import com.mobiledashboard.app.ui.components.widgets.hardware.GpuWidget
import com.mobiledashboard.app.ui.components.widgets.hardware.RamWidget
import com.mobiledashboard.app.ui.components.widgets.media.CompactMediaWidget
import com.mobiledashboard.app.ui.theme.AmoledBlack

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    telemetry: TelemetryPayload,
    layouts: List<PageLayout>,
    serverBaseUrl: String,
    onMediaControl: (String) -> Unit,
    onSystemControl: (String, Int, String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val pageCount = if (layouts.isNotEmpty()) layouts.size else 1
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
    ) {
        if (layouts.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                val page = layouts.getOrNull(pageIndex)
                if (page != null) {
                    DynamicPageRenderer(
                        page = page,
                        telemetry = telemetry,
                        serverBaseUrl = serverBaseUrl,
                        onMediaControl = onMediaControl,
                        onSystemControl = onSystemControl
                    )
                }
            }
        } else {
            // Fallback default screen if no layouts loaded
            DefaultFallbackPage(telemetry, serverBaseUrl, onMediaControl)
        }
    }
}

/**
 * Responsive 2D Grid Page Renderer.
 * Automatically computes row heights based on available viewport height (Landscape / Portrait)
 * so that widgets fit 100% on the screen without unnecessary vertical scrolling or cut-offs.
 */
@Composable
fun DynamicPageRenderer(
    page: PageLayout,
    telemetry: TelemetryPayload,
    serverBaseUrl: String,
    onMediaControl: (String) -> Unit,
    onSystemControl: (String, Int, String) -> Unit = { _, _, _ -> }
) {
    val density = LocalDensity.current
    val spacingPx = with(density) { 8.dp.roundToPx() }
    val minRowHeightPx = with(density) { 68.dp.roundToPx() }
    val pagePadding = 8.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(pagePadding)
    ) {
        val availableHeightPx = constraints.maxHeight
        val widgets = page.widgets

        // Calculate maximum vertical row footprint (e.g. 2, 3, or 4 rows)
        val maxY = widgets.maxOfOrNull { it.y + it.effectiveHeight }?.coerceAtLeast(1) ?: 1

        val totalVerticalSpacingPx = (maxY - 1).coerceAtLeast(0) * spacingPx
        val idealRowHeightPx = (availableHeightPx - totalVerticalSpacingPx) / maxY

        // If ideal row height is large enough, fit everything on screen (0 scroll)
        val fitsOnScreen = idealRowHeightPx >= minRowHeightPx
        val effectiveRowHeightPx = if (fitsOnScreen) idealRowHeightPx else minRowHeightPx
        val totalGridHeightPx = if (fitsOnScreen) availableHeightPx else (maxY * minRowHeightPx) + totalVerticalSpacingPx

        val scrollState = rememberScrollState()
        val scrollModifier = if (!fitsOnScreen) {
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        } else {
            Modifier.fillMaxSize()
        }

        Box(modifier = scrollModifier) {
            Layout(
                content = {
                    widgets.forEach { w ->
                        RenderWidget(
                            widget = w,
                            telemetry = telemetry,
                            serverBaseUrl = serverBaseUrl,
                            onMediaControl = onMediaControl,
                            onSystemControl = onSystemControl,
                            modifier = Modifier
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { measurables, constraints ->
                val totalWidth = constraints.maxWidth
                val colWidth = (totalWidth - (3 * spacingPx)) / 4

                val placeables = measurables.mapIndexed { index, measurable ->
                    val w = widgets[index]
                    val itemW = (w.effectiveWidth * colWidth) + ((w.effectiveWidth - 1) * spacingPx)
                    val itemH = (w.effectiveHeight * effectiveRowHeightPx) + ((w.effectiveHeight - 1) * spacingPx)

                    measurable.measure(
                        Constraints.fixed(itemW.coerceAtLeast(1), itemH.coerceAtLeast(1))
                    )
                }

                layout(totalWidth, totalGridHeightPx) {
                    placeables.forEachIndexed { index, placeable ->
                        val w = widgets[index]
                        val posX = (w.x * colWidth) + (w.x * spacingPx)
                        val posY = (w.y * effectiveRowHeightPx) + (w.y * spacingPx)
                        placeable.placeRelative(posX, posY)
                    }
                }
            }
        }
    }
}

@Composable
fun DefaultFallbackPage(
    telemetry: TelemetryPayload,
    serverBaseUrl: String,
    onMediaControl: (String) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        val isLandscape = maxWidth > maxHeight
        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PixelClockWidget(modifier = Modifier.weight(1f).fillMaxHeight())
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CpuWidget(data = telemetry.cpu, modifier = Modifier.weight(1f).fillMaxHeight())
                        GpuWidget(data = telemetry.gpu, modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                    CompactMediaWidget(
                        data = telemetry.media,
                        serverBaseUrl = serverBaseUrl,
                        onMediaControl = onMediaControl,
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PixelClockWidget(modifier = Modifier.fillMaxWidth().height(180.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CpuWidget(data = telemetry.cpu, modifier = Modifier.weight(1f).height(115.dp))
                    GpuWidget(data = telemetry.gpu, modifier = Modifier.weight(1f).height(115.dp))
                }
                RamWidget(data = telemetry.ram, modifier = Modifier.fillMaxWidth().height(115.dp))
                CompactMediaWidget(
                    data = telemetry.media,
                    serverBaseUrl = serverBaseUrl,
                    onMediaControl = onMediaControl,
                    modifier = Modifier.fillMaxWidth().height(140.dp)
                )
            }
        }
    }
}

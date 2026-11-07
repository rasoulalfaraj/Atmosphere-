package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import com.example.data.model.CityLocation
import com.example.data.model.IraqiCitiesCatalog
import com.example.data.model.RadarLayerType
import com.example.data.model.UserPreferences
import com.example.data.model.WeatherBundle
import com.example.ui.theme.ExtremeHeatCrimson
import com.example.ui.theme.IonCyan
import com.example.ui.theme.OasisEmerald
import com.example.ui.theme.RainCobalt
import com.example.ui.theme.ShamalDustOchre
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.StormViolet
import com.example.util.AppStrings.tr
import com.example.util.UnitFormatter
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun RadarScreen(
    bundle: WeatherBundle,
    comparisonBundles: Map<String, WeatherBundle>,
    selectedLayer: RadarLayerType,
    frameIndex: Int,
    isPlaying: Boolean,
    prefs: UserPreferences,
    onSelectLayer: (RadarLayerType) -> Unit,
    onFrameChange: (Int) -> Unit,
    onTogglePlay: (Int) -> Unit,
    onSelectCityFromMap: (CityLocation) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = prefs.language
    val frames = bundle.radarFrames
    val safeFrameIdx = frameIndex.coerceIn(0, (frames.size - 1).coerceAtLeast(0))
    val activeFrame = frames.getOrNull(safeFrameIdx)

    var zoom by remember { mutableFloatStateOf(1.15f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var inspectedCity by remember(bundle.city.id) { mutableStateOf(bundle.city) }

    val transition = rememberInfiniteTransition(label = "radar_flow")
    val flowPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flow_phase"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("radar_screen_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header & Observed vs Forecast Simulation Provenance Badge
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tr(lang, "Interactive Weather Radar & GIS", "رادار الطقس التفاعلي ونظم المعلومات الجغرافية"),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = tr(
                            lang,
                            "Multi-layer meteorological visualization across Iraq & Middle East",
                            "تمثيل جغرافي متعدد الطبقات للعراق والشرق الأوسط"
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val isForecastFrame = activeFrame?.isForecastSimulation == true || !selectedLayer.isObservedRadar
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isForecastFrame) SolarAmber.copy(alpha = 0.18f) else OasisEmerald.copy(alpha = 0.18f),
                    modifier = Modifier.border(
                        1.dp,
                        if (isForecastFrame) SolarAmber else OasisEmerald,
                        RoundedCornerShape(50)
                    )
                ) {
                    Text(
                        text = if (isForecastFrame) {
                            tr(lang, "FORECAST MODEL", "محاكاة تنبؤية")
                        } else {
                            tr(lang, "OBSERVED RADAR", "رصد راداري فعلي")
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isForecastFrame) SolarAmber else OasisEmerald,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // 2. 7 Meteorological Layer Switcher Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag("radar_layer_row")
            ) {
                items(RadarLayerType.entries) { layer ->
                    val isSelected = layer == selectedLayer
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectLayer(layer) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text(if (lang == AppLanguage.ARABIC) layer.labelAr else layer.labelEn)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IonCyan.copy(alpha = 0.24f),
                            selectedLabelColor = IonCyan
                        )
                    )
                }
            }
        }

        // 3. Interactive GIS Radar Map Viewport
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(370.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFF061022))
                    .border(1.5.dp, IonCyan.copy(alpha = 0.4f), RoundedCornerShape(26.dp))
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, gestureZoom, _ ->
                            zoom = (zoom * gestureZoom).coerceIn(0.85f, 2.8f)
                            val maxPan = 220f * zoom
                            panOffset = Offset(
                                x = (panOffset.x + pan.x).coerceIn(-maxPan, maxPan),
                                y = (panOffset.y + pan.y).coerceIn(-maxPan, maxPan)
                            )
                        }
                    }
                    .pointerInput(zoom, panOffset) {
                        detectTapGestures { tapOffset ->
                            // Hit-test Iraqi & regional city markers
                            val w = size.width.toFloat()
                            val h = size.height.toFloat()
                            val hitCity = IraqiCitiesCatalog.iraqiCities.minByOrNull { c ->
                                val pos = latLonToCanvas(c.latitude, c.longitude, w, h, zoom, panOffset)
                                hypot(pos.x - tapOffset.x, pos.y - tapOffset.y)
                            }
                            if (hitCity != null) {
                                inspectedCity = hitCity
                            }
                        }
                    }
                    .testTag("interactive_radar_canvas")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Base GIS graticule & topographic grid
                    val gridStep = 55f * zoom
                    var gx = (panOffset.x % gridStep + gridStep) % gridStep
                    while (gx < w) {
                        drawLine(
                            color = Color(0x1938BDF8),
                            start = Offset(gx, 0f),
                            end = Offset(gx, h),
                            strokeWidth = 1f
                        )
                        gx += gridStep
                    }
                    var gy = (panOffset.y % gridStep + gridStep) % gridStep
                    while (gy < h) {
                        drawLine(
                            color = Color(0x1938BDF8),
                            start = Offset(0f, gy),
                            end = Offset(w, gy),
                            strokeWidth = 1f
                        )
                        gy += gridStep
                    }

                    // Draw stylized Iraq geopolitical boundary polygon & Tigris/Euphrates rivers
                    val iraqBorderLatLon = listOf(
                        37.3 to 42.4, 37.1 to 44.8, 35.8 to 46.1, 33.7 to 46.0,
                        32.4 to 47.7, 30.0 to 48.5, 29.1 to 46.5, 30.0 to 43.8,
                        32.2 to 39.0, 33.4 to 38.8, 34.4 to 41.0, 36.4 to 41.3, 37.3 to 42.4
                    )
                    val borderPath = Path()
                    iraqBorderLatLon.forEachIndexed { idx, (lat, lon) ->
                        val pt = latLonToCanvas(lat, lon, w, h, zoom, panOffset)
                        if (idx == 0) borderPath.moveTo(pt.x, pt.y) else borderPath.lineTo(pt.x, pt.y)
                    }
                    drawPath(
                        path = borderPath,
                        color = Color(0xFF102241).copy(alpha = 0.72f)
                    )
                    drawPath(
                        path = borderPath,
                        color = IonCyan.copy(alpha = 0.55f),
                        style = Stroke(width = 2.4f)
                    )

                    // Tigris & Euphrates river courses
                    val tigris = listOf(37.0 to 42.8, 36.34 to 43.15, 34.6 to 43.68, 33.31 to 44.36, 32.51 to 45.81, 31.83 to 47.14, 30.50 to 47.78)
                    val euphrates = listOf(34.4 to 41.0, 33.42 to 43.29, 32.46 to 44.41, 31.33 to 45.29, 31.04 to 46.25, 30.50 to 47.78)
                    listOf(tigris, euphrates).forEach { river ->
                        val rPath = Path()
                        river.forEachIndexed { i, (lat, lon) ->
                            val p = latLonToCanvas(lat, lon, w, h, zoom, panOffset)
                            if (i == 0) rPath.moveTo(p.x, p.y) else rPath.lineTo(p.x, p.y)
                        }
                        drawPath(
                            path = rPath,
                            color = Color(0xFF0284C7).copy(alpha = 0.55f),
                            style = Stroke(width = 3f, cap = StrokeCap.Round)
                        )
                    }

                    // Active Meteorological Layer Rendering
                    val timeShift = (safeFrameIdx - 4) * 0.16f
                    when (selectedLayer) {
                        RadarLayerType.PRECIPITATION_RADAR -> {
                            // Precipitation radar cells (Northern mountains & Mediterranean trough)
                            val cells = listOf(
                                Triple(36.3 + timeShift * 0.2, 43.8 + timeShift * 0.5, 105f * zoom),
                                Triple(35.6 + timeShift * 0.15, 45.2 + timeShift * 0.45, 95f * zoom),
                                Triple(33.4 + timeShift * 0.1, 44.2 + timeShift * 0.4, 75f * zoom)
                            )
                            cells.forEach { (lat, lon, radius) ->
                                val cPos = latLonToCanvas(lat, lon, w, h, zoom, panOffset)
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0x9922C55E),
                                            Color(0x773B82F6),
                                            Color.Transparent
                                        ),
                                        center = cPos,
                                        radius = radius
                                    ),
                                    radius = radius,
                                    center = cPos
                                )
                            }
                        }

                        RadarLayerType.DUST_SANDSTORM -> {
                            // Shamal dust corridor sweeping NW -> SE from Anbar/Nineveh through Baghdad to Nasiriyah/Basra
                            val plumes = listOf(
                                Triple(33.2 - timeShift * 0.3, 43.4 + timeShift * 0.4, 150f * zoom),
                                Triple(31.5 - timeShift * 0.25, 45.6 + timeShift * 0.35, 135f * zoom),
                                Triple(34.5 - timeShift * 0.2, 43.5 + timeShift * 0.3, 115f * zoom)
                            )
                            plumes.forEach { (lat, lon, radius) ->
                                val cPos = latLonToCanvas(lat, lon, w, h, zoom, panOffset)
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            ShamalDustOchre.copy(alpha = 0.65f),
                                            SolarAmber.copy(alpha = 0.35f),
                                            Color.Transparent
                                        ),
                                        center = cPos,
                                        radius = radius
                                    ),
                                    radius = radius,
                                    center = cPos
                                )
                            }
                        }

                        RadarLayerType.TEMPERATURE_MAP -> {
                            // Thermal gradient dome (warmer in southern Basra/Nasiriyah, cooler in northern Duhok/Sulaymaniyah)
                            val southCenter = latLonToCanvas(30.8, 46.8, w, h, zoom, panOffset)
                            val northCenter = latLonToCanvas(36.5, 44.2, w, h, zoom, panOffset)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(ExtremeHeatCrimson.copy(alpha = 0.55f), SolarAmber.copy(alpha = 0.30f), Color.Transparent),
                                    center = southCenter,
                                    radius = 220f * zoom
                                ),
                                radius = 220f * zoom,
                                center = southCenter
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(IonCyan.copy(alpha = 0.45f), Color.Transparent),
                                    center = northCenter,
                                    radius = 160f * zoom
                                ),
                                radius = 160f * zoom,
                                center = northCenter
                            )
                        }

                        RadarLayerType.WIND_STREAMLINES -> {
                            // Animated NW Shamal wind streamlines across Mesopotamian plain
                            val windAngleRad = Math.toRadians(bundle.current.windDirectionDeg.toDouble() + 180.0)
                            val dx = sin(windAngleRad).toFloat() * 48f * zoom
                            val dy = -cos(windAngleRad).toFloat() * 48f * zoom

                            for (row in 0..5) {
                                for (col in 0..5) {
                                    val lat = 30.0 + row * 1.3
                                    val lon = 40.5 + col * 1.4
                                    val basePt = latLonToCanvas(lat, lon, w, h, zoom, panOffset)
                                    val animStart = Offset(
                                        basePt.x + dx * (flowPhase - 0.5f),
                                        basePt.y + dy * (flowPhase - 0.5f)
                                    )
                                    val animEnd = Offset(animStart.x + dx * 0.55f, animStart.y + dy * 0.55f)
                                    drawLine(
                                        color = IonCyan.copy(alpha = 0.72f),
                                        start = animStart,
                                        end = animEnd,
                                        strokeWidth = 2.6f,
                                        cap = StrokeCap.Round
                                    )
                                    drawCircle(color = Color.White, radius = 2.5f, center = animEnd)
                                }
                            }
                        }

                        RadarLayerType.PRESSURE_ISOBARS -> {
                            val centerPt = latLonToCanvas(bundle.city.latitude, bundle.city.longitude, w, h, zoom, panOffset)
                            for (ring in 1..4) {
                                drawCircle(
                                    color = StormViolet.copy(alpha = 0.65f - ring * 0.1f),
                                    radius = ring * 52f * zoom,
                                    center = centerPt,
                                    style = Stroke(
                                        width = 2f,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                                    )
                                )
                            }
                        }

                        RadarLayerType.CLOUD_SATELLITE -> {
                            val cloudCenters = listOf(35.8 to 43.9, 33.5 to 44.4, 31.2 to 46.1)
                            cloudCenters.forEachIndexed { idx, (lat, lon) ->
                                val cPt = latLonToCanvas(lat, lon + timeShift * 0.35, w, h, zoom, panOffset)
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color.White.copy(alpha = 0.48f - idx * 0.08f), Color.Transparent),
                                        center = cPt,
                                        radius = 125f * zoom
                                    ),
                                    radius = 125f * zoom,
                                    center = cPt
                                )
                            }
                        }

                        RadarLayerType.ALERT_ZONES -> {
                            val alertCities = listOf(bundle.city, IraqiCitiesCatalog.findById("iq_basra"), IraqiCitiesCatalog.findById("iq_nasiriyah"))
                            alertCities.forEach { ac ->
                                val cPt = latLonToCanvas(ac.latitude, ac.longitude, w, h, zoom, panOffset)
                                drawCircle(
                                    color = ExtremeHeatCrimson.copy(alpha = 0.25f),
                                    radius = 58f * zoom,
                                    center = cPt
                                )
                                drawCircle(
                                    color = ExtremeHeatCrimson,
                                    radius = 58f * zoom,
                                    center = cPt,
                                    style = Stroke(width = 2.2f)
                                )
                            }
                        }
                    }

                    // Draw Interactive Iraqi Governorate Markers
                    IraqiCitiesCatalog.iraqiCities.forEach { cityMarker ->
                        val pos = latLonToCanvas(cityMarker.latitude, cityMarker.longitude, w, h, zoom, panOffset)
                        val isInspected = cityMarker.id == inspectedCity.id
                        val isCurrentActive = cityMarker.id == bundle.city.id

                        if (isInspected || isCurrentActive) {
                            drawCircle(
                                color = if (isInspected) SolarAmber.copy(alpha = 0.35f) else IonCyan.copy(alpha = 0.3f),
                                radius = 18f,
                                center = pos
                            )
                        }
                        drawCircle(
                            color = if (isInspected) SolarAmber else if (isCurrentActive) IonCyan else Color.White,
                            radius = if (isInspected || isCurrentActive) 7.5f else 5f,
                            center = pos
                        )
                    }
                }

                // Floating Zoom & Recenter Controls
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SmallMapControlButton(icon = Icons.Default.Add, desc = "Zoom In") {
                        zoom = (zoom * 1.25f).coerceAtMost(2.8f)
                    }
                    SmallMapControlButton(icon = Icons.Default.Remove, desc = "Zoom Out") {
                        zoom = (zoom / 1.25f).coerceAtLeast(0.85f)
                    }
                    SmallMapControlButton(icon = Icons.Default.CenterFocusStrong, desc = "Reset View") {
                        zoom = 1.15f
                        panOffset = Offset.Zero
                        inspectedCity = bundle.city
                    }
                }

                // Inspected City Marker Telemetry Overlay at Bottom of Map
                val inspectedBundle = comparisonBundles[inspectedCity.id]
                    ?: if (inspectedCity.id == bundle.city.id) bundle else null

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(10.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xDD0B132B))
                        .border(1.dp, IonCyan.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                        .clickable { onSelectCityFromMap(inspectedCity) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${inspectedCity.displayName(lang)} (${inspectedCity.governorateEn})",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (inspectedBundle != null) {
                                "${UnitFormatter.formatTemp(inspectedBundle.current.temperatureC, prefs.tempUnit)} • " +
                                    "${UnitFormatter.formatWind(inspectedBundle.current.windSpeedKmh, prefs)} • " +
                                    "Dust ${inspectedBundle.airQuality.dustUgM3.roundToInt()} µg/m³"
                            } else {
                                tr(lang, "Tap to load full meteorological telemetry →", "اضغط لتحميل بيانات الرصد الكاملة للمحافظة ←")
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = IonCyan
                        )
                    }
                    Text(
                        text = tr(lang, "Switch City", "عرض المدينة"),
                        style = MaterialTheme.typography.labelMedium,
                        color = SolarAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 4. Time-Based Radar Playback Controller
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.82f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { onTogglePlay(frames.size) },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(IonCyan.copy(alpha = 0.2f))
                                .testTag("radar_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause Radar" else "Play Radar",
                                tint = IonCyan
                            )
                        }
                        Column {
                            Text(
                                text = tr(lang, "Radar Timeline Playback", "تشغيل الخط الزمني للرادار"),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = activeFrame?.timeLabel ?: "Live",
                                style = MaterialTheme.typography.labelMedium,
                                color = IonCyan
                            )
                        }
                    }

                    Text(
                        text = "${if (lang == AppLanguage.ARABIC) selectedLayer.labelAr else selectedLayer.labelEn} (${selectedLayer.unitSymbol})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (frames.size > 1) {
                    Slider(
                        value = safeFrameIdx.toFloat(),
                        onValueChange = { onFrameChange(it.roundToInt()) },
                        valueRange = 0f..(frames.size - 1).toFloat(),
                        steps = (frames.size - 2).coerceAtLeast(0),
                        colors = SliderDefaults.colors(
                            thumbColor = IonCyan,
                            activeTrackColor = IonCyan
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallMapControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = Color(0xCC0B132B),
        modifier = Modifier
            .size(36.dp)
            .border(1.dp, IonCyan.copy(alpha = 0.45f), CircleShape)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = desc,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun latLonToCanvas(
    lat: Double,
    lon: Double,
    width: Float,
    height: Float,
    zoom: Float,
    pan: Offset
): Offset {
    // Center map on Iraq (Lat 33.2°N, Lon 44.0°E), spanning Lat 28.5..38.0, Lon 38.0..49.5
    val normX = ((lon - 38.0) / (49.5 - 38.0)).toFloat()
    val normY = (1.0 - ((lat - 28.5) / (38.0 - 28.5))).toFloat()
    val cx = width / 2f
    val cy = height / 2f
    val rawX = normX * width
    val rawY = normY * height
    return Offset(
        x = cx + (rawX - cx) * zoom + pan.x,
        y = cy + (rawY - cy) * zoom + pan.y
    )
}

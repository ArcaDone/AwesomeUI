package com.arcadone.awesomeui.components.chart.progreessionchart

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import com.arcadone.awesomeui.components.chart.line.ChartDataPoint

fun buildSmoothPath(dataPoints: List<Offset>): Path {
    return Path().apply {
        if (dataPoints.isEmpty()) return@apply
        moveTo(dataPoints[0].x, dataPoints[0].y)
        for (i in 1 until dataPoints.size) {
            val prev = dataPoints[i - 1]
            val curr = dataPoints[i]
            val controlX = (prev.x + curr.x) / 2
            cubicTo(controlX, prev.y, controlX, curr.y, curr.x, curr.y)
        }
    }
}

fun findClosestDataPointIndex(
    progress: Float,
    dataSize: Int,
): Int {
    if (dataSize <= 1) return 0
    val index = (progress * (dataSize - 1)).toInt()
    return index.coerceIn(0, dataSize - 1)
}

fun interpolateValueAtProgress(
    data: List<ChartDataPoint>,
    progress: Float,
): Float {
    if (data.isEmpty()) return 0f
    if (data.size == 1) return data[0].value

    val exactIndex = progress * (data.size - 1)
    val lowerIndex = exactIndex.toInt().coerceIn(0, data.size - 2)
    val upperIndex = (lowerIndex + 1).coerceIn(0, data.size - 1)

    val lowerValue = data[lowerIndex].value
    val upperValue = data[upperIndex].value

    val fraction = exactIndex - lowerIndex
    return lowerValue + (upperValue - lowerValue) * fraction
}

fun getPointAtProgress(
    fullPath: Path,
    progress: Float,
): Offset {
    val pathMeasure = PathMeasure()
    pathMeasure.setPath(fullPath, false)
    val pathLength = pathMeasure.length
    val targetLength = pathLength * progress.coerceIn(0f, 1f)
    return pathMeasure.getPosition(targetLength)
}

fun getAnimatedPath(
    fullPath: Path,
    progress: Float,
): Path {
    if (progress >= 1f) return fullPath
    if (progress <= 0f) return Path()

    val pathMeasure = PathMeasure()
    pathMeasure.setPath(fullPath, false)
    val pathLength = pathMeasure.length
    val targetLength = pathLength * progress

    val animatedPath = Path()
    pathMeasure.getSegment(0f, targetLength, animatedPath, true)
    return animatedPath
}

fun DrawScope.drawAnimatedGradient(
    fullPath: Path,
    progress: Float,
    height: Float,
    firstX: Float,
    lineColor: Color,
) {
    val pathMeasure = PathMeasure()
    pathMeasure.setPath(fullPath, false)
    val pathLength = pathMeasure.length
    val targetLength = pathLength * progress

    val gradientPath = Path().apply {
        val steps = 50
        var started = false
        for (i in 0..steps) {
            val distance = (targetLength * i / steps).coerceAtMost(targetLength)
            val pos = pathMeasure.getPosition(distance)
            if (!started) {
                moveTo(pos.x, pos.y)
                started = true
            } else {
                lineTo(pos.x, pos.y)
            }
        }

        val currentPoint = pathMeasure.getPosition(targetLength)
        lineTo(currentPoint.x, height)
        lineTo(firstX, height)
        close()
    }

    drawPath(
        path = gradientPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                lineColor.copy(alpha = 0.25f),
                lineColor.copy(alpha = 0.05f),
                Color.Transparent,
            ),
        ),
        style = Fill,
    )
}

fun getPathSegment(
    fullPath: Path,
    fromProgress: Float,
    toProgress: Float,
): Path {
    val pathMeasure = PathMeasure()
    pathMeasure.setPath(fullPath, false)
    val length = pathMeasure.length

    val start = (length * fromProgress).coerceIn(0f, length)
    val end = (length * toProgress).coerceIn(0f, length)

    val segmentPath = Path()
    if (end > start) {
        pathMeasure.getSegment(start, end, segmentPath, true)
    }
    return segmentPath
}

fun DrawScope.drawSegmentGradient(
    fullPath: Path,
    fromProgress: Float,
    toProgress: Float,
    height: Float,
    lineColor: Color,
) {
    val pathMeasure = PathMeasure()
    pathMeasure.setPath(fullPath, false)
    val length = pathMeasure.length

    val startDist = (length * fromProgress).coerceIn(0f, length)
    val endDist = (length * toProgress).coerceIn(0f, length)

    if (endDist <= startDist) return

    val gradientPath = Path()
    pathMeasure.getSegment(startDist, endDist, gradientPath, true)

    val startPos = pathMeasure.getPosition(startDist)
    val endPos = pathMeasure.getPosition(endDist)

    gradientPath.lineTo(endPos.x, height)
    gradientPath.lineTo(startPos.x, height)
    gradientPath.close()

    drawPath(
        path = gradientPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                lineColor.copy(alpha = 0.25f),
                lineColor.copy(alpha = 0.05f),
                Color.Transparent,
            ),
            startY = 0f,
            endY = height,
        ),
        style = Fill,
    )
}

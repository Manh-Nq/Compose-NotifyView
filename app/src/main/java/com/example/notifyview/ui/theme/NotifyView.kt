package com.example.notifyview.ui.theme

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.toSize
import com.example.notifyview.R

const val RATIO_OFFSET_LEFT_NOTIFY_MOVE = 60f / 434f
const val RATIO_OFFSET_TOP_NOTIFY_PERMANENT = 43f / 289f
const val RATIO_OFFSET_TOP_DOT_BELL = 14f / 47f
const val RATIO_OFFSET_LEFT_DOT_BELL = 21.5f / 213f

const val RATIO_OFFSET_TOP_DOT = 30f / 47f

const val RADIUS_DOT_BELL = 1.5f
const val RADIUS_DOT = 4f
const val PADDING_NOTIFY = 10f

const val RATIO_WIDTH_TO_WIDTH_NOTIFY = 213f / 154f
const val RATIO_HEIGHT_TO_HEIGHT_NOTIFY = 47f / 34f
const val NUM_NOTIFY = 3
const val DURATION = 4000

data class NotifyState(
    var sizePhone: Size = Size.Zero,
    var parentSize: Size = Size.Zero,
    var phone: Phone = Phone(),
    var notifyPermanent: NotifyPermanent = NotifyPermanent(),
    var scale: Float = 0f,
    var notifyMoveOffsetY: Float = 0f
)

data class NotifyPermanent(val top: Float = 0f, val left: Float = 0f, val size: Size = Size.Zero)
data class Phone(val top: Float = 0f, val left: Float = 0f)

fun NotifyState.onSizeChange(s: Size) {
    parentSize = s
    val calcParent = parentSize.width.coerceAtMost(parentSize.height)
    scale = calcParent / (sizePhone.width.coerceAtLeast(sizePhone.height))

    val leftPhone = (parentSize.width / 2f - sizePhone.width / 2f) * scale
    val topPhone = (parentSize.height / 2f - sizePhone.height / 2f) * scale
    setParamsPhone(top = topPhone, left = leftPhone)

}

private fun NotifyState.onChangeSizeView(sizePath: Size) {
    sizePhone = sizePath
}

private fun NotifyState.setParamsPhone(top: Float, left: Float) {
    phone = Phone(top = top, left = left)
}

private fun NotifyState.setParamsNotifyPermanent(
    size: Size = Size.Zero
) {
    val top = sizePhone.height * RATIO_OFFSET_TOP_NOTIFY_PERMANENT
    val left = (parentSize.width - size.width) * scale / 2f - phone.left
    notifyPermanent = NotifyPermanent(top = top, left = left, size = size)

}

private fun NotifyState.calculatorNotifyOffsetY(a: Float, b: Float, c: Float) {
    notifyMoveOffsetY = NUM_NOTIFY - (a + b + c)
}

@Composable
fun rememberNotifyState() = remember { NotifyState() }

@Composable
fun NotifyView(modifier: Modifier = Modifier, duration: Int = DURATION) {

    val notifyState = rememberNotifyState()
    val phone = ImageVector.vectorResource(id = R.drawable.notify_phone_icon)
    val painterPhone = rememberVectorPainter(image = phone)

    val notifyPermanent = ImageVector.vectorResource(id = R.drawable.notify_item_icon)
    val painterNotifyPermanent = rememberVectorPainter(image = notifyPermanent)

    val notify = ImageVector.vectorResource(id = R.drawable.notify_item_blur_icon)
    val painterNotify = rememberVectorPainter(image = notify)
    val context = LocalContext.current

    val anim = rememberInfiniteTransition()

    val valueAnim1 = anim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = duration
                0f at duration / 8
                1f at (duration / 8) * 2
            },
            repeatMode = RepeatMode.Restart
        )
    )

    val valueAnim2 = anim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = duration
                0f at (duration / 8) * 3
                1f at (duration / 8) * 4
            },
            repeatMode = RepeatMode.Restart
        )
    )

    val valueAnim3 = anim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = duration
                0f at (duration / 8) * 5
                1f at (duration / 8) * 6
            },
            repeatMode = RepeatMode.Restart
        )
    )
    val valueAnimEnd = anim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = duration
                0f at (duration / 8) * 7
                1f at duration
            },
            repeatMode = RepeatMode.Restart
        )
    )



    notifyState.onChangeSizeView(painterPhone.intrinsicSize)
    notifyState.calculatorNotifyOffsetY(valueAnim1.value, valueAnim2.value, valueAnim3.value)

    Canvas(modifier = modifier
        .onSizeChanged { notifyState.onSizeChange(it.toSize()) }
        .clipToBounds()) {
        withTransform(transformBlock = {
            translate(left = notifyState.phone.left, top = notifyState.phone.top)
            scale(notifyState.scale)
        }) {
            drawPhoneBackground(painterPhone)

            notifyState.setParamsNotifyPermanent(size = painterNotifyPermanent.intrinsicSize)

            drawNotifyMove(
                painterNotify = painterNotify,
                state = notifyState,
                valueAnim1 = valueAnim1.value,
                valueAnim2 = valueAnim2.value,
                valueAnim3 = valueAnim3.value,
                context = context
            )

            drawNotifyMoveBefore(
                painterNotify = painterNotify,
                state = notifyState,
                valueAnim = valueAnimEnd.value,
                context = context
            )

            drawNotifyPermanent(
                painterNotifyPermanent,
                notifyState,
                context,
                valueAnim1.value,
                valueAnim2.value,
                valueAnim3.value,
                valueAnimEnd.value
            )
        }

    }
}


fun DrawScope.drawNotifyPermanent(
    painterNotify: VectorPainter,
    state: NotifyState,
    context: Context,
    valueAnim1: Float,
    valueAnim2: Float,
    valueAnim3: Float,
    valueEnd: Float
) {

    //draw notify Permanent
    val radiusDotBell = context.dpToPx(RADIUS_DOT_BELL)
    val radiusDot = context.dpToPx(RADIUS_DOT)
    withTransform(transformBlock = {
        translate(left = state.notifyPermanent.left, top = state.notifyPermanent.top)
    }) {
        with(painterNotify) {
            draw(this.intrinsicSize)
        }

        //draw dot in to below bell icon
        val topDotBell =
            painterNotify.intrinsicSize.height.toTopDot(RATIO_OFFSET_TOP_DOT_BELL, radiusDotBell)
        val leftDotBell =
            painterNotify.intrinsicSize.width * RATIO_OFFSET_LEFT_DOT_BELL + radiusDotBell / 2f

        val topDot =
            painterNotify.intrinsicSize.height.toTopDot(
                RATIO_OFFSET_TOP_DOT,
                context.dpToPx(RADIUS_DOT)
            )


        val top = androidx.compose.ui.util.lerp(
            topDotBell,
            topDot,
            valueEnd.checkValidValue(valueAnim1)
        )
        val scale = androidx.compose.ui.util.lerp(
            radiusDotBell,
            radiusDot,
            valueEnd.checkValidValue(valueAnim1)
        )
        val alpha =
            androidx.compose.ui.util.lerp(1f, 0f, valueEnd.checkValidValue(valueAnim1))
        //circle Animation
        drawCircle(
            color = Color(0xFFE798C7),
            radius = scale,
            center = Offset(leftDotBell, top)
        )
        drawCircle(
            color = Color(0xFFE798C7),
            radius = radiusDotBell,
            alpha = 1f - alpha,
            center = Offset(leftDotBell, topDotBell)
        )
        //circle Dot bottom

        drawDot(
            index = 2,
            radius = radiusDot,
            viewParent = painterNotify,
            topDot = topDot,
            alphaDot = valueEnd.checkValidValue(valueAnim2)
        )
        drawDot(
            index = 3,
            radius = radiusDot,
            viewParent = painterNotify,
            topDot = topDot,
            alphaDot = valueEnd.checkValidValue(valueAnim3)
        )
    }
}

private fun Float.checkValidValue(animValue: Float): Float {
    val result = androidx.compose.ui.util.lerp(1f, 0f, this)
    return if (this == 0.0f) animValue else result
}

fun DrawScope.drawDot(
    index: Int = 1,
    radius: Float,
    viewParent: VectorPainter,
    topDot: Float,
    alphaDot: Float
) {
    val left = index * (viewParent.intrinsicSize.width * RATIO_OFFSET_LEFT_DOT_BELL)

    drawCircle(
        color = Color(0xFFE798C7),
        radius = radius,
        alpha = alphaDot,
        center = Offset(left, topDot)
    )
}

private fun Float.toTopDot(ratio: Float, radius: Float): Float {
    return this * ratio + radius / 2f
}

fun DrawScope.drawPhoneBackground(painter: VectorPainter) {
    with(painter) {
        draw(painter.intrinsicSize)
    }
}

fun DrawScope.drawNotifyMove(
    painterNotify: VectorPainter,
    state: NotifyState,
    valueAnim1: Float,
    valueAnim2: Float,
    valueAnim3: Float,
    context: Context
) {
    val wView = painterNotify.intrinsicSize.width
    val hView = painterNotify.intrinsicSize.height

    val heightNotifyPermanent = state.notifyPermanent.size.height
    val topNotifyPermanent = state.notifyPermanent.top
    val paddingTop = context.dpToPx(PADDING_NOTIFY)

    for (index in 0 until NUM_NOTIFY) {
        val top =
            heightNotifyPermanent + topNotifyPermanent + (index * hView) + paddingTop - ((3 - state.notifyMoveOffsetY) * hView)
        val left = wView * RATIO_OFFSET_LEFT_NOTIFY_MOVE

        val valueAnim = when (index) {
            0 -> valueAnim1
            1 -> valueAnim2
            else -> valueAnim3
        }
        val scaleW = androidx.compose.ui.util.lerp(1f, RATIO_WIDTH_TO_WIDTH_NOTIFY, valueAnim)
        val scaleH = androidx.compose.ui.util.lerp(1f, RATIO_HEIGHT_TO_HEIGHT_NOTIFY, valueAnim)

        val alpha = androidx.compose.ui.util.lerp(1f, 0f, valueAnim)

        withTransform(transformBlock = {
            translate(top = top, left = left)
        }) {
            scale(
                scaleX = scaleW,
                scaleY = scaleH,
                pivot = Offset(
                    wView / 2f,
                    hView / 2f
                )
            ) {
                with(painterNotify) {
                    draw(this.intrinsicSize, alpha = alpha)
                }
            }
        }
    }
}

fun DrawScope.drawNotifyMoveBefore(
    painterNotify: VectorPainter,
    state: NotifyState,
    valueAnim: Float,
    context: Context
) {

    val wView = painterNotify.intrinsicSize.width
    val hView = painterNotify.intrinsicSize.height

    val heightNotifyPermanent = state.notifyPermanent.size.height
    val topNotifyPermanent = state.notifyPermanent.top
    val paddingTop = context.dpToPx(PADDING_NOTIFY)
    for (index in 0 until NUM_NOTIFY) {
        val top = heightNotifyPermanent + topNotifyPermanent + paddingTop + (index * hView)

        val scaleW = androidx.compose.ui.util.lerp(RATIO_WIDTH_TO_WIDTH_NOTIFY, 1f, valueAnim)
        val scaleH = androidx.compose.ui.util.lerp(RATIO_HEIGHT_TO_HEIGHT_NOTIFY, 1f, valueAnim)

        val topRs = androidx.compose.ui.util.lerp(topNotifyPermanent, top, valueAnim)
        val left = wView * RATIO_OFFSET_LEFT_NOTIFY_MOVE
        val alpha = androidx.compose.ui.util.lerp(0f, 1f, valueAnim)

        withTransform(transformBlock = {
            translate(top = topRs, left = left)
        }) {
            scale(
                scaleX = scaleW,
                scaleY = scaleH,
                pivot = Offset(
                    wView / 2f,
                    hView / 2f
                )
            ) {
                with(painterNotify) {
                    draw(this.intrinsicSize, alpha = alpha)
                }
            }
        }
    }
}

@Preview
@Composable
fun NotifyViewPreview() {
    MaterialTheme {
        NotifyView()
    }
}


fun Context.dpToPx(dp: Float): Float {
    return (dp * resources.displayMetrics.density + 0.5f)
}
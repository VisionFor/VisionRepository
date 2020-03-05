package com.vision.seekbar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.AbsSeekBar
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.IntDef
import com.vision.seekbarsimple.R
import java.text.NumberFormat
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.properties.Delegates

/**
 * Function:进度条<br/>
 * <br/>
 * Author:Runbinson<br/>
 * Time:2019/10/26 0026 11:17<br/>
 * Version:1.0<br/>
 */
class VisionProgressBar : View {

    private val TAG = javaClass.simpleName

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttr(context, attrs)
    }

    private fun initAttr(context: Context, attrs: AttributeSet?) {
        attrs?.run {
            val typedArray = context.obtainStyledAttributes(this, R.styleable.VisionProgressBar)
            mProgressColor =
                typedArray.getColor(R.styleable.VisionProgressBar_progressColor, mProgressColor)
            mSecondProgressColor =
                typedArray.getColor(
                    R.styleable.VisionProgressBar_secondaryProgressColor,
                    mSecondProgressColor
                )
            mProgressTextColor =
                typedArray.getColor(
                    R.styleable.VisionProgressBar_progressTextColor,
                    mProgressTextColor
                )
            mThumbColor = typedArray.getColor(R.styleable.VisionProgressBar_thumbColor, mThumbColor)
            mThumbBorderColor =
                typedArray.getColor(R.styleable.VisionProgressBar_thumbBorderColor, mLabelColor)
            mLabelColor =
                typedArray.getColor(R.styleable.VisionProgressBar_progressLabelColor, mLabelColor)
            //拖动滑块时，显示当前进度的文本字体大小
            mProgressTextSize =
                typedArray.getDimension(
                    R.styleable.VisionProgressBar_progressTextSize,
                    mProgressTextSize
                )
            mProgressTextSize =
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_PX,
                    mProgressTextSize,
                    resources.displayMetrics
                )
            mThumbRadius =
                typedArray.getDimension(R.styleable.VisionProgressBar_thumbRadius, mThumbRadius)
            //包裹进度值的矩形底部三角形指示器的高度,默认为半径的一半
            mArrowH = min(mThumbRadius / 2, 6.5f)
            //进度条的高度，默认为thumb滑块半径的1/4
            mProgressLineHeight =
                typedArray.getDimension(
                    R.styleable.VisionProgressBar_progressHeight,
                    mThumbRadius / 4
                )
            //为了保证显示效果，如果进度条的高度比滑块半径的0.85还要大，那就调整到1/4
            //进度条高度过高，会导致看不出滑块，即滑块已经在进度条中了
            if (mProgressLineHeight >= mThumbRadius * 0.85f) {
                mProgressLineHeight = mThumbRadius / 4
            }
            mIhumbBorderRadius =
                typedArray.getDimension(
                    R.styleable.VisionProgressBar_thumbBorderRadius,
                    mThumbRadius / 2
                )
            if (mThumbRadius <= mIhumbBorderRadius) {
                mThumbRadius = mIhumbBorderRadius
                mIhumbBorderRadius = mThumbRadius / 2
            }
            //最小进度值
            minProgress = typedArray.getFloat(R.styleable.VisionProgressBar_minProgress, 0f)
            //最大进度值
            maxProgress = typedArray.getFloat(R.styleable.VisionProgressBar_maxProgress, 100f)
            //进度范围校验
            if (minProgress == maxProgress) {
                minProgress = 0f
                maxProgress = 100f
            } else {
                minProgress = min(minProgress, maxProgress)
                maxProgress = max(minProgress, maxProgress)
            }
            //获取初始进度
            mProgress = typedArray.getFloat(R.styleable.VisionProgressBar_progress, 0f)
            transFromProgress(mProgress)
            //获取包裹进度值的圆角矩形框样式
            val style = typedArray.getInt(R.styleable.VisionProgressBar_progressLabelStyle, 0)
            when (style) {
                0 -> {
                    mLabelStyle = Paint.Style.FILL
                }
                1 -> {
                    mLabelStyle = Paint.Style.STROKE
                }
                else -> {
                    mLabelStyle = Paint.Style.FILL
                }
            }

            val labelShape = typedArray.getInt(
                R.styleable.VisionProgressBar_progressLabelShape,
                LABEL_SHAPE_RECTANGLE
            )
            when (labelShape) {
                LABEL_SHAPE_RECTANGLE -> {
                    mLabelShape = LABEL_SHAPE_RECTANGLE
                }

                LABEL_SHAPE_CONE -> {
                    mLabelShape = LABEL_SHAPE_CONE
                }

                else -> {
                    mLabelShape = LABEL_SHAPE_RECTANGLE
                }
            }

            //是否显示刻度值
            flag_showDegreeText =
                typedArray.getBoolean(R.styleable.VisionProgressBar_showDegreeText, false)
            //是否显示分段标记
            flag_showDegree = typedArray.getBoolean(R.styleable.VisionProgressBar_showDegree, false)
            //是否自动跳转到最近的点
            flag_autoSeekBoundary =
                typedArray.getBoolean(R.styleable.VisionProgressBar_isAutoSeekBoundary, false)
            flag_stepByStep =
                typedArray.getBoolean(R.styleable.VisionProgressBar_isStepByStep, false)
            //是否限制触摸的范围
            flag_limitTouchRange =
                typedArray.getBoolean(R.styleable.VisionProgressBar_limitTouchRange, false)
            //刻度值字体的大小
            mDegreeValueTxtSize =
                typedArray.getDimension(
                    R.styleable.VisionProgressBar_degreeTextSize,
                    mDegreeValueTxtSize
                )
            mDegreeValueTxtSize =
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_PX,
                    mDegreeValueTxtSize,
                    resources.displayMetrics
                )
            //刻度值的字体颜色
            mDegreeValueTxtColor =
                typedArray.getColor(
                    R.styleable.VisionProgressBar_progressTextColor,
                    mProgressTextColor
                )
            //刻度分段数
            mSubsectionCount =
                typedArray.getColor(R.styleable.VisionProgressBar_subsectionCount, mSubsectionCount)
            //分段数小于2，就没必要设置一步一步的移动了
            if (mSubsectionCount <= 2) {
                flag_stepByStep = false
            }

            //通过xml文件配置参数后，如果需要自动定位到最近的点，则需要再次处理一下进度值
            if (flag_autoSeekBoundary || flag_stepByStep) {
                reBoundaryProgress()
            }

            typedArray.recycle()
        }
    }

    /**
     * 对进度值进行转换
     * @param progress 待转换的进度值
     */
    private fun transFromProgress(progress: Float) {
        if (!(progress in minProgress..maxProgress)) {
            if (progress <= minProgress) mProgress = minProgress
            if (progress >= maxProgress) mProgress = maxProgress
        }
        mProgress = (progress - minProgress) / (maxProgress - minProgress)
    }

    /**
     * 画布中心点坐标
     */
    private var mCenterPoint = PointF()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        var w = MeasureSpec.getSize(widthMeasureSpec)
        var h = MeasureSpec.getSize(heightMeasureSpec)
        if (wMode == MeasureSpec.EXACTLY && hMode == MeasureSpec.EXACTLY) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            mCenterPoint.set(w / 2f, h / 2f)
            initPoint()
            return
        }

        if (wMode == MeasureSpec.UNSPECIFIED) {
            w = resources.getDimension(R.dimen.dimen_100).toInt()
        }

        if (hMode == MeasureSpec.UNSPECIFIED || hMode == MeasureSpec.AT_MOST) {
            //高度，最小为5倍的滑块半径+底部显示的分段标记的字体大小的两倍
            //滑块直径，即从底部到显示进度的圆角矩形的高度,2倍的滑块半径
            //显示进度的矩形高度，1.5倍的滑块半径
            //显示进度的矩形底部三角形指示器高度：6
            h = (6 * mThumbRadius + 2 * mDegreeValueTxtSize).toInt()
        }
        setMeasuredDimension(w, h)
        initPoint()
    }


    /**
     * 左边界的横坐标
     */
    private var mLeftBoundaryX = 0f
    /**
     * 右边界的横坐标
     */
    private var mRightBoundaryX = 0f

    /**
     * 初始化中心点、边界、以及可用的宽度
     */
    private fun initPoint() {
        mCenterPoint.set(measuredWidth / 2f, measuredHeight / 2f)
        //可用的宽度
        mAvailableWidth = measuredWidth - paddingStart - paddingEnd - 2 * mThumbRadius
        mLeftBoundaryX = paddingStart + mThumbRadius
        mRightBoundaryX = measuredWidth - paddingEnd - mThumbRadius
    }


    /**
     * radius of thumb
     * 拖动滑块的半径
     */
    var mThumbRadius = 15f
        set(value) {
            if (value <= 15f) {
                field = 15f
            } else {
                field = value
            }
        }

    /**
     * radius of thumb border
     * 滑块的描边半径
     */
    var mIhumbBorderRadius = mThumbRadius / 3f

    /**
     * the max width can be used for the canvas
     * if your view has set some padding, just like padding-left,padding-end and so on ,
     * so the max width can be used for the canvas need minus view's padding if neceseray,
     * and also need minus thumb's radius too ,
     * cause program will make sure can be show thumb completely when you dragging thumb to boundary point
     * 可用的宽度
     * 除去左右边距，以及为端点显示滑块预留的边距后的宽度
     */
    private var mAvailableWidth = 0f


    /**
     * 获取进度值
     * @return 返回当前的进度数值
     */
    fun getProgressValue() = getRealProgress(mProgress * mAvailableWidth + mLeftBoundaryX)

    /**
     * 获取进度比例
     * (当前的进度值 - 最小的进度值 )/ 总的进度值 = (当前进度的数值 - minProgress)/(maxProgress - minProgress)
     * eg:
     * 当前进度值是-3，最小进度是-5，最大进度是5
     * 那么进度的比例就是：(-3 -(-5))/(5 - (-5)) = (-3+5)/(5+5) = 2/10 = 0.2
     * eg:
     *当前进度值是3.5，最新进度是3，最大进度是12
     * 那么进度的比例就是：(3.5- 3)/(12 -3) = 0.5/9 = 0.0555
     *
     * @return 返回的是百分比，即当前的进度值占整体进度的百分比
     */
    fun getProgressRate() =
        (getRealProgress(mProgress * mAvailableWidth + mLeftBoundaryX) - minProgress) / (maxProgress - minProgress)


    /**
     * height of progress line
     * 进度条的高度
     */
    var mProgressLineHeight = 3.5f
        set(value) {
            if(value > mThumbRadius * 0.85f){
                field = mThumbRadius/4
            }else{
                field = value
            }
        }
    /**
     * color of progress line
     * 进度条线条颜色
     */
    var mProgressColor = resources.getColor(R.color.progress_color)

    /**
     * paint of progress line
     * 绘制进度条的画笔
     */
    private val mLinePaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = mProgressColor
            this.style = Paint.Style.FILL
            this.strokeWidth = mProgressLineHeight
        }
    }

    /**
     * paint of progress line
     * 绘制进度条的画笔
     */
    private val mDegreeLabelPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = mProgressColor
            this.style = Paint.Style.FILL
            this.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)
            this.strokeWidth = mProgressLineHeight
        }
    }

    /**
     * color of secondary-progress
     * 二级进度条颜色
     * 即：进度条背景色
     */
    var mSecondProgressColor = resources.getColor(R.color.secondary_progress_color)
    /**
     * paint of secondary-progress
     * 绘制二级进度条的画笔
     * 即，剩余的进度的画笔
     */
    private val mLineBgPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = mSecondProgressColor
            this.style = Paint.Style.FILL
            this.strokeWidth = mProgressLineHeight
        }
    }

    /**
     * text color of progress-text
     * the progress-text will be draw when you pressing or dragging thumb
     * 拖动滑块时，显示进度的文本颜色
     */
    var mProgressTextColor = Color.WHITE

    /**
     * text size of progress-text
     * the shape-width which wrap progress-text depended on this value ,
     * and the text length of max-progress value
     * 拖动滑块时，显示进度的文本字体大小
     */
    var mProgressTextSize = 20f

    /**
     *
     * shape style of wrap progress-text
     * available value is fill-style and stork-style
     * fill-style as value 1
     * stroke-style as value 0
     * 显示进度的标签样式
     * <ul>
     *     <li>1 填充</li>
     *     <li>0 描边</li>
     * </ul>
     */
    var mLabelStyle: Paint.Style = Paint.Style.STROKE

    /**
     * 拖动滑块时，绘制进度文本的画笔
     */
    private val mLabelTxtPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = mProgressTextColor
            this.textAlign = Paint.Align.CENTER
            this.textSize = mProgressTextSize
        }
    }


    /**
     *  text color of degree-text
     *  the every section corresponds to one value when whole progress divide into pieces
     * 刻度值的字体颜色
     */
    var mDegreeValueTxtColor = mProgressTextColor


    /**
     * paint of degree-text
     * 绘制刻度值的文本画笔
     */
    private val mDegreeValueTxtPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = mDegreeValueTxtColor
            this.textAlign = Paint.Align.CENTER
            this.textSize = mDegreeValueTxtSize
        }
    }

    /**
     *
     *  color of shape which warp progress-text
     *
     * 拖动滑块时，包裹进度数值的形状颜色
     */
    var mLabelColor = mProgressColor

    /**
     *
     * paint of shape which warp progress-text
     * 绘制包裹进度数值的形状的画笔
     */
    private val mLabelPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = mLabelColor
            this.style = mLabelStyle
        }
    }

    /**
     *
     *  border color of thumb
     * 绘制滑块的描边颜色
     */
    var mThumbBorderColor = Color.WHITE

    /**
     *  color of thumb
     * 绘制滑块的填充颜色
     */
    var mThumbColor = Color.BLUE

    /**
     * border paint of thumb
     * 绘制滑块的画笔：描边
     */
    private val mOuterThumbPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.FILL
            this.color = mThumbBorderColor
        }
    }

    /**
     * paint of thumb
     * 绘制滑块的画笔：内部
     */
    private val mInnerThumbPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.FILL
            this.color = mThumbColor
        }
    }

    override fun onDraw(canvas: Canvas?) {
        //绘制进度
        drawProgressLine(canvas)
        //绘制刻度和刻度值
        drawDegreeValue(canvas)
        //绘制滑块，按压拖拽状态下，还需要绘制标签框
        drawThumb(canvas)
        //绘制进度值，如果有必要
        drawProgressValueIfNeeded(canvas)
    }

    /**
     * 如果不显示刻度，也不显示刻度值
     * 松开手指后，进度条下方会显示当前的进度值
     */
    private fun drawProgressValueIfNeeded(canvas: Canvas?) {
        //如果不显示刻度文本，那么在未按压或拖拽状态时
        // 即手指松开后，要绘制当前的进度值
        if (!flag_showDegreeText && flag_states == STATES_IDLE) {
            val x = mProgress * mAvailableWidth + mLeftBoundaryX
            val y = height - paddingBottom - mDegreeValueTxtSize
            //在进度条下面，绘制当前的进度值
            canvas?.drawText(getLabelText(x), x, y, mDegreeValueTxtPaint)
        }
    }


    private val numberFormatInstance by lazy { NumberFormat.getInstance() }


    /**
     * draw degree value
     * 绘制刻度值
     */
    private fun drawDegreeValue(canvas: Canvas?) {
        if (flag_showDegree) {
            drawDegree(canvas)
        }

        val baseLineY = height - paddingBottom - mDegreeValueTxtSize

        //如果当前的分段刻度只有一段，那么只绘制两端点的刻度
        if (mSubsectionCount <= 1) {
            //起始端点
            canvas?.drawText(
                numberFormatInstance.format(minProgress).toString(),
                mLeftBoundaryX,
                baseLineY,
                mDegreeValueTxtPaint
            )
            //结束端点
            canvas?.drawText(
                numberFormatInstance.format(maxProgress).toString(),
                mRightBoundaryX,
                baseLineY,
                mDegreeValueTxtPaint
            )
            return
        }

        //如果当前也进行了分段，并且显示分度刻度，那么绘制分段的刻度
        if (flag_showDegreeText) {
            var x = mLeftBoundaryX
            for (i in 0..mSubsectionCount) {
                canvas?.drawText(
                    numberFormatInstance.format(
                        String.format(
                            "%.2f",
                            minProgress + i * (maxProgress - minProgress) / mSubsectionCount
                        ).toFloat()
                    ).toString(),
                    x,
                    baseLineY,
                    mDegreeValueTxtPaint
                )
                x += mAvailableWidth / mSubsectionCount
            }
        }

    }

    /**
     * 绘制刻度点
     */
    private fun drawDegree(canvas: Canvas?) {
        val baseLineY = height - paddingBottom - mThumbRadius - 1.5f * mDegreeValueTxtSize
        if (mSubsectionCount <= 1) {
            //左端点
            canvas?.drawCircle(mLeftBoundaryX, baseLineY, mThumbRadius / 3f, mLineBgPaint)
            //右端点
            canvas?.drawCircle(mRightBoundaryX, baseLineY, mThumbRadius / 3f, mLineBgPaint)
            return
        }
        var x = mLeftBoundaryX
        if (flag_stepByStep) {
            for (i in 0..mSubsectionCount) {
                if (floor(mProgress * mSubsectionCount) >= i) {
                    //绘制选中部分
                    canvas?.drawCircle(x, baseLineY, mThumbRadius / 3f, mLinePaint)
                } else {
                    //绘制未选中部分
                    canvas?.drawCircle(x, baseLineY, mThumbRadius / 3f, mLineBgPaint)
                }
                x += mAvailableWidth / mSubsectionCount
            }
        } else {
            for (i in 0..mSubsectionCount) {
                if (mProgress * mAvailableWidth + mLeftBoundaryX > x) {
                    //绘制选中部分
                    canvas?.drawCircle(x, baseLineY, mThumbRadius / 3f, mLinePaint)
                } else {
                    //绘制未选中部分
                    canvas?.drawCircle(x, baseLineY, mThumbRadius / 3f, mLineBgPaint)
                }
                x += mAvailableWidth / mSubsectionCount
            }
        }
    }

    /**
     *  min progress value
     * 进度的最小值
     */
    var minProgress = 0f

    /**
     *  max progress value
     * 进度的最大值
     */
    var maxProgress = 100f

    /**
     * current progress
     * 当前的进度
     */
    private var mProgress by Delegates.vetoable(minProgress) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            mListener.invoke((getRealProgress(newValue * mAvailableWidth + mLeftBoundaryX) - minProgress) / (maxProgress - minProgress))
        }
        oldValue != newValue
    }


    /**
     * how many section will the whole progress be divided into
     * must be positive value and the min section-number is 2
     * 将进度分成几段
     */
    var mSubsectionCount: Int = 2

    /**
     * is show degree-text value
     * it will not be drawn degree-text when you set it false, even if you set the section-count
     * 是否显示分段的数字
     */
    var flag_showDegreeText = false

    /**
     *  is show degree
     *  progress will be contained two endpoint,start and end point,when you set it false
     *  the whole progress look like line segment
     * 是否显示分段的标记
     */
    var flag_showDegree = false

    /**
     * is auto jump the nearest point
     * 是否自动调整数值到最近的点
     */
    var flag_autoSeekBoundary = false

    /**
     * is selected step by step
     * especially,it will not worked when your subsection-count less than 2
     * obviously,the whole progress will not be divided when your subsection-count less than 2
     * so,it's necessary let subsection-count more than 2 when you want select step by step
     * 是否一步一步的选中
     */
    var flag_stepByStep: Boolean = false

    /**
     * 是否限制触摸的范围
     * 如果设置为ture,那么触摸范围将被限制
     * 横向：在整个进度条的宽度 上有效
     * 纵向：进度条高度±滑块半径 上有效
     *
     * 如果不做限制，那么整个view都将响应
     */
    var flag_limitTouchRange = false

    /**
     * text size of degree-text
     * 分段文字的字体大小
     */
    var mDegreeValueTxtSize = 14f


    /**
     * draw thumb of progress
     * 绘制滑块
     */
    private fun drawThumb(canvas: Canvas?) {
        val x = mProgress * mAvailableWidth + mLeftBoundaryX
        val y = height - paddingBottom - mThumbRadius - 1.5f * mDegreeValueTxtSize
        //绘制滑块的底圆
        canvas?.drawCircle(x, y, mThumbRadius, mOuterThumbPaint)
        //绘制拖拽或按压状态的thumb
        if (flag_states == STATES_DRAGGING) {
            //当进入拖拽状态时，内部滑块会进行"放大"，具体表现出来就是滑块外边框收缩(缩小了2/3)
            //这里是采用两个圆叠加实现的，内圆的半径是外圆半径的1/3
            canvas?.drawCircle(x, y, mThumbRadius - mIhumbBorderRadius / 3f, mInnerThumbPaint)
            //绘制当前进度值
            drawLabel(canvas, x)
            return
        }
        //非按压状态，
        canvas?.drawCircle(x, y, mThumbRadius - mIhumbBorderRadius, mInnerThumbPaint)
    }


    /**
     * 绘制进度，以及包裹进度数值的路径
     * 对于圆角矩形的标签而言，端点处不需要做平移处理
     * 特别的，对于锥形的标签，因为没办法像圆角矩形那样平移底部的小箭头从而完成边界处理，所以
     * 锥形的标签在处于边界时，程序会做平移处理
     */
    private fun drawLabel(canvas: Canvas?, x: Float) {
        //高度-底部间距-滑块的半径 - 滑块的描边半径-进度值字体的1.5倍高度 - 进度线条的高度
        val y =
            height - paddingBottom - mThumbRadius - mProgressTextSize - 1.5f * mDegreeValueTxtSize - mProgressLineHeight
        //因为不同形状的标签框，中心的位置不一样，所以需要调整y坐标
        var adjustY = y - mProgressTextSize / 2
        //特别的对于圆锥形的标签框，因为绘制了底部的锥形部分，所以
        //中心需要再上移锥形的高
        if (mLabelShape == LABEL_SHAPE_CONE) {
            adjustY -= mLabelShapeConeH
        }

        //对于矩形框，底部有三角形指示器，所以，再上移三角形指示器的高度
        if (mLabelShape == LABEL_SHAPE_RECTANGLE) {
            adjustY -= mArrowH
        }

        //如果不是边界，则正常绘制
        if (!(isLabelAtLeftBoundary(x) || isLabelAtRightBoundary(x))) {
            //绘制标签
            canvas?.drawPath(mLabelPathFactory.getLabelPath(mLabelShape, x, y), mLabelPaint)
            if (flag_stepByStep) {
                var progressRate = getProgressRate()
                progressRate -= progressRate.rem(1f / mSubsectionCount)
                val v =
                    numberFormatInstance.format(minProgress + progressRate * (maxProgress - minProgress))
                canvas?.drawText(v, x, adjustY, mLabelTxtPaint)
//                if(progressRate in 1f/mSubsectionCount..(1f-1/(2f*mSubsectionCount))){
//                    progressRate -= progressRate.rem(1f / mSubsectionCount)
//                    val v = numberFormatInstance.format(minProgress + progressRate*(maxProgress-minProgress))
//                    canvas?.drawText(v, x, adjustY, mLabelTxtPaint)
//                    return
//                }
//
//                if(progressRate < 1f/mSubsectionCount){
//                    canvas?.drawText(numberFormatInstance.format(minProgress).toString(), x, adjustY, mLabelTxtPaint)
//                    return
//                }
//                canvas?.drawText(numberFormatInstance.format(maxProgress).toString(),x,adjustY,mLabelPaint)

//                val v = floor(mProgress * mSubsectionCount).toInt() * (maxProgress - minProgress) / mSubsectionCount
//                canvas?.drawText(numberFormatInstance.format(v).toString(), x, adjustY, mLabelTxtPaint)
            } else {
                //绘制标签内部的进度值
                canvas?.drawText(getLabelText(x), x, adjustY, mLabelTxtPaint)
            }
            return
        }

        //边界处，需要校正横坐标
        var adjustX = x
        //如果当前处于左边界
        if (isLabelAtLeftBoundary(x)) {
            //调整进度值的横坐标
            // 计算规则：边框的宽度+左边距的一半 - 字体大小的一半
            if (mLabelShape == LABEL_SHAPE_RECTANGLE) {
                adjustX = mLeftBoundaryX + mRectangleHalfWidthBoundary - mProgressTextSize / 4
            } else {
                adjustX = mLeftBoundaryX + mLabelShapeConeH - mProgressTextSize / 2
            }
        }

        //如果当前处于右边界
        if (isLabelAtRightBoundary(x)) {
            //调整进度值的横坐标
            // 计算规则：当前的横坐标-边框宽度的一半 - 文字大小的1/4
            if (mLabelShape == LABEL_SHAPE_RECTANGLE) {
                adjustX = x - mRectangleHalfWidthBoundary - mProgressTextSize / 4
            } else {
                adjustX = x - mLabelShapeConeH

                /*
                //对于边界处理，如果你觉得平移的方案不满意，可以试一下旋转
                adjustX = x - mLabelShapeConeH
                canvas?.run {
                    this.save()
                    this.rotate(-30f,x,y)
                    this.drawPath(getLabelPath(adjustX, y), mLabelPaint)
                    //正常情况，直接绘制
                    this.drawText(getLabelText(x), adjustX, adjustY, mLabelTxtPaint)
                    this.restore()
                    return
                }*/
            }
        }

        //圆角矩形的标签框，底部的小三角角标可以自由移动，因此，不需要对横坐标进行校正
        if (mLabelShape == LABEL_SHAPE_RECTANGLE) {
            canvas?.drawPath(
                mLabelPathFactory.getLabelPath(LABEL_SHAPE_RECTANGLE, x, y),
                mLabelPaint
            )
        } else {
            //对于锥形，锥的顶部没办法像圆角矩形那样横向移动，因此，只能移动整个标签
            //所以需要使用校正位置后的横坐标
            canvas?.drawPath(
                mLabelPathFactory.getLabelPath(LABEL_SHAPE_CONE, adjustX, y),
                mLabelPaint
            )
        }


        if (flag_stepByStep) {

            if (mProgress > 1f - 1f / (2 * mSubsectionCount)) {
                canvas?.drawText(
                    numberFormatInstance.format(maxProgress).toString(),
                    adjustX,
                    adjustY,
                    mLabelPaint
                )
                return
            }

            if (mProgress <= 1f / mSubsectionCount) {
                canvas?.drawText(
                    numberFormatInstance.format(minProgress).toString(),
                    adjustX,
                    adjustY,
                    mLabelPaint
                )
                return
            }

            val v =
                floor(mProgress * mSubsectionCount).toInt() * (maxProgress - minProgress) / mSubsectionCount
            //绘制进度值
            canvas?.drawText(
                numberFormatInstance.format(v).toString(),
                adjustX,
                adjustY,
                mLabelTxtPaint
            )
        } else {
            //绘制进度值
            canvas?.drawText(getLabelText(x), adjustX, adjustY, mLabelTxtPaint)
        }
    }


    /**
     * path of shape which wrap progress-text
     * 包裹进度的路径
     */
    private var mLabelPath = Path()

    /**
     * height of triangle indicator at the bottom of rounded rectangle shape which wrap progress-text
     * 包裹进度值的圆角矩形的底部三角形指示器高度
     */
    private var mArrowH = mThumbRadius / 2

    /**
     *  half-width of rounded rectangle which wrap progress-text
     *  this value is used for judge right or left boundary
     *  program base on this value to judge if need to adjust location of the shape which wrap progress-text
     * 包裹进度值的矩形的一半宽度
     */
    private var mRectangleHalfWidthBoundary = 3f
        get() {
            val len = numberFormatInstance.format((maxProgress - minProgress)*1f/mSubsectionCount)
            return   max(len.length,maxProgress.toString().length)/2* mProgressTextSize
        }

    private val mLabelPathFactory by lazy { LabelPathFactory() }

    private inner class LabelPathFactory {
        /**
         * 获取当前标签的形状路径
         *
         * @param pathType 类型
         * @param x 当前进度的横坐标
         * @param y 当前进度的纵坐标
         *
         * @return 返回路径值。
         * 如果当前设置的是矩形，那么返回圆角路径
         * 如果当前设置的是锥形，那么返回锥形路径
         */
        fun getLabelPath(@LabelShape pathType: Int, x: Float, y: Float): Path {
            when (pathType) {
                LABEL_SHAPE_CONE -> {
                    return mLabelPath.apply {
                        this.reset()
                        this.moveTo(x, y)
                        this.addArc(
                            x - mLabelShapeConeH,
                            y - 2.5f * mLabelShapeConeH,
                            x + mLabelShapeConeH,
                            y - mLabelShapeConeH / 2,
                            30f,
                            -240f
                        )
                        this.lineTo(x, y)
                        this.close()
                    }
                }

                else -> {
                    /*
                     * 绘制圆角矩形
                     * 顺序为：右下->右上->左上->左下
                     */
                    return mLabelPath.apply {
                        this.reset()
                        //绘制三角形指示器
                        this.moveTo(x - mArrowH, y - mArrowH)
                        this.rLineTo(mArrowH, mArrowH * 0.75f)
                        this.rLineTo(mArrowH, -mArrowH * 0.75f)
                        mRectangleHalfWidthBoundary =
                            numberFormatInstance.format(maxProgress).length * mProgressTextSize / 2
                        var left = mRectangleHalfWidthBoundary
                        var right = left

                        //是否处于左边界
                        if (isLabelAtLeftBoundary(x)) {
                            //当前左边宽度，不足以容纳label显示，需要调整箭头和左右宽度
                            left = paddingStart.toFloat()
                            right = 2 * mRectangleHalfWidthBoundary - left
                        }

                        //如果不是右边界，则需要绘制右下圆角
                        if (!isLabelAtRightBoundary(x)) {
                            this.rLineTo(right - mArrowH / 4, 0f)
                            this.rQuadTo(mArrowH / 4, 0f, mArrowH / 4, -mArrowH / 4)
                        }

                        //右上圆角
                        this.rLineTo(0f, -mProgressTextSize * 1.5f - mArrowH / 4)
                        this.rQuadTo(0f, -mArrowH / 4, -mArrowH / 4, -mArrowH / 4)

                        //左上圆角
                        this.rLineTo(-2 * mRectangleHalfWidthBoundary - 1.5f * mArrowH, 0f)
                        this.rQuadTo(-mArrowH / 4, 0f, -mArrowH / 4, mArrowH / 4)

                        //左下圆角
                        this.rLineTo(0f, mProgressTextSize * 1.5f)
                        this.rQuadTo(0f, mArrowH / 4, mArrowH / 4, mArrowH / 4)
                        //闭合路径
                        this.close()
                    }
                }
            }
        }
    }


    /**
     *  shape of wrap progress-text
     * 当前进度值标签的形状
     */
    var mLabelShape = LABEL_SHAPE_RECTANGLE

    /**
     * available style of the shape which wrap progress-text
     * rounded-rectangle value is LABEL_SHAPE_RECTANGLE
     * cone value is LABEL_SHAPE_CONE
     * 可选的标签样式
     */
    @IntDef(LABEL_SHAPE_RECTANGLE, LABEL_SHAPE_CONE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class LabelShape {}


    /**
     * 锥形路径的底部三角形的高度
     * 这个参数会在绘制进度文本的时候用到
     * 因为针对不同的形状，重心不一样，所以，需要动态的对纵坐标调整
     * 这里的锥形三角形的高度取值:进度文本的字体大小的1.5倍再缩小0.65（取值无特殊意义，可自行调整）
     */
    private var mLabelShapeConeH = mProgressTextSize * 1.5f * 0.65f

    /**
     * 绘制标签时，当前位置是否处于右边界
     */
    private fun isLabelAtRightBoundary(x: Float): Boolean {
        if (mLabelShape == LABEL_SHAPE_RECTANGLE) {
            return (x >= mRightBoundaryX || (1 - mProgress) * mAvailableWidth <= mRectangleHalfWidthBoundary / 2) && paddingEnd < mRectangleHalfWidthBoundary
        }
        return x >= mRightBoundaryX && paddingEnd < mLabelShapeConeH
    }

    /**
     * 绘制标签时，当前位置是否处于左边界
     */
    private fun isLabelAtLeftBoundary(x: Float): Boolean {
        if (mLabelShape == LABEL_SHAPE_RECTANGLE) {
            return (x <= mLeftBoundaryX || mProgress * mAvailableWidth <= mRectangleHalfWidthBoundary / 2) && paddingStart < mRectangleHalfWidthBoundary
        }
        return x <= mLeftBoundaryX && paddingStart < mLabelShapeConeH
    }


    private fun getRealProgress(x: Float): Float {
        if (isLabelAtLeftBoundary(x)) {
            return minProgress
        }
        if (isLabelAtRightBoundary(x)) {
            return maxProgress
        }
        return minProgress + mProgress * (maxProgress - minProgress)
    }

    /**
     * 获取标签的文本
     * @param x 当前的位置横坐标
     */
    private fun getLabelText(x: Float) = String.format("%.2f", getRealProgress(x))

    /**
     * 绘制进度线条
     */
    private fun drawProgressLine(canvas: Canvas?) {
        val baseLineY = height - paddingBottom - mThumbRadius - 1.5f * mDegreeValueTxtSize
        canvas?.drawLine(
            mLeftBoundaryX,//X起始点，预留滑块的半径
            baseLineY,//Y起始点，预留滑块半径
            mRightBoundaryX,//X终点，预留滑块的半径
            baseLineY, mLineBgPaint
        )
        /*
        计算当前进度的终点横坐标
        如果按照 进度比*总体宽度计算，会有误差。为什么呢
        首先，两边如果留有边距，假设左边padding是50，滑动到70，实际上只滑动了20，但是获取坐标的时候，拿到的是70
        假设view的宽度是350，70在70/350 = 1/5处了。但实际上，手指滑动的只占了20/(350-50) = 1/15
        所以，我这里采用比例。把在整体上滑动的距离，映射到可用的距离上
        可用的距离是什么？即除去左右的padding，以及代码预留的绘制间距，比如，应用内，为了容纳端点处完整显示滑动，所以这里的可用距离
        要减去左右的两边预留的滑块半径。之后呢，整体滑动了1/5,这时，我把距离映射到可用距离上，也就是滑动了1/5
        最终的定位就是：左边的边距 +进度*可用的宽度 = paddingLeft+预留的半径 + 可用宽度*进度
        */
        val endX = mLeftBoundaryX + mAvailableWidth * mProgress
        canvas?.drawLine(mLeftBoundaryX, baseLineY, endX, baseLineY, mLinePaint)
    }

    /**
     * 拖动滑块时，手指按下的点
     */
    private var mFingerDownPoint = PointF()

    /**
     * states of current thumb is idle、pressing or dragging
     * 标记当前的滑动状态
     */
    private var flag_states = STATES_IDLE

    companion object {
        /**
         * 无按压，无拖动，初始状态
         */
        private const val STATES_IDLE = 1
        /**
         * 按压或者正在拖动
         */
        private const val STATES_DRAGGING = 2

        /**
         * 标签样式：矩形
         */
        const val LABEL_SHAPE_RECTANGLE = 0
        /**
         * 标签样式：圆锥
         */
        const val LABEL_SHAPE_CONE = 1

    }


    override fun performClick(): Boolean = super.performClick()

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                performClick()
                if (flag_limitTouchRange && !isInTouchAround(event)) return false
                //手指按下，请求父容器不要拦截touch事件
                parent.requestDisallowInterceptTouchEvent(true)
                //修改标记为按压/拖动状态
                flag_states = flag_states.shl(1)
                //记录初始的拖动位置
                mFingerDownPoint.set(event.x, event.y)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                //左边界处理
                if (event.x / width <= 0) {
                    mProgress = 0f
                    postInvalidate()
                    return true
                }

                //右边界处理
                if (event.x / width > 1f) {
                    mProgress = 1f
                    postInvalidate()
                    return true
                }

                //计算当前的比例
                mProgress = event.x / width
                postInvalidate()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
                flag_states = flag_states.shr(1)
                when {
                    event.x / width <= 0f -> {
                        mProgress = 0f
                    }

                    event.x >= width -> {
                        mProgress = 1f
                    }

                    //设置自动跳转到最近的点，并且分段要大于等于2
                    //等分数小于2，其实都不用绘制了
                    flag_autoSeekBoundary -> {
                        reBoundaryProgress()
                    }

//                    flag_stepByStep ->{
//                        if(mProgress >= 1-1/(2f*mSubsectionCount)){
//                            mProgress = 1f
//                        } else if(mProgress <= 1f/mSubsectionCount){
//                            mProgress = 1f/mSubsectionCount
//                        }
//
//                        reBoundaryProgress()
//                    }

//                    flag_autoSeekBoundary || flag_stepByStep -> {
//                        reBoundaryProgress()
//                    }
                }
                postInvalidate()
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * 是否在触摸范围内
     */
    private fun isInTouchAround(event: MotionEvent): Boolean {
        return event.x in mLeftBoundaryX..mRightBoundaryX &&//判断x边界
                event.y in height - paddingBottom - mThumbRadius - 1.5f * mDegreeValueTxtSize..height - paddingBottom.toFloat() //判断y边界
    }

    /**
    对进度进行校正回弹
    弹性处理
    如果当前位置在区间中，大于区间一半，则滑动到下一个点，小于区间一半，则滑动到上一个点
    计算规则：
    当前的进度为p，等分的区间段，每个区间占比r
    p取余r 得到剩下的部分dr
    比较dr与r，如果dr>r/2，那么就要再多滑动，小于的话，就回弹
    多滑动：即当前的值与下一个值的差值就是需要多滑的部分
    回弹：即当前的值与前一个值的差值，就是需要回弹的部分
    eg: 当前进度p= 0.73
    等分了5份，每份就是1/5,即r = 1/5
    dr = p%r = 0.73%(1/5) = 0.73%0.2 = 0.13

    dr >= r/2 即，0.13 > 0.1所以需要多滑
    即，当前的进度p，需要补齐剩下的部分差值
    差值dw = r - dr = 0.2- 0.13 = 0.07
    p+dw = 0.73 + 0.07 = 0.8 此时，0.8是整除r= 0.2的。所以就“多滑选定了”

    这里也可以直接对p向上取整，这样0.73取整到0.8也可以，当然了，需要考虑小数位
    比如，Math.ceil(0.73*10)/10 = 0.8

    同理的，当前进度如果是0.63
    dr = 0.63%0.2 = 0.03
    dr < r/2 即，0.03 < 0.2所以需要回弹
    即当前的进度，需要减去多余的那部分dr
    即：p - dr = 0.63- 0.03 = 0.6可以整除 0.2
    这样之后，表现出来就是回弹到最近的整数

    这里也可以直接对p向下取整，这样0.63取整到0.6也可以
    比如，Math.fool(0.63*10)/10 = 0.6
     */
    private fun reBoundaryProgress() {
        var tempSubCount = mSubsectionCount
        if (tempSubCount <= 1) {
            //如果没有分段，但是用户又设置了自动回弹，那么默认分段的计算规则：
            //以当前最小进度的一半作为最小的步长，之后，以总的进度除以步长，即可得到默认的分段数
            tempSubCount = floor((maxProgress - minProgress) / (minProgress / 2)).toInt()
        }

        val round = round(10000 * mProgress.rem(1f / tempSubCount)) / 10000f
        if (round >= 1f / (2 * tempSubCount)) {
            mProgress += (1f / tempSubCount - round)
        } else {
            mProgress -= round
        }
    }

    fun updateProgress(progress: Float) {
        if (progress > maxProgress || progress < minProgress) {
            throw IllegalArgumentException("非法的进度值！\n设定的进度取值范围[$minProgress,$maxProgress],当前的设定值：$progress!")
        }
        transFromProgress(progress)
        postInvalidate()
//        updateProgressByStep(progress)
    }

    /**
     * 标记：当前动画是否正在运行
     */
    private var flag_isUpdateAnimRunning = AtomicBoolean(false)

    /**
     * 更新进度
     * @param toProgress 目标进度值
     */
    private fun updateProgressByStep(toProgress: Float) {
        if (flag_isUpdateAnimRunning.compareAndSet(false, true)) {
            ValueAnimator.ofFloat(mProgress, toProgress).apply {
                this.duration = 500
                this.interpolator = LinearInterpolator()
                this.addUpdateListener {
                    mProgress = (it.animatedValue as Float).toFloat()
                    postInvalidate()
                }
                this.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        flag_isUpdateAnimRunning.compareAndSet(false, true)
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        flag_isUpdateAnimRunning.compareAndSet(true, false)
                    }
                })
            }.start()
        }
    }

    private var mListener: (Float) -> Unit = {}
    /**
     * 设置接收进度改变的监听
     * @param listener 进度监听回调,接收当前进度值
     */
    fun setProgressChangedListener(listener: (Float) -> Unit) {
        mListener = listener
    }
}


fun VisionProgressBar.build(block:VisionProgressBarBuilder.() ->Unit):VisionProgressBar = VisionProgressBarBuilder().apply(block).build(this)

class VisionProgressBarBuilder{

    //最小的刻度值
    var minProgress = 0f
    //最大刻度值
    var maxProgress = 9f
    //整个刻度分为几段
    var subsectionCount = 8

    //进度条的高度，不是seekBar的高度
    var progressLineHeight = 8f

    //刻度值字体颜色
    @ColorInt
    var degreeValueTxtColor = Color.YELLOW

    //刻度值字体大小
    var degreeValueTxtSize = 32f

    //进度值的字体大小
    var progressTextSize = 16f
    //进度值字体颜色
    @ColorInt
    var progressTextColor = Color.YELLOW
    //进度条颜色
    @ColorInt
    var progressColor = Color.YELLOW

    //二级进度条颜色
    @ColorInt
    var secondProgressColor = Color.DKGRAY

    //滑块的颜色
    @ColorInt
    var thumbColor = Color.YELLOW

    //滑块的描边颜色
    @ColorInt
    var thumbBorderColor = Color.YELLOW

    //滑块的半径
    var thumbRadius = 25f

    //滑块的边框半径
    var thumbBorderRadius = 6f

    //包裹进度条的边框颜色
    var wrapLabelBorderColor = Color.YELLOW

    //包裹进度条的边框样式：描边
    var wrapLabelStyle = Paint.Style.STROKE

    //包裹进度条的边框的形状：矩形
    var wrapLabelShape = VisionProgressBar.LABEL_SHAPE_RECTANGLE

    //边界处是否自动回弹到最近的值
    var isAutoSeekBoundary = true
    //显示刻度
    var isShowDegree = true
    //显示刻度值
    var isShowDegreeText = true
    //是否分段选中
    var isStepByStep = false

    fun build(seekBar: VisionProgressBar):VisionProgressBar{
        return seekBar.apply {
            //最小的刻度值
            this.minProgress = this@VisionProgressBarBuilder.minProgress
            //最大刻度值
            this.maxProgress = this@VisionProgressBarBuilder.maxProgress
            //整个刻度分为几段
            this.mSubsectionCount = this@VisionProgressBarBuilder.subsectionCount

            //刻度值字体颜色
            this.mDegreeValueTxtColor = this@VisionProgressBarBuilder.degreeValueTxtColor
            //刻度值字体大小
            this.mDegreeValueTxtSize = this@VisionProgressBarBuilder.degreeValueTxtSize

            //进度值的字体大小
            this.mProgressTextSize = this@VisionProgressBarBuilder.progressTextSize
            //进度值字体颜色
            this.mProgressTextColor = this@VisionProgressBarBuilder.progressTextColor
            //进度条颜色
            this.mProgressColor = this@VisionProgressBarBuilder.progressColor
            //二级进度条颜色
            this.mSecondProgressColor = this@VisionProgressBarBuilder.secondProgressColor
            //进度条的高度，不是seekBar的高度
            this.mProgressLineHeight = this@VisionProgressBarBuilder.progressLineHeight

            //滑块的颜色
            this.mThumbColor = this@VisionProgressBarBuilder.thumbColor
            //滑块的描边颜色
            this.mThumbBorderColor = this@VisionProgressBarBuilder.thumbBorderColor
            //滑块的半径
            this.mThumbRadius = this@VisionProgressBarBuilder.thumbRadius
            //滑块的边框半径
            this.mIhumbBorderRadius = this@VisionProgressBarBuilder.thumbBorderRadius

            //包裹进度条的边框颜色
            this.mLabelColor = this@VisionProgressBarBuilder.wrapLabelBorderColor
            //包裹进度条的边框样式：描边
            this.mLabelStyle = this@VisionProgressBarBuilder.wrapLabelStyle
            //包裹进度条的边框的形状：矩形
            this.mLabelShape = this@VisionProgressBarBuilder.wrapLabelShape

            //边界处是否自动回弹到最近的值
            this.flag_autoSeekBoundary = this@VisionProgressBarBuilder.isAutoSeekBoundary
            //显示刻度
            this.flag_showDegree = this@VisionProgressBarBuilder.isShowDegree
            //显示刻度值
            this.flag_showDegreeText = this@VisionProgressBarBuilder.isShowDegreeText
            //是否分段选中
            this.flag_stepByStep = this@VisionProgressBarBuilder.isStepByStep
        }
    }



}




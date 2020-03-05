package com.vision.seekbarsimple

import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.vision.seekbar.VisionProgressBar
import com.vision.seekbar.build
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //使用build模式初始化seekBar参数的话
        // 你可以不使用“this”关键字来给seekBar的参数赋值
        //init seekBar by build mode
        //you can initialise parameters without using "this" keyword by build mode
        initSeekBarByBuild()

        //直接使用seekBar对象初始化参数
        //other way to init seekBar
//        initSeekBar()

        //获取进度百分比
        btnGetProgressRate.setOnClickListener {
            tvProgressRate.text = visionSeekBar.getProgressRate().toString()
        }

        //获取进度值
        btnGetProgressValue.setOnClickListener {
            tvProgressValue.text = visionSeekBar.getProgressValue().toString()
        }



    }

    private fun initSeekBarByBuild() {
        visionSeekBar.build {
            //最小的刻度值
            minProgress = 1f
            //最大的刻度值
            maxProgress = 9f
            //整个刻度分为几段
            subsectionCount = 8

            //进度条的颜色
            progressColor = resources.getColor(R.color.progress_color)
            //二级进度条的颜色
            secondProgressColor = resources.getColor(R.color.secondary_progress_color)
            //进度条的高度，不是seekBar的高度
            progressLineHeight = 8f
            //当前进度值的文字大小
            progressTextSize = 16f
            //当前进度值的文字颜色
            progressTextColor = progressColor

            //刻度值的文字颜色
            degreeValueTxtColor = progressColor
            //刻度值的文字大小
            degreeValueTxtSize = 32f

            //滑块的颜色
            thumbColor = progressColor
            //滑块的边框颜色
            thumbBorderColor = progressColor
            //滑块的半径
            thumbRadius = 25f
            //滑块的描边半径
            thumbBorderRadius = 6f

            //包裹进度值边框的颜色
            wrapLabelBorderColor = progressColor
            //包裹进度值边框的形状：矩形
            wrapLabelShape = VisionProgressBar.LABEL_SHAPE_RECTANGLE
            //包裹进度值边框的样式：描边
            wrapLabelStyle = Paint.Style.STROKE

            //边界处是否自动回弹到最近的值
            isAutoSeekBoundary = true
            //显示刻度
            isShowDegree = true
            //显示刻度值
            isShowDegreeText = true
            //是否分段选中
            isStepByStep = false
        }.updateProgress(3.65f)
    }

    private fun initSeekBar() {
        visionSeekBar.apply {
            //最小的刻度值
            this.minProgress = 0f
            //最大刻度值
            this.maxProgress = 9f
            //整个刻度分为几段
            this.mSubsectionCount = 8

            //进度条的高度，不是seekBar的高度
            this.mProgressLineHeight = 8f

            //刻度值字体颜色
            this.mDegreeValueTxtColor = resources.getColor(R.color.progress_color)
            //刻度值字体大小
            this.mDegreeValueTxtSize = 32f

            //进度值的字体大小
            this.mProgressTextSize = 16f
            //进度值字体颜色
            this.mProgressTextColor = resources.getColor(R.color.progress_color)
            //进度条颜色
            this.mProgressColor = resources.getColor(R.color.progress_color)
            //二级进度条颜色
            this.mSecondProgressColor = resources.getColor(R.color.secondary_progress_color)
            //进度条的高度
            this.mProgressLineHeight = 5.5f

            //滑块的颜色
            this.mThumbColor = Color.YELLOW
            //滑块的描边颜色
            this.mThumbBorderColor = resources.getColor(R.color.progress_color)
            //滑块的半径
            this.mThumbRadius = 25f
            //滑块的边框半径
            this.mIhumbBorderRadius = 6f

            //包裹进度条的边框颜色
            this.mLabelColor = resources.getColor(R.color.progress_color)
            //包裹进度条的边框样式：描边
            this.mLabelStyle = Paint.Style.STROKE
            //包裹进度条的边框的形状：矩形
            this.mLabelShape = VisionProgressBar.LABEL_SHAPE_RECTANGLE

            //边界处是否自动回弹到最近的值
            this.flag_autoSeekBoundary = true
            //显示刻度
            this.flag_showDegree = true
            //显示刻度值
            this.flag_showDegreeText = true
            //是否分段选中
            this.flag_stepByStep = false
        }.updateProgress(3.65f)//刷新进度值到3.65处
    }
}

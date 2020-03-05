# __自定义SeekBar__

## 效果图
![image](https://github.com/VisionFor/VisionSeekBar/raw/master/screen-img/pic1.jpg)

# 初始化

## xml布局中使用示例： 
~~~
<com.vision.seekbar.VisionProgressBar  
        android:id="@+id/visionSeekBar"  
        android:layout_width="0dp"  
        android:layout_height="wrap_content"  
        app:layout_constraintEnd_toEndOf="parent"  
        app:layout_constraintStart_toStartOf="parent"  
        app:layout_constraintTop_toTopOf="parent"  
        android:layout_marginStart="24dp"  
        android:layout_marginTop="10dp"  
        android:layout_marginEnd="24dp"  
        app:degreeTextSize="14sp" // 刻度值文字大小  
        app:isAutoSeekBoundary="false" //边界处是否自动回弹到最近的值  
        app:limitTouchRange="false" //是否限制seekBar的触摸范围  
        app:maxProgress="15" //最大进度值  
        app:minProgress="0" //最小进度值  
        app:progress="1.35" //当前的进度值  
        app:progressColor="@color/yellow" //进度条颜色  
        app:progressLabelColor="@color/blue" //包裹进度值的文本框颜色  
        app:progressLabelShape="rectangle" //包裹进度值边框的形状：矩形  
        app:progressLabelStyle="stork" //包裹进度值边框的样式：描边  
        app:progressTextColor="@android:color/white" //当前进度值的文字颜色  
        app:progressTextSize="12sp"  //当前进度值的文字大小  
        app:secondaryProgressColor="@android:color/white" //二级进度条的颜色  
        app:showDegree="true" //显示刻度  
        app:showDegreeText="true" //显示刻度值  
        app:subsectionCount="10"  //整个刻度分为几段  
        app:isStepByStep="false" //是否分段选中  
        app:thumbBorderColor="@color/yellow"  //滑块的边框颜色  
        app:thumbBorderRadius="7dp" //滑块的描边半径  
        app:thumbColor="@android:color/white"//滑块的颜色  
        app:thumbRadius="15dp" //滑块的半径   
        />
~~~
   ## 代码中进行初始化  
   ### 1、使用build模式初始化，这种方式初始化，你可以不使用“this”关键字来给seekBar的参数赋值  
   ### 示例如下：  
~~~
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
            thumbColor = Color.YELLOW
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
        }.updateProgress(3.65f)//更新进度值
 ~~~
   ### 2、直接初始化seekBar的参数  
   ### 示例如下： 
 ~~~
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
 ~~~
 
 # 监听事件
 ## 添加进度改变监听
~~~
visionSeekBar.setProgressChangedListener {
            println("当前进度值：$it")
        }
~~~

## 获取当前的进度百分比
~~~ 
visionSeekBar.getProgressRate() 
~~~

## 获取进度值
~~~ 
visionSeekBar.getProgressValue()
~~~

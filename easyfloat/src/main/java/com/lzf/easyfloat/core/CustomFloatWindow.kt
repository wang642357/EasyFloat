package com.lzf.easyfloat.core

import android.R
import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.*
import com.lzf.easyfloat.data.FloatConfig
import com.lzf.easyfloat.data.Position
import com.lzf.easyfloat.enums.ShowPattern
import com.lzf.easyfloat.interfaces.OnFloatTouchListener
import com.lzf.easyfloat.utils.DisplayUtils
import com.lzf.easyfloat.utils.LifecycleUtils

/**
 * 作者：wangjianxiong
 * 创建时间：2021/8/27
 *
 * 依附于Activity上面的window
 */
class CustomFloatWindow(context: Context, config: FloatConfig) : BaseFloatWindow(context, config) {

    private var floatCustomView: View? = null
    private var params: ViewGroup.LayoutParams? = null
    private var parentParams: ViewGroup.LayoutParams? = null
    private var lastLayoutMeasureWidth = -1
    private var lastLayoutMeasureHeight = -1

    override fun createWindow(): Boolean {
        val activity = if (context is Activity) context else LifecycleUtils.getTopActivity()
        initLayoutParams()
        addView(activity)
        return true
    }

    private fun initLayoutParams() {
        val width = if (config.widthMatch) {
            ViewGroup.LayoutParams.MATCH_PARENT
        } else {
            ViewGroup.LayoutParams.WRAP_CONTENT
        }
        var height = if (config.heightMatch) {
            ViewGroup.LayoutParams.MATCH_PARENT
        } else {
            ViewGroup.LayoutParams.WRAP_CONTENT
        }
        if (config.immersionStatusBar && config.heightMatch) {
            height = DisplayUtils.getScreenHeight(context)
        }
        params = ViewGroup.LayoutParams(width, height)
        parentParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun addView(activity: Activity?) {
        frameLayout.tag = config.floatTag
        val floatView = config.layoutView
        floatCustomView =
            floatView ?: LayoutInflater.from(context).inflate(config.layoutId!!, frameLayout, false)
        frameLayout.addView(floatCustomView, params)
        val position = Position(frameLayout.x.toInt(), frameLayout.y.toInt())
        frameLayout.touchListener = object : OnFloatTouchListener {
            override fun onTouch(event: MotionEvent) {
                touchUtils.updateFloat(frameLayout, event, position) {
                    frameLayout.x = position.x.toFloat()
                    frameLayout.y = position.y.toFloat()
                }
            }
        }

        setGravity(frameLayout)
        lastLayoutMeasureWidth = frameLayout.measuredWidth
        lastLayoutMeasureHeight = frameLayout.measuredHeight
        config.apply {
            // 如果设置了过滤当前页，或者后台显示前台创建、前台显示后台创建，隐藏浮窗，否则执行入场动画
            if (filterSelf) {
                dismiss(false)
                //setVisible(View.GONE)
                //initEditText()
            } else {
                show(activity)
            }

            // 设置callbacks
            layoutView = floatView
            invokeView?.invoke(floatView)
            callbacks?.createdResult(true, null, floatView)
            floatCallbacks?.builder?.createdResult?.invoke(true, null, floatView)
        }
    }

    private fun setGravity(view: View?) {
        if (config.locationPair != Pair(0, 0) || view == null) return
        val parentRect = Rect()
        // 获取浮窗所在的矩形
        frameLayout.getDrawingRect(parentRect)
        //windowManager.defaultDisplay.getRectSize(parentRect)
        val location = IntArray(2)
        // 获取在整个屏幕内的绝对坐标
        val params = Position(0, 0)
        view.getLocationOnScreen(location)
        // 通过绝对高度和相对高度比较，判断包含顶部状态栏
        val statusBarHeight = if (location[1] > params.y) DisplayUtils.statusBarHeight(view) else 0
        val parentBottom =
            config.displayHeight.getDisplayRealHeight(context) - statusBarHeight
        when (config.gravity) {
            // 右上
            Gravity.END, Gravity.END or Gravity.TOP, Gravity.RIGHT, Gravity.RIGHT or Gravity.TOP ->
                params.x = parentRect.right - view.width
            // 左下
            Gravity.START or Gravity.BOTTOM, Gravity.BOTTOM, Gravity.LEFT or Gravity.BOTTOM ->
                params.y = parentBottom - view.height
            // 右下
            Gravity.END or Gravity.BOTTOM, Gravity.RIGHT or Gravity.BOTTOM -> {
                params.x = parentRect.right - view.width
                params.y = parentBottom - view.height
            }
            // 居中
            Gravity.CENTER -> {
                params.x = (parentRect.right - view.width).shr(1)
                params.y = (parentBottom - view.height).shr(1)
            }
            // 上中
            Gravity.CENTER_HORIZONTAL, Gravity.TOP or Gravity.CENTER_HORIZONTAL ->
                params.x = (parentRect.right - view.width).shr(1)
            // 下中
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL -> {
                params.x = (parentRect.right - view.width).shr(1)
                params.y = parentBottom - view.height
            }
            // 左中
            Gravity.CENTER_VERTICAL, Gravity.START or Gravity.CENTER_VERTICAL, Gravity.LEFT or Gravity.CENTER_VERTICAL ->
                params.y = (parentBottom - view.height).shr(1)
            // 右中
            Gravity.END or Gravity.CENTER_VERTICAL, Gravity.RIGHT or Gravity.CENTER_VERTICAL -> {
                params.x = parentRect.right - view.width
                params.y = (parentBottom - view.height).shr(1)
            }
            // 其他情况，均视为左上
            else -> {
            }
        }

        // 设置偏移量
        params.x += config.offsetPair.first
        params.y += config.offsetPair.second

        if (config.immersionStatusBar) {
            if (config.showPattern != ShowPattern.CURRENT_ACTIVITY) {
                params.y -= statusBarHeight
            }
        } else {
            if (config.showPattern == ShowPattern.CURRENT_ACTIVITY) {
                params.y += statusBarHeight
            }
        }
        // 更新浮窗位置信息
        frameLayout.x = params.x.toFloat()
        frameLayout.y = params.y.toFloat()
        //windowManager.updateViewLayout(view, params)
    }

    override fun show(activity: Activity?) {
        if (activity == null || activity.isFinishing) {
            return
        }
        if (floatCustomView == null) {
            return
        }
        val parent: ViewParent? = frameLayout.parent
        if (parent != null) {
            (parent as ViewGroup).removeView(frameLayout)
        }
        (activity.findViewById<View>(R.id.content) as ViewGroup).addView(frameLayout, parentParams)
    }

    override fun hide(activity: Activity?) {
        if (activity == null || activity.isFinishing) {
            return
        }
        if (floatCustomView == null) {
            return
        }
        (activity.findViewById<View>(R.id.content) as ViewGroup).removeView(frameLayout)
    }

    override fun dismiss(anim: Boolean, activity: Activity?) {
        if (activity == null || activity.isFinishing) {
            return
        }
        FloatingWindowManager.remove(config.floatTag)
        (activity.findViewById<View>(R.id.content) as ViewGroup).removeView(frameLayout)
    }

    override fun checkShow(activity: Activity) {
        if (activity.isFinishing || activity.isDestroyed) {
            return
        }
        when {
            // 当前页面的浮窗，不需要处理
            config.showPattern == ShowPattern.CURRENT_ACTIVITY -> return
            // 如果没有手动隐藏浮窗，需要考虑过滤信息
            config.needShow -> {
                if (activity.componentName.className !in config.filterSet) {
                    show(activity)
                } else {
                    hide(activity)
                }
            }
        }
    }

    override fun checkDismiss(activity: Activity) {
        if (activity.isFinishing || activity.isDestroyed) {
            return
        }
        if (config.showPattern == ShowPattern.CURRENT_ACTIVITY) {
            dismiss(false, activity)
        } else {
            hide(activity)
        }
    }

    override fun updateFloat(x: Int, y: Int) {

    }
}
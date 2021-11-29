package com.lzf.easyfloat.core

import android.app.Activity
import android.content.Context
import com.lzf.easyfloat.data.FloatConfig
import com.lzf.easyfloat.enums.WindowType
import com.lzf.easyfloat.widget.ParentFrameLayout

/**
 * 作者：wangjianxiong
 * 创建时间：2021/8/27
 *
 *
 */
abstract class BaseFloatWindow(val context: Context, val config: FloatConfig) : FloatWindow {

    internal val frameLayout: ParentFrameLayout =
        ParentFrameLayout(context.applicationContext, config)

    internal val touchUtils: TouchUtils = TouchUtils(context.applicationContext, config)

    fun getWindowType(): WindowType {
        return config.windowType
    }

    abstract fun checkShow(activity: Activity)

    abstract fun checkDismiss(activity: Activity)

    abstract fun updateFloat(x: Int, y: Int)
}
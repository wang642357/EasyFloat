package com.lzf.easyfloat.core

import android.app.Activity

/**
 * 作者：wangjianxiong
 * 创建时间：2021/8/27
 *
 *
 */
interface FloatWindow {

    fun createWindow(): Boolean

    fun show(activity: Activity? = null)

    fun hide(activity: Activity? = null)

    fun dismiss(anim: Boolean = false, activity: Activity? = null)
}
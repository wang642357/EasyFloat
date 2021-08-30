package com.lzf.easyfloat.enums

/**
 * 作者：wangjianxiong
 * 创建时间：2021/8/27
 *
 * 窗口类型
 */
enum class WindowType {
    /**
     * 依附于Activity Content上面的自定义Window
     */
    CUSTOM_WINDOW,

    /**
     * 通过WindowManager创建的window
     */
    SYSTEM_WINDOW
}
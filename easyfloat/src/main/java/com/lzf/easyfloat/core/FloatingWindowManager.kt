package com.lzf.easyfloat.core

import android.app.Activity
import android.content.Context
import com.lzf.easyfloat.WARN_REPEATED_TAG
import com.lzf.easyfloat.data.FloatConfig
import com.lzf.easyfloat.enums.WindowType
import com.lzf.easyfloat.utils.Logger
import java.util.concurrent.ConcurrentHashMap

/**
 * @author: Liuzhenfeng
 * @date: 12/1/20  23:36
 * @Description:
 */
internal object FloatingWindowManager {

    private const val DEFAULT_TAG = "default"
    val windowMap = ConcurrentHashMap<String, BaseFloatWindow>()
    /**
     * 创建浮窗，tag不存在创建，tag存在创建失败
     * 创建结果通过tag添加到相应的map进行管理
     */
    fun create(context: Context, config: FloatConfig) {
        if (!checkTag(config)) {
            val helper = if (config.windowType == WindowType.CUSTOM_WINDOW) {
                CustomFloatWindow(context, config)
            } else {
                SystemFloatWindow(context, config)
            }
            if (helper.createWindow()) windowMap[config.floatTag!!] = helper
        } else {
            // 存在相同的tag，直接创建失败
            config.callbacks?.createdResult(false, WARN_REPEATED_TAG, null)
            config.floatCallbacks?.builder?.createdResult?.invoke(false, WARN_REPEATED_TAG, null)
            Logger.w(WARN_REPEATED_TAG)
        }
    }

    /**
     * 关闭浮窗，执行浮窗的退出动画
     */
    fun dismiss(tag: String? = null, activity: Activity? = null) =
        getWindow(tag)?.dismiss(true, activity)

    /**
     * 隐藏浮窗
     */
    fun hide(tag: String? = null, activity: Activity? = null) = getWindow(tag)?.hide(activity)

    /**
     * 移除当条浮窗信息，在退出完成后调用
     */
    fun remove(floatTag: String?) = windowMap.remove(getTag(floatTag))

    /**
     * 设置浮窗的显隐，用户主动调用隐藏时，needShow需要为false
     */
    fun visible(
        isShow: Boolean,
        tag: String? = null,
        needShow: Boolean = windowMap[tag]?.config?.needShow ?: true
    ) {
        if (isShow) {
            getWindow(tag)?.show()
        } else {
            getWindow(tag)?.dismiss()
        }
        //getWindow(tag)?.setVisible(if (isShow) View.VISIBLE else View.GONE, needShow)
    }

    fun show(activity: Activity, tag: String? = null) {
        getWindow(tag)?.show(activity)
    }

    fun dismiss(showExitAnim: Boolean, activity: Activity, tag: String? = null) {
        getWindow(tag)?.dismiss(showExitAnim, activity)
    }

    /**
     * 检测浮窗的tag是否有效，不同的浮窗必须设置不同的tag
     */
    private fun checkTag(config: FloatConfig): Boolean {
        // 如果未设置tag，设置默认tag
        config.floatTag = getTag(config.floatTag)
        return windowMap.containsKey(config.floatTag!!)
    }

    /**
     * 获取浮窗tag，为空则使用默认值
     */
    private fun getTag(tag: String?) = tag ?: DEFAULT_TAG

    /**
     * 获取具体的系统浮窗管理类
     */
    fun getWindow(tag: String?) = windowMap[getTag(tag)]

}
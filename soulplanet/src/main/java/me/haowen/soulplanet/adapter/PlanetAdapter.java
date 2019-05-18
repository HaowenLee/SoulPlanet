package me.haowen.soulplanet.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * 星球云的适配器
 */
public abstract class PlanetAdapter {

    /**
     * 数据改变监听
     */
    private OnDataSetChangeListener onDataSetChangeListener;

    /**
     * 星球（标签）个数
     *
     * @return 星球（标签）个数
     */
    public abstract int getCount();

    /**
     * 获取标签的View
     *
     * @param context  上下文
     * @param position 位置
     * @param parent   父布局
     * @return 标签的View
     */
    public abstract View getView(Context context, int position, ViewGroup parent);

    /**
     * 获取Item
     *
     * @param position 位置
     * @return Item
     */
    public abstract Object getItem(int position);

    /**
     * 获取标签的权重
     *
     * @param position 位置
     * @return 标签的权重
     */
    public abstract int getPopularity(int position);

    /**
     * 主题颜色改变
     *
     * @param view       视图
     * @param themeColor 主题色
     */
    public abstract void onThemeColorChanged(View view, int themeColor);

    /**
     * 数据更新
     */
    public final void notifyDataSetChanged() {
        if (onDataSetChangeListener == null) {
            return;
        }
        onDataSetChangeListener.onChange();
    }

    /**
     * 设置数据改变监听
     *
     * @param listener 数据改变监听器
     */
    public void setOnDataSetChangeListener(OnDataSetChangeListener listener) {
        onDataSetChangeListener = listener;
    }

    /**
     * 数据改变监听器
     */
    public interface OnDataSetChangeListener {
        /**
         * 数据改变
         */
        void onChange();
    }
}
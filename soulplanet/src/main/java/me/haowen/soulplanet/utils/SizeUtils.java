package me.haowen.soulplanet.utils;

import android.content.Context;

public class SizeUtils {

    /**
     * dp转px
     *
     * @param context 上下文
     * @param dp      dp值
     * @return px值
     */
    public static int dp2px(Context context, float dp) {
        return (int) ((dp * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    /**
     * sp转px
     *
     * @param context 上下文
     * @param sp      sp值
     * @return px值
     */
    public static int sp2px(Context context, float sp) {
        return (int) ((sp * context.getResources().getDisplayMetrics().scaledDensity) + 0.5f);
    }
}

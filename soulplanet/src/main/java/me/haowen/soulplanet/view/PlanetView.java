package me.haowen.soulplanet.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.View;

import me.haowen.soulplanet.utils.SizeUtils;

/**
 * 星球（单个）
 */
public class PlanetView extends View {

    public static final int COLOR_MALE = 0xC1F1F2;
    public static final int COLOR_FEMALE = 0xFBD8D8;
    public static final int COLOR_MOST_ACTIVE = 0xFFFFFF;
    public static final int COLOR_MOST_NEW = 0x9485D8;
    public static final int COLOR_BEST_MATCH = 0x58B4AC;
    private float shadowRadius = -1.0f;
    private boolean isEnlarge = false;
    private float radiusIncrement = 1.0f;
    private float signY;
    private float signWidth;
    private float totalSignWidth = 0.0f;
    private float maxSignRange = 0.0f;
    private float signX;
    private float matchPercentX;
    private float matchPercentY;
    private float matchDescribeX;
    private float matchDescribeY;
    private float signDistanceX = 5.0f;
    private Paint starPaint;
    private Paint signPaint;
    private Paint otherPaint;
    private Paint matchPaint;
    private int starWidth;
    private int starMarginTop;
    private int signTextSize;
    private int matchTextSize;
    private float scale;
    private int starColor;
    private int matchColor;
    private boolean isOverstep;
    private String sign;
    private String matchPercent;
    private String matchDescribe;
    private boolean hasShadow;
    private float starCenterX;
    private float starCenterY;
    private float starRadius;
    private float starMin;

    public PlanetView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        starPaint.setColor(0xFFFF0000);
        signTextSize = SizeUtils.sp2px(context, 9.0f);
        signPaint = new Paint(Paint.HINTING_ON);
        signPaint.setColor(0x7FEEEEEE);
        signPaint.setTextSize((float) signTextSize);
        otherPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        otherPaint.setColor(0x7FEEEEEE);
        otherPaint.setTextSize((float) SizeUtils.sp2px(context, 9.0f));
        matchPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        matchPaint.setColor(0xFFFFFFFF);
        matchTextSize = SizeUtils.sp2px(context, 6.0f);
        matchPaint.setTextSize((float) matchTextSize);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        int startX = SizeUtils.sp2px(context, 50.0f);
        starWidth = SizeUtils.sp2px(context, 20.0f);
        starMarginTop = 0;
        signPaint.setShader(new LinearGradient((float) startX, 0.0f, 0.0f, 0.0f,
                new int[]{0x33333333, 0xFFFFFFFF, 0xFFFFFFFF, 0x33333333},
                new float[]{0.0f, 0.15f, 0.85f, 1.0f}, TileMode.CLAMP));
        otherPaint.setShader(new LinearGradient((float) startX, 0.0f, 0.0f, 0.0f,
                new int[]{0x33333333, 0xFF888888, 0xFF888888, 0x33333333},
                new float[]{0.0f, 0.15f, 0.85f, 1.0f}, TileMode.CLAMP));
        starMin = starWidth * 3.0f / 4.0f;
        starRadius = starWidth - starMin;
        starMin -= 3.0f;
        shadowRadius = starMin;
        radiusIncrement = starMin / 16.0f;
    }

    public PlanetView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public PlanetView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * 设置星星颜色
     *
     * @param color 颜色
     */
    public void setStarColor(int color) {
        this.starColor = color;
    }

    /**
     * 设置匹配颜色
     *
     * @param color 颜色
     */
    public void setMatchColor(int color) {
        this.matchColor = color;
    }

    /**
     * 设置名字
     *
     * @param sign 名字
     */
    public void setSign(String sign) {
        this.sign = sign;
    }

    /**
     * 设置匹配显示
     *
     * @param matchPercent  匹配百分比文字
     * @param matchDescribe 匹配描述（最匹配、最活跃）
     */
    public void setMatch(String matchPercent, String matchDescribe) {
        this.matchPercent = matchPercent;
        this.matchDescribe = matchDescribe;
    }

    /**
     * 设置是否有阴影
     *
     * @param hasShadow 是否有阴影
     */
    public void setHasShadow(boolean hasShadow) {
        this.hasShadow = hasShadow;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        signY = (float) (getPaddingTop() + signTextSize);
        signWidth = signPaint.measureText(sign);
        if (signWidth > w) {
            isOverstep = true;
            totalSignWidth = signWidth + w;
            maxSignRange = (w + signWidth) + signWidth;
            signDistanceX = totalSignWidth;
        } else {
            signX = (w - signWidth) / 2.0f;
        }
        starCenterX = (float) (w / 2);
        starCenterY = (signY + ((float) starMarginTop)) + ((float) (starWidth / 2));
        Rect matchRect = new Rect();
        matchPaint.getTextBounds(matchPercent, 0, matchPercent.length(), matchRect);
        matchPercentX = (float) ((w - matchRect.width()) / 2);
        matchPercentY = (((signY + ((float) starMarginTop)) + ((float) starWidth)) + ((float) starMarginTop)) + ((float) matchRect.height());
        matchPaint.getTextBounds(matchDescribe, 0, matchDescribe.length(), matchRect);
        matchDescribeX = (float) ((w - matchRect.width()) / 2);
        matchDescribeY = (matchPercentY + ((float) this.starMarginTop)) + ((float) matchRect.height());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float radius = starRadius;
        float min = Math.min(scale * 0.5f, 1.0f);
        // 透明度
        int alpha = (int) (0xff * min);
        // 半径
        radius *= min;
        // 设置昵称颜色
        signPaint.setARGB(alpha, 238, 238, 238);
        // 昵称文字过长（跑马灯）
        if (isOverstep) {
            canvas.drawText(sign, totalSignWidth - signDistanceX, signY, signPaint);
        } else {
            canvas.drawText(sign, signX, signY, signPaint);
        }
        // 星星球颜色（透明度）
        starPaint.setColor(starColor | alpha << 24);
        // 是否有阴影
        if (hasShadow) {
            starPaint.setShadowLayer(shadowRadius, 1.0f, 1.0f, alpha);
            canvas.drawCircle(starCenterX, starCenterY, radius, starPaint);
            canvas.drawCircle(starCenterX, starCenterY, radius, starPaint);
        }
        canvas.drawCircle(starCenterX, starCenterY, radius, starPaint);
        matchPaint.setColor(alpha << 24 | matchColor);
        canvas.drawText(matchPercent, matchPercentX, matchPercentY, matchPaint);
        canvas.drawText(matchDescribe, matchDescribeX, matchDescribeY, matchPaint);
        if (hasShadow || isOverstep) {
            if (isOverstep) {
                signDistanceX = signDistanceX + 2.5f;
                if (signDistanceX > maxSignRange) {
                    signDistanceX = signWidth;
                }
            }
            if (hasShadow) {
                if (isEnlarge) {
                    shadowRadius += radiusIncrement;
                } else {
                    shadowRadius -= radiusIncrement;
                }
                if (shadowRadius < 1) {
                    shadowRadius = 1.0f;
                    isEnlarge = true;
                } else if (shadowRadius > radius) {
                    shadowRadius = radius;
                    isEnlarge = false;
                }
            }
        }
    }
}
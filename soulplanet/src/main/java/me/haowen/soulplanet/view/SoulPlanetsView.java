package me.haowen.soulplanet.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.IntDef;

import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import me.haowen.soulplanet.PlanetCalculator;
import me.haowen.soulplanet.R;
import me.haowen.soulplanet.adapter.NullPlanetAdapter;
import me.haowen.soulplanet.adapter.PlanetAdapter;

/**
 * 星球云
 */
public class SoulPlanetsView extends ViewGroup implements Runnable, PlanetAdapter.OnDataSetChangeListener {

    public static final int MODE_DISABLE = 0;
    public static final int MODE_DECELERATE = 1;
    public static final int MODE_UNIFORM = 2;
    private static final float TOUCH_SCALE_FACTOR = 1f;
    private static final float TRACKBALL_SCALE_FACTOR = 10;
    public int mode;
    private float speed = 4f;
    private PlanetCalculator mPlanetCalculator;
    private float mAngleX;
    private float mAngleY;
    private float centerX, centerY;
    private float radius;
    /**
     * 半径的百分比
     */
    private float radiusPercent = 0.9f;
    private float[] darkColor = new float[]{1.0f, 0.0f, 0.0f, 1.0f};
    private float[] lightColor = new float[]{0.9412f, 0.7686f, 0.2f, 1.0f};
    /**
     * 是否支持手动滑动
     */
    private boolean manualScroll;
    private MarginLayoutParams layoutParams;
    private int minSize;
    private boolean isOnTouch = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private PlanetAdapter planetAdapter = new NullPlanetAdapter();
    private OnTagClickListener onTagClickListener;
    private float downX, downY;
    private float scaleX;
    private float startDistance;
    private boolean multiplePointer;
    private float startX;
    private float startY;

    public SoulPlanetsView(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        setFocusableInTouchMode(true);
        mPlanetCalculator = new PlanetCalculator();
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SoulPlanetsView);
            mode = typedArray.getInteger(R.styleable.SoulPlanetsView_autoScrollMode, MODE_DISABLE);
            setManualScroll(typedArray.getBoolean(R.styleable.SoulPlanetsView_manualScroll, true));
            mAngleX = typedArray.getFloat(R.styleable.SoulPlanetsView_startAngleX, 0.5f);
            mAngleY = typedArray.getFloat(R.styleable.SoulPlanetsView_startAngleY, 0.5f);
            setLightColor(typedArray.getColor(R.styleable.SoulPlanetsView_lightColor, Color.WHITE));
            setDarkColor(typedArray.getColor(R.styleable.SoulPlanetsView_darkColor, Color.BLACK));
            setRadiusPercent(typedArray.getFloat(R.styleable.SoulPlanetsView_radiusPercent, radiusPercent));
            setScrollSpeed(typedArray.getFloat(R.styleable.SoulPlanetsView_scrollSpeed, 2f));
            typedArray.recycle();
        }
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        if (wm != null) {
            wm.getDefaultDisplay().getSize(point);
        }
        int screenWidth = point.x;
        int screenHeight = point.y;
        minSize = screenHeight < screenWidth ? screenHeight : screenWidth;

        initFromAdapter();
    }

    public void setManualScroll(boolean manualScroll) {
        this.manualScroll = manualScroll;
    }

    public void setLightColor(int color) {
        lightColor = new float[]{
                Color.alpha(color) / 1.0f / 0xff,
                Color.red(color) / 1.0f / 0xff,
                Color.green(color) / 1.0f / 0xff,
                Color.blue(color) / 1.0f / 0xff}
                .clone();
        onChange();
    }

    public void setDarkColor(int color) {
        darkColor = new float[]{
                Color.alpha(color) / 1.0f / 0xff,
                Color.red(color) / 1.0f / 0xff,
                Color.green(color) / 1.0f / 0xff,
                Color.blue(color) / 1.0f / 0xff}
                .clone();
        onChange();
    }

    public void setRadiusPercent(float percent) {
        if (percent > 1 || percent < 0) {
            throw new IllegalArgumentException("Percent value not in range 0 to 1.");
        } else {
            radiusPercent = percent;
            onChange();
        }
    }

    public void setScrollSpeed(float scrollSpeed) {
        speed = scrollSpeed;
    }

    /**
     * 初始化VIew根据Adapter
     */
    public void initFromAdapter() {
        this.post(new Runnable() {
            @Override
            public void run() {
                // 中心坐标
                centerX = (getRight() - getLeft()) / 2f;
                centerY = (getBottom() - getTop()) / 2f;
                // 半径
                radius = Math.min(centerX, centerY) * radiusPercent;
                mPlanetCalculator.setRadius((int) radius);

                mPlanetCalculator.setTagColorLight(lightColor);
                mPlanetCalculator.setTagColorDark(darkColor);

                mPlanetCalculator.clear();

                for (int i = 0; i < planetAdapter.getCount(); i++) {
                    // 为每个Tag绑定View
                    PlanetModel planetModel = new PlanetModel(planetAdapter.getPopularity(i));
                    View view = planetAdapter.getView(getContext(), i, SoulPlanetsView.this);
                    planetModel.setView(view);
                    mPlanetCalculator.add(planetModel);
                    // 点击事件监听
                    addListener(view, i);
                }
                mPlanetCalculator.create(true);
                mPlanetCalculator.setAngleX(mAngleX);
                mPlanetCalculator.setAngleY(mAngleY);
                mPlanetCalculator.update();

                resetChildren();
            }
        });
    }

    @Override
    public void onChange() {
        post(this);
    }

    private void addListener(View view, final int position) {
        if (!view.hasOnClickListeners() && onTagClickListener != null) {
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onTagClickListener.onItemClick(SoulPlanetsView.this, v, position);
                }
            });
        }
    }

    /**
     * 重新设置子View
     */
    private void resetChildren() {
        removeAllViews();
        // 必须保证getChildAt(i) == mTagCloud.getTagList().get(i)
        for (PlanetModel planetModel : mPlanetCalculator.getTagList()) {
            addView(planetModel.getView());
        }
    }

    public SoulPlanetsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SoulPlanetsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Mode
    public int getAutoScrollMode() {
        return this.mode;
    }

    /**
     * 设置滚动模式
     *
     * @param mode 滚动模式
     */
    public void setAutoScrollMode(@Mode int mode) {
        this.mode = mode;
    }

    /**
     * 谁知适配器
     *
     * @param adapter 适配器
     */
    public final void setAdapter(PlanetAdapter adapter) {
        planetAdapter = adapter;
        planetAdapter.setOnDataSetChangeListener(this);
        onChange();
    }

    public void reset() {
        mPlanetCalculator.reset();
        resetChildren();
    }

    @Override
    public boolean onTrackballEvent(MotionEvent e) {
        if (manualScroll) {
            float x = e.getX();
            float y = e.getY();

            mAngleX = (y) * speed * TRACKBALL_SCALE_FACTOR;
            mAngleY = (-x) * speed * TRACKBALL_SCALE_FACTOR;

            mPlanetCalculator.setAngleX(mAngleX);
            mPlanetCalculator.setAngleY(mAngleY);
            mPlanetCalculator.update();

            resetChildren();
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (manualScroll) {
            handleTouchEvent(e);
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int contentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int contentHeight = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (layoutParams == null) {
            layoutParams = (MarginLayoutParams) getLayoutParams();
        }

        int dimensionX = widthMode == MeasureSpec.EXACTLY ? contentWidth : minSize - layoutParams.leftMargin - layoutParams.rightMargin;
        int dimensionY = heightMode == MeasureSpec.EXACTLY ? contentHeight : minSize - layoutParams.leftMargin - layoutParams.rightMargin;
        setMeasuredDimension(dimensionX, dimensionY);

        measureChildren(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
    }

    /**
     * 处理触摸事件
     */
    private boolean handleTouchEvent(MotionEvent event) {
        // 触摸点个数
        int pointerCount = event.getPointerCount();
        if (pointerCount > 1) {
            multiplePointer = true;
        }
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                isOnTouch = true;
                downX = event.getX();
                downY = event.getY();
                startX = downX;
                startY = downY;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getActionIndex() == 1) {
                    // 第二个触摸点
                    scaleX = getScaleX();
                    startDistance = distance(event.getX(0) - event.getX(1),
                            event.getY(0) - event.getY(1));
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (pointerCount == 1 && !multiplePointer) {
                    // 单点触摸，旋转星球
                    float dx = event.getX() - downX;
                    float dy = event.getY() - downY;
                    if (isValidMove(dx, dy)) {
                        mAngleX = (dy / radius) * speed * TOUCH_SCALE_FACTOR;
                        mAngleY = (-dx / radius) * speed * TOUCH_SCALE_FACTOR;
                        processTouch();
                        downX = event.getX();
                        downY = event.getY();
                    }
                    return isValidMove(downX - startX, downY - startY);
                } else if (pointerCount == 2) {
                    // 双点触摸，缩放
                    float endDistance = distance(event.getX(0) - event.getX(1),
                            event.getY(0) - event.getY(1));
                    // 缩放比例
                    float scale = ((endDistance - startDistance) / (endDistance * 2) + 1) * scaleX;
                    if (scale > 1.4f) {
                        scale = 1.2f;
                    }
                    if (scale < 1) {
                        scale = 1f;
                    }
                    setScaleX(scale);
                    setScaleY(scale);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                multiplePointer = false;
                isOnTouch = false;
                break;
            default:
                break;
        }
        return false;
    }

    /**
     * 两点之间的距离
     *
     * @param x x轴距离
     * @param y y轴距离
     * @return 两点之间的距离
     */
    private float distance(float x, float y) {
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 是否是有效移动
     *
     * @param dx x轴位移
     * @param dy y轴位移
     * @return 是否是有效移动
     */
    private boolean isValidMove(float dx, float dy) {
        int minDistance = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        return (Math.abs(dx) > minDistance || Math.abs(dy) > minDistance);
    }

    /**
     * 更新视图
     */
    private void processTouch() {
        // 设置旋转的X,Y
        if (mPlanetCalculator != null) {
            mPlanetCalculator.setAngleX(mAngleX);
            mPlanetCalculator.setAngleY(mAngleY);
            mPlanetCalculator.update();
        }
        for (int i = 0; i < getChildCount(); i++) {
            PlanetModel planetModel = mPlanetCalculator.get(i);
            View child = planetModel.getView();
            // 更新每一个ChildView
            if (child != null && child.getVisibility() != GONE) {
                planetAdapter.onThemeColorChanged(child, planetModel.getColor());
                // 缩放小于1的设置不可点击
                if (planetModel.getScale() < 1.0f) {
                    child.setScaleX(planetModel.getScale());
                    child.setScaleY(planetModel.getScale());
                    child.setClickable(false);
                } else {
                    child.setClickable(true);
                }
                // 设置透明度
                child.setAlpha(planetModel.getScale());
                int left = (int) (centerX + planetModel.getLoc2DX()) - child.getMeasuredWidth() / 2;
                int top = (int) (centerY + planetModel.getLoc2DY()) - child.getMeasuredHeight() / 2;
                // 从View的Tag里取出位置之前的位置信息，平移新旧位置差值
                int[] originLocation = (int[]) child.getTag();
                if (originLocation != null && originLocation.length > 0) {
                    child.setTranslationX((float) (left - originLocation[0]));
                    child.setTranslationY((float) (top - originLocation[1]));
                    // 小于移动速度，刷新
                    if (Math.abs(mAngleX) <= speed && Math.abs(mAngleY) <= speed) {
                        child.invalidate();
                    }
                }
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (manualScroll) {
            return handleTouchEvent(ev);
        }
        return false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        handler.post(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacksAndMessages(null);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            PlanetModel planetModel = mPlanetCalculator.get(i);
            if (child != null && child.getVisibility() != GONE) {
                planetAdapter.onThemeColorChanged(child, planetModel.getColor());
                // 设置缩放
                if (planetModel.getScale() < 1f) {
                    child.setScaleX(planetModel.getScale());
                    child.setScaleY(planetModel.getScale());
                }
                // 设置透明度
                child.setAlpha(planetModel.getScale());
                // 设置位置
                int left = (int) (centerX + planetModel.getLoc2DX()) - child.getMeasuredWidth() / 2;
                int top = (int) (centerY + planetModel.getLoc2DY()) - child.getMeasuredHeight() / 2;

                child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
                // 设置位置信息的TAG
                child.setTag(new int[]{left, top});
            }
        }
    }

    /**
     * 设置标签点击事件监听
     */
    public void setOnTagClickListener(OnTagClickListener listener) {
        onTagClickListener = listener;
    }

    @IntDef({MODE_DISABLE, MODE_DECELERATE, MODE_UNIFORM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

    public interface OnTagClickListener {
        void onItemClick(ViewGroup parent, View view, int position);
    }

    @Override
    public void run() {
        // 非用户触摸状态，和非不可滚动状态
        if (!isOnTouch && mode != MODE_DISABLE) {
            // 减速模式（均速衰减）
            if (mode == MODE_DECELERATE) {
                if (Math.abs(mAngleX) > 0.2f) {
                    mAngleX -= mAngleX * 0.1f;
                }
                if (Math.abs(mAngleY) > 0.2f) {
                    mAngleY -= mAngleY * 0.1f;
                }
            }
            processTouch();
        }
        handler.removeCallbacksAndMessages(null);
        // 延时
        handler.postDelayed(this, 30);
    }
}
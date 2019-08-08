package me.haowen.soulplanet;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.haowen.soulplanet.view.PlanetModel;

/**
 * 坐标等计算
 */
public class PlanetCalculator {

    private static final int DEFAULT_RADIUS = 3;
    private static final float[] DEFAULT_COLOR_DARK = {0.886f, 0.725f, 0.188f, 1f};
    private static final float[] DEFAULT_COLOR_LIGHT = {0.3f, 0.3f, 0.3f, 1f};
    private float maxDelta = Float.MIN_VALUE;
    private float minDelta = Float.MAX_VALUE;
    private List<PlanetModel> planetModelCloud;
    private int radius;
    private float[] tagColorLight;
    private float[] tagColorDark;
    private float sinAngleX, cosAngleX, sinAngleY, cosAngleY, sinAngleZ, cosAngleZ;
    private float mAngleZ = 0;
    private float mAngleX = 0;
    private float mAngleY = 0;
    /**
     * 用于查找标签颜色的光谱
     */
    private int smallest, largest;
    /**
     * 默认设置是在云端均匀分布标签
     */
    private boolean isEvenly = true;

    public PlanetCalculator() {
        this(DEFAULT_RADIUS);
    }

    public PlanetCalculator(int radius) {
        this(new ArrayList<PlanetModel>(), radius);
    }

    public PlanetCalculator(List<PlanetModel> planetModels, int radius) {
        this(planetModels, radius, DEFAULT_COLOR_DARK, DEFAULT_COLOR_LIGHT);
    }

    public PlanetCalculator(List<PlanetModel> planetModels, int radius, float[] tagColorLight, float[] tagColorDark) {
        this.planetModelCloud = planetModels;
        this.radius = radius;
        this.tagColorLight = tagColorLight;
        this.tagColorDark = tagColorDark;
    }

    public PlanetCalculator(List<PlanetModel> planetModels) {
        this(planetModels, DEFAULT_RADIUS);
    }

    public void clear() {
        planetModelCloud.clear();
    }

    public List<PlanetModel> getTagList() {
        return planetModelCloud;
    }

    public void setTagList(List<PlanetModel> list) {
        planetModelCloud = list;
    }

    public PlanetModel getTop() {
        int i = planetModelCloud.size() - 1;
        return get(i);
    }

    public PlanetModel get(int position) {
        return planetModelCloud.get(position);
    }

    public int indexOf(PlanetModel planetModel) {
        return planetModelCloud.indexOf(planetModel);
    }

    public void reset() {
        create(isEvenly);
    }

    /**
     * 创建并初始化每个Tag的位置
     *
     * @param isEvenly 是否平均分布
     */
    public void create(boolean isEvenly) {
        this.isEvenly = isEvenly;
        // 计算和设置每个Tag的位置
        locationAll(isEvenly);
        sineCosine(mAngleX, mAngleY, mAngleZ);
        updateAll();
        // 现在，让我们计算并设置每个标记的颜色：
        // 首先遍历所有标记以查找最小和最大的填充
        // 权重得到t颜色2，最小的得到t颜色1，其余在中间
        smallest = 9999;
        largest = 0;
        for (int i = 0; i < planetModelCloud.size(); i++) {
            int j = planetModelCloud.get(i).getPopularity();
            largest = Math.max(largest, j);
            smallest = Math.min(smallest, j);
        }
        // 计算并分配颜色/文本大小
        for (int i = 0; i < planetModelCloud.size(); i++) {
            initTag(planetModelCloud.get(i));
        }
    }

    /**
     * 计算所有的位置
     * <p>
     * 球坐标系(r,θ,φ)与直角坐标系(x,y,z)的转换关系:
     * x=rsinθcosφ.
     * y=rsinθsinφ.
     * z=rcosθ.
     * <p>
     * r -> radius
     * θ -> phi
     * φ -> theta
     *
     * @param isEvenly 是否均匀分布
     */
    private void locationAll(boolean isEvenly) {
        double phi;
        double theta;
        int count = planetModelCloud.size();
        for (int i = 1; i < count + 1; i++) {
            if (isEvenly) {
                // 平均（三维直角得Z轴等分[-1,1]） θ范围[-π/2,π/2])
                phi = Math.acos(-1.0 + (2.0 * i - 1.0) / count);
                theta = Math.sqrt(count * Math.PI) * phi;
            } else {
                phi = Math.random() * (Math.PI);
                theta = Math.random() * (2 * Math.PI);
            }

            planetModelCloud.get(i - 1).setLocX((float) (radius * Math.cos(theta) * Math.sin(phi)));
            planetModelCloud.get(i - 1).setLocY((float) (radius * Math.sin(theta) * Math.sin(phi)));
            planetModelCloud.get(i - 1).setLocZ((float) (radius * Math.cos(phi)));
        }
    }

    /**
     * 返回角度转换成弧度之后各方向的值
     * <p>
     * 1度=π/180
     *
     * @param mAngleX x方向旋转距离
     * @param mAngleY y方向旋转距离
     * @param mAngleZ z方向旋转距离
     */
    private void sineCosine(float mAngleX, float mAngleY, float mAngleZ) {
        double degToRad = (Math.PI / 180);
        sinAngleX = (float) Math.sin(mAngleX * degToRad);
        cosAngleX = (float) Math.cos(mAngleX * degToRad);
        sinAngleY = (float) Math.sin(mAngleY * degToRad);
        cosAngleY = (float) Math.cos(mAngleY * degToRad);
        sinAngleZ = (float) Math.sin(mAngleZ * degToRad);
        cosAngleZ = (float) Math.cos(mAngleZ * degToRad);
    }

    /**
     * 更新所有的
     */
    private void updateAll() {
        // 更新标签透明度和比例
        int count = planetModelCloud.size();
        for (int i = 0; i < count; i++) {
            PlanetModel planetModel = planetModelCloud.get(i);
            // 此部分有两个选项：
            // 绕x轴旋转
            float rx1 = (planetModel.getLocX());
            float ry1 = (planetModel.getLocY()) * cosAngleX + planetModel.getLocZ() * -sinAngleX;
            float rz1 = (planetModel.getLocY()) * sinAngleX + planetModel.getLocZ() * cosAngleX;
            // 绕y轴旋转
            float rx2 = rx1 * cosAngleY + rz1 * sinAngleY;
            float ry2 = ry1;
            float rz2 = rx1 * -sinAngleY + rz1 * cosAngleY;
            // 绕z轴旋转
            float rx3 = rx2 * cosAngleZ + ry2 * -sinAngleZ;
            float ry3 = rx2 * sinAngleZ + ry2 * cosAngleZ;
            float rz3 = rz2;
            // 将数组设置为新位置
            planetModel.setLocX(rx3);
            planetModel.setLocY(ry3);
            planetModel.setLocZ(rz3);

            // 添加透视图
            int diameter = 2 * radius;
            float per = diameter / (diameter + rz3);
            // 让我们为标签设置位置、比例和透明度
            planetModel.setLoc2DX(rx3);
            planetModel.setLoc2DY(ry3);
            planetModel.setScale(per);

            // 计算透明度
            float delta = diameter + rz3;
            maxDelta = Math.max(maxDelta, delta);
            minDelta = Math.min(minDelta, delta);
            float alpha = (delta - minDelta) / (maxDelta - minDelta);
            planetModel.setAlpha(1 - alpha);
        }
        sortTagByScale();
    }

    private void initTag(PlanetModel planetModel) {
        float percentage = getPercentage(planetModel);
        float[] argb = getColorFromGradient(percentage);
        planetModel.setColorByArray(argb);
    }

    /**
     * 根据缩放值排序
     */
    public void sortTagByScale() {
        Collections.sort(planetModelCloud, new TagComparator());
    }

    private float getPercentage(PlanetModel planetModel) {
        int p = planetModel.getPopularity();
        return (smallest == largest) ? 1.0f : ((float) p - smallest) / ((float) largest - smallest);
    }

    private float[] getColorFromGradient(float percentage) {
        float[] rgba = new float[4];
        rgba[0] = 1f;
        rgba[1] = (percentage * (tagColorDark[0])) + ((1f - percentage) * (tagColorLight[0]));
        rgba[2] = (percentage * (tagColorDark[1])) + ((1f - percentage) * (tagColorLight[1]));
        rgba[3] = (percentage * (tagColorDark[2])) + ((1f - percentage) * (tagColorLight[2]));
        return rgba;
    }

    /**
     * 更新所有元素的透明度/比例
     */
    public void update() {
        // 如果mAngleX和mAngleY低于阈值，则跳过运动计算以获得性能
        if (Math.abs(mAngleX) > .1 || Math.abs(mAngleY) > .1) {
            sineCosine(mAngleX, mAngleY, mAngleZ);
            updateAll();
        }
    }

    /**
     * 添加单个标签
     *
     * @param planetModel 标签
     */
    public void add(PlanetModel planetModel) {
        initTag(planetModel);
        location(planetModel);
        planetModelCloud.add(planetModel);
        updateAll();
    }

    /**
     * 添加新标签时，只需将其放置在某个随机位置
     * 在多次添加之后，执行一次重置以重新排列所有标记
     *
     * @param planetModel 标签
     */
    private void location(PlanetModel planetModel) {
        double phi;
        double theta;
        phi = Math.random() * (Math.PI);
        theta = Math.random() * (2 * Math.PI);
        planetModel.setLocX((int) (radius * Math.cos(theta) * Math.sin(phi)));
        planetModel.setLocY((int) (radius * Math.sin(theta) * Math.sin(phi)));
        planetModel.setLocZ((int) (radius * Math.cos(phi)));
    }

    /**
     * 设置半径
     *
     * @param radius 半径
     */
    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setTagColorLight(float[] tagColor) {
        this.tagColorLight = tagColor;
    }

    public void setTagColorDark(float[] tagColorDark) {
        this.tagColorDark = tagColorDark;
    }

    public void setAngleX(float mAngleX) {
        this.mAngleX = mAngleX;
    }

    public void setAngleY(float mAngleY) {
        this.mAngleY = mAngleY;
    }

    private static class TagComparator implements Comparator<PlanetModel> {

        @Override
        public int compare(PlanetModel planetModel1, PlanetModel planetModel2) {
            return planetModel1.getScale() > planetModel2.getScale() ? 1 : 0;
        }
    }
}

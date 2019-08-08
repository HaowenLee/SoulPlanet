package me.haowen.sample.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.io.UnsupportedEncodingException;
import java.util.Random;

import me.haowen.soulplanet.adapter.PlanetAdapter;
import me.haowen.soulplanet.utils.SizeUtils;
import me.haowen.soulplanet.view.PlanetView;

public class TestAdapter extends PlanetAdapter {

    @Override
    public int getCount() {
        return 50;
    }

    @Override
    public View getView(Context context, int position, ViewGroup parent) {
        PlanetView planetView = new PlanetView(context);
        planetView.setSign(getRandomNick());

        int starColor = position % 2 == 0 ? PlanetView.COLOR_FEMALE : PlanetView.COLOR_MALE;
        boolean hasShadow = false;

        String str = "";
        if (position % 12 == 0) {
            str = "最活跃";
            starColor = PlanetView.COLOR_MOST_ACTIVE;
        } else if (position % 20 == 0) {
            str = "最匹配";
            starColor = PlanetView.COLOR_BEST_MATCH;
        } else if (position % 33 == 0) {
            str = "最新人";
            starColor = PlanetView.COLOR_MOST_NEW;
        } else if (position % 18 == 0) {
            hasShadow = true;
            str = "最闪耀";
        } else {
            str = "描述";
        }
        planetView.setStarColor(starColor);
        planetView.setHasShadow(hasShadow);
        planetView.setMatch(position * 2 + "%", str);
        if (hasShadow) {
            planetView.setMatchColor(starColor);
        } else {
            planetView.setMatchColor(PlanetView.COLOR_MOST_ACTIVE);
        }
        int starWidth = SizeUtils.dp2px(context, 50.0f);
        int starHeight = SizeUtils.dp2px(context, 85.0f);
        int starPaddingTop = SizeUtils.dp2px(context, 20.0f);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(starWidth, starHeight);
        planetView.setPadding(0, starPaddingTop, 0, 0);
        planetView.setLayoutParams(layoutParams);
        return planetView;
    }

    /**
     * 获取随机昵称
     *
     * @return 随机昵称
     */
    private String getRandomNick() {
        Random random = new Random();
        int len = random.nextInt(12) + 1;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            builder.append(getRandomSingleCharacter());
        }
        return builder.toString();
    }

    /**
     * 获取随机单个汉字
     *
     * @return 随机单个汉字
     */
    private String getRandomSingleCharacter() {
        String str = "";
        int heightPos;
        int lowPos;
        Random rd = new Random();
        heightPos = 176 + Math.abs(rd.nextInt(39));
        lowPos = 161 + Math.abs(rd.nextInt(93));
        byte[] bt = new byte[2];
        bt[0] = Integer.valueOf(heightPos).byteValue();
        bt[1] = Integer.valueOf(lowPos).byteValue();
        try {
            str = new String(bt, "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public int getPopularity(int position) {
        return position % 10;
    }

    @Override
    public void onThemeColorChanged(View view, int themeColor) {

    }
}
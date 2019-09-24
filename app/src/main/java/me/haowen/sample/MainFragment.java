package me.haowen.sample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import me.haowen.sample.adapter.TestAdapter;
import me.haowen.soulplanet.view.SoulPlanetsView;

/**
 * ================================================
 * 作    者：Herve、Li
 * 创建日期：2019/9/24
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class MainFragment extends Fragment {

    private View root;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_main, container, false);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final SoulPlanetsView soulPlanet = root.findViewById(R.id.soulPlanetView);
        soulPlanet.setAdapter(new TestAdapter());

        root.findViewById(R.id.clBackground).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "数据更新中", Toast.LENGTH_SHORT).show();
                soulPlanet.setAdapter(new TestAdapter());
            }
        });


        soulPlanet.setOnTagClickListener(new SoulPlanetsView.OnTagClickListener() {
            @Override
            public void onItemClick(ViewGroup parent, View view, int position) {
                Toast.makeText(getContext(), String.valueOf(position), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

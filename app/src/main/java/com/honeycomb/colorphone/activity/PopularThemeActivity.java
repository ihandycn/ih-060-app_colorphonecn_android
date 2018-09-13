package com.honeycomb.colorphone.activity;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.themeselector.ThemeSelectorAdapter;
import com.ihs.app.framework.activity.HSAppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PopularThemeActivity extends HSAppCompatActivity {

    public static final String NOTIFY_UNSELECTED = "notify_unselected";

    private ThemeSelectorAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ArrayList<Theme> mRecyclerViewData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acb_activity_popular_theme);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setItemAnimator(null);

        initData();
        mAdapter = new ThemeSelectorAdapter(this, mRecyclerViewData);
        mRecyclerView.setLayoutManager(mAdapter.getLayoutManager());
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(mAdapter.getLastSelectedLayoutPos());
        if (holder instanceof ThemeSelectorAdapter.ThemeCardViewHolder) {
            ((ThemeSelectorAdapter.ThemeCardViewHolder) holder).startAnimation();
        }
        mAdapter.updateApplyInformationAutoPilotValue();
    }

    @Override
    protected void onPause() {
        super.onPause();
        RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(mAdapter.getLastSelectedLayoutPos());
        if (holder instanceof ThemeSelectorAdapter.ThemeCardViewHolder) {
            ((ThemeSelectorAdapter.ThemeCardViewHolder) holder).stopAnimation();
        }
    }

    private void initData() {
        mRecyclerViewData.clear();
        for (Theme theme : Theme.themes()) {
            if (theme.isSpecialTopic()) {
                mRecyclerViewData.add(theme);
            }
        }
        Collections.sort(mRecyclerViewData, new Comparator<Theme>() {
            @Override
            public int compare(Theme o1, Theme o2) {
                return o1.getIndex() - o2.getIndex();
            }
        });

    }

}

package com.honeycomb.colorphone.themeselector;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.honeycomb.colorphone.R;

import java.util.ArrayList;

public class ThemeSelectorAdapter extends RecyclerView.Adapter<ThemeCardViewHolder> {

    private ArrayList<String> data = null;

    public ThemeSelectorAdapter(ArrayList<String> data) {
        this.data = data;
    }

    @Override
    public ThemeCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View cardViewContent = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_theme_selector, null);
        ThemeCardViewHolder themeCardViewHolder = new ThemeCardViewHolder(cardViewContent);
        return themeCardViewHolder;
    }

    @Override
    public void onBindViewHolder(ThemeCardViewHolder holder, int position) {
        holder.setPositionTag(position);
        String positionTxt = data.get(position);
        holder.setTxt(positionTxt);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
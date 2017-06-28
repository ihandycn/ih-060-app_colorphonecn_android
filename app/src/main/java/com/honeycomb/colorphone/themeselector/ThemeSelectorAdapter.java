package com.honeycomb.colorphone.themeselector;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.acb.call.themes.Type;
import com.acb.call.views.InCallActionView;
import com.acb.call.views.ThemePreviewWindow;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.ihs.app.framework.HSApplication;

import java.util.ArrayList;

public class ThemeSelectorAdapter extends RecyclerView.Adapter<ThemeSelectorAdapter.ThemeCardViewHolder> {

    private ArrayList<Theme> data = null;

    public ThemeSelectorAdapter(ArrayList<Theme> data) {
        this.data = data;
    }

    @Override
    public ThemeCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View cardViewContent = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_theme_selector, null);
        final ThemeCardViewHolder themeCardViewHolder = new ThemeCardViewHolder(cardViewContent);

        cardViewContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(HSApplication.getContext(), themeCardViewHolder.getPositionTag() + " clicked", Toast.LENGTH_SHORT).show();
            }
        });
        return themeCardViewHolder;
    }

    @Override
    public void onBindViewHolder(ThemeCardViewHolder holder, int position) {
        holder.setPositionTag(position);

        final Theme curTheme = data.get(position);
        String positionTxt = curTheme.getName();
        holder.setTxt(positionTxt);

        holder.previewWindow.playAnimation(Type.valueOf(curTheme.getName()));
        if (!curTheme.isSelected()) {
            holder.previewWindow.stopAnimations();
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ThemeCardViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView txt;

        ThemePreviewWindow previewWindow;
        InCallActionView callActionView;

        private int positionTag;

        public void setPositionTag(int position) {
            positionTag = position;
        }

        public void setTxt(String string) {
            txt.setText(string);
        }

        public int getPositionTag() {
            return positionTag;
        }

        ThemeCardViewHolder(View itemView) {
            super(itemView);

            img = (ImageView) itemView.findViewById(R.id.card_view_img);
            txt = (TextView) itemView.findViewById(R.id.card_view_txt);

            previewWindow = (ThemePreviewWindow) itemView.findViewById(R.id.flash_view);
            callActionView = (InCallActionView) itemView.findViewById(R.id.in_call_view);
            callActionView.setAutoRun(false);

        }
    }
}
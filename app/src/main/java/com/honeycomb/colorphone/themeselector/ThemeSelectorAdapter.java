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
import com.honeycomb.colorphone.ThemePreviewActivity;
import com.ihs.app.framework.HSApplication;

import java.util.ArrayList;

public class ThemeSelectorAdapter extends RecyclerView.Adapter<ThemeSelectorAdapter.ThemeCardViewHolder> {

    private ArrayList<Theme> data = null;

    public ThemeSelectorAdapter(ArrayList<Theme> data) {
        this.data = data;
    }

    @Override
    public ThemeCardViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View cardViewContent = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_theme_selector, null);
        final ThemeCardViewHolder themeCardViewHolder = new ThemeCardViewHolder(cardViewContent);

        cardViewContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = themeCardViewHolder.getPositionTag();
                ThemePreviewActivity.start(parent.getContext(), data.get(pos));
                Toast.makeText(HSApplication.getContext(), themeCardViewHolder.getPositionTag() + " clicked", Toast.LENGTH_SHORT).show();
            }
        });
        // Disable theme original bg. Use our own
        themeCardViewHolder.previewWindow.setBgDrawable(null);

        return themeCardViewHolder;
    }

    // TODO Use bitmap to improve draw performance

    @Override
    public void onBindViewHolder(ThemeCardViewHolder holder, int position) {
        holder.setPositionTag(position);

        final Theme curTheme = data.get(position);
        String name = curTheme.getName();
        holder.setTxt(name);
        holder.downloadTxt.setText(String.valueOf(curTheme.getDownload()));
        if (curTheme.getImageRes() > 0) {
            holder.img.setImageResource(curTheme.getImageRes());
        } else {
            holder.img.setImageDrawable(null);
        }

        holder.previewWindow.playAnimation(Type.values()[curTheme.getThemeId()]);
        if (!curTheme.isSelected()) {
            holder.previewWindow.stopAnimations();
            holder.callActionView.setAutoRun(false);

        } else {
            holder.previewWindow.setAutoRun(true);
            holder.callActionView.setAutoRun(true);
        }

        holder.setSelected(curTheme.isSelected());
        holder.setHotTheme(curTheme.isHot());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ThemeCardViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView txt;
        TextView downloadTxt;
        ThemePreviewWindow previewWindow;
        InCallActionView callActionView;

        private int positionTag;
        private final View hotView;
        private final View selectedView;

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
            txt = (TextView) itemView.findViewById(R.id.card_name);

            selectedView = itemView.findViewById(R.id.theme_selected);
            hotView = itemView.findViewById(R.id.theme_hot);
            downloadTxt = (TextView)itemView.findViewById(R.id.theme_download_txt);
            previewWindow = (ThemePreviewWindow) itemView.findViewById(R.id.flash_view);
            callActionView = (InCallActionView) itemView.findViewById(R.id.in_call_view);
            callActionView.setAutoRun(false);

        }

        public void setSelected(boolean selected) {
            if (selectedView != null) {
                selectedView.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
            }
        }

        public void setHotTheme(boolean hot) {
            if (hotView != null) {
                hotView.setVisibility(hot ? View.VISIBLE : View.INVISIBLE);
            }
        }
    }
}
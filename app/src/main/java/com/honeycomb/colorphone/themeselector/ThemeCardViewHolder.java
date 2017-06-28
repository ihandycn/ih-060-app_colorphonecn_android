package com.honeycomb.colorphone.themeselector;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.honeycomb.colorphone.R;
import com.ihs.app.framework.HSApplication;

public class ThemeCardViewHolder extends RecyclerView.ViewHolder {
    private ImageView img;
    private TextView txt;

    private int positionTag;

    public void setImg(int id) {
        img.setImageResource(id);
    }

    public void setPositionTag(int position) {
        positionTag = position;
    }

    public void setTxt(String string) {
        txt.setText(string);
    }


    ThemeCardViewHolder(View itemView) {
        super(itemView);

        img = (ImageView) itemView.findViewById(R.id.card_view_img);
        img.setImageResource(R.drawable.acb_phone_theme_technological_bg);
        txt = (TextView) itemView.findViewById(R.id.card_view_txt);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(HSApplication.getContext(), positionTag + " clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
package com.honeycomb.colorphone.contact;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.commons.utils.HSLog;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;


public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private static int[] LETTER_COLOR = new int[]{
            0xff4285f4, 0xff757575, 0xffff6f00, 0xff0d9d58, 0xff4051b6, 0xff0297a8
    };

    private static final java.lang.String TAG = "ContactAdapter";
    private final List<SimpleContact> people;
    private final LayoutInflater layoutInflater;
    private final int            rowLayout;

    public ContactAdapter(LayoutInflater layoutInflater, List<SimpleContact> people, @LayoutRes int rowLayout) {
        this.people = people;
        this.layoutInflater = layoutInflater;
        this.rowLayout = rowLayout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = layoutInflater.inflate(rowLayout,
                                        parent,
                                        false);
        final ViewHolder holder =  new ViewHolder(v);
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                people.get(holder.getAdapterPosition()).setSelected(isChecked);
                HSLog.d(TAG, "");
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SimpleContact person = people.get(position);
        holder.fullName.setText(person.getName());
        holder.avater.setTitleText(getSectionName(position));

        int randomHash = Math.abs(person.getRawNumber().hashCode());
        int randomColor = LETTER_COLOR[randomHash % LETTER_COLOR.length];
        holder.avater.setBackgroundColor(randomColor);
    }

    @Override
    public int getItemCount() {
        return people.size();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return ContactUtils.getSectionName(people.get(position).getName());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView fullName;
        private RoundedLetterView avater;
        private CheckBox checkBox;

        public ViewHolder(View view) {
            super(view);
            fullName = (TextView) view.findViewById(R.id.contact_name);
            avater = (RoundedLetterView) view.findViewById(R.id.contact_avatar);
            checkBox = (CheckBox) view.findViewById(R.id.contact_select_box);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean result = !checkBox.isChecked();
                    checkBox.setChecked(result);
                }
            });
        }
    }


}

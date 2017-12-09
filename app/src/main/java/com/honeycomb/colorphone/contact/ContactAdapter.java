package com.honeycomb.colorphone.contact;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.acb.call.themes.Type;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.fastscroller.FastScrollRecyclerView;
import com.honeycomb.colorphone.view.GlideApp;
import com.ihs.commons.utils.HSLog;

import java.util.List;

import static com.flurry.sdk.nr.d;


public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter, FastScrollRecyclerView.MeasurableAdapter {

    private static int[] LETTER_COLOR = new int[]{
            0xff4285f4, 0xff757575, 0xffff6f00, 0xff0d9d58, 0xff4051b6, 0xff0297a8
    };

    private static final java.lang.String TAG = "ContactAdapter";
    private final List<SimpleContact> people;
    private final LayoutInflater layoutInflater;
    private final int            rowLayout;
    /**
     * Indicates contact could be selected, and CheckBox is visible & checkable.
     */
    private boolean inSelectMode = true;

    private int selectedCount = 0;
    private CountTriggerListener mCountTriggerListener;
    private int footerOffset;
    private int headerOffset;
    private int itemHeight;
    private boolean themeVisible = true;
    private List<Type> themeTypeList;

    public ContactAdapter(LayoutInflater layoutInflater, List<SimpleContact> people, @LayoutRes int rowLayout) {
        this.people = people;
        this.layoutInflater = layoutInflater;
        this.rowLayout = rowLayout;
        this.themeTypeList = Type.values();
    }

    public void setInSelectMode(boolean inSelectMode) {
        boolean changed = this.inSelectMode != inSelectMode;
        if (changed) {
            this.inSelectMode = inSelectMode;
            notifyDataSetChanged();
        }
    }

    @Deprecated
    public void setThemeVisible(boolean themeVisible) {
//        boolean changed = this.themeVisible != themeVisible;
//        if (changed) {
//            this.themeVisible = themeVisible;
//            notifyDataSetChanged();
//        }
    }

    public void setFooterOffset(int footerrOffset) {
        this.footerOffset = footerrOffset;
    }

    public void setHeaderOffset(int headerOffset) {
        this.headerOffset = headerOffset;
    }

    public void setItemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = layoutInflater.inflate(rowLayout,
                                        parent,
                                        false);
        final ViewHolder holder =  new ViewHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!inSelectMode) {
                    return;
                }
                boolean result = !holder.checkBox.isChecked();
                holder.checkBox.setChecked(result);
            }
        });
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                people.get(holder.getAdapterPosition()).setSelected(isChecked);
                int lastTimeCount = selectedCount;
                if (isChecked) {
                    selectedCount++;
                } else {
                    selectedCount--;
                }
                /**
                 *  last - current
                 *  0 - 0
                 *  0 - 1
                 *  1 - 0
                 */
                boolean trigger = lastTimeCount + selectedCount <= 1;
                if (trigger) {
                    onTriggerSelectedCount(selectedCount);
                }
                HSLog.d(TAG, "");
            }
        });
        return holder;
    }

    private void onTriggerSelectedCount(int selectedCount) {
        if (mCountTriggerListener != null) {
            mCountTriggerListener.onTrigger(selectedCount);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SimpleContact person = people.get(position);
        holder.fullName.setText(person.getName());
        if (themeVisible) {
            holder.themeName.setText(getThemeName(person.getThemeId()));
        } else {
            holder.themeName.setText("");
        }
        if (inSelectMode) {
            holder.itemView.setClickable(true);
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setEnabled(true);
        } else {
            holder.itemView.setClickable(false);
            holder.checkBox.setVisibility(View.INVISIBLE);
            holder.checkBox.setEnabled(false);
        }
        holder.checkBox.setChecked(person.isSelected());

        String photoUri  = person.getPhotoUri();
        if (!TextUtils.isEmpty(photoUri)) {
            GlideApp.with(holder.avater)
                    .asBitmap()
                    .load(photoUri)
                    .into(holder.avater);
        } if (!ContactUtils.isSectionNameMiscOrDigit(getSectionName(position))) {
            holder.avater.setTitleText(getSectionName(position));
        } else {
            holder.avater.setImageResource(R.drawable.contact_defualt_photo);
        }

        int randomHash = Math.abs(person.getRawNumber().hashCode());
        int randomColor = LETTER_COLOR[randomHash % LETTER_COLOR.length];
        holder.avater.setBackgroundColor(randomColor);
    }

    private String getThemeName(int themeId) {
        if (themeId < 0) {
            return "";
        }
        for (Type type : themeTypeList) {
            if (type.getId() == themeId) {
                return type.getName();
            }
        }
        return  "";
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

    public void setCountTriggerListener(CountTriggerListener countTriggerListener) {
        mCountTriggerListener = countTriggerListener;
    }

    @Override
    public int getViewTypeHeight(RecyclerView recyclerView, int pos) {
        int height = itemHeight;
        if (pos == 0) {
            height += headerOffset;
        } else if (pos == getItemCount() - 1) {
            height += footerOffset;
        }
        return height;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView fullName;
        private TextView themeName;
        private RoundedLetterView avater;
        private CheckBox checkBox;

        public ViewHolder(View view) {
            super(view);
            fullName = (TextView) view.findViewById(R.id.contact_name);
            avater = (RoundedLetterView) view.findViewById(R.id.contact_avatar);
            checkBox = (CheckBox) view.findViewById(R.id.contact_select_box);
            themeName = (TextView) view.findViewById(R.id.theme_name);

        }
    }

    public interface CountTriggerListener {
        void onTrigger(int currentSelectedCount);
    }
}

package colorphone.acb.com.libweather;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.superapps.util.Threads;

import colorphone.acb.com.libweather.util.ViewUtils;
import colorphone.acb.com.libweather.view.SwipeRevealLayout;

/**
 * One item of city list on weather settings page.
 */
public class CityListItem extends SwipeRevealLayout implements View.OnClickListener {

    private ImageView mStartIcon;
    private ImageView mEndIcon;
    private TextView mCityNameText;

    private OnClickListener mOnClickListener;

    public interface OnClickListener {
        void onClickFrontView(SwipeRevealLayout layout);

        void onClickBackView(SwipeRevealLayout layout);

        void onClickActionButton(SwipeRevealLayout layout);
    }

    public CityListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnViewClickListener(OnClickListener listener) {
        this.mOnClickListener = listener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mStartIcon = ViewUtils.findViewById(mFrontView, R.id.weather_settings_city_start_icon);
        mEndIcon = ViewUtils.findViewById(mFrontView, R.id.weather_settings_city_end_icon);
        mCityNameText = ViewUtils.findViewById(mFrontView, R.id.weather_settings_city_name);

        mEndIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.weather_settings_delete));

        mFrontView.setOnClickListener(this);
        mBackView.setOnClickListener(this);
    }

    public void bind(CityData city) {
        setTag(city);

        boolean isLocal = city.isLocal();
        setDragEnabled(!isLocal);
        mEndIcon.setVisibility(isLocal ? INVISIBLE : VISIBLE);
        mEndIcon.setOnClickListener(this);
        mCityNameText.setText(city.getDisplayName());

        setIsFirstItem();
    }

    public void setIsFirstItem() {
        boolean isLocal = ((CityData) getTag()).isLocal();
        int textColorId = R.color.material_text_black_primary;
        mCityNameText.setTextColor(ContextCompat.getColor(getContext(), textColorId));

        if (isLocal) {
            mStartIcon.setImageResource(R.drawable.weather_settings_location);
        } else {
            mStartIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.weather_settings_reorder));
        }
    }

    public View getDragHandle() {
        return mStartIcon;
    }

    @Override
    public void onClick(View v) {
        if (v == mEndIcon) {
            onClickActionIcon();
            if (mOnClickListener != null) {
                mOnClickListener.onClickActionButton(this);
            }
        } else if (v == mFrontView) {
            if (mOnClickListener != null) {
                mOnClickListener.onClickFrontView(this);
            }
        } else if (v == mBackView) {
            onClickDeleteButton();
            if (mOnClickListener != null) {
                mOnClickListener.onClickBackView(this);
            }
        }
    }

    private void onClickActionIcon() {
        open();
    }

    private void onClickDeleteButton() {
        CityData city = (CityData) getTag();
        if (city == null) {
            return;
        }
        final long cityRowId = city.getId();

        ViewGroup cityList = (ViewGroup) getParent();
        if (cityList == null) {
            return;
        }

        Threads.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                getContext().getContentResolver().delete(WeatherDataProvider.CONTENT_URI,
                        WeatherDataProvider.COLUMN_ID + "=?", new String[]{String.valueOf(cityRowId)});
            }
        });
    }
}

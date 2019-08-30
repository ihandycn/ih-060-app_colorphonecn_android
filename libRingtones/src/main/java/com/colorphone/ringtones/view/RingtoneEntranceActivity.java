package com.colorphone.ringtones.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.colorphone.ringtones.R;
import com.colorphone.ringtones.RingtoneApi;
import com.colorphone.ringtones.RingtoneConfig;
import com.colorphone.ringtones.RingtoneImageLoader;
import com.colorphone.ringtones.RingtoneManager;
import com.colorphone.ringtones.RingtoneSetter;
import com.colorphone.ringtones.SubColumnsAdapter;
import com.colorphone.ringtones.bean.RingtoneBean;
import com.colorphone.ringtones.bean.RingtoneListResultBean;
import com.colorphone.ringtones.module.Column;
import com.colorphone.ringtones.module.Ringtone;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Toasts;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sundxing
 */
public class RingtoneEntranceActivity extends AppCompatActivity implements ResizeTextTabLayout.OnTabSelectListener, RingtoneManager.RingtoneSetHandler {

    RingtoneApi mRingtoneApi;
    RingtoneSearchAdapter mRingtoneSearchAdapter;
    private RecyclerView searchListView;
    private View searchEmptyView;
    private View columnRootView;
    private TextEditTextView searchInput;
    private boolean inSearchMode;
    private View searchClearBtn;
    private View searchButton;
    private List<Column> mTabColumns = new ArrayList<>(4);
    private SparseArray<View> mRingtoneViewFrames = new SparseArray<>(4);
    private ResizeTextTabLayout columnTabView;
    private ViewGroup columnFrameContainer;
    private RingtoneSetViewHolder ringtoneSetViewHolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initColumns();
        mRingtoneApi = new RingtoneApi();
        mRingtoneSearchAdapter = new RingtoneSearchAdapter(mRingtoneApi);

        setContentView(R.layout.main_ringone_page);

        columnRootView = findViewById(R.id.classification_container);
        columnFrameContainer = findViewById(R.id.ringtone_list_frame);

        columnTabView = findViewById(R.id.ringtone_tab);
        columnTabView.setOnTabSelectListener(this);

        columnTabView.setSelected(0);

        searchInput = findViewById(R.id.search_input);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String result = charSequence.toString();
                boolean showActions = !TextUtils.isEmpty(result);
                checkSearchActionButtons(showActions);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    searchRingtone(searchInput.getText().toString());
                    return true;
                }
                return false;
            }
        });
        searchInput.setOnKeyBoardHideListener(new TextEditTextView.OnKeyBoardHideListener() {
            @Override
            public void onKeyHide(int keyCode, KeyEvent event) {
                searchInput.clearFocus();
            }
        });

        searchClearBtn = findViewById(R.id.search_clear_btn);
        searchClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchInput.setText("");
            }
        });

        searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchRingtone(searchInput.getText().toString());
            }
        });

        searchListView = findViewById(R.id.ringtone_search_list);
        searchListView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,
                false));

        searchEmptyView = findViewById(R.id.ringtone_search_no_result);

        RingtoneManager.getInstance().setRingtoneSetHandler(this);

    }

    private void initColumns() {
        // TODO id from config
        mTabColumns.add(new Column("304013", "热门"));
        mTabColumns.add(new Column("304017", "最新"));
        mTabColumns.add(new Column("304021", "飙升"));
        if (RingtoneManager.getInstance().isSubColumnsReady()) {
            mTabColumns.add(new Column("303989", "分类"));
        }
    }

    private void checkSearchActionButtons(boolean showActions) {
        int targetVisibility = showActions ? View.VISIBLE : View.GONE;
        if (targetVisibility != searchButton.getVisibility()) {
            searchButton.setVisibility(targetVisibility);
            searchClearBtn.setVisibility(targetVisibility);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void cancelSearchMode() {
        if (inSearchMode) {
            searchInput.setText("");
            columnRootView.setVisibility(View.VISIBLE);

            searchListView.setVisibility(View.INVISIBLE);
            searchListView.setAdapter(null);
            searchEmptyView.setVisibility(View.INVISIBLE);
            inSearchMode = false;
        }
    }

    private void searchRingtone(String text) {
        HSLog.d("Search Text : " + text);

        if (TextUtils.isEmpty(text)) {
            Toasts.showToast("请输入内容");
            return;
        }
        mRingtoneApi.search(text, new RingtoneApi.ResultCallback<RingtoneListResultBean>() {
            @Override
            public void onFinish(@Nullable RingtoneListResultBean bean) {
                List<Ringtone> results = new ArrayList<>();
                if (bean != null) {
                    List<RingtoneBean> beans = bean.getData();
                    if (beans != null) {
                        for (RingtoneBean rb : beans) {
                            results.add(Ringtone.valueOf(rb));
                        }
                    }
                }
                onSearchResultOk(results);
            }
        });
    }

    private void onSearchResultOk(List<Ringtone> results) {
        inSearchMode = true;
        hideKeyboard();
        columnRootView.setVisibility(View.GONE);

        if (results == null || results.isEmpty()) {
            // No result
            searchListView.setVisibility(View.INVISIBLE);
            searchEmptyView.setVisibility(View.VISIBLE);
        } else {
            searchListView.setVisibility(View.VISIBLE);
            searchListView.setAdapter(mRingtoneSearchAdapter);
            searchEmptyView.setVisibility(View.INVISIBLE);
        }
        mRingtoneSearchAdapter.updateDate(results);
    }

    @Override
    public void onBackPressed() {
        if (isRingtoneSetPageShow()) {
            ringtoneSetViewHolder.hide();
        } else if (inSearchMode) {
            cancelSearchMode();
        } else {
            super.onBackPressed();
        }
    }

    private boolean isRingtoneSetPageShow() {
        return ringtoneSetViewHolder != null && ringtoneSetViewHolder.isVisible();
    }

    @Override
    public void onTabSelected(int index) {
        View targetFrameView = mRingtoneViewFrames.get(index);
        if (targetFrameView == null) {
            boolean isNormalList = index != 3;
            Column column = mTabColumns.get(index);
            if (isNormalList) {
                targetFrameView = getLayoutInflater().inflate(R.layout.frame_ringtone_list_normal, columnFrameContainer, false);

                final RecyclerView recyclerView = targetFrameView.findViewById(R.id.ringtone_list);
                recyclerView.setLayoutManager(new LinearLayoutManager(this,
                        LinearLayoutManager.VERTICAL,
                        false));
                recyclerView.setAdapter(new RingtoneListAdapter(mRingtoneApi, column.getId(), index == 0));

            } else {
                // 分类
                targetFrameView = getLayoutInflater().inflate(R.layout.frame_ringtone_list_calssify, columnFrameContainer, false);

                final View expandBtn = targetFrameView.findViewById(R.id.ringtone_classification_expand_btn);
                final RecyclerView recyclerView = targetFrameView.findViewById(R.id.ringtone_list);
                recyclerView.setLayoutManager(new LinearLayoutManager(this,
                        LinearLayoutManager.VERTICAL,
                        false));

                String subColumnId = RingtoneManager.getInstance().getSelectedSubColumn().getId();
                final RingtoneListAdapter adatper = new RingtoneListAdapter(mRingtoneApi, subColumnId, false);
                recyclerView.setAdapter(adatper);


                final RecyclerView recyclerViewSubColumns = targetFrameView.findViewById(R.id.ringtone_classification_list);
                recyclerViewSubColumns.setLayoutManager(new GridLayoutManager(this, 4));
                recyclerViewSubColumns.addItemDecoration(new RecyclerView.ItemDecoration() {
                    int space = Dimensions.pxFromDp(4);
                    @Override
                    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                        outRect.bottom = space;
                        outRect.top = space;
                        outRect.left = space;
                        outRect.right = space;
                    }
                });
                SubColumnsAdapter subColumnsAdapter = new SubColumnsAdapter();
                subColumnsAdapter.setExpandButton(expandBtn);
                subColumnsAdapter.setOnColumnSelectedListener(new SubColumnsAdapter.OnColumnSelectedListener() {
                    @Override
                    public void onColumnSelect(Column column) {
                        adatper.requestRingtoneList(column.getId());
                    }
                });
                recyclerViewSubColumns.setAdapter(subColumnsAdapter);

            }
            mRingtoneViewFrames.put(index, targetFrameView);
        }
        columnFrameContainer.removeAllViews();
        columnFrameContainer.addView(targetFrameView);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isRingtoneSetPageShow()) {
            ringtoneSetViewHolder.hide();
        }
    }

    @Override
    public void onSetRingtone(Ringtone ringtone) {
        if (ringtoneSetViewHolder == null) {
            ViewStub stub = findViewById(R.id.stub_ringtone_set_frame);
            if (stub != null) {
                stub.inflate();
            }
            View rootRingtoneSetView = findViewById(R.id.ringtone_set_root);
            ringtoneSetViewHolder = new RingtoneSetViewHolder(rootRingtoneSetView);
            ringtoneSetViewHolder.backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });

            RingtoneSetter ringtoneSetter = RingtoneConfig.getInstance().getRingtoneSetter();
            ringtoneSetViewHolder.setSomeoneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ringtoneSetter != null) {
                        ringtoneSetter.onSetForSomeOne(ringtone);
                    }

                }
            });

            ringtoneSetViewHolder.setDefaultButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ringtoneSetter != null) {
                        ringtoneSetter.onSetAsDefault(ringtone);
                    }
                }
            });
        }

        ringtoneSetViewHolder.rootView.setVisibility(View.VISIBLE);

        ringtoneSetViewHolder.title.setText(ringtone.getTitle());
        ringtoneSetViewHolder.singer.setText(ringtone.getSinger());
        RingtoneImageLoader imageLoader = RingtoneConfig.getInstance().getRingtoneImageLoader();
        imageLoader.loadImage(
                ringtoneSetViewHolder.cover.getContext(),
                ringtone.getImgUrl(),
                ringtoneSetViewHolder.cover,
                R.drawable.ringtone_item_cover_default);
    }

    private static class RingtoneSetViewHolder {
        private final TextView title;
        private final TextView singer;
        private final ImageView cover;
        private final TextView setDefaultButton;
        private final TextView setSomeoneButton;

        private View rootView;
        private View backButton;


        public RingtoneSetViewHolder(View rootView) {
            this.rootView = rootView;
            rootView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    // block event
                    return true;
                }
            });

            backButton = rootView.findViewById(R.id.nav_back);
            title = (TextView) rootView.findViewById(R.id.ringtone_title);
            singer = (TextView) rootView.findViewById(R.id.ringtone_singer);
            cover = (ImageView) rootView.findViewById(R.id.cover_image);
            setDefaultButton = rootView.findViewById(R.id.ringtone_set_default);
            setSomeoneButton = rootView.findViewById(R.id.ringtone_set_someone);


            Drawable drawable1 = BackgroundDrawables.createBackgroundDrawable(Color.parseColor("#FFE048"),
                    Dimensions.pxFromDp(26), true);

            Drawable drawable2 = BackgroundDrawables.createBackgroundDrawable(Color.parseColor("#E8E8E9"),
                    Dimensions.pxFromDp(26), true);

            setDefaultButton.setBackground(drawable1);

            setSomeoneButton.setBackground(drawable2);
        }

        public boolean isVisible() {
            return rootView.getVisibility() == View.VISIBLE;
        }

        public void hide() {
            rootView.setVisibility(View.GONE);
        }
    }
}

package com.colorphone.ringtones.view;

import android.content.Context;
import android.graphics.Rect;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.colorphone.ringtones.R;
import com.colorphone.ringtones.RingtoneApi;
import com.colorphone.ringtones.RingtoneManager;
import com.colorphone.ringtones.RingtoneSetDelegate;
import com.colorphone.ringtones.SubColumnsAdapter;
import com.colorphone.ringtones.bean.RingtoneBean;
import com.colorphone.ringtones.bean.RingtoneListResultBean;
import com.colorphone.ringtones.module.Column;
import com.colorphone.ringtones.module.Ringtone;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;
import com.superapps.util.Toasts;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sundxing
 */
public class RingtoneEntranceActivity extends AppCompatActivity implements ResizeTextTabLayout.OnTabSelectListener {

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
    private RingtoneSetDelegate mRingtoneSetDelegate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initColumns();
        mRingtoneApi = new RingtoneApi();
        mRingtoneSearchAdapter = new RingtoneSearchAdapter(this, mRingtoneApi);

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


        mRingtoneSetDelegate = new RingtoneSetDelegate(this);

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
        mRingtoneSearchAdapter.setSearchText(text);
        mRingtoneApi.search(text, 0, new RingtoneApi.ResultCallback<RingtoneListResultBean>() {
            @Override
            public void onFinish(@Nullable RingtoneListResultBean bean) {
                List<Ringtone> results = new ArrayList<>();
                if (bean != null) {
                    mRingtoneSearchAdapter.setSizeTotalCount(bean.getTotal());
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
        if (mRingtoneSetDelegate.handleBackPress()) {
        } else if (inSearchMode) {
            cancelSearchMode();
        } else {
            super.onBackPressed();
        }
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
                recyclerView.setAdapter(new RingtoneListAdapter(RingtoneEntranceActivity.this, mRingtoneApi, column.getId(), index == 0));

            } else {
                // 分类
                targetFrameView = getLayoutInflater().inflate(R.layout.frame_ringtone_list_calssify, columnFrameContainer, false);

                final View expandBtn = targetFrameView.findViewById(R.id.ringtone_classification_expand_btn);
                final RecyclerView recyclerView = targetFrameView.findViewById(R.id.ringtone_list);
                recyclerView.setLayoutManager(new LinearLayoutManager(this,
                        LinearLayoutManager.VERTICAL,
                        false));

                String subColumnId = RingtoneManager.getInstance().getSelectedSubColumn().getId();
                final RingtoneListAdapter adatper = new RingtoneListAdapter(this, mRingtoneApi, subColumnId, false);
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
        RingtoneManager.getInstance().setRingtoneSetHandler(mRingtoneSetDelegate);
        mRingtoneSetDelegate.onStart();
    }

}
package com.devivan.smartdataaccess.Smart.Controllers;

import android.annotation.SuppressLint;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.devivan.smartdataaccess.Smart.Beans.SmartBean;
import com.devivan.smartdataaccess.Smart.Adapters.RecyclerViewAdapter;
import com.devivan.smartdataaccess.Smart.DAO.SQLiteUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class SmartAbstractActivity extends AppCompatActivity {

    // Constants //
    protected static final String SEPARATOR = "_";
    protected static final String COMMA = ",";
    protected static final String EMPTY = "";
    protected static final String SIZE = "size";
    protected static final String FILTER = "filter";
    protected static final String INSERT = "insert";
    public static final String DELETE = "delete";
    // Constants //

    // Lists //
    protected TypedArray smartLines = null;
    protected List<View> smartViews = new ArrayList<>();
    protected List<View> listViews = new ArrayList<>();
    protected List<RecyclerView> listRecyclerViews = new ArrayList<>();
    // Lists //

    protected void setSmartContentView(int layoutResID, Integer arrayResID, int... smartViews) {
        setContentView(layoutResID);
        if (null != arrayResID && -1 != arrayResID) smartLines = getResources().obtainTypedArray(arrayResID);
        for (int smartView : smartViews) this.smartViews.add(findViewById(smartView));
    }

    protected void bindDataToView() {
        for (View view : smartViews) findViews(view);
        getData();
    }

    protected String getKey(View view) {
        return view.getResources().getResourceEntryName(view.getId());
    }

    protected boolean isButton(View view) {
        return view instanceof Button;
    }

    protected boolean isImageView(View view) {
        return view instanceof ImageView;
    }

    protected boolean isTextView(View view) {
        return view instanceof TextView;
    }

    protected void cleanTextView(TextView textView) {
        textView.setText(EMPTY);
    }

    protected boolean isRecyclerView(View view) {
        return view instanceof RecyclerView;
    }

    protected boolean isLayout(View view) {
        return view instanceof ViewGroup;
    }

    protected void findViews(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                View v = viewGroup.getChildAt(i);
                if (isLayout(v)) {
                    if (isRecyclerView(v)) listViews.add(v);
                    else findViews(v);
                } else listViews.add(v);
            }
        } else listViews.add(view);
    }

    protected void getData() {
        if (!listViews.isEmpty()) {
            SQLiteUtil.connect(this);
            for (View view : listViews) bind(view);
        }
    }

    protected void displayData(View view) {
        String key = getKey(view);
        if ((isButton(view) || isImageView(view)) && key.split(SEPARATOR).length == 2) { // Button | ImageView
            String action = key.split(SEPARATOR)[0];
            String bean = key.split(SEPARATOR)[1];
            if (INSERT.equals(action)) {
                view.setOnClickListener(v -> {
                    List<TextView> textViews = listViews.stream().distinct().filter(tw -> getKey(tw).startsWith(bean + SEPARATOR) && tw instanceof TextView).map(vw -> (TextView) vw).collect(Collectors.toCollection(ArrayList::new));
                    if (!textViews.isEmpty()) {
                        for (TextView tw : textViews) {
                            SQLiteUtil.addValue(getKey(tw), tw.getText().toString());
                            cleanTextView(tw);
                        }
                    }
                    if (listRecyclerViews.stream().anyMatch(rv -> getKey(rv).endsWith(bean))) {
                        RecyclerView recyclerView = listRecyclerViews.stream().filter(rv -> getKey(rv).endsWith(bean)).findFirst().get();
                        RecyclerViewAdapter recyclerViewAdapter = (RecyclerViewAdapter) recyclerView.getAdapter();
                        Objects.requireNonNull(recyclerViewAdapter).notifyItemInserted(0);
                    }
                });
            }
        }
        else if (isRecyclerView(view) && key.split(SEPARATOR).length == 2) { // RecyclerView
            RecyclerView recyclerView = (RecyclerView) view;
            listRecyclerViews.add(recyclerView);
            String bean = key.split(SEPARATOR)[1];
            int resource = null != smartLines ? smartLines.getResourceId(Integer.parseInt(recyclerView.getTag().toString()), -1) : -1;;
            List<List<SmartBean>> listOfListSmartBeans = new ArrayList<>();

            if (resource != -1) {
                @SuppressLint("InflateParams") ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(this).inflate(resource, null);
                int count = viewGroup.getChildCount();
                for (int i = 0; i < count; i++) {
                    View v = viewGroup.getChildAt(i);
                    if (getKey(v).startsWith(bean) && isTextView(v)) {
                        List<SmartBean> smartBeans = SQLiteUtil.geValues(getKey(v));
                        if (listOfListSmartBeans.size() == smartBeans.size()) {
                            for (int x = 0; x < smartBeans.size(); x++) {
                                listOfListSmartBeans.get(x).add(smartBeans.get(x));
                            }
                        } else {
                            for (SmartBean smartBean : smartBeans) {
                                listOfListSmartBeans.add(new ArrayList<>(Collections.singletonList(smartBean)));
                            }
                        }
                    }
                }
                recyclerView.setAdapter(new RecyclerViewAdapter(recyclerView, resource, listOfListSmartBeans));
            }
        }
        else if (isTextView(view)) { // TextView
            TextView textView = (TextView) view;
            String value = SQLiteUtil.getValue(key);
            if (value != null) textView.setText(value);
        }
    }

    protected void bind(View view) {
        displayData(view);
        String key = getKey(view);
        if (view instanceof EditText) { // EditText
            EditText editText = (EditText) view;
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    SQLiteUtil.setValue(key, s.toString());

                    if  (key.split(SEPARATOR).length == 2) {
                        String bean = key.split(SEPARATOR)[0];
                        String action = key.split(SEPARATOR)[1];
                        if (FILTER.equals(action)) {
                            List<String> filterParams = new ArrayList<>();
                            if (editText.getTag().toString().split(COMMA).length > 0) {
                                filterParams.addAll(Arrays.asList(editText.getTag().toString().split(COMMA)));
                            }
                            if (listRecyclerViews.stream().anyMatch(rv -> getKey(rv).endsWith(bean))) {
                                RecyclerView recyclerView = listRecyclerViews.stream().filter(rv -> getKey(rv).endsWith(bean)).findFirst().get();
                                RecyclerViewAdapter recyclerViewAdapter = (RecyclerViewAdapter) recyclerView.getAdapter();

                                List<List<SmartBean>> listOfListSmartBeans = new ArrayList<>();

                                int resource = null != smartLines ? smartLines.getResourceId(Integer.parseInt(recyclerView.getTag().toString()), -1) : -1;

                                if (resource != -1) {
                                    @SuppressLint("InflateParams") ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(SmartAbstractActivity.this).inflate(resource, null);
                                    for (int i = 0; i < viewGroup.getChildCount(); i++) {
                                        View v = viewGroup.getChildAt(i);
                                        if (getKey(v).startsWith(bean) && isTextView(v) && (filterParams.isEmpty() || filterParams.stream().anyMatch(fp -> getKey(v).endsWith(SEPARATOR + fp)))) {
                                            List<SmartBean> smartBeans = SQLiteUtil.geValues(getKey(v), s.toString());
                                            if (listOfListSmartBeans.size() == smartBeans.size()) {
                                                for (int x = 0; x < smartBeans.size(); x++) {
                                                    listOfListSmartBeans.get(x).add(smartBeans.get(x));
                                                }
                                            } else {
                                                for (SmartBean smartBean : smartBeans) {
                                                    listOfListSmartBeans.add(new ArrayList<>(Collections.singletonList(smartBean)));
                                                }
                                            }
                                        }
                                    }
                                    Objects.requireNonNull(recyclerViewAdapter).listOfListSmartBeans = listOfListSmartBeans;
                                    Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
                                }
                            }
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        } else if (view instanceof TextView) { // TextView
            if  (key.split(SEPARATOR).length == 2) {
                String bean = key.split(SEPARATOR)[0];
                String action = key.split(SEPARATOR)[1];
                if (SIZE.equals(action) && listRecyclerViews.stream().anyMatch(rv -> getKey(rv).endsWith(bean))) {
                    RecyclerView recyclerView = listRecyclerViews.stream().filter(rv -> getKey(rv).endsWith(bean)).findFirst().get();
                    RecyclerViewAdapter recyclerViewAdapter = (RecyclerViewAdapter) recyclerView.getAdapter();
                    ((TextView) view).setText(String.valueOf(Objects.requireNonNull(recyclerViewAdapter).listOfListSmartBeans.size()));
                    Objects.requireNonNull(recyclerViewAdapter).registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                        @Override
                        public void onItemRangeRemoved(int positionStart, int itemCount) {
                            super.onItemRangeChanged(positionStart, itemCount);
                            ((TextView) view).setText(String.valueOf(recyclerViewAdapter.listOfListSmartBeans.size()));
                        }

                        @Override
                        public void onItemRangeInserted(int positionStart, int itemCount) {
                            super.onItemRangeInserted(positionStart, itemCount);
                            ((TextView) view).setText(String.valueOf(recyclerViewAdapter.listOfListSmartBeans.size()));
                        }

                        @Override
                        public void onChanged() {
                            super.onChanged();
                            ((TextView) view).setText(String.valueOf(recyclerViewAdapter.listOfListSmartBeans.size()));
                        }
                    });
                }
            }
        }
    }
}

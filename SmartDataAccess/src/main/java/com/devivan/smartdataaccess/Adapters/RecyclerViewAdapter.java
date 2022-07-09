package com.devivan.smartdataaccess.Adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devivan.smartdataaccess.Beans.SmartBean;
import com.devivan.smartdataaccess.Controllers.SmartAbstractActivity;
import com.devivan.smartdataaccess.DAO.SQLiteUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    RecyclerView recyclerView;
    int resource;
    public List<List<SmartBean>> listOfListSmartBeans;
    private static TextWatcher textWatcher;

    public RecyclerViewAdapter(RecyclerView recyclerView, int resource, List<List<SmartBean>> listOfListSmartBeans) {
        this.recyclerView = recyclerView;
        this.resource = resource;
        this.listOfListSmartBeans = listOfListSmartBeans;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        displayData(holder, position);
    }

    @Override
    public int getItemCount() {
        return listOfListSmartBeans != null ? listOfListSmartBeans.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // EDITABLE DATA //
            ViewGroup viewGroup = (ViewGroup) itemView;
            int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                View view = viewGroup.getChildAt(i);
                String key = view.getResources().getResourceEntryName(view.getId());

                if ((view instanceof Button || view instanceof ImageView) && key.split("_").length == 2) {
                    String action = key.split("_")[0];
                    String bean = key.split("_")[1];
                    view.setOnClickListener(v -> {
                        int position = getAdapterPosition();
                        if (position != -1 && position < listOfListSmartBeans.size()) {
                            if (SmartAbstractActivity.DELETE.equals(action)) {
                                deleteSmartBean(bean, position);
                            }
                        }
                    });
                } else if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    textView.setOnFocusChangeListener((v, hasFocus) -> {
                        int position = getAdapterPosition();
                        if (position != -1 && position < listOfListSmartBeans.size()) {
                            if (hasFocus) {
                                textWatcher = new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence s, int start, int count1, int after) {

                                    }

                                    @Override
                                    public void onTextChanged(CharSequence s, int start, int before, int count1) {
                                        updateSmartBean(key, position);
                                    }

                                    @Override
                                    public void afterTextChanged(Editable s) {

                                    }
                                };
                                textView.addTextChangedListener(textWatcher);
                            } else {
                                textView.removeTextChangedListener(textWatcher);
                            }
                        }
                    });
                }
            }
        }
    }

    private void displayData(ViewHolder holder, int position) {
        ViewGroup viewGroup = (ViewGroup) holder.itemView;
        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = viewGroup.getChildAt(i);
            String key = v.getResources().getResourceEntryName(v.getId());
            if (key.startsWith(key.split("_")[0]+"_") && v instanceof TextView) {
                if (listOfListSmartBeans.get(position).stream().anyMatch(smartBean -> smartBean.getKey().equals(key))) {
                    TextView textView  = (TextView) v;
                    SmartBean smartBean = listOfListSmartBeans.get(position).stream().filter(o -> o.getKey().equals(key)).findFirst().get();
                    textView.setText(smartBean.getValue());
                }
            }
        }
    }

    private void updateSmartBean(String key, int position) {
        ViewGroup viewGroup = (ViewGroup) Objects.requireNonNull(recyclerView.findViewHolderForAdapterPosition(position)).itemView;
        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof TextView && view.getResources().getResourceEntryName(view.getId()).equals(key)) {
                for (SmartBean smartBean : listOfListSmartBeans.get(position).stream().filter(o -> o.getKey().equals(key)).collect(Collectors.toCollection(ArrayList::new))) {
                    smartBean.setValue(((TextView) view).getText().toString());
                    SQLiteUtil.setValue(smartBean.getId(), smartBean.getValue());
                }
            }
        }
    }

    private void deleteSmartBean(String bean, int position) {
        ViewGroup viewGroup = (ViewGroup) Objects.requireNonNull(recyclerView.findViewHolderForAdapterPosition(position)).itemView;
        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = viewGroup.getChildAt(i);
            String key = view.getResources().getResourceEntryName(view.getId());
            if (view instanceof TextView && key.startsWith(bean+"_")) {
                for (SmartBean smartBean : listOfListSmartBeans.get(position).stream().filter(o -> o.getKey().equals(key)).collect(Collectors.toCollection(ArrayList::new))) {
                    SQLiteUtil.delValue(smartBean.getId());
                }
            }
        }
        listOfListSmartBeans.remove(position);
        notifyItemRemoved(position);
    }
}

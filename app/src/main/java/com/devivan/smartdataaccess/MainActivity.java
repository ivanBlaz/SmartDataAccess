package com.devivan.smartdataaccess;

import android.os.Bundle;

import com.devivan.smartdataaccess.Smart.Controllers.SmartActivity;

public class MainActivity extends SmartActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSmartContentView(R.layout.activity_main, R.array.smartLines, R.id.clSmartLayout);
    }
}
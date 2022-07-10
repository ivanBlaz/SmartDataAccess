package com.devivan.smartdataaccess.Smart.Controllers;

public class SmartActivity extends SmartAbstractActivity {

    @Override
    protected void onResume() {
        super.onResume();
        bindDataToView();
    }
}

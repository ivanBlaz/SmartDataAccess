package com.devivan.smartdataaccess.Controllers;

public class SmartActivity extends SmartAbstractActivity {

    @Override
    protected void onResume() {
        super.onResume();
        bindDataToView();
    }

    @Override
    public void recreate() {
        super.recreate();
    }
}

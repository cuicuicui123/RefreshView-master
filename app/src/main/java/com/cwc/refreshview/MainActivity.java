package com.cwc.refreshview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private RefreshScrollView mRefreshScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRefreshScrollView = (RefreshScrollView) findViewById(R.id.refreshScrollView);
        View contentView = LayoutInflater.from(this).inflate(R.layout.content, mRefreshScrollView.getViewGroup(),
                false);
        mRefreshScrollView.newAddView(contentView);
    }
}

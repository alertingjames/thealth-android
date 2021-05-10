package com.app.thealth.classes;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class CustomSwipeRefreshLayout extends SwipeRefreshLayout {

    private ScrollView scrollview;

    public CustomSwipeRefreshLayout(Context context) {
        super(context);
    }

    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setView(ScrollView view) {
        this.scrollview = view;
    }

    @Override
    public boolean canChildScrollUp() {
        return scrollview.getScrollY() != 0;
    }

}
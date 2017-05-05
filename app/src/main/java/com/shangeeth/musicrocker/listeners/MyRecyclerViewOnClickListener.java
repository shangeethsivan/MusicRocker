package com.shangeeth.musicrocker.listeners;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by user on 04/05/17.
 */

public class MyRecyclerViewOnClickListener implements RecyclerView.OnItemTouchListener {

    OnItemClickListener mOnItemClickListener;
    GestureDetector mGestureDetector;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public MyRecyclerViewOnClickListener(Context mContext, OnItemClickListener mOnItemClickListener) {

        mGestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        this.mOnItemClickListener = mOnItemClickListener;

    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView pView, MotionEvent pMotionEvent) {

        View childView = pView.findChildViewUnder(pMotionEvent.getX(), pMotionEvent.getY());

        if (childView != null && mOnItemClickListener != null && mGestureDetector.onTouchEvent(pMotionEvent)) {
            mOnItemClickListener.onItemClick(childView, pView.getChildAdapterPosition(childView));
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}

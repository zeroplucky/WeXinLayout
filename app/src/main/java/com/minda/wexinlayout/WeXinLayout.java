package com.minda.wexinlayout;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Scroller;

/**
 * Created by Administrator on 2019/7/3.
 */
public class WeXinLayout extends FrameLayout {

    private String TAG = "layoutView";
    private boolean isDraggable = true;
    private int deltaY; // 偏移量
    private float lastY;
    private boolean direction = true;//true为上滑
    private Scroller scroller; //处理滑动事件
    private int maxOffset = 300;

    public WeXinLayout(@NonNull Context context) {
        this(context, null);
    }

    public WeXinLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeXinLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        scroller = new Scroller(context);
    }

    /**
     * @param recyclerView
     */
    public void setRecyclerView(RecyclerView recyclerView) {
        if (recyclerView == null) {
            throw new NullPointerException("recyclerView must not Null");
        }
        // 监听recyclerView的滑动事件
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                updateRecyclerViewScrollState(recyclerView);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                updateRecyclerViewScrollState(recyclerView);
            }
        });
    }


    private void updateRecyclerViewScrollState(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            int position = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
            if (position == 0) { // 第一个可见View
                View firstView = recyclerView.getChildAt(0);
                int top = firstView.getTop();
                int paddingTop = recyclerView.getPaddingTop();
                Log.e(TAG, "updateRecyclerViewScrollState: top = " + top + "  , paddingTop = " + paddingTop + "  ,  position = " + position);
                if (top == paddingTop) { // recyclerView处于顶部
                    isDraggable = true;
                    return;
                }
            }
        }
        isDraggable = false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if ((!isDraggable)) {
            return false;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                lastY = ev.getY();
                if (!scroller.isFinished()) {
                    scroller.forceFinished(true);
                    return true;
                }
            }
            break;
            case MotionEvent.ACTION_MOVE:
                if (ev.getY() < lastY && Math.abs(getScrollY()) == 0) {
                    return false;
                }
                return true;
            default:
                return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                lastY = ev.getY();
                if (Math.abs(getScrollY()) == maxOffset) {
                    return false;
                }
                return true;
            }
            case MotionEvent.ACTION_UP: {
                int scrollY = getScrollY();
                int offset = 0;
                Log.e(TAG, "ACTION_UP:  --------------------------scrollY = " + scrollY + " , deltaY = " + deltaY + "  , ");
                if (direction) { // 下滑
                    if (Math.abs(scrollY) == maxOffset) {
                        return false;
                    }
                    if (Math.abs(scrollY) > maxOffset) {
                        offset = Math.abs(Math.abs(scrollY) - maxOffset);
                    } else if (Math.abs(scrollY) < maxOffset) {
                        offset = -Math.abs(maxOffset - Math.abs(scrollY));
                    }
                    scroller.startScroll(getScrollX(), scrollY, 0, offset);
                } else { //上滑
                    if (scrollY == 0) {
                        return false;
                    }
                    scroller.startScroll(getScrollX(), scrollY, 0, -scrollY);
                    Log.e(TAG, "onTouchEvent: " + scrollY + "  " + offset);
                }
                invalidate();
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                deltaY = (int) ((int) (ev.getY() - lastY) * 0.6f);
                int scrollY = getScrollY();
                Log.e(TAG, "onTouchEvent: ======================================= scrollY =  " + scrollY + "  , deltaY = " + deltaY);
                if (deltaY > 0) { //  deltaY > 0 为下滑
                    if (Math.abs(scrollY) < maxOffset && deltaY - scrollY <= maxOffset) {
                        scrollBy(0, -deltaY);
                    }
                    direction = true;
                } else if (deltaY < 0) {// deltaY < 0 为上滑
                    if (scrollY < 0 && Math.abs(deltaY + scrollY) <= maxOffset) { // 和坐标值的正反相反
                        scrollBy(0, -deltaY);
                    }
                    direction = false;
                }
                lastY = ev.getY();
                return true;
            }
            default:
                return false;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        if (!scroller.isFinished() && scroller.computeScrollOffset()) {
            int currY = scroller.getCurrY();
            Log.e(TAG, "computeScroll: currY = " + currY);
            scrollTo(0, currY);
            if (currY > 0) {
                scroller.abortAnimation();
            } else {
                invalidate();
            }
        }
    }

}

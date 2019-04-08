package com.example.android_practice;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class GridDivider extends RecyclerView.ItemDecoration {

//    private Drawable mDividerDarwable;
//    private int mDividerHight = 1;
//    private Paint mColorPaint;
//
//
//    public final int[] ATRRS = new int[]{android.R.attr.listDivider};
//
//    public GridDivider(Context context) {
//        final TypedArray ta = context.obtainStyledAttributes(ATRRS);
//        this.mDividerDarwable = ta.getDrawable(0);
//        ta.recycle();
//    }
//
//    public GridDivider(Context context, int dividerHight) {
//        this(context);
//        mDividerHight = dividerHight;
//    }

//    /*
//     int dividerHight  分割线的线宽
//     int dividerColor  分割线的颜色
//     */
//    public GridDivider(Context context, int dividerHight, int dividerColor) {
//        this(context);
//        mDividerHight = dividerHight;
//        mColorPaint = new Paint();
//        mColorPaint.setColor(dividerColor);
//    }
//
//    /*
//     int dividerHight  分割线的线宽
//     Drawable dividerDrawable  图片分割线
//     */
//    public GridDivider(Context context, int dividerHight, Drawable dividerDrawable) {
//        this(context);
//        mDividerHight = dividerHight;
//        mDividerDarwable = dividerDrawable;
//    }
//
//    @Override
//    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
//        super.onDraw(c, parent, state);
//        //画水平和垂直分割线
//        drawHorizontalDivider(c, parent);
//        drawVerticalDivider(c, parent);
//    }
//
//    public void drawVerticalDivider(Canvas c, RecyclerView parent) {
//        final int childCount = parent.getChildCount();
//        for (int i = 0; i < childCount; i++) {
//            final View child = parent.getChildAt(i);
//            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
//
//            final int top = child.getTop() - params.topMargin;
//            final int bottom = child.getBottom() + params.bottomMargin;
//
//            int left = 0;
//            int right = 0;
//
//            //左边第一列
//            if ((i % 3) == 0) {
//                //item左边分割线
//                left = child.getLeft();
//                right = left + mDividerHight;
//                mDividerDarwable.setBounds(left, top, right, bottom);
//                mDividerDarwable.draw(c);
//                if (mColorPaint != null) {
//                    c.drawRect(left, top, right, bottom, mColorPaint);
//                }
//                //item右边分割线
//                left = child.getRight() + params.rightMargin - mDividerHight;
//                right = left + mDividerHight;
//            } else {
//                //非左边第一列
//                left = child.getRight() + params.rightMargin - mDividerHight;
//                right = left + mDividerHight;
//            }
//            //画分割线
//            mDividerDarwable.setBounds(left, top, right, bottom);
//            mDividerDarwable.draw(c);
//            if (mColorPaint != null) {
//                c.drawRect(left, top, right, bottom, mColorPaint);
//            }
//
//        }
//    }
//
//    public void drawHorizontalDivider(Canvas c, RecyclerView parent) {
//
//        final int childCount = parent.getChildCount();
//        for (int i = 0; i < childCount; i++) {
//            final View child = parent.getChildAt(i);
//            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
//
//            final int left = child.getLeft() - params.leftMargin - mDividerHight;
//            final int right = child.getRight() + params.rightMargin;
//            int top = 0;
//            int bottom = 0;
//
//            // 最上面一行
//            if ((i / 3) == 0) {
//                //当前item最上面的分割线
//                top = child.getTop();
//                //当前item下面的分割线
//                bottom = top + mDividerHight;
//                mDividerDarwable.setBounds(left, top, right, bottom);
//                mDividerDarwable.draw(c);
//                if (mColorPaint != null) {
//                    c.drawRect(left, top, right, bottom, mColorPaint);
//                }
//                top = child.getBottom() + params.bottomMargin;
//                bottom = top + mDividerHight;
//            } else {
//                top = child.getBottom() + params.bottomMargin;
//                bottom = top + mDividerHight;
//            }
//            //画分割线
//            mDividerDarwable.setBounds(left, top, right, bottom);
//            mDividerDarwable.draw(c);
//            if (mColorPaint != null) {
//                c.drawRect(left, top, right, bottom, mColorPaint);
//            }
//        }
//    }

//    public static int dp2px(Context context, float dipValue) {
//        final float scale = context.getResources().getDisplayMetrics().density;
//        return (int) (dipValue * scale + 0.5f);
//    }

//    @Override
//    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
//        // super.getItemOffsets(outRect, view, parent, state);
//        int position = parent.getChildAdapterPosition(view);
//        // 每列分配的间隙大小，包括左间隙和右间隙
//        int colPadding = dp2px(parent.getContext(), 0) * (mDividerHight + 1) / mDividerHight;
//        // 列索引
//        int colIndex = position % mDividerHight;
//        // 列左、右空隙。右间隙=space-左间隙
//        outRect.left = dp2px(parent.getContext(), 0) * (colIndex + 1) - colPadding * colIndex;
//        outRect.right = colPadding * (colIndex + 0) - dp2px(parent.getContext(), 0) * (colIndex + 1);
//        // 行间距
//        if (position >= mDividerHight) {
//            outRect.top = dp2px(parent.getContext(), 0);
//        }
//    }

//    @Override
//    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
//        //不是第一個的格子都設一個左邊和底部的間距
//        outRect.left = mDividerHight;
//        outRect.bottom = mDividerHight;
//        //由於每行都只有3個,所以第一個都是3的倍數,把左邊距設為0
//        if (parent.getChildLayoutPosition(view) % mDividerHight == 0) {
//            outRect.left = 0;
//        }
//    }

    private int spanCount;//几行或几列
    private int orientation;//方向
    private int itemSpace;//空隙大小

    private Rect mBounds = new Rect();
    private Paint mPaint;//用来将空隙绘制成红色的画笔

    public GridDivider(GridLayoutManager gridLayoutManager) {
        spanCount = gridLayoutManager.getSpanCount();
        orientation = gridLayoutManager.getOrientation();
        // initPaint();
    }

//    private void initPaint() {
//        mPaint = new Paint();
//        mPaint.setAntiAlias(true);
//        mPaint.setColor(Color.RED);
//    }

    public void setItemSpace(int space) {
        itemSpace = space;
    }

//    @Override
//    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
//        c.save();
//        final int childCount = parent.getChildCount();
//        for (int i = 0; i &lt; childCount; i++) {
//            final View child = parent.getChildAt(i);
//            parent.getLayoutManager().getDecoratedBoundsWithMargins(child, mBounds);
//            c.drawRect(mBounds, mPaint);
//        }
//        c.restore();
//    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        //获取当前view的layoutPosition
        int itemPosition = parent.getChildLayoutPosition(view);
        //计算该View位于哪一行哪一列
        int positionOfGroup = itemPosition % spanCount;
        int itemGroup = itemPosition / spanCount;

        //根据不同方向进行不同处理，最终效果都要实现除四周的View 外，内部的View之间横竖都以相同空隙间隔开
        //实现方式，以水平方向为例：
        //每个view的left和bottom都设置相同间隙
        //去掉第1列的left，和最后一行的bottom，也就实现了除四周外内部view都以相同间隙空隔开
        if (orientation == LinearLayoutManager.HORIZONTAL) {
            outRect.set(itemSpace, 0, 0, itemSpace);
            if (itemGroup == 0) {
                outRect.left = 0;
            }
            if (positionOfGroup == (spanCount - 1)) {
                outRect.bottom = 0;
            }
        } else if (orientation == LinearLayoutManager.VERTICAL) {
            outRect.set(0, itemSpace, itemSpace, 0);
            if (itemGroup == 0) {
                outRect.top = 0;
            }
            if (positionOfGroup == (spanCount - 1)) {
                outRect.right = 0;
            }
        }
    }
}

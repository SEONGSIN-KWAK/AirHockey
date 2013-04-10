package com.kss.airhockey;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;

import com.kss.airhockey.Balls.Ball;

import java.util.Vector;



public class Balls extends Vector<Ball> {
    /***********************************************************************/
    public static class Ball {
        private float mX;
        private float mY;
        private int mResourceID;
        private Bitmap mBitmap;
        private int mCurrentFrmae = 0;
        private int mTargetFrmae = 0;
        private boolean mIsRemoving = false;

        public Ball(float x, float y) {
            this(x, y, 0);
        }

        public Ball(float x, float y, int resourceID) {
            mX = x;
            mY = y;
            mResourceID = resourceID;
        }

        public float getX() {
            return mX;
        }

        public float getY() {
            return mY;
        }

        public void setX(float x) {
            mX = x;
        }

        public void setY(float y) {
            mY = y;
        }

        public int getResourceID() {
            return mResourceID;
        }

        public int getCurrentFrmae() {
            return mCurrentFrmae;
        }

        public void setCurrentFrmae(int mCurrentFrmae) {
            this.mCurrentFrmae = mCurrentFrmae;
        }

        public int getTargetFrmae() {
            return mTargetFrmae;
        }

        public void setTargetFrmae(int mTargetFrmae) {
            this.mTargetFrmae = mTargetFrmae;
        }

        public void setRemoving(boolean bool) {
            mIsRemoving = bool;
        }

        public boolean isRemoving() {
            return this.mIsRemoving;
        }

    }

    /***********************************************************************/



    public static final int NOT_CONTAINED = -1;


    private String mName;
    private PointF mPoint;
    private float mVector;
    private RectF mRect;
    private int mWidth;
    private int mHeight;
    private Bitmap mBitmap;


    public Balls(Bitmap bitmap) {
        this(bitmap, 0);
    }

    public Balls(Bitmap bitmap, int resoucreID) {
        if (bitmap == null) {
            return;
        }

        mWidth = bitmap.getWidth();
        mHeight = bitmap.getHeight();
        mBitmap = bitmap;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public RectF getRectF(int index) {

        Ball ball = this.get(index);

        mRect = null;
        mRect =
                new RectF(ball.mX - (mWidth / 2), ball.mY - (mHeight / 2), ball.mX + (mWidth / 2),
                        ball.mY + (mHeight / 2));

        return mRect;
    }

    public int getIndexIfContained(float x, float y) {
        for (int i = this.size() - 1; i > -1; i--) {
            RectF rect = this.getRectF(i);

            if (rect.contains(x, y)) {
                return i;
            }
        }
        return NOT_CONTAINED;
    }
}

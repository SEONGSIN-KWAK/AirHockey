package com.kss.airhockey;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.renderscript.Matrix3f;
import android.util.Log;

import com.kss.airhockey.Balls.Ball;

public class PhysicsEngine {
    public static final String TAG = PhysicsEngine.class.getName();
    public static final float G = 9.80665f;

    private float mWidth = 0.0f;
    private float mheight = 0.0f;
    private int mFrame = 33;

    public PhysicsEngine(float width, float height, int frame) {
        mWidth = width;
        mheight = height;
        mFrame = frame;
    }

    public void Dynamic2D(Ball b) {
        Log.d(TAG, "[Dynamic2D]");
        setFomulaA(b);
    }

    private void setFomulaA(Ball b) {
        float v = b.getMass() * (G / mFrame) + b.getVelocity();
        float y = b.getY() + v;

        b.setY(y > mheight - (b.getHeight() / 2) ? mheight - (b.getHeight() / 2) : y);
        b.setVelocity(y > mheight - (b.getHeight() / 2) ? 0 : v);
    }

    private void VectorDynamic(Matrix previous, Matrix current) {

    }

    public boolean isCrashed(Matrix3f map, Ball b){
        
        int xStart = (int)(b.getX()-(b.getWidth()/2));
        int xEnd = (int)(b.getX()+(b.getWidth()/2));
        int yStart = (int)(b.getY()-(b.getHeight()/2));
        int yEnd = (int)(b.getY()-(b.getHeight()/2));
        
        for(int i = )
        
        return false;
    }
}

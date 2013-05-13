package com.kss.airhockey;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import com.kss.airhockey.Balls.Ball;

public class PhysicsEngine {
    public static final String TAG = PhysicsEngine.class.getName();
    public static final float G = 9.80665f;
    private static RectF mCoordination;

    private int mFrame = 33;


    public static PointF convertCanvas2Engine(float x, float y) {
        PointF pointF = new PointF(x - mCoordination.left, y - mCoordination.bottom);
        return pointF;
    }

    public static PointF convertEngine2Canvas(float x, float y) {
        PointF pointF = new PointF(x + mCoordination.left, y + mCoordination.bottom);
        return pointF;
    }



    public PhysicsEngine(float left, float top, float right, float bottoom, int frame) {
        this(new RectF(left, top, right, bottoom), frame);
    }

    public PhysicsEngine(RectF coordination, int frame) {
        mFrame = frame;
        setCoordination(coordination);
    }

    private void initialize() {

    }

    public void setCoordination(float left, float top, float right, float bottoom) {
        this.setCoordination(new RectF(left, top, right, bottoom));
    }

    public void setCoordination(RectF coordination) {
        mCoordination = coordination;
    }

    public RectF getCoordination() {
        return mCoordination;
    }

    public PointF convertPoint2Coordination(float x, float y) {
        return null;
    }



    public void Dynamic2D(Ball b) {
        Log.d(TAG, "[Dynamic2D]");
        setFomulaA(b);
    }

    private void setFomulaA(Ball b) {
        float v = b.getMass() * (G / mFrame) + b.getVelocity();
        float y = b.getY() + v;

        float height = Math.abs(getCoordination().height());

        b.setY(y > height - (b.getHeight() / 2) ? height - (b.getHeight() / 2) : y);
        b.setVelocity(y > height - (b.getHeight() / 2) ? 0 : v);
    }

    private void VectorDynamic(Matrix previous, Matrix current) {

    }
}

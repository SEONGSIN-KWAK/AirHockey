package com.kss.airhockey;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.kss.airhockey.Balls.Ball;

import java.util.Iterator;
import java.util.Vector;


public class MySurfaceView extends SurfaceView implements Callback {
    private static final String TAG = MySurfaceView.class.getName();
    public static boolean DEBUG_MODE = true;

    private class ViewThread extends Thread {
        private int mFramerate;
        SurfaceHolder mMySurfaceholder;
        MySurfaceView mMySurfaceView;
        private boolean mFlag = false;

        public ViewThread(MySurfaceView view, SurfaceHolder holder) {
            this(view, holder, 33);
        }

        public ViewThread(MySurfaceView view, SurfaceHolder holder, int fps) {
            mMySurfaceholder = holder;
            mMySurfaceView = view;
            mFramerate = fps;
        }

        @Override
        public void run() {
            while (mFlag) {
                Canvas canvas = null;
                canvas = mMySurfaceholder.lockCanvas();

                try {
                    synchronized (mMySurfaceholder) {
                        mMySurfaceView.onDraw(canvas);
                        Thread.sleep(mFramerate);
                    }
                } catch (Exception e) {

                } finally {
                    if (canvas != null) {
                        mMySurfaceholder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }

        public int getFramerate() {
            return mFramerate;
        }

        public void setFlag(boolean flag) {
            mFlag = flag;
        }

        public boolean getFlag() {
            return mFlag;
        }

    }


    private static final float DURATION_FOR_ANIMATION = 1000f;

    private Context mContext;
    private ViewThread myViewThread;
    private Balls mBallsVector;
    private Balls mGoalVector;
    private Balls mSummitVector;

    private Paint paintForText;
    private Paint paintForCanvasClaening;

    Bitmap mBitmapBall = new BitmapFactory().decodeResource(getResources(), R.drawable.ball);
    Bitmap mBitmapGoal = new BitmapFactory().decodeResource(getResources(), R.drawable.goal);

    private Vector<Bitmap> mLoadedResource;

    int pointX;
    int pointY;

    PhysicsEngine mPhysicsEngine;



    public MySurfaceView(Context context) {
        super(context);
        mContext = context;
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        myViewThread = new ViewThread(this, holder, 5);

        // Vector Setting
        mBallsVector = new Balls(mBitmapBall);
        mGoalVector = new Balls(mBitmapGoal, R.drawable.goal);

        paintForText = new Paint();
        paintForText.setTextSize(30);
        paintForText.setColor(Color.GREEN);

        paintForCanvasClaening = new Paint();
        paintForCanvasClaening.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
        mSummitVector = new Balls(null);


        // Loading resources
        mLoadedResource = new Vector<Bitmap>();
        mLoadedResource.add(mBitmapBall);
        int j = (int)(DURATION_FOR_ANIMATION / myViewThread.getFramerate());
        for (int i = 1; i < j; i++) {
            mLoadedResource.add(getResizedBitmapImage(mBitmapBall, i, j));
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (myViewThread == null || myViewThread.isAlive()) {
            return;
        }

        if (mGoalVector.size() == 0) {
            Ball goal = new Ball(360.0f, 800.0f, mBitmapGoal);
            goal.setMass(0.0f);
            mBallsVector.add(goal);
            mGoalVector.add(goal);
        }

        // PhysicsEngine
        mPhysicsEngine =
                new PhysicsEngine(0.0f, (float)getHolder().getSurfaceFrame().height(),
                        (float)getHolder().getSurfaceFrame().width(), 0.0f,
                        myViewThread.getFramerate());

        myViewThread.setFlag(true);
        myViewThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (myViewThread == null) {
            return;
        }

        boolean retry = true;
        myViewThread.setFlag(false);
        while (retry) {
            try {
                myViewThread.join();
                retry = false;
            } catch (Exception e) {
                Log.d(getClass().getName(), "Error occurred while surfaceview is destroying : "
                        + e.getMessage());
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF point = PhysicsEngine.convertCanvas2Engine(event.getX(), event.getY());

        Ball buff;
        int index;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                index = mBallsVector.getIndexIfContained(point.x, point.y);
                if (index != Balls.NOT_CONTAINED) {
                    buff = mBallsVector.remove(index);
                    buff.setX(point.x);
                    buff.setY(point.y);
                    mBallsVector.add(buff);
                    return true;
                }
                mBallsVector.add(new Ball(point.x, point.y, mBitmapBall));
                return true;

            case MotionEvent.ACTION_MOVE:
                buff = mBallsVector.remove(mBallsVector.size() - 1);
                buff.setX(point.x);
                buff.setY(point.y);
                buff.setPicked(true);
                mBallsVector.add(buff);
                return true;

            case MotionEvent.ACTION_UP:

                buff = mBallsVector.lastElement();
                buff.setPicked(false);
                if (buff.getBitmap() == mBitmapGoal) {
                    return true;
                }

                index = mGoalVector.getIndexIfContained(point.x, point.y);
                if (index != Balls.NOT_CONTAINED) {
                    if (!buff.isRemoving()) {
                        buff.setTargetFrmae((int)(DURATION_FOR_ANIMATION / myViewThread.getFramerate()));
                        buff.setCurrentFrmae(0);
                        buff.setRemoving(true);
                        return true;
                    }

                    if (buff.getCurrentFrmae() < buff.getTargetFrmae() - 1) {
                        mBallsVector.remove(mBallsVector.size() - 1);
                        return true;
                    }
                    return true;
                }

            default:
                return false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        synchronized (mBallsVector) {
            if (!myViewThread.getFlag()) {
                return;
            }
            Long start = System.currentTimeMillis();

            canvas.drawPaint(paintForCanvasClaening);

            // summit
            Iterator<Ball> it = getSummitedIterator();
            Ball p = null;
            while (it.hasNext() && myViewThread.getFlag()) {
                p = it.next();
                Bitmap image = p.getBitmap();
                if (p.isRemoving()) {
                    if (p.getCurrentFrmae() < p.getTargetFrmae()) {
                        int currentFps = p.getCurrentFrmae();
                        currentFps++;
                        p.setCurrentFrmae(currentFps);
                        image = mLoadedResource.get(currentFps);// getResizedBitmapImage(image, p);
                    } else {
                        mBallsVector.remove(p);
                        continue;
                    }
                }
                if (!p.isPicked()) {
                    mPhysicsEngine.Dynamic2D(p);
                }
                PointF canvansPoint = PhysicsEngine.convertEngine2Canvas(p.getX(), p.getY());
                canvas.drawBitmap(image, canvansPoint.x - (image.getWidth() / 2), canvansPoint.y
                        - (image.getHeight() / 2), null);
            }

            Long end = System.currentTimeMillis();

            if (DEBUG_MODE) {
                canvas.drawText("Expended Time:\t" + (end - start), 50, 50, paintForText);
                canvas.drawText("" + (int)p.getX() + "\t:\t" + (int)p.getY(), 50, 90, paintForText);
                canvas.drawText("Object num :\t" + mBallsVector.size(), 50, 140, paintForText);
                canvas.drawText("Goal position" + (int)mGoalVector.lastElement().getX() + "\t:\t"
                        + (int)mGoalVector.lastElement().getX(), 50, 180, paintForText);

                canvas.drawText("Animation Frame - Current :\t"
                        + (int)mBallsVector.lastElement().getCurrentFrmae(), 50, 220, paintForText);
                canvas.drawText("Animation Frame - Target :\t"
                        + (int)mBallsVector.lastElement().getTargetFrmae(), 50, 260, paintForText);

            }
            super.onDraw(canvas);
        }
    }

    private Iterator<Ball> getSummitedIterator() {

        Balls summitedVactor = (Balls)mBallsVector.clone();

        // Iterator<Ball> it = mGoalVector.iterator();
        //
        // while (it.hasNext()) {
        // summitedVactor.add(it.next());
        // }

        return summitedVactor.iterator();
    }

    public Bitmap getResizedBitmapImage(Bitmap source, Ball ball) {
        int width = source.getWidth();
        int height = source.getHeight();

        float ration = (float)ball.getCurrentFrmae() / (float)ball.getTargetFrmae();

        float ratioWidth = width - (ration * width);
        float ratioHeight = height - (ration * height);


        Bitmap bmp = Bitmap.createScaledBitmap(source, (int)ratioWidth, (int)ratioHeight, true);



        Matrix matrix = new Matrix();
        matrix.postRotate(3.0f * ration * 360f);

        Bitmap rotateBitmap =
                Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

        return rotateBitmap;
    }

    public Bitmap getResizedBitmapImage(Bitmap source, int currentFrame, int targetFrame) {
        int width = source.getWidth();
        int height = source.getHeight();

        float ration = (float)currentFrame / (float)targetFrame;

        float ratioWidth = width - (ration * width);
        float ratioHeight = height - (ration * height);

        if (ratioWidth < 1 || ratioHeight < 1) {
            ratioWidth = 1;
            ratioHeight = 1;
        }
        Bitmap bmp = Bitmap.createScaledBitmap(source, (int)ratioWidth, (int)ratioHeight, true);

        Matrix matrix = new Matrix();
        matrix.postRotate(3.0f * ration * 360f);



        Bitmap rotateBitmap =
                Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);



        return rotateBitmap;
    }

}

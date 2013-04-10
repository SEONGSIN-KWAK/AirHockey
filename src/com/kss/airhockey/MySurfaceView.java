package com.kss.airhockey;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.kss.airhockey.R;
import com.kss.airhockey.Balls.Ball;

import java.util.Iterator;


public class MySurfaceView extends SurfaceView implements Callback {
    public static boolean DEBUG_MODE = true;


    private class ViewThread extends Thread {
        private int mFramerate;
        SurfaceHolder mMySurfaceholder;
        MySurfaceView mMySurfaceView;

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
            while (!Thread.currentThread().isInterrupted()) {
                Canvas canvas = null;
                canvas = mMySurfaceholder.lockCanvas();

                try {
                    synchronized (mMySurfaceholder) {
                        mMySurfaceView.onDraw(canvas);
                        Thread.sleep(mFramerate);
                    }
                } catch (Exception e) {

                } finally {
                    if (mMySurfaceholder != null) {
                        mMySurfaceholder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }

        public int getFramerate() {
            return mFramerate;
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


    int pointX;
    int pointY;

    public MySurfaceView(Context context) {
        super(context);
        mContext = context;
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        myViewThread = new ViewThread(this, holder, 5);

        // Vector Setting
        Bitmap bitmapBall = new BitmapFactory().decodeResource(getResources(), R.drawable.ball);
        mBallsVector = new Balls(bitmapBall);
        Bitmap bitmapGoal = new BitmapFactory().decodeResource(getResources(), R.drawable.goal);
        mGoalVector = new Balls(bitmapGoal, R.drawable.goal);

        paintForText = new Paint();
        paintForText.setTextSize(30);
        paintForText.setColor(Color.GREEN);

        paintForCanvasClaening = new Paint();
        paintForCanvasClaening.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
        mSummitVector = new Balls(null);


    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (myViewThread == null) {
            return;
        }
        if (mGoalVector.size() == 0) {
            Ball goal = new Ball(360.0f, 800.0f, R.drawable.goal);
            mBallsVector.add(goal);
            mGoalVector.add(goal);
        }
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
        myViewThread.interrupt();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Ball buff;
        int index;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                index = mBallsVector.getIndexIfContained(event.getX(), event.getY());
                if (index != Balls.NOT_CONTAINED) {
                    buff = mBallsVector.remove(index);
                    buff.setX(event.getX());
                    buff.setY(event.getY());
                    mBallsVector.add(buff);
                    return true;
                }
                mBallsVector.add(new Ball(event.getX(), event.getY()));
                return true;

            case MotionEvent.ACTION_MOVE:
                buff = mBallsVector.remove(mBallsVector.size() - 1);
                buff.setX(event.getX());
                buff.setY(event.getY());
                mBallsVector.add(buff);
                return true;

            case MotionEvent.ACTION_UP:

                buff = mBallsVector.lastElement();
                if (buff.getResourceID() == R.drawable.goal) {
                    return true;
                }

                index = mGoalVector.getIndexIfContained(buff.getX(), buff.getY());
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
            Long start = System.currentTimeMillis();

            canvas.drawPaint(paintForCanvasClaening);

            // summit
            Iterator<Ball> it = getSummitedIterator();
            Ball p = null;
            while (it.hasNext()) {
                p = it.next();
                Bitmap image;
                switch (p.getResourceID()) {
                    case R.drawable.ball:
                        image = mBallsVector.getBitmap();
                        break;

                    case R.drawable.goal:
                        image = mGoalVector.getBitmap();
                        break;

                    default:
                        image = mBallsVector.getBitmap();
                        break;
                }
                if (p.isRemoving()) {
                    if (p.getCurrentFrmae() < p.getTargetFrmae()) {
                        int currentFps = p.getCurrentFrmae();
                        currentFps++;
                        p.setCurrentFrmae(currentFps);
                        image = getResizedBitmapImage(image, p);
                    } else {
                        mBallsVector.remove(p);
                        continue;
                    }
                }
                canvas.drawBitmap(image, p.getX() - (image.getWidth() / 2), p.getY()
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
}

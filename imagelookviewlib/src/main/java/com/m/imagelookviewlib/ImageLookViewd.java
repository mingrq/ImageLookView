package com.m.imagelookviewlib;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatImageView;

public class ImageLookViewd extends AppCompatImageView {


    private Matrix scaleMatrix;

    private float scaling;//初始缩放比例


    private boolean init = true;//初始化
    private float[] matrixValues = new float[9];

    private int firstPointIndex = 0;
    private int secondPointIndex = 1;
    //基础点
    private PointF firstBasicsPoint;
    private PointF secondBasicsPoint;
    private PointF firstPoint;
    private PointF secondPoint;
    private Matrix bitmapCalculateMatrix;

    private RectF viewRectF;//控件尺寸
    private RectF bitmapRectF;//原始bitmap尺寸
    private RectF bitmapNowRectF;
    private Matrix bitmapInitMatrix;

  /*  private boolean allowShrink = true;//是否允许缩小
    private float allowLeastScaling = 1;//允许最小缩放比例*/

    public ImageLookViewd(Context context) {
        this(context, null);
    }

    public ImageLookViewd(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageLookViewd(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        firstBasicsPoint = new PointF();
        secondBasicsPoint = new PointF();
        bitmapNowRectF = new RectF();
        bitmapRectF = new RectF(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
        bitmapCalculateMatrix = new Matrix();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN://第一次手指按下
                firstBasicsPoint.x = event.getX();
                firstBasicsPoint.y = event.getY();
                break;
            case MotionEvent.ACTION_POINTER_DOWN://第二个手指按下
                if (event.getPointerCount() == 2) {
                    secondBasicsPoint.x = event.getX(1);
                    secondBasicsPoint.y = event.getY(1);
                }
                break;
            case MotionEvent.ACTION_MOVE://滑动

                //有两个触点--缩放
                if (event.getPointerCount() == 2) {
                    if (event.getPointerId(0) != firstPointIndex) {
                        //第一个触点id变化
                        firstPointIndex = event.getPointerId(0);
                        firstBasicsPoint.set(getPointNow(event, 0));
                    } else if (event.getPointerId(1) != secondPointIndex) {
                        //第二个触点id变化
                        secondPointIndex = event.getPointerId(1);
                        secondBasicsPoint.set(getPointNow(event, 1));
                    } else {
                        firstPoint = getPointNow(event, 0);//获取第一触点位置
                        secondPoint = getPointNow(event, 1);//获取第二触点位置
                        setScale(firstPoint, secondPoint, firstBasicsPoint, secondBasicsPoint);
                        firstBasicsPoint.set(firstPoint);
                        secondBasicsPoint.set(secondPoint);
                    }
                }

                //只有一个触点--平移
                if (event.getPointerCount() == 1) {
                    if (event.getPointerId(0) != firstPointIndex) {
                        //第一个触点id变化
                        firstPointIndex = event.getPointerId(0);
                        firstBasicsPoint.set(getPointNow(event, 0));
                    } else {
                        firstPoint = getPointNow(event, 0);//获取第一触点位置
                        setTranslation(firstPoint.x - firstBasicsPoint.x, firstPoint.y - firstBasicsPoint.y);
                        firstBasicsPoint.set(firstPoint);
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP://手指抬起
              /*  if (allowShrink) {
                    if (matrixValues[0] < scaling) {
                        startScaleAnimation();
                    }
                }*/

                break;
        }
        return true;
    }

    /**
     * 获取两点距离
     *
     * @param firstPoint
     * @param secondPoint
     * @return
     */
    private float getTwoPointDistance(PointF firstPoint, PointF secondPoint) {
        float moveX = Math.abs(secondPoint.x - firstPoint.x);
        float moveY = Math.abs(secondPoint.y - firstPoint.y);
        float move = (float) Math.sqrt(moveX * moveX + moveY * moveY);
        return move;
    }

    /**
     * 获取触点位置
     *
     * @param event
     * @return
     */
    private PointF getPointNow(MotionEvent event, int index) {
        return new PointF(event.getX(index), event.getY(index));
    }


    /**
     * 获取两点中点
     *
     * @param p1
     * @param p2
     * @return
     */
    public static PointF getMiddlePoint(PointF p1, PointF p2) {
        return new PointF((p1.x + p2.x) / 2.0f, (p1.y + p2.y) / 2.0f);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (init) {
            scaleMatrix = getImageMatrix();
            viewRectF = new RectF(0, 0, getWidth(), getHeight());
            scaleMatrix.getValues(matrixValues);
            scaling = matrixValues[0];
            bitmapInitMatrix = new Matrix();
            bitmapInitMatrix.set(scaleMatrix);
            init = false;
        }
    }


    /**
     * 设置平移
     */
    private void setTranslation(float oneMoveX, float oneMoveY) {
        float movex = 0;
        float movey = 0;
        scaleMatrix = getImageMatrix();
        //获取bitmap现在的边界
        bitmapNowRectF.set(bitmapRectF);
        scaleMatrix.mapRect(bitmapNowRectF);
        if (bitmapNowRectF.height() > viewRectF.height()) {
            movey = oneMoveY;
        }
        if (bitmapNowRectF.width() > viewRectF.width()) {
            movex = oneMoveX;
        }
        if (movey > 0 && bitmapNowRectF.top + movey > 0) {
            movey = -bitmapNowRectF.top;
        }
        if (movey < 0 && bitmapNowRectF.bottom + movey < viewRectF.height()) {
            movey = viewRectF.height() - bitmapNowRectF.bottom;
        }
        if (movex > 0 && bitmapNowRectF.left + movex > 0) {
            movex = -bitmapNowRectF.left;
        }
        if (movex < 0 && bitmapNowRectF.right + movex < viewRectF.width()) {
            movex = viewRectF.width() - bitmapNowRectF.right;
        }
        scaleMatrix.postTranslate(movex, movey);
        invalidate();
    }


    /**
     * 设置缩放
     */
    private void setScale(PointF firstPoint, PointF secondPoint, PointF firstBasicsPoint, PointF secondBasicsPoint) {
        float movePointSize = getTwoPointDistance(firstPoint, secondPoint);//移动后两点距离
        float basicsPointSize = getTwoPointDistance(firstBasicsPoint, secondBasicsPoint);//基础点距离
        scaleMatrix = getImageMatrix();
        scaleMatrix.getValues(matrixValues);

        float scale = movePointSize / basicsPointSize;//缩放倍数
        Log.e("scale", String.valueOf(scale));
        PointF pointF = getScaleCentricPoint(scale, firstBasicsPoint, secondBasicsPoint);
        RectF bitmapScaleBeforeRectF = getBitmapScaleBeforeRectF();
        Log.e("top", String.valueOf(bitmapScaleBeforeRectF.top));
        RectF bitmapScaleAfterRectF = getBitmapScaleAfterRectF(scale, pointF);
        if (bitmapScaleBeforeRectF.left < viewRectF.left && viewRectF.left < bitmapScaleAfterRectF.left) {
            scale = pointF.x / (pointF.x - bitmapScaleBeforeRectF.left);
            Log.e("scale1", pointF.x + "  " + bitmapScaleBeforeRectF.left + "  " + scale);
        }
        if (bitmapScaleBeforeRectF.height() > viewRectF.height() && viewRectF.height() > bitmapScaleAfterRectF.height()) {
            scale = bitmapScaleAfterRectF.height() / bitmapScaleBeforeRectF.height();
            Log.e("scale2", String.valueOf(scale));
        }

        if (scale * matrixValues[0] > scaling) {
            scaleMatrix.postScale(scale, scale, pointF.x, pointF.y);
            invalidate();
        } else {
            scaleMatrix.set(bitmapInitMatrix);
            invalidate();
        }

    }


    /**
     * 获取bitmap缩放前的矩形
     *
     * @return
     */
    private RectF getBitmapScaleBeforeRectF() {
        RectF bitmapScaleBeforeRectF = new RectF(bitmapRectF);//当前图片矩形
        Matrix imageMatrix = getImageMatrix();//当前图片的矩阵
        imageMatrix.mapRect(bitmapScaleBeforeRectF);
        return bitmapScaleBeforeRectF;
    }

    /**
     * 获取bitmap缩放后的矩形
     *
     * @return
     */
    private RectF getBitmapScaleAfterRectF(float scale, PointF scaleCentricPoint) {
        RectF bitmapScaleAfterRectF = new RectF();//当前图片矩形
        Matrix imageMatrix = getImageMatrix();//当前图片的矩阵
        imageMatrix.postScale(scale, scale, scaleCentricPoint.x, scaleCentricPoint.y);
        bitmapScaleAfterRectF.set(bitmapRectF);
        imageMatrix.mapRect(bitmapScaleAfterRectF);
        return bitmapScaleAfterRectF;
    }

    /**
     * 获取缩放中心点
     *
     * @param scale             缩放倍数  小于1：缩小  大于1：放大
     * @param firstBasicsPoint
     * @param secondBasicsPoint
     * @return
     */
    private PointF getScaleCentricPoint(float scale, PointF firstBasicsPoint, PointF secondBasicsPoint) {
        PointF scaleCentricPointF = getMiddlePoint(firstBasicsPoint, secondBasicsPoint);//缩放中心点
        RectF bitmapScaleBeforeRectF = getBitmapScaleBeforeRectF();
        if (scale > 1) {
            //放大
            if (bitmapScaleBeforeRectF.width() <= viewRectF.width())
                scaleCentricPointF.x = getWidth() / 2;
            if (bitmapScaleBeforeRectF.height() <= viewRectF.height())
                scaleCentricPointF.y = getHeight() / 2;
        } else {
            //缩小
            if (!viewRectF.contains(bitmapScaleBeforeRectF)) {
                if (bitmapScaleBeforeRectF.height() > viewRectF.height()) {
                    if (bitmapScaleBeforeRectF.top >= viewRectF.top) {
                        scaleCentricPointF.y = viewRectF.top;
                    }
                    if (bitmapScaleBeforeRectF.bottom <= viewRectF.bottom) {
                        scaleCentricPointF.y = viewRectF.bottom;
                    }
                } else {
                    scaleCentricPointF.y = viewRectF.height() / 2;
                }

                if (bitmapScaleBeforeRectF.width() > viewRectF.width()) {
                    if (bitmapScaleBeforeRectF.left >= viewRectF.left) {
                        scaleCentricPointF.x = viewRectF.left;
                    }
                    if (bitmapScaleBeforeRectF.right <= viewRectF.right) {
                        scaleCentricPointF.x = viewRectF.right;
                    }
                } else {
                    scaleCentricPointF.x = viewRectF.width() / 2;
                }
            } else {
                //bitmap比控件小
                scaleCentricPointF.x = viewRectF.width() / 2;
                scaleCentricPointF.y = viewRectF.height() / 2;
            }
        }
        return scaleCentricPointF;
    }

    /**
     * 开始缩放动画，将bitmap放大到初始大小
     */
    private void startScaleAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(1, scaling);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float scale = (float) valueAnimator.getAnimatedValue();
                scaleMatrix = getImageMatrix();
                scaleMatrix.getValues(matrixValues);

                scaleMatrix.postScale(scale / matrixValues[0], scale / matrixValues[0], getWidth() / 2, getHeight() / 2);
                invalidate();
            }
        });
        animator.start();
    }
}

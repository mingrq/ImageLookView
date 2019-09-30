package com.m.imagelookviewlib;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatImageView;

public class ImageLookViews extends AppCompatImageView {


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

    public ImageLookViews(Context context) {
        this(context, null);
    }

    public ImageLookViews(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageLookViews(Context context, AttributeSet attrs, int defStyleAttr) {
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
        PointF pointF = getMiddlePoint(firstBasicsPoint, secondBasicsPoint);
        scaleMatrix = getImageMatrix();
        scaleMatrix.getValues(matrixValues);


        float scale = movePointSize / basicsPointSize;//缩放倍数
        if (movePointSize > basicsPointSize) {
            //放大

            //bitmap与imageview有边距，放大点设置在view中间
            if (matrixValues[2] > 0)
                pointF.x = getWidth() / 2;
            if (matrixValues[5] > 0)
                pointF.y = getHeight() / 2;
        } else {
            //缩小
            //获取bitmap现在的边界
            bitmapNowRectF.set(bitmapRectF);
            bitmapCalculateMatrix.set(scaleMatrix);
            bitmapCalculateMatrix.postScale(scale, scale, pointF.x, pointF.y);
            bitmapCalculateMatrix.mapRect(bitmapNowRectF);
            if (!viewRectF.contains(bitmapNowRectF)) {
                //bitmap比控件大时缩放
                if (bitmapNowRectF.height() > viewRectF.height()) {
                    if (bitmapNowRectF.top > 0) {
                        pointF.y = 0;
                    }
                    if (bitmapNowRectF.bottom < viewRectF.bottom) {
                        pointF.y = viewRectF.bottom;
                    }
                } else {
                    pointF.y = viewRectF.height() / 2;
                }

                if (bitmapNowRectF.width() > viewRectF.width()) {
                    if (bitmapNowRectF.left > 0) {
                        pointF.x = 0;
                    }
                    if (bitmapNowRectF.right < viewRectF.right) {
                        pointF.x = viewRectF.right;
                    }
                } else {
                    pointF.x = viewRectF.width() / 2;
                }
            } else {
                //bitmap比控件小时缩放
                pointF.x = viewRectF.width() / 2;
                pointF.y = viewRectF.height() / 2;
            }
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

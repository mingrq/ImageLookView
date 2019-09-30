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

public class ImageLookView extends AppCompatImageView {


    private float scaling;//初始缩放比例
    private boolean init = true;//初始化
    private RectF viewInitRectF;//初始控件尺寸
    private RectF bitmapInitRectF;//初始bitmap尺寸


    private float maxScaling = 5.0f;//最大缩放比例
    private float minScaling = 1.0f;//最小缩放比例


    private float[] matrixValues = new float[9];
    private Matrix scaleMatrix;

    private int firstPointIndex = 0;//第一触点下标
    private int secondPointIndex = 1;//第二触点下标
    //基础点
    private PointF firstBasicsPoint;
    private PointF secondBasicsPoint;
    private PointF firstMoveAfterPoint;
    private PointF secondMoveAfterPoint;

    private RectF bitmapNowRectF;


    public ImageLookView(Context context) {
        this(context, null);
    }

    public ImageLookView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageLookView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        firstBasicsPoint = new PointF();
        secondBasicsPoint = new PointF();
        bitmapNowRectF = new RectF();
        bitmapInitRectF = new RectF(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
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
                        firstMoveAfterPoint = getPointNow(event, 0);//获取第一触点位置
                        secondMoveAfterPoint = getPointNow(event, 1);//获取第二触点位置
                        setScale(firstMoveAfterPoint, secondMoveAfterPoint, firstBasicsPoint, secondBasicsPoint);
                        firstBasicsPoint.set(firstMoveAfterPoint);
                        secondBasicsPoint.set(secondMoveAfterPoint);
                    }
                }

                //只有一个触点--平移
                if (event.getPointerCount() == 1) {
                    if (event.getPointerId(0) != firstPointIndex) {
                        //第一个触点id变化
                        firstPointIndex = event.getPointerId(0);
                        firstBasicsPoint.set(getPointNow(event, 0));
                    } else {
                        firstMoveAfterPoint = getPointNow(event, 0);//获取第一触点位置
                        setTranslation(firstMoveAfterPoint.x - firstBasicsPoint.x, firstMoveAfterPoint.y - firstBasicsPoint.y);
                        firstBasicsPoint.set(firstMoveAfterPoint);
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP://手指抬起
                getImageMatrix().getValues(matrixValues);
                if (matrixValues[0] < scaling) {
                    startScaleAnimation(matrixValues[0]);
                }
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
            viewInitRectF = new RectF(0, 0, getWidth(), getHeight());//控件矩形
            getImageMatrix().getValues(matrixValues);
            scaling = matrixValues[0];//初始缩放倍数
            init = false;
        }
    }


    /**
     * 设置平移
     */
    private void setTranslation(float oneMoveX, float oneMoveY) {
        float movex;
        float movey;
        scaleMatrix = getImageMatrix();
        //获取bitmap现在的边界
        bitmapNowRectF.set(bitmapInitRectF);
        scaleMatrix.mapRect(bitmapNowRectF);
        //设置可移动条件
        if (bitmapNowRectF.height() > viewInitRectF.height() && bitmapNowRectF.top <= viewInitRectF.top && bitmapNowRectF.bottom >= viewInitRectF.bottom) {
            movey = oneMoveY;
        } else {
            movey = 0;
        }
        if (bitmapNowRectF.width() > viewInitRectF.width() && bitmapNowRectF.left <= viewInitRectF.left && bitmapNowRectF.right >= viewInitRectF.right) {
            movex = oneMoveX;
        } else {
            movex = 0;
        }
        //设置界限移动距离
        if (movey > 0 && bitmapNowRectF.top + movey > 0) {
            movey = -bitmapNowRectF.top;
        }
        if (movey < 0 && bitmapNowRectF.bottom + movey < viewInitRectF.height()) {
            movey = viewInitRectF.height() - bitmapNowRectF.bottom;
        }
        if (movex > 0 && bitmapNowRectF.left + movex > 0) {
            movex = -bitmapNowRectF.left;
        }
        if (movex < 0 && bitmapNowRectF.right + movex < viewInitRectF.width()) {
            movex = viewInitRectF.width() - bitmapNowRectF.right;
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
        float scale = movePointSize / basicsPointSize;//缩放倍数

        scaleMatrix = getImageMatrix();
        scaleMatrix.getValues(matrixValues);
        //设置可缩放条件
        if (matrixValues[0] >= minScaling && matrixValues[0] <= maxScaling) {
            //缩放范围必须在设置的最大缩放与最小缩放值之间

            //设置极限缩放
            if (matrixValues[0] * scale < minScaling) {
                scale = minScaling / matrixValues[0];
            }
            if (matrixValues[0] * scale > maxScaling) {
                scale = maxScaling / matrixValues[0];
            }

            //获取缩放中心点
            PointF scaleCentricPoint = getScaleCentricPoint(scale, firstBasicsPoint, secondBasicsPoint);

            //缩放
            scaleMatrix.postScale(scale, scale, scaleCentricPoint.x, scaleCentricPoint.y);

            //调整bitmap位置
            if (scale < 1) {
                RectF scaleAfterRectF = getBitmapScaleAfterRectF(scaleMatrix);//缩放后的图片矩形
                if (scaleAfterRectF.width() > viewInitRectF.width()) {
                    if (scaleAfterRectF.left > 0) {
                        scaleMatrix.postTranslate(-scaleAfterRectF.left, 0);
                    }
                    if (scaleAfterRectF.right < viewInitRectF.right) {
                        scaleMatrix.postTranslate(viewInitRectF.right - scaleAfterRectF.right, 0);
                    }
                } else {
                    scaleMatrix.postTranslate((viewInitRectF.width() - scaleAfterRectF.width()) / 2 - scaleAfterRectF.left, 0);
                }
                if (scaleAfterRectF.height() > viewInitRectF.height()) {
                    if (scaleAfterRectF.top > 0) {
                        scaleMatrix.postTranslate(0, -scaleAfterRectF.top);
                    }
                    if (scaleAfterRectF.bottom < viewInitRectF.bottom) {
                        scaleMatrix.postTranslate(0, viewInitRectF.bottom - scaleAfterRectF.bottom);
                    }
                } else {
                    scaleMatrix.postTranslate(0, (viewInitRectF.height() - scaleAfterRectF.height()) / 2 - scaleAfterRectF.top);
                }
            }
            invalidate();
        }

    }


    /**
     * 获取bitmap缩放后的矩形
     *
     * @return
     */
    RectF scaleAfterRectF;

    private RectF getBitmapScaleAfterRectF(Matrix matrix) {
        if (scaleAfterRectF == null)
            scaleAfterRectF = new RectF();
        scaleAfterRectF.set(bitmapInitRectF);
        matrix.mapRect(scaleAfterRectF);
        return scaleAfterRectF;
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
        PointF scaleCentricPoint = getMiddlePoint(firstBasicsPoint, secondBasicsPoint);
        bitmapNowRectF.set(bitmapInitRectF);
        scaleMatrix.mapRect(bitmapNowRectF);

        //设置缩放点
        if (!viewInitRectF.contains(bitmapNowRectF)) {

            if (bitmapNowRectF.height() > viewInitRectF.height()) {
                if (scale < 1) {
                    if (bitmapNowRectF.top >= viewInitRectF.top) {
                        scaleCentricPoint.y = viewInitRectF.top;
                    }
                    if (bitmapNowRectF.bottom <= viewInitRectF.bottom) {
                        scaleCentricPoint.y = viewInitRectF.bottom;
                    }
                }
            } else {
                scaleCentricPoint.y = viewInitRectF.height() / 2;
            }

            if (bitmapNowRectF.width() > viewInitRectF.width()) {
                if (scale < 1) {
                    if (bitmapNowRectF.left >= viewInitRectF.left) {
                        scaleCentricPoint.x = viewInitRectF.left;
                    }
                    if (bitmapNowRectF.right <= viewInitRectF.right) {
                        scaleCentricPoint.x = viewInitRectF.right;
                    }
                }
            } else {
                scaleCentricPoint.x = viewInitRectF.width() / 2;
            }
        } else {
            //bitmap比控件小
            scaleCentricPoint.x = viewInitRectF.width() / 2;
            scaleCentricPoint.y = viewInitRectF.height() / 2;
        }
        return scaleCentricPoint;
    }

    /**
     * 开始缩放动画，将bitmap放大到初始大小
     */
    private void startScaleAnimation(float startsacl) {
        ValueAnimator animator = ValueAnimator.ofFloat(startsacl, scaling);
        animator.setDuration(200);
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



    //-------------------------------------------对外方法---------------------------------------------------


    /**
     * 获取初始缩放比例
     *
     * @return
     */
    public float getScaling() {
        return scaling;
    }

    /**
     * 获取最大缩放值
     *
     * @return
     */
    public float getMaxScaling() {
        return maxScaling;
    }

    /**
     * 设置最大缩放值
     *
     * @param maxScaling
     */
    public void setMaxScaling(float maxScaling) {
        this.maxScaling = maxScaling;
    }

    /**
     * 获取最小缩放值
     *
     * @return
     */
    public float getMinScaling() {
        return minScaling;
    }

    /**
     * 设置最小缩放值
     *
     * @param minScaling
     */
    public void setMinScaling(float minScaling) {
        this.minScaling = minScaling;
    }

    /**
     * 设置旋转
     * @param angle 旋转角度
     */
    public void setRotate(int angle){

    }
}

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

public class ImageLookView extends AppCompatImageView {

    private boolean init = true;//初始化
    private RectF viewInitRectF;//初始控件尺寸
    private RectF bitmapInitRectF;//初始bitmap尺寸
    private Matrix bitmapInitMatrix;//初始bitmap矩阵
    private Matrix bitmapMatrix;//图片未经缩放矩阵

    private float maxScaling = 5.0f;//最大缩放比例
    private float minScaling = 1.0f;//最小缩放比例

    private float[] matrixValues = new float[9];
    private float[] matrixInitValues = new float[9];
    private float[] bitmapMatrixInitValues = new float[9];

    int firstPointIndex;//第一触点下标
    int secondPointIndex;//第二触点下标

    private float rotateAngle = 0;//旋转角度

    private RectF bitmapNowRectF;

    //手指触摸点
    private PointF firstBasicsPoint;
    private PointF secondBasicsPoint;


    public ImageLookView(Context context) {
        this(context, null);
    }

    public ImageLookView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageLookView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        bitmapNowRectF = new RectF();
        bitmapInitRectF = new RectF(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
        bitmapMatrix = new Matrix();
        bitmapMatrix.getValues(bitmapMatrixInitValues);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (init) {
            viewInitRectF = new RectF(0, 0, getWidth(), getHeight());//控件矩形
            bitmapInitMatrix = new Matrix(getImageMatrix());
            bitmapInitMatrix.getValues(matrixInitValues);
            init = false;
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF firstMoveAfterPoint;
        PointF secondMoveAfterPoint;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN://第一次手指按下
                firstBasicsPoint = getPointNow(event, 0);
                break;
            case MotionEvent.ACTION_POINTER_DOWN://第二个手指按下
                if (event.getPointerCount() == 2) {
                    secondBasicsPoint = getPointNow(event, 1);
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
                bitmapInitMatrix.getValues(matrixInitValues);
                if ((matrixValues[0] > 0 && matrixValues[0] < matrixInitValues[0]) || (matrixValues[0] < 0 && matrixValues[0] > matrixInitValues[0])) {
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


    /**
     * 设置平移
     */
    private void setTranslation(float oneMoveX, float oneMoveY) {
        float movex;
        float movey;
        //获取bitmap现在的边界
        bitmapNowRectF.set(bitmapInitRectF);
        getImageMatrix().mapRect(bitmapNowRectF);
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
        getImageMatrix().postTranslate(movex, movey);
        invalidate();
    }


    /**
     * 设置缩放
     */
    private void setScale(PointF firstPoint, PointF secondPoint, PointF firstBasicsPoint, PointF secondBasicsPoint) {
        float movePointSize = getTwoPointDistance(firstPoint, secondPoint);//移动后两点距离
        float basicsPointSize = getTwoPointDistance(firstBasicsPoint, secondBasicsPoint);//基础点距离
        float scale = movePointSize / basicsPointSize;//缩放倍数

        getImageMatrix().getValues(matrixValues);

        //设置极限缩放
        if (bitmapMatrixInitValues[0] > 0) {
            if (matrixValues[0] * scale < minScaling * bitmapMatrixInitValues[0]) {
                scale = (minScaling * bitmapMatrixInitValues[0]) / matrixValues[0];
            } else if (matrixValues[0] * scale > maxScaling * bitmapMatrixInitValues[0]) {
                scale = (maxScaling * bitmapMatrixInitValues[0]) / matrixValues[0];
            }
        } else if (bitmapMatrixInitValues[0] < 0) {
            if (matrixValues[0] * scale > minScaling * bitmapMatrixInitValues[0]) {
                scale = (minScaling * bitmapMatrixInitValues[0]) / matrixValues[0];
            } else if (matrixValues[0] * scale < maxScaling * bitmapMatrixInitValues[0]) {
                scale = (maxScaling * bitmapMatrixInitValues[0]) / matrixValues[0];
            }
        } else {
            if (bitmapMatrixInitValues[1] > 0) {
                if (matrixValues[1] * scale < minScaling * bitmapMatrixInitValues[1]) {
                    scale = (minScaling * bitmapMatrixInitValues[1]) / matrixValues[1];
                } else if (matrixValues[1] * scale > maxScaling * bitmapMatrixInitValues[1]) {
                    scale = (maxScaling * bitmapMatrixInitValues[1]) / matrixValues[1];
                }
            } else {
                if (matrixValues[1] * scale > minScaling * bitmapMatrixInitValues[1]) {
                    scale = (minScaling * bitmapMatrixInitValues[1]) / matrixValues[1];
                } else if (matrixValues[1] * scale < maxScaling * bitmapMatrixInitValues[1]) {
                    scale = (maxScaling * bitmapMatrixInitValues[1]) / matrixValues[1];
                }
            }
        }


        //获取缩放中心点
        PointF scaleCentricPoint = getScaleCentricPoint(scale, firstBasicsPoint, secondBasicsPoint);

        //缩放
        getImageMatrix().postScale(scale, scale, scaleCentricPoint.x, scaleCentricPoint.y);
        //调整bitmap位置
        if (scale < 1) {
            RectF scaleAfterRectF = getBitmapScaleAfterRectF(getImageMatrix());//缩放后的图片矩形
            if (scaleAfterRectF.width() > viewInitRectF.width()) {
                if (scaleAfterRectF.left > 0) {
                    getImageMatrix().postTranslate(-scaleAfterRectF.left, 0);
                }
                if (scaleAfterRectF.right < viewInitRectF.right) {
                    getImageMatrix().postTranslate(viewInitRectF.right - scaleAfterRectF.right, 0);
                }
            } else {
                getImageMatrix().postTranslate((viewInitRectF.width() - scaleAfterRectF.width()) / 2 - scaleAfterRectF.left, 0);
            }
            if (scaleAfterRectF.height() > viewInitRectF.height()) {
                if (scaleAfterRectF.top > 0) {
                    getImageMatrix().postTranslate(0, -scaleAfterRectF.top);
                }
                if (scaleAfterRectF.bottom < viewInitRectF.bottom) {
                    getImageMatrix().postTranslate(0, viewInitRectF.bottom - scaleAfterRectF.bottom);
                }
            } else {
                getImageMatrix().postTranslate(0, (viewInitRectF.height() - scaleAfterRectF.height()) / 2 - scaleAfterRectF.top);
            }
        }
        invalidate();
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
        getImageMatrix().mapRect(bitmapNowRectF);

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
        bitmapInitMatrix.getValues(matrixInitValues);
        ValueAnimator animator = ValueAnimator.ofFloat(startsacl, matrixInitValues[0]);
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float scale = (float) valueAnimator.getAnimatedValue();
                getImageMatrix().getValues(matrixValues);
                getImageMatrix().postScale(scale / matrixValues[0], scale / matrixValues[0], getWidth() / 2, getHeight() / 2);
                invalidate();
            }
        });
        animator.start();

    }

    /**
     * 开始旋转动画
     *
     * @param angle
     */

    private void startRotateAnim(final int angle) {
        final int[] alreadyAngle = {0};

        final float px = viewInitRectF.width() / 2;
        final float py = viewInitRectF.height() / 2;

        //获取偏移距离
        RectF skewingRectF = new RectF(bitmapInitRectF);
        getImageMatrix().mapRect(skewingRectF);
        final float skewingPx = px - (skewingRectF.width() / 2 + skewingRectF.left);
        final float skewingPy = py - (skewingRectF.height() / 2 + skewingRectF.top);
        getImageMatrix().postTranslate(skewingPx, skewingPy);

        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, angle);

        valueAnimator.setDuration(200);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int rotate = (int) valueAnimator.getAnimatedValue();//属性动画获取角度值
                getImageMatrix().postRotate(rotate - alreadyAngle[0], px, py);
                bitmapNowRectF.set(bitmapInitRectF);
                getImageMatrix().mapRect(bitmapNowRectF);
                //-------旋转后图片大小调整------------
                float bitmapw = bitmapNowRectF.width();
                float vieww = viewInitRectF.width();
                float bitmaph = bitmapNowRectF.height();
                float viewh = viewInitRectF.height();

                //计算宽高缩放比
                float wb = vieww / bitmapw;
                float hb = viewh / bitmaph;
                if (wb < hb) {
                    getImageMatrix().postScale(wb, wb, px, py);
                } else {
                    getImageMatrix().postScale(hb, hb, px, py);
                }

                alreadyAngle[0] = rotate;
                invalidate();
                if (rotate == angle) {
                    bitmapInitMatrix.set(getImageMatrix());
                    bitmapInitMatrix.getValues(matrixInitValues);
                    bitmapMatrix.postRotate(rotate, viewInitRectF.width() / 2, viewInitRectF.height() / 2);
                    bitmapMatrix.getValues(bitmapMatrixInitValues);
                }
            }
        });
        valueAnimator.start();
    }

    //-------------------------------------------对外方法---------------------------------------------------


    /**
     * 获取初始缩放比例
     *
     * @return
     */
    public float getScaling() {
        return 0f;
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
     *
     * @param angle 旋转角度
     */
    public void setRotate(int angle) {
        rotateAngle += angle;
        Matrix matrix = new Matrix(getImageMatrix());
        final float px = viewInitRectF.width() / 2;
        final float py = viewInitRectF.height() / 2;
        matrix.postRotate(angle, px, py);
        bitmapNowRectF.set(bitmapInitRectF);
        matrix.mapRect(bitmapNowRectF);
        float bitmapw = bitmapNowRectF.width();
        float vieww = viewInitRectF.width();
        float bitmaph = bitmapNowRectF.height();
        float viewh = viewInitRectF.height();

        //计算宽高缩放比
        float wb = vieww / bitmapw;
        float hb = viewh / bitmaph;
        if (wb < hb) {
            matrix.postScale(wb, wb, px, py);
        } else {
            matrix.postScale(hb, hb, px, py);
        }
        startRotateAnim(angle);
    }

    /**
     * 获取旋转角度
     *
     * @return
     */
    public float getRotateAngle() {
        return rotateAngle;
    }

}

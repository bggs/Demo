package com.example.demo.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import com.example.demo.OnUnlockListener;
import com.example.demo.R;
import com.example.demo.bean.CircleRectData;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liujie on 2017/9/25.
 */

public class LockView extends View {
    private static final int STATE_NORMAL = 1; // 默认
    private static final int STATE_SELECT = 2; // 选中
    private static final int STATE_CORRECT = 3; // 正确
    private static final int STATE_WRONG = 4; // 错误
    private int normalColor = Color.GRAY; // 默认显示的颜色
    private int selectColor = Color.YELLOW; // 选中时显示的颜色
    private int correctColor = Color.GREEN; // 正确时显示的颜色
    private int wrongColor = Color.RED; // 错误时显示的颜色
    private int lineWidth = -1; // 连线的宽度

    private int width; // 父布局分配给这个View的宽度
    private int height; // 父布局分配给这个View的高度
    private int rectRadius; // 每个小圆圈的宽度（直径）

    private List<CircleRectData> rectList; // 存储所有圆圈对象的列表
    private List<CircleRectData> pathList; // 存储用户绘制的连线上的所有圆圈对象

    private Canvas mCanvas; // 画布
    private Bitmap mBitmap; // Bitmap
    private Path mPath; //线条
    private Path tmpPath; // 记录用户以前绘制过的线条
    private Paint circlePaint; // 用户绘制圆圈的画笔
    private Paint pathPaint; // 用户绘制连线的画笔
    private int startX; // 上一个节点的X坐标
    private int startY; // 上一个节点的Y坐标
    private boolean isUnlocking; // 是否正在解锁（手指落下时是否刚好在一个节点上）

    private OnUnlockListener listener;

    public LockView(Context context) {
        this(context, null);
    }

    public LockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        rectList = new ArrayList<>();
        pathList = new ArrayList<>();
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LockView, defStyleAttr, 0);  //获取自定义属性
        int count = array.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = array.getIndex(i);
            switch (attr) {
                case R.styleable.LockView_normalColor:
                    normalColor = array.getColor(attr, Color.GRAY);
                    break;
                case R.styleable.LockView_selectColor:
                    selectColor = array.getColor(attr, Color.YELLOW);
                    break;
                case R.styleable.LockView_correctColor:
                    correctColor = array.getColor(attr, Color.GREEN);
                    break;
                case R.styleable.LockView_wrongColor:
                    wrongColor = array.getColor(attr, Color.RED);
                    break;
                case R.styleable.LockView_lineWidth:
                    lineWidth = (int) array.getDimension(attr, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics()));
                    break;
            }
        }
        if (lineWidth == -1) {
            lineWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth(); // 获取到控件的宽高属性值
        height = getMeasuredHeight();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setDither(true);
        mPath = new Path();
        tmpPath = new Path();
        pathPaint = new Paint();
        pathPaint.setDither(true);
        pathPaint.setAntiAlias(true);
        pathPaint.setStyle(Paint.Style.STROKE);

        int horizontalSpacing;
        int verticalSpacing;
        if (width <= height) {
            horizontalSpacing = 0;
            verticalSpacing = (height - width) / 2;
            rectRadius = width / 14;
        } else {
            horizontalSpacing = (width - height) / 2;
            verticalSpacing = 0;
            rectRadius = height / 14;
        }
        for (int i = 1; i <= 9; i++) {
            int x = ((i - 1) % 3 * 2 + 1) * rectRadius * 2 + horizontalSpacing + getPaddingLeft() + rectRadius;
            int y = ((i - 1) / 3 * 2 + 1) * rectRadius * 2 + verticalSpacing + getPaddingTop() + rectRadius;
            CircleRectData rect = new CircleRectData(i, x, y, STATE_NORMAL);
            rectList.add(rect);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, null);
        for (int i = 0; i < rectList.size(); i++) {
            drawCircle(rectList.get(i), rectList.get(i).getState());
        }
        canvas.drawPath(mPath, pathPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int currX = (int) event.getX();
        int currY = (int) event.getY();
        CircleRectData rect = getOuterRect(currX, currY);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.reset(); // 保证手指按下后所有元素都是初始状态
                // 判断手指落点是否在某个圆圈中，如果是则设置该圆圈为选中状态
                if (rect != null) {
                    rect.setState(STATE_SELECT);
                    startX = rect.getX();
                    startY = rect.getY();
                    tmpPath.moveTo(startX, startY);
                    pathList.add(rect);
                    isUnlocking = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isUnlocking) {
                    mPath.reset();
                    mPath.addPath(tmpPath);
                    mPath.moveTo(startX, startY);
                    mPath.lineTo(currX, currY);
                    if (rect != null) {
                        rect.setState(STATE_SELECT);
                        startX = rect.getX();
                        startY = rect.getY();
                        tmpPath.lineTo(startX, startY);
                        pathList.add(rect);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                isUnlocking = false;
                if (pathList.size() > 0) {
                    mPath.reset();
                    mPath.addPath(tmpPath);
                    StringBuilder result = new StringBuilder();
                    for (int i = 0; i < pathList.size(); i++) {
                        result.append(pathList.get(i).getCode());
                    }
                    if (listener.isUnlockSuccess(result.toString())) {
                        listener.onSuccess();
                        setWholePathState(STATE_CORRECT);
                    } else {
                        listener.onFailure();
                        setWholePathState(STATE_WRONG);
                    }
                }
                break;
        }
        invalidate();
        return true;
    }

    /**
     * 根据状态（解锁成功/失败）改变整条路径上所有元素的颜色
     */
    private void setWholePathState(int state) {
        pathPaint.setColor(getColorByState(state));
        for (CircleRectData rect : pathList) {
            rect.setState(state);
        }
    }

    /**
     * 通过状态得到应显示的颜色
     */
    private int getColorByState(int state) {
        int color = normalColor;
        switch (state) {
            case STATE_NORMAL:
                color = normalColor;
                break;
            case STATE_SELECT:
                color = selectColor;
                break;
            case STATE_CORRECT:
                color = correctColor;
                break;
            case STATE_WRONG:
                color = wrongColor;
                break;
        }
        return color;
    }

    /**
     * 根据参数中提供的圆圈参数绘制圆圈
     */
    private void drawCircle(CircleRectData rect, int state) {
        circlePaint.setColor(getColorByState(state));
        mCanvas.drawCircle(rect.getX(), rect.getY(), rectRadius, circlePaint);
    }

    /**
     * 判断参数中的x、y坐标对应的点是否在某个圆圈内，如果在则返回这个圆圈，否则返回null
     */
    private CircleRectData getOuterRect(int x, int y) {
        for (int i = 0; i < rectList.size(); i++) {
            CircleRectData rect = rectList.get(i);
            if ((x - rect.getX()) * (x - rect.getX()) + (y - rect.getY()) * (y - rect.getY()) <= rectRadius * rectRadius) {
                if (rect.getState() != STATE_SELECT) {
                    return rect;
                }
            }
        }
        return null;
    }
    /**
     * 为当前View设置结果监听器
     */
    public void setOnUnlockListener(OnUnlockListener listener) {
        this.listener = listener;
    }
    /**
     * 重置所有元素的状态到初始状态
     */
    public void reset() {
        setWholePathState(STATE_NORMAL);
        pathPaint.setColor(selectColor);
        mPath.reset();
        tmpPath.reset();
        pathList = new ArrayList<>();
    }
}

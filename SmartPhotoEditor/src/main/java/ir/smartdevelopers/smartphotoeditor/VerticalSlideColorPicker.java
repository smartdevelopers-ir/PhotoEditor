package ir.smartdevelopers.smartphotoeditor;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.RequiresApi;

/**
 * Created by Mark on 11/08/2016.
 */

public class VerticalSlideColorPicker extends View {

    public static final int DEFAULT_COLOR=Color.parseColor("#1E88E5");
    private int mColor;
    private Paint paint;
    private Paint strokePaint;
    private Path path;
    private Bitmap bitmap;
    private int viewWidth;
    private int viewHeight;
    private int centerX;
    private float colorPickerRadius;
    private Listener mListener;
    private RectF colorPickerBody;
    private float selectorYPos;
    private int borderColor;
    private float borderWidth;
    private int[] colors;
    private boolean cacheBitmap = true;
    private int mDeviceWidth;
    private static final int[] DEFAULT_COLORS= {
            Color.rgb(0,0,0),
            Color.rgb(255,255,255),
            Color.rgb(255,0,0),
            Color.rgb(255,255,0),
            Color.rgb(0,255,0),
            Color.rgb(0,255,255),
            Color.rgb(0,0,255),
            Color.rgb(255,0,255),
            Color.rgb(255,0,0)

    };
    public VerticalSlideColorPicker(Context context) {
        super(context);
        init(context);
    }

    public VerticalSlideColorPicker(Context context, AttributeSet attrs) {
      super(context, attrs);
        TypedArray a = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.VerticalSlideColorPicker, 0, 0);

        try {
            borderColor = a.getColor(R.styleable.VerticalSlideColorPicker_borderColor, Color.WHITE);
            mColor =
                    a.getColor(R.styleable.VerticalSlideColorPicker_defaultColor, DEFAULT_COLOR);
            borderWidth = a.getDimension(R.styleable.VerticalSlideColorPicker_borderWidth, 5f);
            int colorsResourceId =
                    a.getResourceId(R.styleable.VerticalSlideColorPicker_colors, 0);
            if (colorsResourceId==0){
                colors = DEFAULT_COLORS;
            }else {
                colors = a.getResources().getIntArray(colorsResourceId);
            }
        } finally {
            a.recycle();
        }
        init(context);
    }

    public VerticalSlideColorPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VerticalSlideColorPicker(Context context, AttributeSet attrs, int defStyleAttr,
                                    int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        path = new Path();

        strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(borderColor);
        strokePaint.setAntiAlias(true);
        strokePaint.setStrokeWidth(borderWidth);

        setDrawingCacheEnabled(true);
        mDeviceWidth=context.getResources().getDisplayMetrics().widthPixels;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        path.addCircle(centerX, borderWidth + colorPickerRadius, colorPickerRadius, Path.Direction.CW);
        path.addRect(colorPickerBody, Path.Direction.CW);
        path.addCircle(centerX, viewHeight - (borderWidth + colorPickerRadius), colorPickerRadius,
                Path.Direction.CW);

        canvas.drawPath(path, strokePaint);
        canvas.drawPath(path, paint);

        if (cacheBitmap) {
            bitmap = getDrawingCache();
            cacheBitmap = false;
            invalidate();
        } else {
            //canvas.drawLine(colorPickerBody.left, selectorYPos, colorPickerBody.right, selectorYPos, strokePaint);
        }
    }

    private Rect bound=new Rect();
    private boolean mScaling=false;
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float yPos = Math.min(event.getY(), colorPickerBody.bottom);
        yPos = Math.max(colorPickerBody.top, yPos);

        selectorYPos = yPos;
        mColor = bitmap.getPixel(viewWidth / 2, (int) selectorYPos);

        if (mListener != null) {
            mListener.onColorChange(mColor);
        }
        if (event.getAction()==MotionEvent.ACTION_UP){
            mScaling=false;
            if (mListener != null) {
                mListener.onRelease();
            }
        }

        if (event.getAction()==MotionEvent.ACTION_MOVE){
            if (mListener != null) {
                mListener.onMove();
            }

            getHitRect(bound);
            int w=getWidth();
            bound.inset(-w,-w);
            int x= (int) (getLeft()+event.getX());
            int y= (int) (getTop()+event.getY());
            if (!bound.contains(x,y) || mScaling){
                mScaling=true;
                float xPercent=1-(event.getRawX()/mDeviceWidth);
                if (mListener != null) {
                    mListener.onBrushScaled(xPercent);
                }
            }
        }
        //invalidate();

        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        viewWidth = w;
        viewHeight = h;

        centerX = viewWidth / 2;
        colorPickerRadius = (viewWidth / 2f) - borderWidth;

        colorPickerBody = new RectF(centerX - colorPickerRadius, borderWidth + colorPickerRadius,
                centerX + colorPickerRadius, viewHeight - (borderWidth + colorPickerRadius));

        LinearGradient gradient =
                new LinearGradient(0, colorPickerBody.top, 0, colorPickerBody.bottom, colors, null,
                        Shader.TileMode.CLAMP);
        paint.setShader(gradient);

        resetToDefault();
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
        invalidate();
    }

    public void setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
        invalidate();
    }

    public void setColors(int[] colors) {
        this.colors = colors;
        cacheBitmap = true;
        invalidate();
    }

    public void resetToDefault() {
        selectorYPos = borderWidth + colorPickerRadius;

        if (mListener != null) {
            mListener.onColorChange(mColor);
        }

        invalidate();
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
        if (listener != null) {
            listener.onColorChange(mColor);
        }
    }

    public int getColor() {
        return mColor;
    }


    public interface Listener {
        void onColorChange(int selectedColor);
        void onBrushScaled(float percentChanged);
        void onRelease();
        void onMove();
    }

}

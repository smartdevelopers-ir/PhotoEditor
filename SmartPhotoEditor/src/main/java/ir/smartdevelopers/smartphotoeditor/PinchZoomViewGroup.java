package ir.smartdevelopers.smartphotoeditor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;

public class PinchZoomViewGroup extends FrameLayout implements ScaleGestureDetector.OnScaleGestureListener {

    private boolean zoomEnabled=true;
    private boolean scrollEnabled=true;



    private enum Mode {
        NONE,
        DRAG,
        ZOOM
    }

    private static final float MIN_ZOOM = 1.0f;
    private static final float MAX_ZOOM = 4.0f;

    private Mode mode = Mode.NONE;
    private float scale = 1.0f;
    private float lastScaleFactor = 0f;

    private float startX = 0f;
    private float startY = 0f;

    private float dx = 0f;
    private float dy = 0f;
    private float prevDx = 0f;
    private float prevDy = 0f;

    public PinchZoomViewGroup(Context context) {
        super(context);
        init(context);
    }

    public PinchZoomViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PinchZoomViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private float lastMoveX,lastMoveY;
    private boolean isMoving;
    @SuppressLint("ClickableViewAccessibility")
    public void init(Context context) {
        final ScaleGestureDetector scaleDetector = new ScaleGestureDetector(context, this);
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        if (scale > MIN_ZOOM && scrollEnabled) {
                            mode = Mode.DRAG;
                            startX = motionEvent.getX() - prevDx;
                            startY = motionEvent.getY() - prevDy;
                            lastMoveX=motionEvent.getX();
                            lastMoveY=motionEvent.getY();
                        }else {
                            mode= Mode.NONE;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!isMoving){
                            float diffX=Math.abs(lastMoveX-motionEvent.getX());
                            float diffY=Math.abs(lastMoveY-motionEvent.getY());
                            if (diffX < 15 || diffY < 15){
                                return true;
                            }
                        }
                        if (mode == Mode.DRAG && scrollEnabled) {
                            dx = motionEvent.getX() - startX;
                            dy = motionEvent.getY() - startY;

                        }
                        isMoving=true;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        if (zoomEnabled){
                            mode = Mode.ZOOM;
                        }else {
                            mode= Mode.NONE;
                        }
                        isMoving=true;
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        if (scrollEnabled){
                            mode = Mode.DRAG;
                        }else {
                            mode = Mode.NONE;
                        }
                        lastMoveY=motionEvent.getY();
                        lastMoveX=motionEvent.getX();
                        isMoving=false;
                        return true;
//                        break;
                    case MotionEvent.ACTION_UP:
                        mode = Mode.NONE;
                        prevDx = dx;
                        prevDy = dy;
                        isMoving=false;
                        break;
                }
                scaleDetector.onTouchEvent(motionEvent);

                if ((mode == Mode.DRAG && scale >= MIN_ZOOM) || mode == Mode.ZOOM) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    float maxDx = (child().getWidth() - (child().getWidth() / scale)) / 2 * scale;
                    float maxDy = (child().getHeight() - (child().getHeight() / scale)) / 2 * scale;
                    dx = Math.min(Math.max(dx, -maxDx), maxDx);
                    dy = Math.min(Math.max(dy, -maxDy), maxDy);
                    applyScaleAndTranslation();

                }

                return true;
            }
        });
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleDetector) {
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleDetector) {
        float scaleFactor = scaleDetector.getScaleFactor();
        if (lastScaleFactor == 0 || (Math.signum(scaleFactor) == Math.signum(lastScaleFactor))) {
            scale *= scaleFactor;
            scale = Math.max(MIN_ZOOM, Math.min(scale, MAX_ZOOM));
            lastScaleFactor = scaleFactor;
        } else {
            lastScaleFactor = 0;
        }
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleDetector) {
    }

    private void applyScaleAndTranslation() {
        if(zoomEnabled){
            child().setScaleX(scale);
            child().setScaleY(scale);
        }
        if (scrollEnabled){
            child().setTranslationX(dx);
            child().setTranslationY(dy);
        }
    }

    private View child() {
        return getChildAt(0);
    }
    public boolean isScrollEnabled() {
        return scrollEnabled;
    }

    public void setScrollEnabled(boolean scrollEnabled) {
        this.scrollEnabled = scrollEnabled;
    }

    public boolean isZoomEnabled() {
        return zoomEnabled;
    }

    public void setZoomEnabled(boolean zoomEnabled) {
        this.zoomEnabled = zoomEnabled;
    }
}

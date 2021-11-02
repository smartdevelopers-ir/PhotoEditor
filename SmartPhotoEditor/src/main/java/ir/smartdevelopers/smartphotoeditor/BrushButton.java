package ir.smartdevelopers.smartphotoeditor;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewPropertyAnimator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;

public class BrushButton extends ConstraintLayout {
    private View mBackgroundView,mBrushSizeView;
    private AppCompatImageView imgIcon;
    private int mColor= Color.parseColor("#1E88E5");
    private final float MIN_BRUSH_SCALE =0.1f;
    private final float MAX_BRUSH_SCALE =0.8f;
    private final float MAX_BRASH_SIZE=48.0f;
    private float mBrushSize= MIN_BRUSH_SCALE;
    public BrushButton(@NonNull Context context) {
        this(context,null);
    }

    public BrushButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BrushButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }

    public BrushButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }
    private void init(Context context){
        View view=LayoutInflater.from(context).inflate(R.layout.spe_brush_button_layout,this,true);
        mBackgroundView=view.findViewById(R.id.spe_btnBrush_background);
        mBrushSizeView=view.findViewById(R.id.spe_btnBrush_brushSize);
        imgIcon=view.findViewById(R.id.spe_btnBrush_icon);
        // set default bg
        setColor(mColor);
        mBackgroundView.setVisibility(View.GONE);
        changeBrushSize(0.25f);
        showEditIcon();
        imgIcon.setImageResource(R.drawable.spe_ic_brush);
    }


    public void showEditIcon(){
        mBrushSizeView.setVisibility(GONE);
        imgIcon.setVisibility(View.VISIBLE);
    }
    public void showBrushSize(){
        mBrushSizeView.setVisibility(VISIBLE);
        imgIcon.setVisibility(View.GONE);
    }
    public void showBackGround(){
        mBackgroundView.setScaleX(0);
        mBackgroundView.setScaleY(0);
        mBackgroundView.setVisibility(View.VISIBLE);
        mBackgroundView.animate().scaleY(1).scaleX(1).setDuration(60).start();
    }
    public void hideBackground(){
        mBackgroundView.animate().scaleY(0).scaleX(0).setDuration(60)
                .withEndAction(()->{
                    mBackgroundView.setVisibility(View.GONE);
                })
                .start();

    }
    public void setColor(int color){
        mColor=color;
        GradientDrawable drawable=new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        mBackgroundView.setBackground(drawable);
    }

    public void changeBrushSize(float percent){
        float width=getMeasuredWidth();
        if (width==0){
            width=MAX_BRASH_SIZE;
        }
        float scale=percent;
        if (percent < MIN_BRUSH_SCALE){
            scale= MIN_BRUSH_SCALE;
        }else if (percent > MAX_BRUSH_SCALE){
            scale= MAX_BRUSH_SCALE;
        }
        mBrushSize=width*scale;

        mBrushSizeView.setScaleX(scale);
        mBrushSizeView.setScaleY(scale);
    }

    public int getColor() {
        return mColor;
    }

    public float getBrushSize() {
        return mBrushSize;
    }
}

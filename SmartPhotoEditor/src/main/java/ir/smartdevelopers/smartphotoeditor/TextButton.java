package ir.smartdevelopers.smartphotoeditor;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;

public class TextButton extends ConstraintLayout {
    private View mBackgroundView;
    private AppCompatImageView imgIcon;
    private int mColor= Color.parseColor("#1E88E5");
    
    public TextButton(@NonNull Context context) {
        super(context);
        init(context);
    }

    public TextButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TextButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public TextButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }
    private void init(Context context){
        View view=LayoutInflater.from(context).inflate(R.layout.spe_text_button_layout,this,true);
        mBackgroundView=view.findViewById(R.id.spe_btnText_background);
        imgIcon=view.findViewById(R.id.spe_btnText_icon);
        // set default bg
        setColor(mColor);
        mBackgroundView.setVisibility(View.GONE);
        imgIcon.setImageResource(R.drawable.spe_ic_text);
    }

    public void changeIconToFilledBackground(){
        imgIcon.setImageResource(R.drawable.spe_ic_text_filled);
    }
    public void changeIconToNoBackground(){
        imgIcon.setImageResource(R.drawable.spe_ic_text);
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
    

    public int getColor() {
        return mColor;
    }

}

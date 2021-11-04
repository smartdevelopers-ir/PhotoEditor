package ir.smartdevelopers.imageeditor;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.util.Objects;

import ir.smartdevelopers.smartphotoeditor.BrushButton;
import ir.smartdevelopers.smartphotoeditor.VerticalSlideColorPicker;
import ir.smartdevelopers.smartphotoeditor.photoeditor.OnPhotoEditorListener;
import ir.smartdevelopers.smartphotoeditor.photoeditor.PhotoEditor;
import ir.smartdevelopers.smartphotoeditor.photoeditor.PhotoEditorView;
import ir.smartdevelopers.smartphotoeditor.photoeditor.ViewType;
import ir.smartdevelopers.smartphotoeditor.photoeditor.shape.ShapeBuilder;
import ir.smartdevelopers.smartphotoeditor.photoeditor.shape.ShapeType;


public class MainActivity extends AppCompatActivity  {

    private static final int CROP_REQ_CODE = 656;
    private PhotoEditor mPhotoEditor;
    private PhotoEditorView mPhotoEditorView;
    private PinchZoomViewGroup mZoomLayout;
    private VerticalSlideColorPicker mVerticalSlideColorPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPhotoEditorView=findViewById(R.id.photoEditorView);
        mZoomLayout=findViewById(R.id.zoomLayout);
        mVerticalSlideColorPicker=findViewById(R.id.colorPicker);
        Button btnBrush=findViewById(R.id.btnBrush);
        BrushButton btnBrushIcon=findViewById(R.id.btnBrushIcon);
        btnBrushIcon.showBackGround();
        mPhotoEditor=new PhotoEditor.Builder(this,mPhotoEditorView)
                .build();


        btnBrush.setOnClickListener(v->{
//            mPhotoEditor.addText("salam", Color.BLACK);
//
//            if (Objects.equals(btnBrush.getTag(),"on")){
////                mPhotoEditor.setBrushDrawingMode(false);
//                btnBrush.setText("turn on brush");
//                btnBrush.setTag("off");
//                btnBrushIcon.hideBackground();
//            }else {
////                mPhotoEditor.setBrushDrawingMode(true);
//                btnBrush.setText("turn off brush");
//                btnBrush.setTag("on");
//                btnBrushIcon.showBackGround();
//
//            }
            Intent intent=new Intent(this,CropActivity.class);
            startActivityForResult(intent,CROP_REQ_CODE);
        });
        mPhotoEditor.setOnPhotoEditorListener(new OnPhotoEditorListener() {
            @Override
            public void onEditTextChangeListener(View rootView, String text, int colorCode) {
                Log.v("TTT","onEditTextChangeListener");

            }

            @Override
            public void onAddViewListener(ViewType viewType, int numberOfAddedViews) {

            }

            @Override
            public void onRemoveViewListener(ViewType viewType, int numberOfAddedViews) {

            }

            @Override
            public void onStartViewChangeListener(ViewType viewType) {
                Log.v("TTT","onStartViewChangeListener");
                mZoomLayout.setScrollEnabled(false);
                mZoomLayout.setZoomEnabled(false);
            }

            @Override
            public void onStopViewChangeListener(ViewType viewType) {
                Log.v("TTT","onStopViewChangeListener");
                mZoomLayout.setScrollEnabled(true);
                mZoomLayout.setZoomEnabled(true);
            }

            @Override
            public void onTouchSourceImage(MotionEvent event) {

            }
        });
        mVerticalSlideColorPicker.setListener(new VerticalSlideColorPicker.Listener() {
            @Override
            public void onColorChange(int selectedColor) {
                btnBrushIcon.showBrushSize();
                btnBrushIcon.setColor(selectedColor);
            }

            @Override
            public void onBrushScaled(float percentChanged) {

                btnBrushIcon.changeBrushSize(percentChanged);
            }

            @Override
            public void onRelease() {

                btnBrushIcon.showEditIcon();
                mPhotoEditor.setBrushDrawingMode(true);
                ShapeBuilder shapeBuilder=new ShapeBuilder()
                        .withShapeColor(btnBrushIcon.getColor())
                        .withShapeSize(btnBrushIcon.getBrushSize())
                        .withShapeType(ShapeType.BRUSH);
                mPhotoEditor.setShape(shapeBuilder);
            }

            @Override
            public void onMove() {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==CROP_REQ_CODE){
            if (resultCode == RESULT_OK && data!=null){
                RectF rectF=data.getParcelableExtra("rect");
                cropImage(rectF);
            }
        }
    }

    private void cropImage(RectF rectF) {
        Drawable drawable=mPhotoEditorView.getSource().getDrawable();
        Bitmap source=drawableToBitmap(drawable);
        Bitmap cropped=Bitmap.createBitmap(source,(int)rectF.left,(int)rectF.top,(int)rectF.width(),(int)rectF.height());
        mPhotoEditorView.getSource().setImageBitmap(cropped);
    }
    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

//        if (drawable instanceof BitmapDrawable) {
//            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
//            if(bitmapDrawable.getBitmap() != null) {
//                return bitmapDrawable.getBitmap();
//            }
//        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
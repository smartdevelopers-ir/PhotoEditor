package ir.smartdevelopers.imageeditor;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.util.Objects;

import ir.smartdevelopers.smartphotoeditor.VerticalSlideColorPicker;
import ir.smartdevelopers.smartphotoeditor.photoeditor.OnPhotoEditorListener;
import ir.smartdevelopers.smartphotoeditor.photoeditor.PhotoEditor;
import ir.smartdevelopers.smartphotoeditor.photoeditor.PhotoEditorView;
import ir.smartdevelopers.smartphotoeditor.photoeditor.ViewType;


public class MainActivity extends AppCompatActivity implements ColorPickerDialog.OnColorChangedListener {

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
        mPhotoEditor=new PhotoEditor.Builder(this,mPhotoEditorView)
                .build();


        btnBrush.setOnClickListener(v->{
            mPhotoEditor.addText("salam", Color.BLACK);
            new ColorPickerDialog(MainActivity.this, MainActivity.this, Color.WHITE).show();

            if (Objects.equals(btnBrush.getTag(),"on")){
//                mPhotoEditor.setBrushDrawingMode(false);
                btnBrush.setText("turn on brush");
                btnBrush.setTag("off");
            }else {
//                mPhotoEditor.setBrushDrawingMode(true);
                btnBrush.setText("turn off brush");
                btnBrush.setTag("on");
            }
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
        mVerticalSlideColorPicker.setOnColorChangeListener(new VerticalSlideColorPicker.OnColorChangeListener() {
            @Override
            public void onColorChange(int selectedColor) {
                btnBrush.setTextColor(selectedColor);
            }
        });
    }

    @Override
    public void colorChanged(int color) {

    }
}
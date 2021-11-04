package ir.smartdevelopers.imageeditor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import ir.smartdevelopers.smartphotoeditor.CropImageView;

public class CropActivity extends AppCompatActivity {

    private CropImageView mCropImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        mCropImageView=findViewById(R.id.cropView);
        Drawable drawable= ContextCompat.getDrawable(this,R.drawable.dd);
        mCropImageView.setImageDrawable(drawable);
        Button btnSave=findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v->{
            save();
        });
    }

    private void save() {
        RectF rectF=mCropImageView.getActualCropRect();
        Intent intent=new Intent();
        intent.putExtra("rect",rectF);
        setResult(RESULT_OK,intent);
        finish();
    }
}
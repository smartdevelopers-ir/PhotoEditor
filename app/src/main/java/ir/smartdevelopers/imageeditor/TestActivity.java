package ir.smartdevelopers.imageeditor;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import java.io.File;

import ir.smartdevelopers.smartphotoeditor.PhotoEditorFragment;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Uri pic=Uri.parse("android.resource://"+getPackageName()+"/"+R.drawable.dd);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentView,
                PhotoEditorFragment.getInstance(pic))
        .commit();
    }
}
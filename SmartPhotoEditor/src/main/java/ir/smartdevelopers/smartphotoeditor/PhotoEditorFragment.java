package ir.smartdevelopers.smartphotoeditor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import ir.smartdevelopers.smartphotoeditor.photoeditor.PhotoEditor;
import ir.smartdevelopers.smartphotoeditor.photoeditor.PhotoEditorView;

public class PhotoEditorFragment extends Fragment {

    private PhotoEditorView mPhotoEditorView;
    private PhotoEditor mPhotoEditor;
    private String mImagePath;

    public static PhotoEditorFragment getInstance(String imagePath) {
        PhotoEditorFragment fragment=new PhotoEditorFragment();
        Bundle bundle=new Bundle();
        bundle.putString("mImagePath",imagePath);
        fragment.setArguments(bundle);
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.spe_fragment_photo_editor,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle=getArguments();
        if (bundle != null) {
            mImagePath=bundle.getString("mImagePath");
        }
        Glide.with(view).load(mImagePath).into(mPhotoEditorView.getSource());
        mPhotoEditor=new PhotoEditor.Builder(getContext(),mPhotoEditorView).build();
    }
}

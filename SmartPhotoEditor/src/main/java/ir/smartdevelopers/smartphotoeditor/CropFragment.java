package ir.smartdevelopers.smartphotoeditor;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ir.smartdevelopers.smartphotoeditor.cropper.CropImageView;

public class CropFragment extends Fragment {
    private CropImageView mCropImageView;
    private EditorViewModel mEditorViewModel;
    private String mParentTag;

    public static CropFragment getInstance(String parentTag) {
        CropFragment fragment=new CropFragment();
        Bundle bundle=new Bundle();
        bundle.putString("mParentTag",parentTag);
        fragment.setArguments(bundle);
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.spe_fragment_crop,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle=getArguments();
        if (bundle != null) {
            mParentTag=bundle.getString("mParentTag");
        }
        Fragment owner=getParentFragmentManager().findFragmentByTag(mParentTag);
        if (owner==null){
            owner=this;
        }
        mEditorViewModel=new ViewModelProvider(owner).get(EditorViewModel.class);
        mCropImageView=view.findViewById(R.id.spe_cropView);
        AppCompatButton btnDone=view.findViewById(R.id.spe_btnDone);
        AppCompatButton btnCancel=view.findViewById(R.id.spe_btnCancel);

        Rect imageRect=mEditorViewModel.getImageCropRect();
        Bitmap sourceBitmap=mEditorViewModel.getEditedBitmap();
        if (sourceBitmap != null) {
            mCropImageView.setImageBitmap(sourceBitmap);
//            if (previousRect != null) {
//                mCropImageView.setCropWindowRect(previousRect);
//            }
            if (imageRect!=null){
                mCropImageView.setCropRect(imageRect);
            }
        }

        btnCancel.setOnClickListener(v->{
            close();
        });
        btnDone.setOnClickListener(v->{
            saveAndClose();
        });
        if (getActivity()!=null){
            getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    close();
                }
            });
        }
    }

    private void saveAndClose() {
        RectF windowCropRect=mCropImageView.getCropWindowRect();
        mEditorViewModel.setWindowCroppedRect(windowCropRect);
        mEditorViewModel.setImageCropRect(mCropImageView.getCropRect());
        close();
    }

    private void close() {
        Fragment editFragment=getParentFragmentManager().findFragmentByTag(mParentTag);
        if (editFragment instanceof PhotoEditorFragment){
            PhotoEditorFragment photoEditorFragment= (PhotoEditorFragment) editFragment;
            if (photoEditorFragment.getOnEditorListener() !=null){
                photoEditorFragment.getOnEditorListener().onCropWindowClosed();
            }
        }
        getParentFragmentManager().popBackStack();

    }
}

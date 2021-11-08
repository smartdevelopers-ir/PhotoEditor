package ir.smartdevelopers.smartphotoeditor;

import android.Manifest;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.annotation.RestrictTo;
import androidx.fragment.app.Fragment;

import java.io.OutputStream;

import ir.smartdevelopers.smartphotoeditor.photoeditor.OnSaveBitmap;
import ir.smartdevelopers.smartphotoeditor.photoeditor.PhotoEditor;

public class PhotoEditorFragment extends Fragment {

    private Uri mImageUri;
    private EditFragment mEditFragment;
    private OnEditorListener mOnEditorListener;
    public static PhotoEditorFragment getInstance(Uri photoUri) {
        PhotoEditorFragment fragment=new PhotoEditorFragment();
        Bundle bundle=new Bundle();
        bundle.putParcelable("mImageUri",photoUri);
        fragment.setArguments(bundle);
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.spe_fragment_photo_editor_layout,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle=getArguments();
        if (bundle != null) {
            mImageUri=bundle.getParcelable("mImageUri");
        }
        mEditFragment=EditFragment.getInstance(mImageUri);
        getChildFragmentManager().beginTransaction().replace(R.id.spe_fragment_view,
                mEditFragment)
                .commit();
    }


    public void registerEditorListener(OnEditorListener onEditorListener){
        mOnEditorListener=onEditorListener;
    }
    public void unRegisterEditorListener(){
        mOnEditorListener=null;
    }
    void openCropFragment(){
        getChildFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.spe_anim_enter_fragment,0,0,R.anim.spe_anim_pop_exit_fragment)
                .addToBackStack(null)
                .add(R.id.spe_fragment_view,new CropFragment())
                .commit();
        if (mOnEditorListener != null) {
            mOnEditorListener.onCropWindowOpened();
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public OnEditorListener getOnEditorListener() {
        return mOnEditorListener;
    }

    public void goToEditFragment() {
        getChildFragmentManager().popBackStack();
        if (mOnEditorListener != null) {
            mOnEditorListener.onCropWindowClosed();
        }
    }
    public void setImageUri(Uri uri){
        mImageUri=uri;
        mEditFragment.setImageUri(uri);
    }

    public interface OnEditorListener{
        void onCropWindowOpened();
        void onCropWindowClosed();
        void onFadeViews(float alpha);
    }
    public abstract class SimpleOnEditorListener implements OnEditorListener{
        @Override
        public void onCropWindowClosed() {}

        @Override
        public void onCropWindowOpened() {}

        @Override
        public void onFadeViews(float alpha) {}
    }
    /**
     * @param compressFormat see {@link Bitmap.CompressFormat} default = {@code Bitmap.CompressFormat.JPEG}
     * @param outputStream is outputStream of where you want to save image
     * */
    @RequiresPermission(allOf = {Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void saveAsFile(OutputStream outputStream, Bitmap.CompressFormat compressFormat, PhotoEditor.OnSaveListener onSaveListener){
        Fragment currentFragment=getChildFragmentManager().findFragmentById(R.id.spe_fragment_view);
        if (currentFragment instanceof EditFragment){
            ((EditFragment) currentFragment).saveAsFile(outputStream,compressFormat, onSaveListener);
        }
    }
    /**
     * @param compressFormat see {@link Bitmap.CompressFormat} default = {@code Bitmap.CompressFormat.JPEG}
     * */
    public void saveAsBitmap(Bitmap.CompressFormat compressFormat,OnSaveBitmap onSaveBitmap){
        Fragment currentFragment=getChildFragmentManager().findFragmentById(R.id.spe_fragment_view);
        if (currentFragment instanceof EditFragment){
            ((EditFragment) currentFragment).saveAsBitmap(compressFormat,onSaveBitmap);
        }
    }
}

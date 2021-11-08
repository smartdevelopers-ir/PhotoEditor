package ir.smartdevelopers.smartphotoeditor;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PhotoEditorFragment extends Fragment {

    private Uri mImageUri;
    private EditFragment mEditFragment;
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


    void openCropFragment(){
        getChildFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.spe_anim_enter_fragment,0,0,R.anim.spe_anim_pop_exit_fragment)
                .addToBackStack(null)
                .add(R.id.spe_fragment_view,new CropFragment())
                .commit();
    }

    public void goToEditFragment() {
        getChildFragmentManager().popBackStack();
    }
    public void setImageUri(Uri uri){
        mImageUri=uri;
        mEditFragment.setImageUri(uri);
    }

    public interface OnEditorListener{
        void onCropWindowOpen();
        void onCropWindowClosed();
        void onDrawing();
    }
}

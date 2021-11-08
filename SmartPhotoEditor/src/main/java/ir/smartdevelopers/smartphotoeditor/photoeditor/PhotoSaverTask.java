package ir.smartdevelopers.smartphotoeditor.photoeditor;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Created by Burhanuddin Rashid on 18/05/21.
 *
 * @author <https://github.com/burhanrashid52>
 */
class PhotoSaverTask extends AsyncTask<OutputStream, String, PhotoSaverTask.SaveResult> {

    public static final String TAG = "PhotoSaverTask";
    private @NonNull
    SaveSettings mSaveSettings;
    private @Nullable
    PhotoEditor.OnSaveListener mOnSaveListener;
    private @Nullable
    OnSaveBitmap mOnSaveBitmap;
    private final PhotoEditorView mPhotoEditorView;
    private final BoxHelper mBoxHelper;
    private final DrawingView mDrawingView;


    public PhotoSaverTask(PhotoEditorView photoEditorView, BoxHelper boxHelper) {
        mPhotoEditorView = photoEditorView;
        mDrawingView = photoEditorView.getDrawingView();
        mBoxHelper = boxHelper;
        mSaveSettings = new SaveSettings.Builder().build();
    }

    public void setOnSaveListener(@Nullable PhotoEditor.OnSaveListener onSaveListener) {
        this.mOnSaveListener = onSaveListener;
    }

    public void setOnSaveBitmap(@Nullable OnSaveBitmap onSaveBitmap) {
        mOnSaveBitmap = onSaveBitmap;
    }

    public void setSaveSettings(@NonNull SaveSettings saveSettings) {
        mSaveSettings = saveSettings;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mBoxHelper.clearHelperBox();
        mDrawingView.destroyDrawingCache();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected SaveResult doInBackground(OutputStream... outputStreams) {
        // Create a media file name
        if (outputStreams.length == 0) {
            return saveImageAsBitmap();
        } else {
            return saveImageInFile(outputStreams[0]);
        }
    }

    private SaveResult saveImageAsBitmap() {
        if (mPhotoEditorView != null) {
            return new SaveResult(null, false, buildBitmap());
        } else {
            return new SaveResult(null, false, null);
        }
    }

    @NonNull
    private SaveResult saveImageInFile(OutputStream out) {

        try {
//            FileOutputStream out = new FileOutputStream(file, false);
            if (mPhotoEditorView != null) {
                Bitmap capturedBitmap = buildBitmap();
                capturedBitmap.compress(mSaveSettings.getCompressFormat(), mSaveSettings.getCompressQuality(), out);
            }
            out.flush();
            out.close();
            Log.d(TAG, "Filed Saved Successfully");
            return new SaveResult(null, true, null);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to save File");
            return new SaveResult(e, true, null);
        }
    }

    private Bitmap buildBitmap() {
        return mSaveSettings.isTransparencyEnabled()
                ? BitmapUtil.removeTransparency(captureView(mPhotoEditorView))
                : captureView(mPhotoEditorView);
    }

    @Override
    protected void onPostExecute(SaveResult saveResult) {
        super.onPostExecute(saveResult);
        if (!saveResult.asFile) {
            handleBitmapCallback(saveResult);
        } else {
            handleFileCallback(saveResult);
        }

    }

    private void handleFileCallback(SaveResult saveResult) {
        Exception exception = saveResult.mException;
//        String imagePath = saveResult.mImagePath;
        if (exception == null) {
            //Clear all views if its enabled in save settings
            if (mSaveSettings.isClearViewsEnabled()) {
                mBoxHelper.clearAllViews(mDrawingView);
            }
            if (mOnSaveListener != null) {
//                assert imagePath != null;
                mOnSaveListener.onSuccess();
            }
        } else {
            if (mOnSaveListener != null) {
                mOnSaveListener.onFailure(exception);
            }
        }
    }

    private void handleBitmapCallback(SaveResult saveResult) {
        Bitmap bitmap = saveResult.mBitmap;
        if (bitmap != null) {
            if (mSaveSettings.isClearViewsEnabled()) {
                mBoxHelper.clearAllViews(mDrawingView);
            }
            if (mOnSaveBitmap != null) {
                mOnSaveBitmap.onBitmapReady(bitmap);
            }
        } else {
            if (mOnSaveBitmap != null) {
                mOnSaveBitmap.onFailure(new Exception("Failed to load the bitmap"));
            }
        }
    }

    private Bitmap captureView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(
                view.getWidth(),
                view.getHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public void saveBitmap() {
        execute();
    }

    public void saveFile(OutputStream outputStream) {
        execute(outputStream);
    }

    static class SaveResult {
        final Exception mException;
        final Bitmap mBitmap;
        final boolean asFile;

        public SaveResult(Exception exception, boolean asFile,Bitmap bitmap) {
            mException = exception;
            mBitmap = bitmap;
            this.asFile = asFile;
        }
    }
}

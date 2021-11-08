package ir.smartdevelopers.smartphotoeditor;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

 public class EditorViewModel extends ViewModel {
    private final MutableLiveData<Bitmap> mScaledBitmapLiveData;
    private final MutableLiveData<Bitmap> mEditedBitmap;
    private final MutableLiveData<RectF> mWindowCroppedRectLiveData;
    private final MutableLiveData<Rect> mImageCroppedRectLiveData;


    public EditorViewModel() {
        mScaledBitmapLiveData=new MutableLiveData<>();
        mWindowCroppedRectLiveData =new MutableLiveData<>();
        mImageCroppedRectLiveData =new MutableLiveData<>();
        mEditedBitmap =new MutableLiveData<>();
    }
     void setSourceBitmap(Bitmap sourceBitmap){
        mScaledBitmapLiveData.setValue(sourceBitmap);
     }
     void setEditedBitmap(Bitmap editedBitmap){
         mEditedBitmap.setValue(editedBitmap);
     }
     Bitmap getScaledSourceBitmap(){
        return mScaledBitmapLiveData.getValue();
     }
     Bitmap getEditedBitmap(){
        return mEditedBitmap.getValue();
     }

      MutableLiveData<Bitmap> getScaledBitmapLiveData() {
         return mScaledBitmapLiveData;
     }
     void setWindowCroppedRect(RectF rect){
        mWindowCroppedRectLiveData.setValue(rect);
     }
     RectF getWindowCroppedRect(){
        return mWindowCroppedRectLiveData.getValue();
     }

     public MutableLiveData<RectF> getWindowCroppedRectLiveData() {
         return mWindowCroppedRectLiveData;
     }

     void setImageCropRect(Rect rect){
        mImageCroppedRectLiveData.setValue(rect);
     }
     Rect getImageCropRect(){
        return mImageCroppedRectLiveData.getValue();
     }

     public MutableLiveData<Rect> getImageCroppedRectLiveData() {
         return mImageCroppedRectLiveData;
     }
 }

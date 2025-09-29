package ir.smartdevelopers.smartphotoeditor;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;

import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ir.smartdevelopers.smartphotoeditor.cropper.BitmapUtils;
import ir.smartdevelopers.smartphotoeditor.photoeditor.OnPhotoEditorListener;
import ir.smartdevelopers.smartphotoeditor.photoeditor.OnSaveBitmap;
import ir.smartdevelopers.smartphotoeditor.photoeditor.PhotoEditor;
import ir.smartdevelopers.smartphotoeditor.photoeditor.PhotoEditorView;
import ir.smartdevelopers.smartphotoeditor.photoeditor.SaveSettings;
import ir.smartdevelopers.smartphotoeditor.photoeditor.TextStyleBuilder;
import ir.smartdevelopers.smartphotoeditor.photoeditor.ViewType;
import ir.smartdevelopers.smartphotoeditor.photoeditor.shape.ShapeBuilder;

public class PhotoEditorFragment extends Fragment implements KeyboardHeightProvider.KeyboardHeightObserver,
EmojiDialog.OnEmojiListener{

    private PhotoEditorView mPhotoEditorView;
    private PhotoEditor mPhotoEditor;
    private Uri mImageUri;
    private VerticalSlideColorPicker mBrushColorPiker,mTextColorPicker;
    private HorizontaSlideColorPicker mTextBackgroundColorSelector;
    private BrushButton btnBrush;
    private TextButton btnInsertText;
    private EmojiButton btnEmoji;
    private EditText edtTextInput;
    private ImageButton btnUndo,btnCrop;
    private FrameLayout mTextInputContainer;
    private AppCompatTextView txtTextInputHelper;
    private static final long ANIMATION_DURATION=200;
    private  float DEFAULT_TEXT_SIZE;//dp
    private KeyboardHeightProvider mKeyboardHeightProvider;
    private ExecutorService mExecutorService= Executors.newSingleThreadExecutor();
    private ProgressBar mProgressBar;
    private EditorViewModel mEditorViewModel;
    private static int mAddedViewCount=0;
//    private Bitmap mThumbnail;
    private boolean mShowThumbnail;
    private OnEditorListener mOnEditorListener;
    private Bitmap mPreview;


    public static PhotoEditorFragment getInstance(Uri imageUri,boolean showThumbnail) {
        PhotoEditorFragment fragment=new PhotoEditorFragment();
        Bundle bundle=new Bundle();
        bundle.putString("mImageUri",String.valueOf(imageUri));
        bundle.putBoolean("mShowThumbnail",showThumbnail);
        fragment.setArguments(bundle);
        return fragment;
    }
    public static PhotoEditorFragment getInstance(Uri imageUri,Bitmap preview) {
        PhotoEditorFragment fragment=new PhotoEditorFragment();
        Bundle bundle=new Bundle();
        bundle.putString("mImageUri",String.valueOf(imageUri));
        bundle.putParcelable("mPreview",preview);

        fragment.setArguments(bundle);
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.spe_fragment_edit_layout,container,false);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (TextUtils.isEmpty(getTag())){
            throw new RuntimeException("You must set tag for PhotoEditorFragment");
        }
        mEditorViewModel=new ViewModelProvider(this).get(EditorViewModel.class);
        DEFAULT_TEXT_SIZE=getResources().getDimensionPixelSize(R.dimen.spe_default_input_text_size);
        mKeyboardHeightProvider=new KeyboardHeightProvider(view);
        view.post(()->{
            mKeyboardHeightProvider.start();
//            translateTextInputToCenter();
        });
        Bundle bundle=getArguments();
        if (bundle != null) {
            String uriSt=bundle.getString("mImageUri");
            if (!TextUtils.isEmpty(uriSt)){
                mImageUri =Uri.parse(uriSt);
            }
            mPreview = bundle.getParcelable("mPreview");
            mShowThumbnail=bundle.getBoolean("mShowThumbnail");

        }
        findViews(view);
//        mThumbnail=mEditorViewModel.getThumbnail();

        mPhotoEditor=new PhotoEditor.Builder(getContext(),mPhotoEditorView).build();
//        mPhotoEditorView.getSource().setImageBitmap(mThumbnail);
//        mPhotoEditorView.getSource().setImageBitmap(getThumbnail(mImageUri));


//        Glide.with(view).load(mImageUri).into(mPhotoEditorView.getSource());
        initViews();
        if (mPreview != null){


            mPhotoEditorView.getSource().setImageBitmap(mPreview);
            if (mOnEditorListener != null){
                mOnEditorListener.onPreviewLoaded();
            }
        }
        setImageUri(mImageUri);
        initPhotoEditorListener();
    }



    private void initPhotoEditorListener() {
        mPhotoEditor.setOnPhotoEditorListener(new OnPhotoEditorListener() {

            private boolean animate=false;
            @Override
            public void onEditTextChangeListener(View rootView, String text, int colorCode,Drawable background) {
                rootView.setVisibility(View.GONE);
                showTextInput(rootView,text,colorCode,background);
            }

            @Override
            public void onAddViewListener(ViewType viewType, int numberOfAddedViews) {
                mAddedViewCount=numberOfAddedViews;
                if (numberOfAddedViews > 0){
                    btnUndo.setVisibility(View.VISIBLE);
                }
                if (mOnEditorListener != null) {
                    mOnEditorListener.onEdit(isEdited());
                }

            }

            @Override
            public void onRemoveViewListener(ViewType viewType, int numberOfAddedViews) {
                mAddedViewCount=numberOfAddedViews;
                if (numberOfAddedViews==0){
                    btnUndo.setVisibility(View.GONE);
                }
                if (mOnEditorListener != null) {
                    mOnEditorListener.onEdit(isEdited());
                }
            }

            @Override
            public void onStartViewChangeListener(ViewType viewType) {
                if (viewType == ViewType.BRUSH_DRAWING){
                    animate=true;
                   new Handler().postDelayed(()->{
                       if (animate){
                           hideAllViews();
                       }
                   },200);
                }
            }

            @Override
            public void onStopViewChangeListener(ViewType viewType) {
                if (viewType == ViewType.BRUSH_DRAWING){
                    animate=false;
                   showAllViews();
                }
            }

            @Override
            public void onTouchSourceImage(MotionEvent event) {

            }
        });

    }

    private ValueAnimator mViewAnimator;
    private float mAnimatedAlpha=1;
    private void hideAllViews(){
       animateView(0);
    }
    private void showAllViews(){
        animateView(1);
    }
    private void animateView(float endAlpha){
        if (mViewAnimator!=null){
            mViewAnimator.cancel();
        }

        mViewAnimator=ObjectAnimator.ofFloat(mAnimatedAlpha,endAlpha);
        mViewAnimator.setDuration(ANIMATION_DURATION);
        mViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha= (float) animation.getAnimatedValue();
                btnBrush.setAlpha(alpha);
                btnCrop.setAlpha(alpha);
                btnInsertText.setAlpha(alpha);
                btnEmoji.setAlpha(alpha);
                btnUndo.setAlpha(alpha);
                mBrushColorPiker.setAlpha(alpha);
                mAnimatedAlpha=alpha;
                if (mOnEditorListener != null) {
                    mOnEditorListener.onFadeViews(alpha);
                }
            }
        });
        mViewAnimator.start();
    }

    private void initViews() {
        //<editor-fold desc="Brush Section">
        mBrushColorPiker.setVisibility(View.INVISIBLE);
        btnBrush.setOnClickListener(v->{
            if (Objects.equals("active",btnBrush.getTag())){
                // hide color picker and background
                deactivateBrushingTools(true);
            }else {
                // show color picker
                btnBrush.setTag("active");
                showColorPicker(mBrushColorPiker,true);
                btnBrush.showBackGround();
                ShapeBuilder shapeBuilder=new ShapeBuilder()
                        .withShapeSize(btnBrush.getBrushSize())
                        .withShapeColor(btnBrush.getColor());
                mPhotoEditor.setShape(shapeBuilder);
                mPhotoEditor.setBrushDrawingMode(true);
            }
        });
        mBrushColorPiker.setListener(new VerticalSlideColorPicker.Listener() {
            @Override
            public void onColorChange(int selectedColor) {
                btnBrush.setColor(selectedColor);
            }

            @Override
            public void onBrushScaled(float percentChanged) {
                btnBrush.changeBrushSize(percentChanged);
            }

            @Override
            public void onRelease() {
                btnBrush.showEditIcon();
                ShapeBuilder shapeBuilder=new ShapeBuilder()
                        .withShapeSize(btnBrush.getBrushSize())
                        .withShapeColor(btnBrush.getColor());
                mPhotoEditor.setShape(shapeBuilder);

            }
            @Override
            public void onMove() {
                btnBrush.showBrushSize();

            }
        });
        //</editor-fold>

        mTextColorPicker.setVisibility(View.INVISIBLE);
//        edtTextInput.setTextSize(TypedValue.COMPLEX_UNIT_DIP,DEFAULT_TEXT_SIZE);
//        txtTextInputHelper.setTextSize(DEFAULT_TEXT_SIZE);
        btnInsertText.setOnClickListener(v->{
            if (btnInsertText.getTag()==null){
                showTextInput(null,"", VerticalSlideColorPicker.DEFAULT_COLOR, null);
                btnInsertText.setTag("no_bg");
                btnInsertText.changeIconToNoBackground();
            }else if (Objects.equals("no_bg",btnInsertText.getTag())){
                showTextBackground();
                btnInsertText.setTag("with_bg");
                btnInsertText.changeIconToFilledBackground();
                setTextInputBackground(mTextBackgroundColorSelector.getColor());
            }else if (Objects.equals("with_bg",btnInsertText.getTag())){
                hideTextBackgroundColor();
                btnInsertText.setTag("no_bg");
                btnInsertText.changeIconToNoBackground();
            }
        });

        if (getActivity()!=null){
            getActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
                @SuppressWarnings("ConstantConditions")
                @Override
                public void handleOnBackPressed() {
                    if (btnInsertText.getTag()!= null){
                        manageInsertText();
                    }else {
                        this.setEnabled(false);
                        getActivity().onBackPressed();
                    }
                }
            });
        }

        mTextColorPicker.setListener(new VerticalSlideColorPicker.Listener() {
            @Override
            public void onColorChange(int selectedColor) {
                edtTextInput.setTextColor(selectedColor);
                btnInsertText.setColor(selectedColor);
            }

            @Override
            public void onBrushScaled(float percentChanged) {

            }

            @Override
            public void onRelease() {

            }

            @Override
            public void onMove() {

            }
        });
        mTextBackgroundColorSelector.setListener(new HorizontaSlideColorPicker.Listener() {
            @Override
            public void onColorChange(int selectedColor) {
                setTextInputBackground(selectedColor);
            }

            @Override
            public void onBrushScaled(float percentChanged) {

            }

            @Override
            public void onRelease() {

            }

            @Override
            public void onMove() {

            }
        });
        txtTextInputHelper.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                edtTextInput.setTextSize(TypedValue.COMPLEX_UNIT_PX,txtTextInputHelper.getTextSize());
            }
        });
        edtTextInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int lines=edtTextInput.getLineCount();
                if (lines <= 16) {
                    txtTextInputHelper.setText(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                int lines=edtTextInput.getLineCount();
                if (lines > 16){
                    s.delete(s.length()-1,s.length());
                }
            }
        });

        btnUndo.setImageResource(R.drawable.spe_ic_undo);
        btnUndo.setOnClickListener(v->{
            mPhotoEditor.undo();
        });

        btnEmoji.setOnClickListener(v->{

            if (Objects.equals(btnBrush.getTag(),"active")){
                deactivateBrushingTools(true);
            }
            btnEmoji.showBackGround();
            showEmojiBottomSheet();
        });

        btnCrop.setImageResource(R.drawable.spe_ic_crop_rotate);
        btnCrop.setOnClickListener(v->{
            openCropFragment();
        });

        mEditorViewModel.getImageCroppedRectLiveData().observe(getViewLifecycleOwner(), new Observer<Rect>() {
            @Override
            public void onChanged(Rect rect) {
                if (rect !=null){
                    cropImage(rect);
                }
            }
        });

        Bundle bundle=getArguments();
        if (bundle != null) {
            String transitionName=bundle.getString("transitionName");
            ViewCompat.setTransitionName(mPhotoEditorView.getSource(),transitionName);
        }
    }

    private void setTextInputBackground(int color){
        if (Objects.equals(btnInsertText.getTag(),"with_bg")){
            GradientDrawable gradientDrawable=new GradientDrawable();
            gradientDrawable.setColor(color);
            gradientDrawable.setShape(GradientDrawable.RECTANGLE);
            gradientDrawable.setCornerRadius(24f);
            edtTextInput.setBackground(gradientDrawable);
        }
    }
    private void showTextBackground() {
        mTextBackgroundColorSelector.setAlpha(0);
        mTextBackgroundColorSelector.setTranslationY(-mTextBackgroundColorSelector.getMeasuredHeight());
        mTextBackgroundColorSelector.setVisibility(View.VISIBLE);
        mTextBackgroundColorSelector.post(()->{
            mTextBackgroundColorSelector.animate().setDuration(ANIMATION_DURATION)
                    .alpha(1).translationY(0).start();
        });
    }
    private void hideTextBackgroundColor(){
        edtTextInput.setBackground(null);
        mTextBackgroundColorSelector.animate().alpha(0).translationY(-mTextBackgroundColorSelector.getHeight())
                .withEndAction(()->{
                    mTextBackgroundColorSelector.setVisibility(View.INVISIBLE);
                }).start();
    }

    private void openCropFragment() {

        Bitmap source=mEditorViewModel.getScaledSourceBitmap();
        mEditorViewModel.setEditedBitmap(source);
        int containerId=((View)(getView().getParent())).getId();
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.spe_anim_enter_fragment,0,0,R.anim.spe_anim_pop_exit_fragment)
                .add(containerId,CropFragment.getInstance(getTag()),
                        CropFragment.class.getName())
                .addToBackStack(null)
                .commit();
        if (mOnEditorListener != null) {
            mOnEditorListener.onCropWindowOpened();
        }
//        if(getParentFragment() instanceof PhotoEditorFragment){
//            PhotoEditorFragment fragment=(PhotoEditorFragment) getParentFragment();
//            fragment.openCropFragment();
//        }
    }



    private void showEmojiBottomSheet() {
        EmojiDialog emojiDialog=new EmojiDialog();
        if (getChildFragmentManager().findFragmentByTag("emoji_dialog")==null){
            emojiDialog.show(getChildFragmentManager(),"emoji_dialog");
        }
    }

    private void deactivateBrushingTools(boolean animateColorPicker) {
        btnBrush.setTag("deactivate");

        hideColorPicker(mBrushColorPiker,animateColorPicker);
        btnBrush.hideBackground();
        mPhotoEditor.setBrushDrawingMode(false);
    }

    private void showTextInput(View editingView, String text, int colorCode, Drawable background) {
        btnInsertText.setTag("no_bg");
        boolean animate=true;
        if (Objects.equals(btnBrush.getTag(),"active")){
            animate=false;
            deactivateBrushingTools(false);
        }
        showColorPicker(mTextColorPicker,animate);
        btnInsertText.showBackGround();
        // reset text size
        edtTextInput.setTextSize(TypedValue.COMPLEX_UNIT_PX,DEFAULT_TEXT_SIZE);
//                txtTextInputHelper.setTextSize(DEFAULT_TEXT_SIZE);

        mTextInputContainer.setVisibility(View.VISIBLE);
        mTextInputContainer.post(this::translateTextInputToCenter);
        edtTextInput.setText(text);
        if (!TextUtils.isEmpty(text)){
            edtTextInput.setSelection(text.length());
        }
        edtTextInput.setTextColor(colorCode);
        edtTextInput.setTag(editingView);
        btnInsertText.setColor(colorCode);
        edtTextInput.requestFocus();
        if (background instanceof GradientDrawable){
            btnInsertText.changeIconToFilledBackground();
            btnInsertText.setTag("with_bg");
            showTextBackground();
            edtTextInput.setBackground(background);
        }
        if (mOnEditorListener != null) {
            mOnEditorListener.onOpenAddInputText();
        }
        showKeyboard();
    }

    private void manageInsertText() {


        CharSequence text=edtTextInput.getText();
        int color= btnInsertText.getColor();
        float textSize= edtTextInput.getTextSize() / getResources().getDisplayMetrics().scaledDensity;
        String inputText="";
        if (!TextUtils.isEmpty(text) && edtTextInput.getTag()==null){ // is in add mode
            inputText=text.toString();
            TextStyleBuilder textStyleBuilder=new TextStyleBuilder();
            textStyleBuilder.withTextColor(color);
            textStyleBuilder.withTextSize(textSize);
            textStyleBuilder.withGravity(Gravity.CENTER);
            if (Objects.equals(btnInsertText.getTag(),"with_bg")){
                textStyleBuilder.withBackgroundDrawable(edtTextInput.getBackground());
            }
            mPhotoEditor.addText(inputText,textStyleBuilder);
            if (mOnEditorListener != null) {
                mOnEditorListener.onInputTextAdded(text);
            }

            edtTextInput.setText("");
            txtTextInputHelper.setText("");
        } else if (edtTextInput.getTag() != null) {
            if (edtTextInput.getTag() instanceof View){
                View view= (View) edtTextInput.getTag();
                if (TextUtils.isEmpty(text)){
                    mPhotoEditor.removeView(view,ViewType.TEXT);
                }else {
                    TextStyleBuilder textStyleBuilder=new TextStyleBuilder();
                    textStyleBuilder.withTextColor(color);
                    textStyleBuilder.withTextSize(textSize);
                    textStyleBuilder.withGravity(Gravity.CENTER);
                    if (Objects.equals(btnInsertText.getTag(),"with_bg")){
                        textStyleBuilder.withBackgroundDrawable(edtTextInput.getBackground());
                    }
                    view.setVisibility(View.VISIBLE);
                    mPhotoEditor.editText(view,text.toString(),textStyleBuilder);

                }
                if (mOnEditorListener != null) {
                    mOnEditorListener.onInputTextEdited(text);
                }
            }
        }
        btnInsertText.hideBackground();
        mTextInputContainer.setVisibility(View.GONE);
        hideColorPicker(mTextColorPicker,true);
        hideTextBackgroundColor();
        btnInsertText.changeIconToNoBackground();
        btnInsertText.setTag(null);
        if (mOnEditorListener != null) {
            mOnEditorListener.onCloseAddInputText();
        }
    }

    private void showKeyboard(){
        if (getContext() == null) {
            return;
        }
        InputMethodManager imm=(InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(edtTextInput,InputMethodManager.SHOW_FORCED);
    }
    private void hideColorPicker(VerticalSlideColorPicker colorPiker,boolean animate) {
        long duration=0;
        if (animate){
            duration=ANIMATION_DURATION;
        }
        int deviceWidth=getResources().getDisplayMetrics().widthPixels;
        colorPiker.animate().setDuration(duration)
                .alpha(0)
                .translationX(Math.abs(deviceWidth - colorPiker.getLeft()))
                .withEndAction(()->{
                    colorPiker.setVisibility(View.INVISIBLE);
                })
                .start();
    }

    private void showColorPicker(VerticalSlideColorPicker colorPiker,boolean animate) {
        long duration=0;
        if (animate){
            duration=ANIMATION_DURATION;
        }
        colorPiker.setAlpha(0);
        int deviceWidth=getResources().getDisplayMetrics().widthPixels;
        colorPiker.setTranslationX(Math.abs(deviceWidth - colorPiker.getLeft()));
        colorPiker.setVisibility(View.VISIBLE);
        long finalDuration = duration;
        colorPiker.post(()->{
            colorPiker.animate().setDuration(finalDuration)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .alpha(1)
                    .translationX(0)
                    .start();
        });
    }

    private void findViews(View view) {
        mPhotoEditorView=view.findViewById(R.id.spe_photo_editor_view);
        btnBrush=view.findViewById(R.id.spe_photo_editor_btnBrush);
        btnInsertText=view.findViewById(R.id.spe_photo_editor_btnInsertText);
        mBrushColorPiker=view.findViewById(R.id.spe_photo_editor_brushColorSelector);
        mTextColorPicker=view.findViewById(R.id.spe_photo_editor_textColorSelector);
        edtTextInput=view.findViewById(R.id.spe_photo_editor_edtInputText);
        txtTextInputHelper=view.findViewById(R.id.spe_photo_editor_txtInputTextHelper);
        mTextInputContainer=view.findViewById(R.id.spe_photo_editor_textInputContainer);
        btnUndo=view.findViewById(R.id.spe_photo_editor_brushUndo);
        btnEmoji=view.findViewById(R.id.spe_photo_editor_btnEmoji);
        btnCrop=view.findViewById(R.id.spe_photo_editor_btnCrop);
        mProgressBar=view.findViewById(R.id.spe_photo_editor_loading);
        mTextBackgroundColorSelector=view.findViewById(R.id.spe_photo_editor_textBackgroundColorSelector);
    }

    @Override
    public void onResume() {
        super.onResume();
        mKeyboardHeightProvider.setKeyboardHeightObserver(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mKeyboardHeightProvider.setKeyboardHeightObserver(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mKeyboardHeightProvider.close();
    }

    private void cropImage(Rect rect) {
        Bitmap scaledSourceBitmap=mEditorViewModel.getScaledSourceBitmap();
        if (scaledSourceBitmap!=null){
            Bitmap croppedBitmap=Bitmap.createBitmap(scaledSourceBitmap,
                    rect.left,rect.top,rect.width(),rect.height());
            mPhotoEditorView.getSource().setImageBitmap(croppedBitmap);

            if (mOnEditorListener != null) {
                mOnEditorListener.onEdit(isEdited());
            }
        }


    }

    @SuppressLint("CheckResult")
    public void setImageUri(Uri uri){
        mImageUri=uri;
        mEditorViewModel.setWindowCroppedRect(null);
//        if (!mShowThumbnail){
//            mProgressBar.setVisibility(View.VISIBLE);
//        }
        RequestBuilder<Bitmap> glide=Glide.with(getContext()).asBitmap().dontAnimate().load(uri)

                .addListener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        new Handler(Looper.getMainLooper()).post(()->{
                                    if (mOnEditorListener != null) {
                                        mOnEditorListener.onImageLoadFailed(e);
                                    }
                        });

//                        mProgressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        new Handler(Looper.getMainLooper()).post(()->{
                            mEditorViewModel.setSourceBitmap(resource);
                            mPhotoEditorView.getSource().setImageBitmap(resource);
                            if (mOnEditorListener != null) {
                                mOnEditorListener.onImageLoaded(resource,isFirstResource);
                            }
                        });

//                        mProgressBar.setVisibility(View.GONE);
                        return false;
                    }

                });
        if (mShowThumbnail){
            glide.thumbnail(0.1f);
        }
        DisplayMetrics metrics=getResources().getDisplayMetrics();
        glide.skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
               .submit(metrics.widthPixels,metrics.heightPixels);

    }
    /** Call this methode when fragment is attached and created*/
    public void setImageBitmap(Bitmap bitmap){
        if (mPhotoEditorView==null){
            throw new RuntimeException("You must call this methode when fragment is attached and created");
        }
        mPhotoEditorView.getSource().setImageBitmap(bitmap);
        mImageUri=null;
        mEditorViewModel.setSourceBitmap(bitmap);
    }
    /** Call this methode when fragment is attached and created*/
    public ImageView getSourceImageView(){
        if (mPhotoEditorView!=null){
            return mPhotoEditorView.getSource();
        }
        return null;
    }
    @Override
    public void onKeyboardHeightChanged(int height, int orientation) {

        if (height==0){
            translateTextInputToCenter();
        }else {
            edtTextInput.setTranslationY(-height);
        }
    }
    private void translateTextInputToCenter(){
        int centerY=getResources().getDisplayMetrics().heightPixels/2;
        int inputTextHeight=edtTextInput.getMeasuredHeight();

        float top=edtTextInput.getTop();
        float diff=top - centerY ;
        float t= -1 * ((inputTextHeight /2f)+ diff );
        int[] pos=new int[2];
        edtTextInput.getLocationOnScreen(pos);
        edtTextInput.setTranslationY(t);
    }

    public boolean isEdited() {
        return (mAddedViewCount > 0 || mEditorViewModel.getImageCropRect() !=null);
    }

    private interface BitmapLoadListener{
        void onBitmapLoaded(Bitmap bitmap, int rotation, int sampleSize);
    }

    private void loadBitmap(Uri uri,BitmapLoadListener loadListener){
        if (getContext()==null){
            return ;
        }
        if (!mShowThumbnail){
            mProgressBar.setVisibility(View.VISIBLE);
        }
        WeakReference<ProgressBar> wProgress=new WeakReference<>(mProgressBar);
        WeakReference<Context> wContext=new WeakReference<>(getContext());
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        double densityAdj = metrics.density > 1 ? 1 / metrics.density : 1;
        int width = (int) (metrics.widthPixels * densityAdj);
       int height = (int) (metrics.heightPixels * densityAdj);
       mExecutorService.execute(()->{
           if (wContext.get()!=null) {
               BitmapUtils.BitmapSampled decodeResult =
                       BitmapUtils.decodeSampledBitmap(wContext.get(), uri, width, height);
               BitmapUtils.RotateBitmapResult rotateResult =
                       BitmapUtils.rotateBitmapByExif(decodeResult.bitmap, wContext.get(), uri);

               if (rotateResult.bitmap != null) {


                   if (loadListener != null) {
                       new Handler(Looper.getMainLooper()).post(()->{
                           loadListener.onBitmapLoaded(rotateResult.bitmap,rotateResult.degrees,decodeResult.sampleSize);
                            if (wProgress.get()!=null){
                                wProgress.get().setVisibility(View.GONE);
                            }
                       });
                   }
               }
           }
       });
    }
    private Bitmap getRotatedBitmap(Bitmap source,int rotation){
        Matrix matrix=new Matrix();
        if (rotation!=0){
            matrix.preRotate(rotation);
        }
        return  Bitmap.createBitmap(source, 0, 0, source.getWidth(),
                source.getHeight(), matrix, true);
    }


    /** Clears all drawings and all views*/
    public void clearAll(){
        mPhotoEditor.clearAllViews();
        if (mOnEditorListener != null) {
            mOnEditorListener.onEdit(isEdited());
        }
    }
    /**
     * @param compressFormat see {@link Bitmap.CompressFormat} default = {@code Bitmap.CompressFormat.JPEG}
     * */
     public void saveAsBitmap(@Nullable Bitmap.CompressFormat compressFormat,OnSaveBitmap onSaveBitmap){
        if (compressFormat==null){
            compressFormat= Bitmap.CompressFormat.JPEG;
        }
        SaveSettings settings=new SaveSettings.Builder()
                .setClearViewsEnabled(false)
                .setCompressFormat(compressFormat)
                .setTransparencyEnabled(true).build();
        mPhotoEditor.saveAsBitmap(settings,onSaveBitmap);
    }
    /**
     * @param compressFormat see {@link Bitmap.CompressFormat} default = {@code Bitmap.CompressFormat.JPEG}
     * @param outputStream is outputStream of where you want to save image
     * */
     public void saveAsFile(OutputStream outputStream,@Nullable Bitmap.CompressFormat compressFormat,
                     PhotoEditor.OnSaveListener onSaveListener){
        if (compressFormat==null){
            compressFormat= Bitmap.CompressFormat.JPEG;
        }
        SaveSettings settings=new SaveSettings.Builder()
                .setClearViewsEnabled(false)
                .setCompressFormat(compressFormat)
                .setTransparencyEnabled(true).build();
        mPhotoEditor.saveAsFile(outputStream,settings,onSaveListener);
    }

    public void setTransitionName(String name){
        Bundle bundle=getArguments();
        if (bundle != null) {
            bundle.putString("transitionName",name);
        }
    }
    public void registerEditorListener(PhotoEditorFragment.OnEditorListener onEditorListener){
        mOnEditorListener=onEditorListener;
    }
    public void unRegisterEditorListener(){
        mOnEditorListener=null;
    }

     OnEditorListener getOnEditorListener() {
        return mOnEditorListener;
    }

    @Override
    public void onEmojiClicked(String emoji) {
        mPhotoEditor.addEmoji(emoji);
    }

    /*Emoji dialog*/
    @Override
    public void onDismiss() {
        btnEmoji.hideBackground();
    }


    public interface OnEditorListener{
        void onCropWindowOpened();
        void onCropWindowClosed();
        void onFadeViews(float alpha);
        void onEdit(boolean edited);
        void onImageLoaded(Bitmap bitmap, boolean isFirstResource);
        void onImageLoadFailed(Exception e);
        void onOpenAddInputText();
        void onCloseAddInputText();
        void onInputTextAdded(CharSequence text);
        void onInputTextEdited(CharSequence editedText);
        void onPreviewLoaded();
    }
    public static abstract class SimpleOnEditorListener implements PhotoEditorFragment.OnEditorListener {
        public void onCropWindowClosed() {}
        public void onCropWindowOpened() {}
        public void onFadeViews(float alpha) {}
        public void onEdit(boolean edited) {}
        public void onImageLoaded(Bitmap bitmap, boolean isFirstResource) {}
        public void onImageLoadFailed(Exception e) {}
        public void onOpenAddInputText(){}
        public void onCloseAddInputText(){}
        public void onInputTextAdded(CharSequence text){}
        public void onInputTextEdited(CharSequence editedText){}
        public void onPreviewLoaded(){}
    }

}

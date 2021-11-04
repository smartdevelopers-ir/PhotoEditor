package ir.smartdevelopers.smartphotoeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import java.util.Objects;

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
    private BrushButton btnBrush;
    private TextButton btnInsertText;
    private EmojiButton btnEmoji;
    private EditText edtTextInput;
    private ImageButton btnUndo;
    private FrameLayout mTextInputContainer;
    private AppCompatTextView txtTextInputHelper;
    private static final long ANIMATION_DURATION=200;
    private  float DEFAULT_TEXT_SIZE;//dp
    private KeyboardHeightProvider mKeyboardHeightProvider;

    public static PhotoEditorFragment getInstance(Uri imageUri) {
        PhotoEditorFragment fragment=new PhotoEditorFragment();
        Bundle bundle=new Bundle();
        bundle.putString("mImageUri",String.valueOf(imageUri));
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
        DEFAULT_TEXT_SIZE=getResources().getDimensionPixelSize(R.dimen.spe_default_input_text_size);
        mKeyboardHeightProvider=new KeyboardHeightProvider(view);
        view.post(()->{
            mKeyboardHeightProvider.start();
            translateTextInputToCenter();
        });
        Bundle bundle=getArguments();
        if (bundle != null) {
            String uriSt=bundle.getString("mImageUri");
            if (!TextUtils.isEmpty(uriSt)){
                mImageUri =Uri.parse(uriSt);
            }
        }
        findViews(view);


        mPhotoEditor=new PhotoEditor.Builder(getContext(),mPhotoEditorView).build();
        mPhotoEditorView.getSource().setImageURI(mImageUri);
//        Glide.with(view).load(mImageUri).into(mPhotoEditorView.getSource());
        initViews();

        initPhotoEditorListener();
    }

    private void initPhotoEditorListener() {
        mPhotoEditor.setOnPhotoEditorListener(new OnPhotoEditorListener() {
            private int mDrawingViewCount=0;
            @Override
            public void onEditTextChangeListener(View rootView, String text, int colorCode) {
                showTextInput(rootView,text,colorCode);
            }

            @Override
            public void onAddViewListener(ViewType viewType, int numberOfAddedViews) {
                if (numberOfAddedViews >0 && viewType==ViewType.BRUSH_DRAWING){
                    mDrawingViewCount++;
                }
                if (mDrawingViewCount > 0){
                    btnUndo.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onRemoveViewListener(ViewType viewType, int numberOfAddedViews) {
                if (viewType == ViewType.BRUSH_DRAWING){
                    mDrawingViewCount--;
                }
                if (mDrawingViewCount==0){
                    btnUndo.setVisibility(View.GONE);
                }
            }

            @Override
            public void onStartViewChangeListener(ViewType viewType) {

            }

            @Override
            public void onStopViewChangeListener(ViewType viewType) {

            }

            @Override
            public void onTouchSourceImage(MotionEvent event) {

            }
        });

    }

    public void save(){


        mPhotoEditor.saveAsBitmap(new OnSaveBitmap() {
            @Override
            public void onBitmapReady(Bitmap saveBitmap) {

            }

            @Override
            public void onFailure(Exception e) {

            }
        });
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
            if (!Objects.equals("active",btnInsertText.getTag())){
                showTextInput(null,"", VerticalSlideColorPicker.DEFAULT_COLOR);

            }else {
                //todo : change text style

            }
        });

        if (getActivity()!=null){
            getActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
                @SuppressWarnings("ConstantConditions")
                @Override
                public void handleOnBackPressed() {
                    if (Objects.equals(btnInsertText.getTag(),"active")){
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
            mPhotoEditorView.getSource().setImageResource(R.drawable.ff);

//            if (Objects.equals(btnBrush.getTag(),"active")){
//                deactivateBrushingTools(true);
//            }
//            btnEmoji.showBackGround();
//            showEmojiBottomSheet();
        });
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

    private void showTextInput(View editingView,String text, int colorCode) {
        btnInsertText.setTag("active");
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
        edtTextInput.setText(text);
        edtTextInput.setTextColor(colorCode);
        edtTextInput.setTag(editingView);
        btnInsertText.setColor(colorCode);
        edtTextInput.requestFocus();
        showKeyboard();
    }

    private void manageInsertText() {
        btnInsertText.setTag("deactivate");
        hideColorPicker(mTextColorPicker,true);

        CharSequence text=edtTextInput.getText();
        int color= btnInsertText.getColor();
//        float textSize= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,8,getResources().getDisplayMetrics());
        float textSize= edtTextInput.getTextSize() / getResources().getDisplayMetrics().scaledDensity;
        String inputText="";
        if (!TextUtils.isEmpty(text) && edtTextInput.getTag()==null){ // is in add mode
            //noinspection ConstantConditions
            inputText=text.toString();
            TextStyleBuilder textStyleBuilder=new TextStyleBuilder();
            textStyleBuilder.withTextColor(color);
            textStyleBuilder.withTextSize(textSize);
            textStyleBuilder.withGravity(Gravity.CENTER);
            mPhotoEditor.addText(inputText,textStyleBuilder);


            edtTextInput.setText("");
            txtTextInputHelper.setText("");
        } else if (edtTextInput.getTag() != null) {
            if (edtTextInput.getTag() instanceof View){
                View view= (View) edtTextInput.getTag();
                if (TextUtils.isEmpty(text)){
                    mPhotoEditor.removeView(view,ViewType.TEXT);
                }else {
                    mPhotoEditor.editText(view,text.toString(),color);
                }
            }
        }
        btnInsertText.hideBackground();
        mTextInputContainer.setVisibility(View.GONE);

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
        edtTextInput.setTranslationY(t);
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
}

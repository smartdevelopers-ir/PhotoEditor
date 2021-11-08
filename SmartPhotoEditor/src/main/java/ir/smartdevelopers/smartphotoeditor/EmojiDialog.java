package ir.smartdevelopers.smartphotoeditor;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class EmojiDialog extends BottomSheetDialogFragment {

    private RecyclerView mEmojiRecyclerView;
    private EmojiAdapter mEmojiAdapter;
    private OnEmojiListener mOnEmojiListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL,R.style.spe_EmojiDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.spe_dialog_emoji_layout,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mOnEmojiListener = (OnEmojiListener) getParentFragment();
        mEmojiRecyclerView=view.findViewById(R.id.spe_emojiRecyclerView);
        int spanCount=getResources().getInteger(R.integer.spe_emoji_span_count);
        mEmojiRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),spanCount));
        mEmojiAdapter=new EmojiAdapter(getEmojis(getContext()));
        mEmojiAdapter.setOnEmojiClickListener(new OnEmojiListener() {
            @Override
            public void onEmojiClicked(String emoji) {
                if (mOnEmojiListener != null) {
                    mOnEmojiListener.onEmojiClicked(emoji);
                }
                dismiss();
            }

            @Override
            public void onDismiss() {

            }
        });
        mEmojiRecyclerView.setAdapter(mEmojiAdapter);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog= (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        BottomSheetBehavior behavior=dialog.getBehavior();

//        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);
        return dialog;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mOnEmojiListener != null) {
            mOnEmojiListener.onDismiss();
        }
    }

    /**
     * Provide the list of emoji in form of unicode string
     *
     * @param context context
     * @return list of emoji unicode
     */
    public static ArrayList<String> getEmojis(Context context) {
        ArrayList<String> convertedEmojiList = new ArrayList<>();
        String[] emojiList = context.getResources().getStringArray(R.array.photo_editor_emoji);
        for (String emojiUnicode : emojiList) {
            convertedEmojiList.add(convertEmoji(emojiUnicode));
        }
        return convertedEmojiList;
    }

    private static String convertEmoji(String emoji) {
        String returnedEmoji;
        try {
            int convertEmojiToInt = Integer.parseInt(emoji.substring(2), 16);
            returnedEmoji = new String(Character.toChars(convertEmojiToInt));
        } catch (NumberFormatException e) {
            returnedEmoji = "";
        }
        return returnedEmoji;
    }



    interface OnEmojiListener {
        void onEmojiClicked(String emoji);
        void onDismiss();
    }
}

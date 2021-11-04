package ir.smartdevelopers.smartphotoeditor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder>{
    private List<String> mEmojiList;
    private EmojiDialog.OnEmojiListener mOnEmojiListener;

    public EmojiAdapter(List<String> emojiList) {
        mEmojiList = emojiList;
    }

    @NonNull
    @Override
    public EmojiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.spe_item_emoji_layout,parent,false);
        return new EmojiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmojiViewHolder holder, int position) {
        holder.bindView(mEmojiList.get(position));
    }

    @Override
    public int getItemCount() {
        return mEmojiList.size();
    }

    public void setOnEmojiClickListener(EmojiDialog.OnEmojiListener onEmojiListener) {
        mOnEmojiListener = onEmojiListener;
    }

    class EmojiViewHolder extends RecyclerView.ViewHolder {
        TextView txtEmoji;
        public EmojiViewHolder(@NonNull View itemView) {
            super(itemView);
            txtEmoji=itemView.findViewById(R.id.spe_txtEmoji);
            txtEmoji.setOnClickListener(v->{
                if (mOnEmojiListener != null) {
                    mOnEmojiListener.onEmojiClicked(mEmojiList.get(getAdapterPosition()));
                }
            });
        }
        void bindView(String emoji){
            txtEmoji.setText(emoji);
        }
    }

}

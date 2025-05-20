package com.qtcoding.btl_android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qtcoding.btl_android.R;
import com.qtcoding.btl_android.model.Vocabulary;

import java.util.ArrayList;
import java.util.List;

public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.VocabViewHolder> {

    private List<Vocabulary> vocabList;
    private OnVocabClickListener onClick;

    public interface OnVocabClickListener {
        void onVocabClick(Vocabulary vocabulary);
        void onSpeakerClick(Vocabulary vocabulary);
    }

    public VocabularyAdapter() {
        this.vocabList = new ArrayList<>();
    }

    public void setVocabList(List<Vocabulary> vocabList) {
        this.vocabList.clear();
        this.vocabList.addAll(vocabList);
        notifyDataSetChanged();
    }

    public void setOnClick(OnVocabClickListener onClick) {
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public VocabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vocab_item, parent, false);
        return new VocabViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VocabViewHolder holder, int position) {
        Vocabulary vocab = vocabList.get(position);

        // Hiển thị từ vựng
        holder.tvWord.setText(vocab.getWord());
        holder.tvMeaning.setText(vocab.getMeaning());
        holder.tvExample.setText(vocab.getExample());

        // Mặt trước hiển thị
        holder.frontLayout.setVisibility(View.VISIBLE);
        holder.backLayout.setVisibility(View.GONE);
        holder.isFrontVisible = true;

        // Xử lý sự kiện click vào loa
        holder.ivSpeaker.setOnClickListener(v -> {
            if (onClick != null) {
                onClick.onSpeakerClick(vocab);
            }
        });

        // Xử lý sự kiện kéo để lật thẻ
        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            float startX = 0f;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float diffX;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        holder.isDragging = true;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        diffX = event.getX() - startX;
                        if (holder.isDragging && Math.abs(diffX) > 100f) {
                            flipCard(holder, v.getContext());
                            holder.isDragging = false;
                            return true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        diffX = event.getX() - startX;
                        if (holder.isDragging && Math.abs(diffX) <= 10f) {
                            if (onClick != null && vocab.getId() != null) {
                                onClick.onVocabClick(vocab);
                            }
                        }
                        holder.isDragging = false;
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        holder.isDragging = false;
                        return true;
                }
                return false;
            }
        });
    }

    private void flipCard(VocabViewHolder holder, Context context) {
        Animation flipIn = AnimationUtils.loadAnimation(context, R.anim.flip_in);
        Animation flipOut = AnimationUtils.loadAnimation(context, R.anim.flip_out);

        if (holder.isFrontVisible) {
            holder.frontLayout.startAnimation(flipOut);
            holder.backLayout.startAnimation(flipIn);
            holder.frontLayout.setVisibility(View.GONE);
            holder.backLayout.setVisibility(View.VISIBLE);
            holder.isFrontVisible = false;
        } else {
            holder.backLayout.startAnimation(flipOut);
            holder.frontLayout.startAnimation(flipIn);
            holder.backLayout.setVisibility(View.GONE);
            holder.frontLayout.setVisibility(View.VISIBLE);
            holder.isFrontVisible = true;
        }
    }

    @Override
    public int getItemCount() {
        return vocabList.size();
    }

    static class VocabViewHolder extends RecyclerView.ViewHolder {
        TextView tvWord, tvMeaning, tvExample;
        LinearLayout frontLayout, backLayout;
        ImageView ivSpeaker;
        boolean isFrontVisible = true;
        boolean isDragging = false;

        public VocabViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWord = itemView.findViewById(R.id.tvWord);
            tvMeaning = itemView.findViewById(R.id.tvMeaning);
            tvExample = itemView.findViewById(R.id.tvExample);
            backLayout = itemView.findViewById(R.id.backLayout);
            frontLayout = itemView.findViewById(R.id.frontLayout);
            ivSpeaker = itemView.findViewById(R.id.ivSpeaker);
        }
    }
}
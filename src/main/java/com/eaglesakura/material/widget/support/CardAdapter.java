package com.eaglesakura.material.widget.support;

import com.eaglesakura.android.util.ViewUtil;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerViewのシンプルなカードを実現する
 */
public abstract class CardAdapter<T> extends RecyclerView.Adapter<CardAdapter.CardItem> {

    /**
     * カード用アイテム
     */
    private List<T> mCardModels = new ArrayList<>(32);


    @Override
    public CardItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View card = onCreateCard(parent, viewType);
        return new CardItem(card);
    }

    @Override
    public void onBindViewHolder(CardItem holder, int position) {
        ViewUtil.matchCardWidth(holder.itemView);
        onBindCard(holder.itemView, position, mCardModels.get(position));
    }

    @Override
    public int getItemCount() {
        return mCardModels.size();
    }

    /**
     * 全てのカード用アイテムを削除する
     */
    public void clearAnimated() {
        while (mCardModels.isEmpty()) {
            mCardModels.remove(0);
            notifyItemRemoved(0);
        }
    }

    /**
     * 全てのカード用アイテムをすぐさま削除する
     */
    public void clear() {
        mCardModels.clear();
        notifyDataSetChanged();
    }

    /**
     * アイテムを削除する
     */
    public void remove(T item) {
        int index = mCardModels.indexOf(item);
        if (index < 0) {
            throw new IllegalArgumentException();
        }

        remove(index);
    }

    /**
     * 指定した位置のアイテムを削除する
     */
    public void remove(int index) {
        mCardModels.remove(index);
        notifyItemRemoved(index);
    }

    /**
     * カード用のアイテムを追加する
     */
    public void addItem(T item) {
        mCardModels.add(item);
        notifyItemInserted(mCardModels.size() - 1);
    }

    /**
     * カード用のアイテムを挿入する
     */
    public void addItem(int index, T item) {
        mCardModels.add(index, item);
        notifyItemInserted(index);
    }

    /**
     * 表示用のViewを生成する
     */
    protected abstract View onCreateCard(ViewGroup parent, int viewType);

    /**
     * 表示用のViewを構築する
     */
    protected abstract void onBindCard(View card, int position, T item);

    public static class CardItem extends RecyclerView.ViewHolder {
        public CardItem(View itemView) {
            super(itemView);
        }
    }
}

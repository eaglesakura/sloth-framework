package com.eaglesakura.sloth.view.adapter;

import com.eaglesakura.lambda.Action3;
import com.eaglesakura.lambda.ResultAction2;
import com.eaglesakura.util.CollectionUtil;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * シンプルな見た目のSpinnerAdapterを提供する
 */
public class SupportArrayAdapter<T> extends BaseAdapter {

    /**
     * DropDown表記のコンバートを行う
     */
    ResultAction2<Integer, T, String> mTitleConverter = (index, item) -> item.toString();

    /**
     * DropDownViewの設定を行う
     */
    Action3<Integer, T, View> mDropdownViewConverter = (index, item, view) -> {
        ((TextView) view.findViewById(android.R.id.text1)).setText(mTitleConverter.action(index, item));
    };

    /**
     * SelectionViewの設定を行う
     */
    Action3<Integer, T, View> mSelectionViewConvert = (index, item, view) -> {
        ((TextView) view.findViewById(android.R.id.text1)).setText(mTitleConverter.action(index, item));
    };

    @LayoutRes
    private final int mItemLayoutId;

    @LayoutRes
    private final int mDropdownLayoutId;

    protected final Context mContext;

    /**
     * 管理されているアイテム
     */
    private final List<T> mItems = new ArrayList<>();

    public SupportArrayAdapter(Context context, @LayoutRes int itemLayoutId, @LayoutRes int dropdownViewId) {
        mContext = context;
        mItemLayoutId = itemLayoutId;
        mDropdownLayoutId = dropdownViewId;
    }

    public void setDropdownViewConverter(@NonNull Action3<Integer, T, View> dropdownViewConverter) {
        mDropdownViewConverter = dropdownViewConverter;
    }

    public void setSelectionViewConvert(@NonNull Action3<Integer, T, View> selectionViewConvert) {
        mSelectionViewConvert = selectionViewConvert;
    }

    public void setTitleConverter(ResultAction2<Integer, T, String> titleConverter) {
        mTitleConverter = titleConverter;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mDropdownLayoutId, parent, false);
        }
        try {
            mDropdownViewConverter.action(position, getItem(position), convertView);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mItemLayoutId, parent, false);
        }
        try {
            mSelectionViewConvert.action(position, getItem(position), convertView);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public T getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * アイテムを追加する
     */
    public boolean add(T item) {
        return mItems.add(item);
    }

    /**
     * インデックスを指定して追加する
     */
    public void add(int index, T item) {
        mItems.add(index, item);
    }

    public boolean addAll(Collection<? extends T> collection) {
        return mItems.addAll(collection);
    }

    public boolean addAll(int i, Collection<? extends T> collection) {
        return mItems.addAll(i, collection);
    }

    public boolean removeAll(Collection<?> collection) {
        return mItems.removeAll(collection);
    }

    /**
     * 指定したアイテムが存在しなければ追加する
     *
     * @return 追加されたインデックス
     */
    public int addUnique(T item) {
        return CollectionUtil.addUniqueRequestIndex(mItems, item);
    }

    /**
     * 指定されたすべてのアイテムを、必要に応じて追加する
     */
    public void addAllUnique(Iterable<T> items) {
        for (T it : items) {
            addUnique(it);
        }
    }
}

package com.eaglesakura.material.widget;

import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.R;
import com.eaglesakura.android.util.ContextUtil;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class MaterialProgressView extends FrameLayout {
    public MaterialProgressView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public MaterialProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public MaterialProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    void init(Context context, AttributeSet attrs, int defStyleAttr) {
        LayoutInflater inflater = ContextUtil.getInflater(context);
        View view = inflater.inflate(R.layout.esm_view_material_progress, null);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, new int[]{
                R.attr.esmText,
        });

        new AQuery(view)
                .id(R.id.EsMaterial_Progress_Text).text(typedArray.getText(0));
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(view, params);
    }
}

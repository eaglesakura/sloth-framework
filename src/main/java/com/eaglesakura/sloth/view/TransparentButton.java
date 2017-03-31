package com.eaglesakura.sloth.view;

import com.eaglesakura.sloth.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * 透過を指定されたボタン
 */
public class TransparentButton extends FrameLayout {
    public TransparentButton(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public TransparentButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public TransparentButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    /**
     * 初期化する
     */
    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setClickable(true);
        setBackgroundResource(R.drawable.esm_button_transparent_fill);

        if (!isInEditMode()) {
            int margin = context.getResources().getDimensionPixelSize(R.dimen.EsMaterial_Button_Margin);
            setPadding(margin, margin, margin, margin);
        }
    }
}

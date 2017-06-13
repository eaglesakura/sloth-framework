package com.eaglesakura.sloth.view.builder;

import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.sloth.SlothLog;
import com.eaglesakura.sloth.R;
import com.eaglesakura.sloth.app.lifecycle.UiLifecycle;
import com.eaglesakura.sloth.ui.progress.DialogToken;
import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.lambda.Action0;
import com.eaglesakura.lambda.Action1Throwable;
import com.eaglesakura.lambda.Action2;
import com.eaglesakura.lambda.ResultAction1;
import com.eaglesakura.util.CollectionUtil;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * アプリ用の共通ダイアログを生成する
 */
public class DialogBuilder<T> {
    protected AlertDialog.Builder mBuilder;

    protected View mContent;

    /**
     * 選択対象のアイテム
     */
    protected List<?> mSelections;

    /**
     * セレクタアクション
     */
    protected Action2<Integer, T> mSelector;

    protected Integer mLayoutWidth;

    protected Integer mLayoutHeight;

    protected boolean mFullScreen;

    protected boolean mCanceledOnTouchOutside = true;

    public DialogBuilder(AlertDialog.Builder builder) {
        mBuilder = builder;
    }


    public DialogBuilder title(String title) {
        mBuilder.setTitle(title);
        return this;
    }

    public DialogBuilder title(@StringRes int titleId) {
        mBuilder.setTitle(titleId);
        return this;
    }

    public DialogBuilder layout(int width, int height) {
        mLayoutWidth = width;
        mLayoutHeight = height;
        return this;
    }

    /**
     * ダイアログの表示を開始する
     */
    public Dialog show(@Nullable UiLifecycle delegate) {
        AlertDialog dialog = mBuilder.show();

        if (delegate != null) {
            delegate.addAutoDismiss(dialog);
        }

        if (mBuilder.getContext() instanceof Activity) {
            ContextUtil.closeIME((Activity) mBuilder.getContext());
        }

        if (CollectionUtil.allNotNull(mLayoutWidth, mLayoutHeight)) {
            dialog.getWindow().setLayout(mLayoutWidth, mLayoutHeight);
        }

        if (!mCanceledOnTouchOutside) {
            dialog.setCanceledOnTouchOutside(false);
        }
        return dialog;
    }

    public DialogBuilder<T> fullScreen(boolean set) {
        mFullScreen = set;
        return this;
    }

    /**
     * ダイアログの表示を開始する
     */
    public Dialog show(@Nullable UiLifecycle delegate, Object tag) {
        AlertDialog dialog = mBuilder.show();

        if (delegate != null) {
            delegate.addAutoDismiss(dialog, tag);
        }

        if (mFullScreen) {
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        return dialog;
    }

    /**
     * 同一タグのダイアログが存在しないならば、ダイアログを表示する
     */
    @Nullable
    public Dialog showOnce(@NonNull UiLifecycle delegate, @NonNull Object tag) {

        if (delegate.hasAutoDismissObject(tag)) {
            SlothLog.system("cancel dialog boot[%s]", tag.toString());
            return null;
        }

        AlertDialog dialog = mBuilder.show();

        if (delegate != null) {
            delegate.addAutoDismiss(dialog, tag);
        }
        return dialog;
    }

    /**
     * インターフェースをラップする
     */
    protected DialogInterface.OnClickListener wrap(@Nullable Action0 action) {
        if (action == null) {
            return null;
        }
        return (dlg, which) -> {
            try {
                action.action();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * 表示用のViewを指定する
     */
    public DialogBuilder view(@LayoutRes int resId) {
        mBuilder.setView(resId);
        return this;
    }

    /**
     * 表示用のViewを指定する
     */
    public DialogBuilder view(View contentView) {
        mBuilder.setView(contentView);
        mContent = contentView;
        return this;
    }

    /**
     * 選択時のアクションを設定する
     */
    public DialogBuilder selected(Action2<Integer, T> action) {
        mSelector = action;
        return this;
    }

    public DialogBuilder positiveButton(@StringRes int textId, Action0 action) {
        mBuilder.setPositiveButton(textId, wrap(action));
        return this;
    }

    public DialogBuilder positiveButton(@NonNull String text, Action0 action) {
        mBuilder.setPositiveButton(text, wrap(action));
        return this;
    }

    public DialogBuilder negativeButton(@StringRes int textId, Action0 action) {
        mBuilder.setNegativeButton(textId, wrap(action));
        return this;
    }

    public DialogBuilder negativeButton(@NonNull String text, Action0 action) {
        mBuilder.setNegativeButton(text, wrap(action));
        return this;
    }

    public DialogBuilder neutralButton(@StringRes int textId, Action0 action) {
        mBuilder.setNeutralButton(textId, wrap(action));
        return this;
    }

    public DialogBuilder cancelable(boolean cancel) {
        mBuilder.setCancelable(cancel);
        return this;
    }

    public DialogBuilder canceledOnTouchOutside(boolean enable) {
        mCanceledOnTouchOutside = enable;
        return this;
    }

    public DialogBuilder<T> canceled(Action0 action) {
        mBuilder.setOnCancelListener(dialog -> {
            try {
                action.action();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
        return this;
    }

    public DialogBuilder<T> dismissed(Action0 action) {
        mBuilder.setOnDismissListener(dialog -> {
            try {
                action.action();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
        return this;
    }

    /**
     * 独自のコンテンツを用いてBuilderを生成する
     */
    public static DialogBuilder newCustomContent(Context context, String title, View contentView) {
        DialogBuilder builder = new DialogBuilder(new AlertDialog.Builder(context));
        builder.mBuilder.setTitle(title);
        return builder.view(contentView);
    }

    /**
     * Progress表記を行う
     *
     * @param message 表示メッセージ
     */
    public static DialogBuilder newProgress(Context context, String message) {
        View content = LayoutInflater.from(context).inflate(R.layout.esm_dialog_material_progress, null, false);
        new AQuery(content).id(R.id.EsMaterial_Progress_Text).text(message);
        DialogBuilder builder = new DialogBuilder(new AlertDialog.Builder(context));
        return builder.view(content);
    }

    public static DialogBuilder newInformation(Context context, String message) {
        DialogBuilder builder = new DialogBuilder(new AlertDialog.Builder(context));
        builder.mBuilder.setTitle(R.string.EsMaterial_Title_Common_Information);
        builder.mBuilder.setMessage(message);
        return builder;
    }

    public static DialogBuilder newInformation(Context context, @StringRes int messageId) {
        DialogBuilder builder = new DialogBuilder(new AlertDialog.Builder(context));
        builder.mBuilder.setTitle(R.string.EsMaterial_Title_Common_Information);
        builder.mBuilder.setMessage(messageId);
        return builder;
    }

    public static DialogBuilder newAlert(Context context, @StringRes int messageId) {
        DialogBuilder builder = new DialogBuilder(new AlertDialog.Builder(context));
        builder.mBuilder.setTitle(R.string.EsMaterial_Title_Common_Alert);
        builder.mBuilder.setMessage(messageId);
        return builder;
    }

    public static DialogBuilder newAlert(Context context, @NonNull String message) {
        DialogBuilder builder = new DialogBuilder(new AlertDialog.Builder(context));
        builder.mBuilder.setTitle(R.string.EsMaterial_Title_Common_Alert);
        builder.mBuilder.setMessage(message);
        return builder;
    }

    /**
     * 選択ダイアログを取得
     */
    public static <T> DialogBuilder<T> newSelections(Context context, Collection<T> objects, ResultAction1<T, String> selectorTextConverter) {
        DialogBuilder builder = new DialogBuilder(new AlertDialog.Builder(context));
        List<String> selections;
        try {
            selections = CollectionUtil.asOtherList(objects, selectorTextConverter);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        builder.mSelections = new ArrayList<>(objects);
        builder.mBuilder.setItems(CollectionUtil.asArray(selections, new String[selections.size()]), (dlg, which) -> {
            if (builder.mSelector != null) {
                try {
                    builder.mSelector.action(which, builder.mSelections.get(which));
                } catch (Throwable e) {
                    throw new IllegalStateException(e);
                }
            }
        });
        return builder;
    }

    static class DialogTokenImpl implements DialogToken {
        DialogBuilder mBuilder;

        Dialog mDialog;

        public DialogTokenImpl(DialogBuilder builder) {
            mBuilder = builder;
        }

        @Override
        public boolean isCanceled() throws Exception {
            return mDialog != null && !mDialog.isShowing();
        }

        Dialog show(UiLifecycle delegate) {
            mDialog = UIHandler.await(() -> mBuilder.show(delegate));
            return mDialog;
        }

        @Override
        public void updateContent(Action1Throwable<View, Exception> action) {
            try {
                UIHandler.await(() -> {
                    action.action(mBuilder.mContent);
                    return 0;
                });
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() throws IOException {
            UIHandler.await(() -> {
                try {
                    mDialog.dismiss();
                } catch (Throwable e) {
                }
                return 0;
            });
        }
    }

    /**
     * try-with-resourceで使用するダイアログとして表示を行う
     */
    public static DialogToken showAsToken(DialogBuilder builder, UiLifecycle delegate) {
        DialogTokenImpl impl = new DialogTokenImpl(builder);
        impl.show(delegate);
        return impl;
    }
}

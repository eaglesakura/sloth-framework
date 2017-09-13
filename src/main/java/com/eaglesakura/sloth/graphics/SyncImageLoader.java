package com.eaglesakura.sloth.graphics;

import com.eaglesakura.alternet.Alternet;
import com.eaglesakura.alternet.request.ConnectRequest;
import com.eaglesakura.alternet.request.SimpleHttpRequest;
import com.eaglesakura.cerberus.BackgroundTask;
import com.eaglesakura.cerberus.CallbackTime;
import com.eaglesakura.cerberus.ExecuteTarget;
import com.eaglesakura.lambda.Action2;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.sloth.R;
import com.eaglesakura.sloth.app.lifecycle.Lifecycle;
import com.eaglesakura.sloth.data.SupportCancelCallbackBuilder;
import com.eaglesakura.sloth.graphics.loader.DrawableImageLoader;
import com.eaglesakura.sloth.graphics.loader.FileImageLoader;
import com.eaglesakura.sloth.graphics.loader.NetworkImageLoader;
import com.eaglesakura.sloth.graphics.loader.UriImageLoader;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * キャッシュコントロールと割り込みに対応したイメージローダ
 */
public class SyncImageLoader {
    @NonNull
    final Context mContext;

    /**
     * エラー画像用の画像キャッシュ
     */
    final ImageCache mErrorCache;

    /**
     * 成功した画像用の画像キャッシュ
     */
    final ImageCache mImageCache;

    public SyncImageLoader(Context context) {
        this(context, 8, 4);
    }

    public SyncImageLoader(Context context, @IntRange(from = 1) int imageCacheNum, @IntRange(from = 1) int errorCacheNum) {
        mContext = context.getApplicationContext();
        mImageCache = new ImageCache(imageCacheNum);
        mErrorCache = new ImageCache(errorCacheNum);
    }


    @NonNull
    public Context getContext() {
        return mContext;
    }

    public ImageCache getErrorCache() {
        return mErrorCache;
    }

    public ImageCache getImageCache() {
        return mImageCache;
    }

    public <T extends com.eaglesakura.sloth.graphics.loader.ImageLoader> Builder newImage(com.eaglesakura.sloth.graphics.loader.ImageLoader<T> loader) {
        return new Builder(loader);
    }

    /**
     * ファイルからのローダーを生成する
     */
    public Builder newImage(File src, boolean onMemoryCache) {
        FileImageLoader loader = new FileImageLoader(mContext, mImageCache, src);
        if (onMemoryCache) {
            loader.cache();
        }

        return new Builder(loader);
    }

    /**
     * ファイルからのローダーを生成する
     */
    public Builder newImage(Uri src, boolean onMemoryCache) {
        UriImageLoader loader = new UriImageLoader(mContext, mImageCache, src);
        if (onMemoryCache) {
            loader.cache();
        }
        return new Builder(loader);
    }

    /**
     * ネットワークダウンローダーを生成する
     */
    public Builder newImage(Alternet connector, String url, boolean onMemoryCache) {
        SimpleHttpRequest request = new SimpleHttpRequest(ConnectRequest.Method.GET);
        request.setUrl(url, null);
        return newImage(connector, request, onMemoryCache);
    }

    /**
     * ネットワークダウンローダーを生成する
     */
    public Builder newImage(Alternet connector, ConnectRequest request, boolean onMemoryCache) {
        NetworkImageLoader loader = new NetworkImageLoader(mContext, mImageCache, connector, request);
        if (onMemoryCache) {
            loader.cache();
        }
        return new Builder(loader);
    }

    public class Builder {
        com.eaglesakura.sloth.graphics.loader.ImageLoader mErrorLoader;

        com.eaglesakura.sloth.graphics.loader.ImageLoader mLoader;

        public Builder(com.eaglesakura.sloth.graphics.loader.ImageLoader loader) {
            mLoader = loader;
        }

        /**
         * ImageViewに対してロード&画像セットを行なう
         *
         * @param lifecycle 対象となるライフサイクル
         * @param view      対象のImageView
         */
        public BackgroundTask<Drawable> inject(Lifecycle lifecycle, ImageView view) {
            return inject(lifecycle, view, (v, img) -> view.setImageDrawable(img));
        }

        /**
         * ImageViewに対してロード&画像セットを行なう
         *
         * @param view      対象のView
         * @param lifecycle 対象となるライフサイクル
         */
        public <T extends View> BackgroundTask<Drawable> inject(Lifecycle lifecycle, T view, Action2<T, Drawable> action) {
            view.setTag(R.id.ImageLoader_TargetBuilder, this);
            return lifecycle.async(ExecuteTarget.LocalParallel, CallbackTime.Alive, (BackgroundTask<Drawable> task) -> {
                return await(SupportCancelCallbackBuilder.from(task).build());
            }).completed(result -> {
                Builder tag = (Builder) view.getTag(R.id.ImageLoader_TargetBuilder);
                if (tag == this) {
                    view.setTag(R.id.ImageLoader_TargetBuilder, null);
                    try {
                        action.action((T) view, result);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }

        /**
         * エラー時の代替画像を指定する
         */
        public Builder errorImage(@DrawableRes int drawableId, boolean cached) {
            mErrorLoader = new DrawableImageLoader(mContext, drawableId, mErrorCache);
            if (cached) {
                mErrorLoader.cache();
            }
            return this;
        }

        /**
         * エラー時の代替画像を指定する
         */
        public Builder errorTintImage(@DrawableRes int drawableId, @ColorInt int tintColor, boolean cached) {
            mErrorLoader = new DrawableImageLoader(mContext, drawableId, mErrorCache).tint(tintColor);
            if (cached) {
                mErrorLoader.cache();
            }
            return this;
        }

        public com.eaglesakura.sloth.graphics.loader.ImageLoader getErrorLoader() {
            return mErrorLoader;
        }

        public com.eaglesakura.sloth.graphics.loader.ImageLoader getLoader() {
            return mLoader;
        }

        /**
         * アスペクト比を保ってリサイズする
         */
        public Builder keepAspectResize(int width, int height) {
            mLoader.keepAspectResize(width, height);
            if (mErrorLoader != null) {
                mErrorLoader.keepAspectResize(width, height);
            }
            return this;
        }

        /**
         * 画像の取得待ちを行う
         */
        public Drawable await(@NonNull CancelCallback cancelCallback) throws IOException {
            if (cancelCallback == null) {
                throw new IllegalArgumentException();
            }
            mLoader.setCancelCallback(cancelCallback);
            if (mErrorLoader != null) {
                mErrorLoader.setCancelCallback(cancelCallback);
            }

            try {
                return mLoader.await();
            } catch (InterruptedIOException e) {
                throw e;
            } catch (IOException e) {
                if (mErrorLoader != null) {
                    return mErrorLoader.await();
                } else {
                    throw e;
                }
            }
        }
    }
}

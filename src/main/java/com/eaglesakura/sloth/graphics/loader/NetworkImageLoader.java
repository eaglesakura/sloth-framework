package com.eaglesakura.sloth.graphics.loader;

import com.eaglesakura.alternet.Alternet;
import com.eaglesakura.alternet.Result;
import com.eaglesakura.alternet.parser.BitmapParser;
import com.eaglesakura.alternet.request.ConnectRequest;
import com.eaglesakura.lambda.CallbackUtils;
import com.eaglesakura.sloth.graphics.ImageCache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;

public class NetworkImageLoader extends ImageLoader<NetworkImageLoader> {
    @NonNull
    final Alternet mNetworkConnector;

    @NonNull
    final ConnectRequest mRequest;

    public NetworkImageLoader(@NonNull Context context, @NonNull ImageCache imageManager, @NonNull Alternet networkConnector, @NonNull ConnectRequest request) {
        super(context, imageManager);
        mNetworkConnector = networkConnector;
        mRequest = request;
    }

    @NonNull
    @Override
    protected String getUniqueId() {
        return mRequest.getCachePolicy().getCacheKey(mRequest);
    }

    @NonNull
    @Override
    protected Object onLoad() throws Throwable {
        Result<Bitmap> result = mNetworkConnector.fetch(mRequest, new BitmapParser(), (it) -> CallbackUtils.isCanceled(mCancelCallback));
        return new BitmapDrawable(mNetworkConnector.getContext().getResources(), result.getResult());
    }
}

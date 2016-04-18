package android.support.v4.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class InternalSupportFragmentUtil {
    public static void startActivityForResult(@NonNull Fragment fragment, int requestCode, @NonNull Intent intent, @Nullable Bundle options) {
        fragment.mHost.onStartActivityFromFragment(fragment, intent, requestCode, options);
    }
}

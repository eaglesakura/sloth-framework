package com.eaglesakura.sloth.ui.license;

import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.cerberus.BackgroundTask;
import com.eaglesakura.cerberus.error.TaskCanceledException;
import com.eaglesakura.collection.DataCollection;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.sloth.SlothLog;
import com.eaglesakura.sloth.R;
import com.eaglesakura.sloth.annotation.BindInterface;
import com.eaglesakura.sloth.annotation.FragmentLayout;
import com.eaglesakura.sloth.app.SlothFragment;
import com.eaglesakura.sloth.app.lifecycle.FragmentLifecycle;
import com.eaglesakura.sloth.app.support.ViewBindingSupport;
import com.eaglesakura.sloth.data.SupportCancelCallbackBuilder;
import com.eaglesakura.sloth.view.adapter.CardAdapter;
import com.eaglesakura.sloth.util.AppSupportUtil;
import com.eaglesakura.util.CollectionUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * ライセンス一覧表記を行うFragment
 */
@FragmentLayout(resName = "esm_license_list")
public class LicenseListFragment extends SlothFragment {

    @Bind(resName = "eglibrary.Content.List")
    RecyclerView mCardList;

    @BindInterface
    Callback mCallback;

    private View mRootView;

    @Override
    protected void onCreateLifecycle(FragmentLifecycle newLifecycle) {
        super.onCreateLifecycle(newLifecycle);
        ViewBindingSupport.bind(newLifecycle, this, new ViewBindingSupport.Callback() {
            @Override
            public void onAfterViews(View rootView) {
                mCardList.setLayoutManager(new LinearLayoutManager(getContext()));
                mCardList.setItemAnimator(new DefaultItemAnimator());
                mCardList.setHasFixedSize(false);
                mCardList.setAdapter(mAdapter);

                mRootView = rootView;
            }

            @Override
            public void onAfterBindMenu(Menu menu) {

            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLicenses();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getActionBar().setTitle(R.string.EsMaterial_Widget_License_Title);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return mRootView;
    }

    @UiThread
    void loadLicenses() {
        asyncQueue((BackgroundTask<DataCollection<LicenseItem>> task) -> {
            return listLicenses(SupportCancelCallbackBuilder.from(task).build());
        }).completed((result, task) -> {
            SlothLog.system("Loaded %d items", result.size());
            mAdapter.getCollection().addAll(result.list());
            mCardList.setAdapter(mAdapter);
        }).failed((error, task) -> {
            error.printStackTrace();
        }).start();
    }

    @SuppressLint("NewApi")
    DataCollection<LicenseItem> listLicenses(CancelCallback cancelCallback) throws IOException, TaskCanceledException {
        Set<String> ignoreFiles = CollectionUtil.asOtherSet(CollectionUtil.asList(getResources().getStringArray(R.array.EsMaterial_Widget_Licenses_IgnoreFiles)), it -> it);
        List<LicenseItem> result = new ArrayList<>();
        String LICENSE_PATH = "license";

        String[] files = getContext().getAssets().list(LICENSE_PATH);
        for (String file : files) {
            AppSupportUtil.assertNotCanceled(cancelCallback);

            // 拡張子が一致して、かつignoreリストに含まれていなければ登録する
            if (file.endsWith(".license") && !ignoreFiles.contains(file)) {
                SlothLog.widget("load license(%s)", file);
                // １行目にOSSの表示名が格納されている
                String path = LICENSE_PATH + "/" + file;
                try (InputStream is = getContext().getAssets().open(path)) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String title = reader.readLine();
                    SlothLog.widget("OSS(%s)", title);

                    StringBuilder lineBuffer = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        lineBuffer.append(line).append("\n");
                    }

                    result.add(new LicenseItem(title, lineBuffer.toString(), path));
                }
            }
        }

        return new DataCollection<>(result);
    }


    CardAdapter<LicenseItem> mAdapter = new CardAdapter<LicenseItem>() {
        @Override
        protected View onCreateCard(ViewGroup parent, int viewType) {
            return LayoutInflater.from(getContext()).inflate(R.layout.esm_license_card, parent, false);
        }

        @Override
        protected void onBindCard(CardBind<LicenseItem> bind, int position) {
            LicenseItem item = bind.getItem();

            bind.query()
                    .id(R.id.eglibrary_Button_Item).clicked(view -> mCallback.requestDetail(LicenseListFragment.this, item))
                    .id(R.id.eglibrary_Item_Name).text(item.title);
        }
    };

    public static class LicenseItem {
        final String title;

        final String path;

        final String text;

        public LicenseItem(String title, String text, String path) {
            this.title = title;
            this.text = text;
            this.path = path;
        }

        public String getTitle() {
            return title;
        }

        public String getPath() {
            return path;
        }

        public String getText() {
            return text;
        }
    }

    public interface Callback {
        void requestDetail(LicenseListFragment self, LicenseItem item);
    }
}

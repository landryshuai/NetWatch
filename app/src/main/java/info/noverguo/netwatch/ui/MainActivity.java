package info.noverguo.netwatch.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tencent.noverguo.hooktest.R;
import info.noverguo.netwatch.adapter.BlackRecyclerViewAdapter;
import info.noverguo.netwatch.adapter.MultiSelectRecyclerViewAdapter;
import info.noverguo.netwatch.model.PackageUrl;
import info.noverguo.netwatch.model.PackageUrlSet;
import info.noverguo.netwatch.receiver.ReloadReceiver;
import info.noverguo.netwatch.service.LocalUrlService;
import info.noverguo.netwatch.utils.DLog;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import info.noverguo.netwatch.utils.UrlServiceUtils;

public class MainActivity extends AppCompatActivity {
    final static String TAG = MainActivity.class.getSimpleName();
    LocalUrlService urlService;
    @Bind(R.id.rv_urls)
    RecyclerView mRecyclerView;
    @Bind(R.id.fab)
    FloatingActionButton mFab;
    BlackRecyclerViewAdapter blackAdapter;
    List<PackageUrlSet> packageBlackList = Collections.emptyList();
    List<PackageUrlSet> packageUrlList = Collections.emptyList();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initView() {
        DLog.i("initView start");
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));//这里用线性显示 类似于listview
        DLog.i("initView end");
    }

    private void initData() {
        DLog.i("initData start");
        urlService = new LocalUrlService(getApplicationContext());
        blackAdapter = new BlackRecyclerViewAdapter(getApplicationContext());
        blackAdapter.setSelectedListener(new MultiSelectRecyclerViewAdapter.SelectedListener() {
            @Override
            public void onSelect(int selectCount) {
                Log.i(TAG, "onSelect: " + selectCount);
                if (selectCount == 0) {
                    mFab.setVisibility(View.INVISIBLE);
                    return;
                }
                mFab.setVisibility(View.VISIBLE);
                if (selectCount == 1) {
                    mFab.setImageResource(R.drawable.ic_edit_white_24dp);
                    mFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
//                    mFab.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    mFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            editBlackUrl();
                        }
                    });
                } else {
                    mFab.setImageResource(R.drawable.ic_clear_white_24dp);
                    mFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorWarning)));
//                    mFab.setBackgroundColor(getResources().getColor(R.color.colorWarning));
                    mFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            removeBlackUrls();
                        }
                    });
                }
            }
        });
        mRecyclerView.setAdapter(blackAdapter);
        reloadUrls();
        resetAdapter();
    }
    ReloadReceiver reloadBlackReceiver;
    ReloadReceiver reloadPackageReceiver;
    @Override
    protected void onResume() {
        super.onResume();
        reloadBlackReceiver = ReloadReceiver.registerReloadBlack(getApplicationContext(), new Runnable() {
            @Override
            public void run() {
                resetAdapter();
                reloadBlack();
            }
        });
        reloadPackageReceiver = ReloadReceiver.registerReloadBlack(getApplicationContext(), new Runnable() {
            @Override
            public void run() {
                resetAdapter();
                reloadPackage();
            }
        });
        reloadUrls();
    }

    @Override
    protected void onPause() {
        super.onPause();
        reloadBlackReceiver.unregister(getApplicationContext());
        reloadPackageReceiver.unregister(getApplicationContext());
    }

    private void editBlackUrl() {
        if (blackAdapter.getSelectedItemCount() == 0) {
            return;
        }
        final PackageUrl item = blackAdapter.getItem(blackAdapter.getSelectedItems().get(0));
        // TODO 修改拦截规则
        new MaterialDialog.Builder(this)
                .title(R.string.dialog_title_add)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .inputRange(1, 50, Color.RED)
                .input(getString(R.string.dialog_add_input_hint), item.url, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        urlService.removeBlackUrls(Arrays.asList(new PackageUrlSet(item.packageName, Arrays.asList(item.url))), null);
                        urlService.addBlackUrls(Arrays.asList(new PackageUrlSet(item.packageName, Arrays.asList(input.toString()))), null);
                    }
                })
                .negativeText(R.string.dialog_btn_cancel)
                .positiveText(R.string.dialog_btn_edit).build().show();
    }

    private void removeBlackUrls() {
        urlService.removeBlackUrls(blackAdapter.getSelectUrls(), null);
        resetAdapter();
    }


    private void resetAdapter() {
        blackAdapter.clearSelectedState();
        notifyChange(blackAdapter);
    }

    private void reloadUrls() {
        reloadBlack();
        reloadPackage();
    }

    private void reloadPackage() {
        urlService.getAccessUrls(new LocalUrlService.GetUrlsCallback() {
            @Override
            public void onGet(List<PackageUrlSet> result) {
                packageUrlList = PackageUrlSet.copy(result);
                notifyUrlsChange();
            }
        });
    }

    private void reloadBlack() {
        urlService.getBlackUrls(new LocalUrlService.GetUrlsCallback() {
            @Override
            public void onGet(List<PackageUrlSet> result) {
                packageBlackList = PackageUrlSet.copy(result);
                notifyUrlsChange();
            }
        });
    }

    private void notifyUrlsChange() {
        blackAdapter.setUrls(packageBlackList, packageUrlList);
        notifyChange(blackAdapter);
    }

    private void notifyChange(final RecyclerView.Adapter adapter) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            startActivity(new Intent(this, AddActivity.class));
            return true;
        } else if (id == R.id.action_add_black) {
            new MaterialDialog.Builder(this)
                    .title(R.string.dialog_title_add)
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .inputRange(1, 80, Color.RED)
                    .input(getString(R.string.dialog_add_input_hint), "", new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(MaterialDialog dialog, CharSequence input) {
                            urlService.addBlackUrls(Arrays.asList(new PackageUrlSet(UrlServiceUtils.USER_ADD_PACKAGE, Arrays.asList(input.toString()))), null);
                        }
                    })
                    .negativeText(R.string.dialog_btn_cancel)
                    .positiveText(R.string.dialog_btn_add).build().show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

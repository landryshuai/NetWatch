package info.noverguo.netwatch.ui;

import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import info.noverguo.netwatch.BuildConfig;
import info.noverguo.netwatch.R;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import info.noverguo.netwatch.adapter.MultiSelectRecyclerViewAdapter;
import info.noverguo.netwatch.adapter.PackageRecyclerViewAdapter;
import info.noverguo.netwatch.model.PackageHostMap;
import info.noverguo.netwatch.model.PackageUrlSet;
import info.noverguo.netwatch.receiver.ReloadReceiver;
import info.noverguo.netwatch.tools.UrlsManager;
import info.noverguo.netwatch.utils.BrowserUtils;
import info.noverguo.netwatch.utils.DLog;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

public class AddActivity extends AppCompatActivity {
    final static String TAG = AddActivity.class.getSimpleName();
    @Bind(R.id.rv_urls)
    RecyclerView mRecyclerView;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.fab)
    FloatingActionButton mFab;
    PackageRecyclerViewAdapter packageAdapter;
    UrlsManager urlsManager;
    int mSelectCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initView() {
        if (BuildConfig.DEBUG) DLog.i("initView start");
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));//这里用线性显示 类似于listview
        if (BuildConfig.DEBUG) DLog.i("initView end");
    }

    private void initData() {
        if (BuildConfig.DEBUG) DLog.i("initData start");
        urlsManager = UrlsManager.get(getApplicationContext());
        packageAdapter = new PackageRecyclerViewAdapter(getApplicationContext(), new PackageRecyclerViewAdapter.ItemClickListener() {
            @Override
            public void onItemClick(PackageHostMap item) {
                try {
                    new MaterialDialog.Builder(AddActivity.this)
                            .title(item.url)
                            .icon(getPackageManager().getApplicationIcon(item.packageName))
                            .items(item.relativePackageUrls)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                    try {
                                        BrowserUtils.openBrowser(AddActivity.this, text.toString());
                                    } catch (Exception e) {
                                        Toast.makeText(getApplicationContext(), R.string.browser_not_found, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .autoDismiss(false)
                            .positiveText(R.string.ok)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        packageAdapter.setSelectedListener(new MultiSelectRecyclerViewAdapter.SelectedListener() {
            @Override
            public void onSelect(int selectCount) {
                if (BuildConfig.DEBUG) Log.i(TAG, "onSelect: " + selectCount);
                mSelectCount = selectCount;
                invalidateOptionsMenu();
                if (selectCount == 0) {
                    mFab.setVisibility(View.INVISIBLE);
                    mToolbar.setTitle(R.string.title_add_name);
                    return;
                }
                mToolbar.setTitle("已选择" + selectCount + "条规则");
                mFab.setVisibility(View.VISIBLE);
                if (selectCount > 0) {
                    mFab.setImageResource(R.drawable.ic_done_white_24dp);
                    mFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
//                    mFab.setBackgroundColor(getResources().getColor(R.color.colorWarning));
                    mFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addBlackUrls();
                        }
                    });
                }
            }
        });
        mRecyclerView.setAdapter(packageAdapter);
        notifyUrlsChange();
    }
    ReloadReceiver reloadBlackReceiver;
    ReloadReceiver reloadPackageReceiver;
    boolean canChange = false;
    @Override
    protected void onResume() {
        super.onResume();
        reloadBlackReceiver = ReloadReceiver.registerReloadBlack(getApplicationContext(), new Runnable() {
            @Override
            public void run() {
                if (canChange || packageAdapter.getItemCount() == 0) {
                    notifyUrlsChange();
                }
            }
        });
        reloadPackageReceiver = ReloadReceiver.registerReloadPackage(getApplicationContext(), new Runnable() {
            @Override
            public void run() {
                if (canChange || packageAdapter.getItemCount() == 0) {
                    notifyUrlsChange();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        reloadBlackReceiver.unregister(getApplicationContext());
        reloadPackageReceiver.unregister(getApplicationContext());
    }

    private void addBlackUrls() {
        canChange = true;
        urlsManager.addBlackUrls(packageAdapter.getSelectUrls());
        resetAdapter();
    }

    private void resetAdapter() {
        packageAdapter.clearSelectedState();
        notifyChange(packageAdapter);
    }

    private void notifyUrlsChange() {
        canChange = false;
        Schedulers.io().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                packageAdapter.setUrls(urlsManager.getUrlList());
            }
        });
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
    public void onBackPressed() {
        if (!cancelSelected()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mSelectCount == 0) {
            getMenuInflater().inflate(R.menu.menu_add, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_add_select, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_cancel_select) {
            cancelSelected();
        } else if (id == R.id.action_del_pkg) {
            deletePackageUrls();
        }
        return super.onOptionsItemSelected(item);
    }

    private void deletePackageUrls() {
        List<PackageUrlSet> selectUrls = packageAdapter.getSelectUrls();
        if (selectUrls.isEmpty()) {
            return;
        }
        canChange = true;
        for (PackageUrlSet selectUrl : selectUrls) {
            for (String url : selectUrl.relativeUrls) {
                urlsManager.removePackageUrl(selectUrl.packageName, url);
            }
        }
    }

    private boolean cancelSelected() {
        if (packageAdapter.getSelectedItemCount() > 0) {
            resetAdapter();
            return true;
        }
        return false;
    }
}

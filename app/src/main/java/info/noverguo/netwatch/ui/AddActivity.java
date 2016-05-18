package info.noverguo.netwatch.ui;

import android.content.res.ColorStateList;
import android.os.Bundle;
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

import com.tencent.noverguo.hooktest.R;

import info.noverguo.netwatch.adapter.MultiSelectRecyclerViewAdapter;
import info.noverguo.netwatch.adapter.PackageRecyclerViewAdapter;
import info.noverguo.netwatch.model.PackageUrl;
import info.noverguo.netwatch.model.PackageUrlSet;
import info.noverguo.netwatch.receiver.ReloadReceiver;
import info.noverguo.netwatch.service.LocalUrlService;
import info.noverguo.netwatch.utils.DLog;
import info.noverguo.netwatch.utils.SizeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AddActivity extends AppCompatActivity {
    final static String TAG = AddActivity.class.getSimpleName();
    LocalUrlService urlService;
    @Bind(R.id.rv_urls)
    RecyclerView mRecyclerView;
    @Bind(R.id.fab)
    FloatingActionButton mFab;
    PackageRecyclerViewAdapter packageAdapter;
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));//这里用线性显示 类似于listview
        DLog.i("initView end");
    }

    private void initData() {
        DLog.i("initData start");
        urlService = new LocalUrlService(getApplicationContext());
        packageAdapter = new PackageRecyclerViewAdapter(getApplicationContext(), new PackageRecyclerViewAdapter.Callback() {
            @Override
            public void onItemClick(PackageUrl item) {
                Toast.makeText(getApplicationContext(), item.url, Toast.LENGTH_LONG).show();
            }
        });
        packageAdapter.setSelectedListener(new MultiSelectRecyclerViewAdapter.SelectedListener() {
            @Override
            public void onSelect(int selectCount) {
                Log.i(TAG, "onSelect: " + selectCount);
                if (selectCount == 0) {
                    mFab.setVisibility(View.INVISIBLE);
                    return;
                }
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
                reloadBlack();
            }
        });
        reloadPackageReceiver = ReloadReceiver.registerReloadBlack(getApplicationContext(), new Runnable() {
            @Override
            public void run() {
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

    private void addBlackUrls() {
        urlService.addBlackUrls(packageAdapter.getSelectUrls(), null);
        resetAdapter();
    }

    private void resetAdapter() {
        packageAdapter.clearSelectedState();
        notifyChange(packageAdapter);
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
        packageAdapter.setUrls(packageBlackList, packageUrlList);
        notifyChange(packageAdapter);
    }

    private void notifyChange(final RecyclerView.Adapter adapter) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private List<String> toList(Map<String, Set<String>> urlSet) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : urlSet.entrySet()) {
            String host = entry.getKey();
            Set<String> paths = entry.getValue();
            if (SizeUtils.isEmpty(paths)) {
                result.add(host + "/");
            } else {
                for (String path : entry.getValue()) {
                    result.add(host + path);
                }
            }
        }
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

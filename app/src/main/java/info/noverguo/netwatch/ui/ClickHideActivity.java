package info.noverguo.netwatch.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.Bind;
import butterknife.ButterKnife;
import info.noverguo.netwatch.BuildConfig;
import info.noverguo.netwatch.R;
import info.noverguo.netwatch.adapter.ClickHideAdapter;
import info.noverguo.netwatch.tools.AppDataManager;
import info.noverguo.netwatch.utils.DLog;

public class ClickHideActivity extends AppCompatActivity {
    final static String TAG = ClickHideActivity.class.getSimpleName();
    @Bind(R.id.rv_urls)
    RecyclerView mRecyclerView;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.fab)
    FloatingActionButton mFab;
    ClickHideAdapter clickHideAdapter;
    AppDataManager appDataManager;
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
        appDataManager = AppDataManager.get(getApplicationContext());
        clickHideAdapter = new ClickHideAdapter(getApplicationContext());
        mRecyclerView.setAdapter(clickHideAdapter);
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_add_select, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}

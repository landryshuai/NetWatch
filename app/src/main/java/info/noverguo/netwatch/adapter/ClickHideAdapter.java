package info.noverguo.netwatch.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import info.noverguo.netwatch.BuildConfig;
import info.noverguo.netwatch.R;
import info.noverguo.netwatch.tools.AppDataManager;

/**
 * Created by noverguo on 2016/6/5.
 */

public class ClickHideAdapter extends RecyclerView.Adapter<ClickHideAdapter.FilterViewHolder> {
    private final LayoutInflater mLayoutInflater;
    List<PackageInfo> installedPackages;
    PackageManager mPackageManager;
    AppDataManager appDataManager;
    Context mContext;
    public ClickHideAdapter(Context context) {
        mContext = context.getApplicationContext();
        mLayoutInflater = LayoutInflater.from(context);
        mPackageManager = context.getPackageManager();
        appDataManager = AppDataManager.get(context);
        installedPackages = mPackageManager.getInstalledPackages(0);
        Collections.sort(installedPackages, new Comparator<PackageInfo>() {
            @Override
            public int compare(PackageInfo lhs, PackageInfo rhs) {
                int res = (lhs.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) - (rhs.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM);
                if (res != 0) {
                    return res;
                }
                return lhs.packageName.compareTo(rhs.packageName);
            }
        });
    }
    @Override
    public FilterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FilterViewHolder(mLayoutInflater.inflate(R.layout.filter_list, parent, false));
    }

    @Override
    public void onBindViewHolder(FilterViewHolder holder, int position) {
        final PackageInfo item = installedPackages.get(position);
        holder.mFilter.setTag(item.packageName);
        holder.mFilter.setChecked(appDataManager.checkClickHide(item.packageName));
        holder.mFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String pkg = (String) buttonView.getTag();
                if (isChecked) {
                    appDataManager.addClickHidePackage(pkg);
                } else {
                    appDataManager.removeClickHidePackage(pkg);
                }
            }
        });
        try {
            String appName = mPackageManager.getApplicationLabel(mPackageManager.getApplicationInfo(item.packageName, PackageManager.GET_META_DATA)).toString();
            holder.mContent.setText(appName);
            holder.mIcon.setImageDrawable(mPackageManager.getApplicationIcon(item.packageName));
        } catch (PackageManager.NameNotFoundException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return installedPackages.size();
    }

    public static class FilterViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_content)
        TextView mContent;
        @Bind(R.id.iv_icon)
        ImageView mIcon;
        @Bind(R.id.sb_filter)
        SwitchButton mFilter;

        FilterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}

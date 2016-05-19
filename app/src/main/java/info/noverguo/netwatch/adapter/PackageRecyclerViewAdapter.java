package info.noverguo.netwatch.adapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.noverguo.hooktest.R;
import info.noverguo.netwatch.model.HostPath;
import info.noverguo.netwatch.model.PackageUrl;
import info.noverguo.netwatch.model.PackageUrlSet;
import info.noverguo.netwatch.model.PackageUrls;
import info.noverguo.netwatch.utils.DLog;
import info.noverguo.netwatch.utils.UrlServiceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by noverguo on 2016/5/11.
 */
public class PackageRecyclerViewAdapter extends MultiSelectRecyclerViewAdapter<PackageRecyclerViewAdapter.NormalTextViewHolder> {
    private static final int TYPE_DESC = 0;
    private static final int TYPE_HEAD = 1;
    private static final int TYPE_BODY = 2;
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private final PackageManager mPackageManager;
    private int count = 0;
    private SparseArray<PackageUrlSet> headItemIndexes = new SparseArray<>();
    private SparseArray<PackageUrlSet> interceptItems = new SparseArray<>();
    private Callback callback;
    private ClickListener listener = new ClickListener() {
        @Override
        public void onItemClicked(int position) {
            if (getSelectedItemCount() == 0 && !isHead(position)) {
                if (callback != null) {
                    callback.onItemClick(getItem(position));
                }
            } else {
                switchSelectedState(position);
            }
        }

        @Override
        public boolean onItemLongClicked(int position) {
            if (getSelectedItemCount() == 0) {
                switchSelectedState(position);
                return true;
            }
            return false;
        }
    };

    public PackageRecyclerViewAdapter(Context context, Callback callback) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mPackageManager = mContext.getPackageManager();
        this.callback = callback;
        setClickListener(listener);
    }

    public void setUrls(List<PackageUrlSet> packageBlackList, List<PackageUrlSet> packageUrlList) {
        DLog.i("PackageRecyclerViewAdapter.setUrls.packageBlackList: " + packageBlackList);
        DLog.i("PackageRecyclerViewAdapter.setUrls.packageUrlList: " + packageUrlList);
        SparseArray<PackageUrlSet> _headItemIndexes = new SparseArray<>();
        SparseArray<PackageUrlSet> _interceptItems = new SparseArray<>();
        int _count = 0;
        for (PackageUrlSet pus : packageUrlList) {
            String packageName = pus.packageName;
            List<String> urls = new ArrayList<>();
            List<String> interceptUrls = new ArrayList<>();
            for (String url : pus.relativeUrls) {
                if (!UrlServiceUtils.isMatchBlack(packageBlackList, url)) {
                    urls.add(url);
                } else {
                    interceptUrls.add(url);
                }
            }
            if (!urls.isEmpty()) {
                PackageUrlSet packageUrlSet = new PackageUrlSet(packageName, urls);
                _interceptItems.put(_count, new PackageUrlSet(packageName, interceptUrls));
                _headItemIndexes.put(_count++, packageUrlSet);
                _count += packageUrlSet.relativeUrls.size();
            }
        }
        synchronized (this) {
            interceptItems = _interceptItems;
            headItemIndexes = _headItemIndexes;
            count = _count;
        }
    }

    @Override
    public boolean canSelected(int position) {
        return true;
    }

    public List<PackageUrlSet> getSelectUrls() {
        Map<String, PackageUrlSet> selectUrls = new HashMap<>();
        List<Integer> selectedItems = getSelectedItems();
        for(Integer index : selectedItems) {
            PackageUrl pu = getItem(index);
            PackageUrlSet.put(selectUrls, pu.packageName, pu.url);

        }
        return new ArrayList<>(selectUrls.values());
    }

    @Override
    public int getItemViewType(int position) {
        return isHead(position) ? TYPE_HEAD : TYPE_BODY;
    }


    @Override
    public NormalTextViewHolder onCreateMultiSelectViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEAD) {
            return new HeadViewHolder(mLayoutInflater.inflate(R.layout.head_item, parent, false));
        }
        return new NormalTextViewHolder(mLayoutInflater.inflate(R.layout.item_text, parent, false));
    }

    @Override
    public void onBindViewHolder(NormalTextViewHolder holder, int position, boolean isSelected) {
        PackageUrl item = getItem(position);
        holder.mContent.setText(item.url);
        if (isHead(position)) {
            onBindHead((HeadViewHolder) holder, (PackageUrls) item, isSelected);
        } else {
            onBindContent(holder, item, isSelected);
        }
    }

    private void onBindHead(HeadViewHolder holder, PackageUrls item, boolean isSelected) {
        try {
            String appName = mPackageManager.getApplicationLabel(mPackageManager.getApplicationInfo(item.packageName, PackageManager.GET_META_DATA)).toString();
            holder.mContent.setText(appName);
            holder.mIcon.setImageDrawable(mContext.getPackageManager().getApplicationIcon(item.packageName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        holder.mCount.setText(item.relativeUrls.size() + "");
        holder.mContent.setTextSize(18);
        holder.mContent.setTextColor(Color.argb(255, 5, 5, 5));
    }

    private void onBindContent(NormalTextViewHolder holder, PackageUrl item, boolean isSelected) {
        holder.itemView.setBackgroundColor(isSelected ? mContext.getResources().getColor(R.color.colorPrimaryAlphaHalf) : Color.WHITE);
        holder.mContent.setTextSize(15);
        holder.mContent.setTextColor(isSelected ? Color.WHITE : Color.BLACK);
    }

    @Override
    public void switchSelectedState(int position) {
        if (isHead(position)) {
            PackageUrlSet packageUrlSet = headItemIndexes.get(position);
            int selectCount = 0;
            for (int i=0;i<packageUrlSet.relativeUrls.size();++i) {
                if (isSelected(position + i + 1)) {
                    ++selectCount;
                }
            }
            if (selectCount != packageUrlSet.relativeUrls.size()) {
                // 全选
                for (int i=0;i<packageUrlSet.relativeUrls.size();++i) {
                    selected(position + i + 1);
                }
            } else {
                // 全不选
                for (int i=0;i<packageUrlSet.relativeUrls.size();++i) {
                    disselected(position + i + 1);
                }
            }
            return;
        }
        super.switchSelectedState(position);
    }

    @Override
    public int getItemCount() {
        return count;
    }

    private boolean isHead(int position) {
        synchronized (this) {
            return headItemIndexes.get(position) != null;
        }
    }

    public PackageUrl getItem(int position) {
        synchronized (this) {
            PackageUrlSet packageUrlSet = headItemIndexes.get(position);
            if (packageUrlSet == null) {
                int size = headItemIndexes.size();
                for (int i = 0; i < size; ++i) {
                    int index = headItemIndexes.keyAt(i);
                    packageUrlSet = headItemIndexes.get(index);
                    int curCount = packageUrlSet.relativeUrls.size();
                    if (position > index && position <= index + curCount) {
                        return new PackageUrl(packageUrlSet.packageName, packageUrlSet.relativeUrls.get(position - index - 1));
                    }
                }
                throw new RuntimeException("不应该走到这的,请检查bug!!!");
            } else {
                return new PackageUrls(packageUrlSet.packageName, packageUrlSet.relativeUrls);
            }
        }
    }

    public static class NormalTextViewHolder extends MultiSelectRecyclerViewAdapter.MultiSelectViewHolder {
        @Bind(R.id.tv_content)
        TextView mContent;

        NormalTextViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public static class HeadViewHolder extends ImageTextViewHolder{
        @Bind(R.id.tv_count)
        TextView mCount;
        @Bind(R.id.ll_item)
        View mItem;
        HeadViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }


    public static class ImageTextViewHolder extends NormalTextViewHolder{
        @Bind(R.id.tv_content)
        TextView mContent;
        @Bind(R.id.iv_icon)
        ImageView mIcon;

        ImageTextViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public interface Callback {
        void onItemClick(PackageUrl item);
    }
}
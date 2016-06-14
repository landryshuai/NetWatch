package info.noverguo.netwatch.adapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import info.noverguo.netwatch.BuildConfig;
import info.noverguo.netwatch.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import info.noverguo.netwatch.model.HostPath;
import info.noverguo.netwatch.model.PackageHostMap;
import info.noverguo.netwatch.model.PackageUrl;
import info.noverguo.netwatch.model.PackageUrlSet;
import info.noverguo.netwatch.model.PackageUrls;
import info.noverguo.netwatch.tools.UrlsManager;
import info.noverguo.netwatch.utils.DLog;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

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
    private int allCount = 0;
    private int showCount = 0;
    private SparseArray<PackageUrlSet> headItemIndexes = new SparseArray<>();
    private SparseArray<PackageUrlSet> interceptItems = new SparseArray<>();
    private SparseArray<PackageHostMap> contentItemIndexes = new SparseArray<>();
    private int[] itemIndexMap;
    private SparseBooleanArray headShowMap = new SparseBooleanArray();
    private ItemClickListener itemClickListener;
    private UrlsManager urlsManager;
    private ClickListener listener = new ClickListener() {
        @Override
        public void onItemClicked(int position) {
            if (isHead(position)) {
                toggleHead(position);
                return;
            }
            if (getSelectedItemCount() > 0) {
                switchSelectedState(position);
            } else {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick((PackageHostMap) getItem(position));
                }
            }
        }

        @Override
        public boolean onItemLongClicked(int position) {
            switchSelectedState(position);
            return true;
        }
    };

    public PackageRecyclerViewAdapter(Context context, ItemClickListener itemClickListener) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mPackageManager = mContext.getPackageManager();
        urlsManager = UrlsManager.get(context);
        this.itemClickListener = itemClickListener;
        setClickListener(listener);
    }

    public void setUrls(List<PackageUrlSet> packageUrlList) {
        if (BuildConfig.DEBUG) DLog.i("PackageRecyclerViewAdapter.setUrls.packageUrlList: " + packageUrlList);
        final SparseArray<PackageUrlSet> _headItemIndexes = new SparseArray<>();
        final SparseArray<PackageUrlSet> _interceptItems = new SparseArray<>();
        final SparseArray<PackageHostMap> _contentItemIndexes = new SparseArray<>();
        final SparseBooleanArray _headShowMap = new SparseBooleanArray();
        int _count = 0;
        for (PackageUrlSet pus : packageUrlList) {
            String packageName = pus.packageName;
            Map<String, PackageHostMap> map = new HashMap<>();
            List<String> urls = new ArrayList<>();
            List<String> interceptUrls = new ArrayList<>();
            for (String url : pus.relativeUrls) {
                HostPath hostPath = HostPath.create(url);
                if (urlsManager.checkIsIntercept(packageName, hostPath.host, hostPath.path)) {
                    interceptUrls.add(url);
                } else {
                    String host = hostPath.host;
                    if (!map.containsKey(host)) {
                        map.put(host, new PackageHostMap(packageName, host));
                    }
                    map.get(host).add(url);
                    urls.add(url);
                }
            }
            if (!map.isEmpty()) {
                PackageUrlSet packageUrlSet = new PackageUrlSet(packageName);
                _interceptItems.put(_count, new PackageUrlSet(packageName, interceptUrls));
                _headShowMap.put(_count, false);
                _headItemIndexes.put(_count++, packageUrlSet);
                for (PackageHostMap hostMap : map.values()) {
                    packageUrlSet.add(hostMap.url);
                    _contentItemIndexes.put(_count++, hostMap);
                }
            }
        }
        final int tmpCount = _count;
        // 保证在主线程运行
        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                synchronized (PackageRecyclerViewAdapter.this) {
                    clearSelectedState();
                    interceptItems = _interceptItems;
                    headItemIndexes = _headItemIndexes;
                    headShowMap = _headShowMap;
                    contentItemIndexes = _contentItemIndexes;
                    allCount = tmpCount;
                    showCount = allCount;
                    resetShowFlags();
                }
                notifyDataSetChanged();
            }
        });
    }

    private void resetShowFlags() {
        itemIndexMap = new int[allCount];
        int j = 0;
        boolean show = true;
        for (int i=0;i<allCount;++i) {
            if (isHead(i, false)) {
                itemIndexMap[j++] = i;
                show = headShowMap.get(i);
            } else {
                if (show) {
                    itemIndexMap[j++] = i;
                }
            }
        }
        showCount = j;
        if (BuildConfig.DEBUG) DLog.d("resetShowFlags: " + allCount, showCount, Arrays.toString(itemIndexMap));
    }

    private boolean isHeadShow(int position) {
        return headShowMap.get(fixPos(position));
    }

    private void setHeadShow(int position, boolean isShow) {
        headShowMap.put(fixPos(position), isShow);
    }

    public void toggleHead(int position) {
        setHeadShow(position, !isHeadShow(position));
        resetShowFlags();
        notifyDataSetChanged();
    }

    @Override
    public boolean canSelected(int position) {
        return !isHead(position);
    }

    @Override
    public boolean isSelected(int position) {
        return super.isSelected(fixPos(position));
    }


    @Override
    public void selected(int position) {
        selectedItems.put(fixPos(position), true);
        notifyItemChanged(position);
    }

    @Override
    public void disselected(int position) {
        selectedItems.delete(fixPos(position));
        notifyItemChanged(position);
    }

    public List<PackageUrlSet> getSelectUrls() {
        Map<String, PackageUrlSet> selectUrls = new HashMap<>();
        List<Integer> selectedItems = getSelectedItems();
        for(Integer index : selectedItems) {
            PackageUrl pu = getItem(index, false);
            PackageUrlSet.put(selectUrls, pu.packageName, pu.url);
        }
        return new ArrayList<>(selectUrls.values());
    }

    @Override
    public void switchSelectedState(int position) {
        if (isHead(position)) {
            if (BuildConfig.DEBUG) DLog.i("PackageRecyclerViewAdapter.switchSelectedState1: " + position + ", " + Arrays.toString(itemIndexMap) + ", " + getSelectedItems());
            int fixPos = fixPos(position);
            PackageUrlSet packageUrlSet = headItemIndexes.get(fixPos);
            int selectCount = 0;
            for (int i=0;i<packageUrlSet.relativeUrls.size();++i) {
                if (super.isSelected(fixPos + i + 1)) {
                    ++selectCount;
                }
            }
            if (selectCount != packageUrlSet.relativeUrls.size()) {
                // 全选
                for (int i=0;i<packageUrlSet.relativeUrls.size();++i) {
                    selectedItems.put(fixPos + i + 1, true);
                }
            } else {
                // 全不选
                for (int i=0;i<packageUrlSet.relativeUrls.size();++i) {
                    selectedItems.delete(fixPos + i + 1);
                }
            }
            if (isHeadShow(position)) {
                notifyItemRangeChanged(position + 1, packageUrlSet.relativeUrls.size());
            }
            if (BuildConfig.DEBUG) DLog.i("PackageRecyclerViewAdapter.switchSelectedState2: " + position + ", " + Arrays.toString(itemIndexMap) + ", " + getSelectedItems());
        } else {
            super.switchSelectedState(position);
        }
    }

    @Override
    public int getItemCount() {
        return showCount;
    }

    public PackageUrl getItem(int position) {
        return getItem(position, true);
    }

    public PackageUrl getItem(int position, boolean fix) {
        if (fix) {
            position = fixPos(position);
        }
        synchronized (this) {
            PackageUrlSet packageUrlSet = headItemIndexes.get(position);
            if (packageUrlSet == null) {
                return contentItemIndexes.get(position);
            } else {
                return new PackageUrls(packageUrlSet.packageName, packageUrlSet.relativeUrls);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return isHead(position) ? TYPE_HEAD : TYPE_BODY;
    }


    @Override
    public NormalTextViewHolder onCreateMultiSelectViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEAD) {
            return new HeadViewHolder(mLayoutInflater.inflate(R.layout.package_list_head, parent, false));
        }
        return new ContentViewHolder(mLayoutInflater.inflate(R.layout.package_list_content, parent, false));
    }

    @Override
    public void onBindViewHolder(NormalTextViewHolder holder, int position, boolean isSelected) {
        PackageUrl item = getItem(position);
        if (isHead(position)) {
            onBindHead((HeadViewHolder) holder, (PackageUrls) item, isSelected);
        } else {
            onBindContent((ContentViewHolder) holder, (PackageHostMap) item, isSelected);
        }
    }

    private void onBindHead(HeadViewHolder holder, PackageUrls item, boolean isSelected) {
        try {
            String appName = mPackageManager.getApplicationLabel(mPackageManager.getApplicationInfo(item.packageName, PackageManager.GET_META_DATA)).toString();
            holder.mContent.setText(appName);
            holder.mIcon.setImageDrawable(mContext.getPackageManager().getApplicationIcon(item.packageName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            removePackageUrls(item.packageName);
        }
        holder.mCount.setText(item.relativeUrls.size() + "");
        holder.mContent.setTextSize(18);
        holder.mContent.setTextColor(Color.argb(255, 5, 5, 5));
    }

    private void onBindContent(ContentViewHolder holder, PackageHostMap item, boolean isSelected) {
        holder.mContent.setText(item.url);
        holder.mItem.setBackgroundColor(isSelected ? mContext.getResources().getColor(R.color.colorPrimaryAlphaHalf) : Color.WHITE);
        holder.mContent.setTextSize(15);
        holder.mContent.setTextColor(isSelected ? Color.WHITE : Color.BLACK);
        holder.mCount.setText("共" + item.relativePackageUrls.size() + "条");
    }
    Set<String> needRemovePackage;
    Handler uiHandler;
    final int MSG_REMOVE_PACKAGE = 1;
    private void removePackageUrls(String packageName) {
        urlsManager.removePackage(packageName);
        if (needRemovePackage == null) {
            needRemovePackage = new HashSet<>(1);
        }
        synchronized (needRemovePackage) {
            needRemovePackage.add(packageName);
        }
        if (uiHandler == null) {
            uiHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == MSG_REMOVE_PACKAGE) {
                        synchronized (needRemovePackage) {
                            List<Integer> removeKeys = new ArrayList<>();
                            for(String packageName : needRemovePackage) {
                                int headSize = headItemIndexes.size();
                                for (int i=0;i<headSize;++i) {
                                    if (packageName.equals(headItemIndexes.valueAt(i).packageName)) {
                                        headItemIndexes.removeAt(i);
                                        break;
                                    }
                                }
                                int contentSize = contentItemIndexes.size();
                                for (int i=0;i<contentSize;++i) {
                                    if (packageName.equals(contentItemIndexes.valueAt(i).packageName)) {
                                        removeKeys.add(contentItemIndexes.keyAt(i));
                                    }
                                }
                                if (!removeKeys.isEmpty()) {
                                    for (Integer rkey : removeKeys) {
                                        contentItemIndexes.remove(rkey);
                                    }
                                    removeKeys.clear();
                                }
                            }
                        }
                    }
                }
            };
        }
        uiHandler.removeMessages(MSG_REMOVE_PACKAGE);
        uiHandler.sendEmptyMessage(MSG_REMOVE_PACKAGE);
    }

    public static class NormalTextViewHolder extends MultiSelectRecyclerViewAdapter.MultiSelectViewHolder {
        @Bind(R.id.tv_content)
        TextView mContent;

        NormalTextViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }


    public static class ImageTextViewHolder extends NormalTextViewHolder {
        @Bind(R.id.iv_icon)
        ImageView mIcon;

        ImageTextViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public static class HeadViewHolder extends ImageTextViewHolder {
        @Bind(R.id.tv_count)
        TextView mCount;
        @Bind(R.id.ll_item)
        View mItem;
        HeadViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public static class ContentViewHolder extends NormalTextViewHolder {
        @Bind(R.id.ll_item)
        View mItem;
        @Bind(R.id.tv_count)
        TextView mCount;
        ContentViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public interface ItemClickListener {
        void onItemClick(PackageHostMap item);
    }

    private int fixPos(int position) {
        return itemIndexMap[position];
    }

    private boolean isHead(int position) {
        return isHead(position, true);
    }

    private boolean isHead(int position, boolean fix) {
        if (fix) {
            position = fixPos(position);
        }
        synchronized (this) {
            return headItemIndexes.get(position) != null;
        }
    }
}
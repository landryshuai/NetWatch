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
import info.noverguo.netwatch.model.PackageUrl;
import info.noverguo.netwatch.model.PackageUrlSet;
import info.noverguo.netwatch.tools.AppDataManager;
import info.noverguo.netwatch.utils.DLog;
import info.noverguo.netwatch.utils.SizeUtils;
import info.noverguo.netwatch.utils.UrlServiceUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by noverguo on 2016/5/11.
 */
public class BlackRecyclerViewAdapter extends MultiSelectRecyclerViewAdapter<BlackRecyclerViewAdapter.NormalTextViewHolder> {
    private static final int TYPE_DESC = 0;
    private static final int TYPE_HEAD = 1;
    private static final int TYPE_BODY = 2;
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private final PackageManager mPackageManager;
    private AppDataManager appDataManager;
    private int allCount = 0;
    private int showCount = 0;
    private SparseArray<PackageUrlSet> headItemIndexes = new SparseArray<>();
    private SparseArray<PackageUrl> contentItemIndexes = new SparseArray<>();
    private SparseArray<List<PackageUrl>> interceptItemIndexes = new SparseArray<>();
    private int[] itemIndexMap;
    private SparseBooleanArray headShowMap = new SparseBooleanArray();
    private ItemClickListener itemClickListener;
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
                    itemClickListener.onItemClick(getItem(position), interceptItemIndexes.get(fixPos(position)));
                }
            }
        }

        @Override
        public boolean onItemLongClicked(int position) {
            switchSelectedState(position);
            return true;
        }
    };

    public BlackRecyclerViewAdapter(Context context, ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mPackageManager = mContext.getPackageManager();
        appDataManager = AppDataManager.get(context);
        setClickListener(listener);
    }

    public void setUrls(List<PackageUrlSet> packageBlackList, List<PackageUrlSet> packageUrlList) {
        if (BuildConfig.DEBUG) DLog.i("BlackRecyclerViewAdapter.setUrls: " + packageBlackList.size() + ", " + packageUrlList.size());
        Collections.sort(packageBlackList, new Comparator<PackageUrlSet>() {
            @Override
            public int compare(PackageUrlSet lhs, PackageUrlSet rhs) {
                if (UrlServiceUtils.isUserAddPackage(lhs.packageName)) {
                    return -1;
                }
                if (UrlServiceUtils.isUserAddPackage(rhs.packageName)) {
                    return 1;
                }

                return -lhs.packageName.compareTo(rhs.packageName);
            }
        });
        final SparseArray<PackageUrlSet> _headItemIndexes = new SparseArray<>();
        final SparseArray<List<PackageUrl>> _interceptItemIndexes = new SparseArray<>();
        final SparseBooleanArray _headShowMap = new SparseBooleanArray();
        final SparseArray<PackageUrl> _contentItemIndexes = new SparseArray<>();
        int _count = 0;
        for (PackageUrlSet pus : packageBlackList) {
            String packageName = pus.packageName;
            // 默认不展开
            _headShowMap.put(_count, false);
            _headItemIndexes.put(_count++, pus);
            if (UrlServiceUtils.isUserAddPackage(packageName)) {
                List<PackageUrlSet> urlList = appDataManager.getUrlList();
                for (String url : pus.relativeUrls) {
                    _contentItemIndexes.put(_count, new PackageUrl(packageName, url));
                    _interceptItemIndexes.put(_count++, UrlServiceUtils.getMatchPackageUrls(url, urlList));
                }
            } else {
                PackageUrlSet packageUrlSet = appDataManager.getPackageUrl(packageName);
                for (String url : pus.relativeUrls) {
                    _contentItemIndexes.put(_count, new PackageUrl(packageName, url));
                    _interceptItemIndexes.put(_count++, UrlServiceUtils.getMatchPackageUrls(url, packageUrlSet));
                }
            }
        }
        final int tmpCount = _count;
        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                synchronized (BlackRecyclerViewAdapter.this) {
                    headItemIndexes = _headItemIndexes;
                    headShowMap = _headShowMap;
                    interceptItemIndexes = _interceptItemIndexes;
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
            PackageUrl pu = getItem(index);
            PackageUrlSet.put(selectUrls, pu.packageName, pu.url);
        }
        return new ArrayList<>(selectUrls.values());
    }

    @Override
    public void switchSelectedState(int position) {
        if (isHead(position)) {
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
            PackageUrlSet blackUrlSet = headItemIndexes.get(position);
            if (blackUrlSet == null) {
                return contentItemIndexes.get(position);
            }
            return new PackageUrl(blackUrlSet.packageName, blackUrlSet.packageName);
        }
    }


    @Override
    public int getItemViewType(int position) {
        return isHead(position) ? TYPE_HEAD : TYPE_BODY;
    }

    @Override
    public NormalTextViewHolder onCreateMultiSelectViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEAD) {
            return new HeadViewHolder(mLayoutInflater.inflate(R.layout.black_list_head, parent, false));
        }
        return new ContentViewHolder(mLayoutInflater.inflate(R.layout.black_list_item_content, parent, false));
    }

    @Override
    public void onBindViewHolder(NormalTextViewHolder holder, int position, boolean isSelected) {
        PackageUrl item = getItem(position);
        if (isHead(position)) {
            onBindHead((HeadViewHolder) holder, item, isSelected);
        } else {
            onBindContent((ContentViewHolder) holder, item, interceptItemIndexes.get(position), isSelected);
        }
    }

    private void onBindHead(HeadViewHolder holder, PackageUrl item, boolean isSelected) {
        if (UrlServiceUtils.isUserAddPackage(item.packageName)) {
            holder.mContent.setText(R.string.global_intercept_rule);
            holder.mIcon.setImageResource(R.drawable.ic_language_24dp);
        } else {
            try {
                String appName = mPackageManager.getApplicationLabel(mPackageManager.getApplicationInfo(item.packageName, PackageManager.GET_META_DATA)).toString();
                holder.mContent.setText(appName);
                holder.mIcon.setImageDrawable(mContext.getPackageManager().getApplicationIcon(item.packageName));
            } catch (PackageManager.NameNotFoundException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
                removePackageUrls(item.packageName);
            }
        }
    }

    private void onBindContent(ContentViewHolder holder, PackageUrl item, List<PackageUrl> interceptItems, boolean isSelected) {
        holder.mContent.setText(item.url);
        holder.mCount.setText("已匹配" + SizeUtils.getSize(interceptItems) + "条");

        // 设置选中后的颜色
        holder.mItem.setBackgroundColor(isSelected ? mContext.getResources().getColor(R.color.colorPrimaryAlphaHalf) : Color.WHITE);
        holder.mContent.setTextColor(isSelected ? Color.WHITE : Color.BLACK);
    }

    Set<String> needRemovePackage;
    Handler uiHandler;
    final int MSG_REMOVE_PACKAGE = 1;
    private void removePackageUrls(String packageName) {
        appDataManager.removeBlack(packageName);
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
        @Bind(R.id.ll_item)
        View mItem;
        HeadViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public static class ContentViewHolder extends ImageTextViewHolder {
        @Bind(R.id.tv_count)
        TextView mCount;
        @Bind(R.id.ll_item)
        View mItem;
        ContentViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public interface ItemClickListener {
        void onItemClick(PackageUrl item, List<PackageUrl> interceptItems);
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
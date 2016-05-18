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
import info.noverguo.netwatch.model.PackageUrlMap;
import info.noverguo.netwatch.model.PackageUrlSet;
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
public class BlackRecyclerViewAdapter extends MultiSelectRecyclerViewAdapter<BlackRecyclerViewAdapter.NormalTextViewHolder> {
    private static final int TYPE_DESC = 0;
    private static final int TYPE_HEAD = 1;
    private static final int TYPE_BODY = 2;
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private final PackageManager mPackageManager;
    private int count = 0;
    private SparseArray<PackageUrlMap> headItemIndexes = new SparseArray<>();
    private ClickListener listener = new ClickListener() {
        @Override
        public void onItemClicked(int position) {
            if (getSelectedItemCount() > 0) {
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

    public BlackRecyclerViewAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mPackageManager = mContext.getPackageManager();
        setClickListener(listener);
    }

    public void setUrls(List<PackageUrlSet> packageBlackList, List<PackageUrlSet> packageUrlList) {
        List<PackageUrlMap> _blackUrls = new ArrayList<>();
        SparseArray<PackageUrlMap> _headItemIndexes = new SparseArray<>();
        int _count = 0;
        for (PackageUrlSet pus : packageBlackList) {
            String packageName = pus.packageName;
            for (String url : pus.relativeUrls) {
                String regex = HostPath.create(url).toString();
                PackageUrlMap blackUrlMap = new PackageUrlMap(new PackageUrl(packageName, url), UrlServiceUtils.getMatchPackageUrls(regex, packageUrlList));
                _blackUrls.add(blackUrlMap);
                _headItemIndexes.put(_count++, blackUrlMap);
                _count += blackUrlMap.size();
            }
        }
        synchronized (this) {
            headItemIndexes = _headItemIndexes;
            count = _count;
        }
    }

    @Override
    public boolean canSelected(int position) {
        return isHead(position);
    }

    private boolean isHead(int position) {
        synchronized (this) {
            return headItemIndexes.get(position) != null;
        }
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
            return new HeadViewHolder(mLayoutInflater.inflate(R.layout.head_text, parent, false));
        }
        return new ImageTextViewHolder(mLayoutInflater.inflate(R.layout.item_content, parent, false));
    }

    @Override
    public void onBindViewHolder(NormalTextViewHolder holder, int position, boolean isSelected) {
        PackageUrl item = getItem(position);
        if (isHead(position)) {
            onBindHead((HeadViewHolder) holder, (PackageUrlMap) item, isSelected);
        } else {
            onBindContent((ImageTextViewHolder) holder, item, isSelected);
        }
    }

    private void onBindHead(HeadViewHolder holder, PackageUrlMap item, boolean isSelected) {
        holder.mItem.setBackgroundColor(isSelected ? mContext.getResources().getColor(R.color.colorPrimaryAlphaHalf) : Color.WHITE);
        holder.mIcon.setVisibility(View.GONE);
        holder.mContent.setTextColor(isSelected ? Color.WHITE : Color.BLACK);
        holder.mCount.setText("已匹配" + item.relativePackageUrls.size() + "条");
        bindIconAndText(holder, item, isSelected);
    }

    private void onBindContent(ImageTextViewHolder holder, PackageUrl item, boolean isSelected) {
        bindIconAndText(holder, item, isSelected);
    }

    private void bindIconAndText(ImageTextViewHolder holder, PackageUrl item, boolean isSelected) {
        holder.mContent.setText(HostPath.toSimpleUrl(item.url));
        if (UrlServiceUtils.isUserAddPackage(item.packageName)) {
            holder.mIcon.setImageResource(R.drawable.ic_edit);
        } else {
            try {
                holder.mIcon.setImageDrawable(mContext.getPackageManager().getApplicationIcon(item.packageName));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return count;
    }

    public PackageUrl getItem(int position) {
        synchronized (this) {
            PackageUrlMap blackUrlMap = headItemIndexes.get(position);
            if (blackUrlMap == null) {
                int size = headItemIndexes.size();
                for (int i = 0; i < size; ++i) {
                    int key = headItemIndexes.keyAt(i);
                    blackUrlMap = headItemIndexes.get(key);
                    int curCount = blackUrlMap.relativePackageUrls.size();
                    if (blackUrlMap.show && position >= key && position <= key + curCount) {
                        return blackUrlMap.relativePackageUrls.get(position - key - 1);
                    }
                }
            }
            return blackUrlMap;
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
}
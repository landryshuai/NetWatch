package info.noverguo.netwatch.adapter;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;

import info.noverguo.netwatch.utils.SparseArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by noverguo on 2016/5/11.
 */
public abstract class MultiSelectRecyclerViewAdapter<T extends MultiSelectRecyclerViewAdapter.MultiSelectViewHolder> extends RecyclerView.Adapter<T> {

    SparseBooleanArray selectedItems;
    private ClickListener clickListener;
    private SelectedListener selectedListener;

    public MultiSelectRecyclerViewAdapter() {
        this(null);
    }

    public MultiSelectRecyclerViewAdapter(ClickListener clickListener) {
        this.clickListener = clickListener;
        this.selectedItems = new SparseBooleanArray();
    }

    public void setClickListener(ClickListener listener) {
        clickListener = listener;
        setListenerWrapper();
    }

    public void setSelectedListener(SelectedListener selectedListener) {
        this.selectedListener = selectedListener;
        setListenerWrapper();
    }

    private void setListenerWrapper() {
        if (clickListener != null) {
            if (clickListener instanceof ClickListenerWrapper) {
                clickListener = ((ClickListenerWrapper) clickListener).target;
            }
            if (this.selectedListener != null) {
                clickListener = new ClickListenerWrapper(clickListener) {
                    @Override
                    void afterOnItemClicked(int position) {
                        selectedListener.onSelect(getSelectedItemCount());
                    }

                    @Override
                    void afterItemLongClicked(int position) {
                        selectedListener.onSelect(getSelectedItemCount());
                    }
                };
            }
        }
    }

    public boolean canSelected(int position) {
        return true;
    }

    public boolean isSelected(int position) {
        return selectedItems.get(position, false);
    }

    public void selected(int position) {
        selectedItems.put(position, true);
        notifyItemChanged(position);
    }

    public void disselected(int position) {
        selectedItems.delete(position);
        notifyItemChanged(position);
    }

    public void switchSelectedState(int position) {
        if (!canSelected(position)) {
            return;
        }
        if (isSelected(position)) {
            disselected(position);
        } else {
            selected(position);
        }
    }

    public void clearSelectedState() {
        SparseBooleanArray clone = selectedItems.clone();
        selectedItems.clear();
        SparseArrayUtils.forEachSelectItem(clone, new SparseArrayUtils.ForEachCallback<Integer, Boolean>() {
            @Override
            public void onItem(Integer key, Boolean value) {
                notifyItemChanged(key);
            }
        });
        if (selectedListener != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    selectedListener.onSelect(0);
                }
            });
        }
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        final List<Integer> items = new ArrayList<>(selectedItems.size());
        SparseArrayUtils.forEachSelectItem(selectedItems, new SparseArrayUtils.ForEachCallback<Integer, Boolean>() {
            @Override
            public void onItem(Integer key, Boolean value) {
                items.add(key);
            }
        });
        return items;
    }

    @Override
    final public T onCreateViewHolder(ViewGroup parent, int viewType) {
        T holder = onCreateMultiSelectViewHolder(parent, viewType);
        holder.setListener(clickListener);
        return holder;
    }

    public abstract T onCreateMultiSelectViewHolder(ViewGroup parent, int viewType);

    @Override
    final public void onBindViewHolder(T holder, int position) {
        onBindViewHolder(holder, position, isSelected(position));
    }

    abstract public void onBindViewHolder(T holder, int position, boolean isSelected);

    public static abstract class MultiSelectViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        ClickListener listener;
        public MultiSelectViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            itemLayoutView.setOnClickListener(this);
            itemLayoutView.setOnLongClickListener(this);
        }

        public void setListener(ClickListener listener) {
            this.listener = listener;
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClicked(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View view) {
            return listener != null && listener.onItemLongClicked(getAdapterPosition());
        }
    }

    protected static abstract class ClickListenerWrapper implements ClickListener {
        ClickListener target;
        ClickListenerWrapper(ClickListener target) {
            this.target = target;
        }

        @Override
        public void onItemClicked(int position) {
            target.onItemClicked(position);
            afterOnItemClicked(position);
        }

        @Override
        public boolean onItemLongClicked(int position) {
            boolean res = target.onItemLongClicked(position);
            afterItemLongClicked(position);
            return res;
        }

        abstract void afterOnItemClicked(int position);
        abstract void afterItemLongClicked(int position);
    }

    public interface ClickListener {
        void onItemClicked(int position);
        boolean onItemLongClicked(int position);
    }
    public interface SelectedListener {
        void onSelect(int selectCount);
    }
}

package info.noverguo.netwatch.utils;

import android.support.v4.util.LongSparseArray;
import android.util.SparseArray;
import android.util.SparseBooleanArray;

/**
 * Created by noverguo on 2016/5/11.
 */
public class SparseArrayUtils {

    public static <E> void forEachSelectItem(LongSparseArray<E> items, ForEachCallback<Long, E> callback) {
        int size = items.size();
        for(int i=0;i<size;++i) {
            callback.onItem(items.keyAt(i), items.valueAt(i));
        }
    }

    public static <E> void forEachSelectItem(SparseBooleanArray items, ForEachCallback<Integer, Boolean> callback) {
        int size = items.size();
        for(int i=0;i<size;++i) {
            callback.onItem(items.keyAt(i), items.valueAt(i));
        }
    }

    public static <E> void forEachSelectItem(SparseArray<E> items, ForEachCallback<Integer, E> callback) {
        int size = items.size();
        for(int i=0;i<size;++i) {
            callback.onItem(items.keyAt(i), items.valueAt(i));
        }
    }

    public static interface ForEachCallback<T, E> {
        void onItem(T key, E value);
    }
}

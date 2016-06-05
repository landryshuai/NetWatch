package info.noverguo.netwatch.utils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Created by noverguo on 2016/5/18.
 */
public class RxJavaUtils {
    public static <T> Observable<T> retry(Observable<T> src, final int count, final int sec) {
        return src.retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
            @Override
            public Observable<?> call(Observable<? extends Throwable> errors) {
                return errors.zipWith(Observable.range(1, count), new Func2<Throwable, Integer, Integer>() {
                    @Override
                    public Integer call(Throwable throwable, Integer i) {
                        return i;
                    }
                }).flatMap(new Func1<Integer, Observable<?>>() {
                    @Override
                    public Observable<? extends Long> call(Integer retryCount) {
                        return Observable.timer((long) Math.pow(sec, retryCount), TimeUnit.SECONDS);
                    }
                });
            }
        });
    }
    public static <T> Observable<T> io2AndroidMain(Observable<T> src) {
        return src.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public static <T> Observable<T> io2io(Observable<T> src) {
        return src.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

}

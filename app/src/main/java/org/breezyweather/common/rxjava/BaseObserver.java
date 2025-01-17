package org.breezyweather.common.rxjava;

import io.reactivex.rxjava3.observers.DisposableObserver;
import retrofit2.HttpException;
import org.breezyweather.BreezyWeather;

public abstract class BaseObserver<T> extends DisposableObserver<T> {

    protected Integer code;

    public abstract void onSucceed(T t);

    public abstract void onFailed();

    public Integer getStatusCode() {
        return code;
    }

    public Boolean isApiLimitReached() {
        return code != null && code == 429;
    }

    public Boolean isApiUnauthorized() {
        return code != null && code == 401;
    }

    @Override
    public void onNext(T t) {
        if (t == null) {
            onFailed();
        } else {
            onSucceed(t);
        }
    }

    @Override
    public void onError(Throwable e) {
        if (e instanceof HttpException) {
            this.code = ((HttpException) e).code();
        }
        if (BreezyWeather.getInstance().getDebugMode()) {
            e.printStackTrace();
        }
        onFailed();
    }

    @Override
    public void onComplete() {
        // do nothing.
    }
}
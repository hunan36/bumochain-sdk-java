package cn.bumo.access.utils.concurrent;

public interface AsyncFutureListener<TSource>{

    public void complete(AsyncFuture<TSource> future);

}

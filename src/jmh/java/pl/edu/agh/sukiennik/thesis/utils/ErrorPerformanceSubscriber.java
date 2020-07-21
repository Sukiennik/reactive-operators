package pl.edu.agh.sukiennik.thesis.utils;

import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import org.openjdk.jmh.infra.Blackhole;
import org.reactivestreams.Subscription;

public final class ErrorPerformanceSubscriber implements FlowableSubscriber<Object>, Observer<Object>,
        SingleObserver<Object>, CompletableObserver, MaybeObserver<Object> {

    private final Blackhole bh;

    public ErrorPerformanceSubscriber(Blackhole bh) {
        this.bh = bh;
    }

    @Override
    public void onSuccess(Object value) {
        bh.consume(value);
    }

    @Override
    public void onSubscribe(Disposable d) {
    }

    @Override
    public void onSubscribe(Subscription s) {
        s.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(Object t) {
        bh.consume(t);
    }

    @Override
    public void onError(Throwable t) { }

    @Override
    public void onComplete() {
        bh.consume(true);
    }
}

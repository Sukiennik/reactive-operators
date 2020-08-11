package pl.edu.agh.sukiennik.thesis.examples;

import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RxJavaExample {

    public void abstraction() {
        Maybe<String> noData = Maybe.empty();
        Single<String> singleString = Single.just("foo");

        List<String> iterable = Arrays.asList("one", "two", "three");
        Flowable<String> sequencerArray = Flowable.fromIterable(iterable);

        Flowable<Integer> sequenceOfNumbers = Flowable.range(0, 100);

        noData.subscribe();
        singleString.subscribe(string -> {/* handle result*/});
        //sequencerArray.subscribe(subscriberReference);
        sequenceOfNumbers.subscribe(
                result -> {/* handle result*/},
                throwable -> {/* handle error*/},
                () -> {/* handle completion*/});
    }

    public void composition() {
        FlowableTransformer<String, String> transformationOperator = flowable -> flowable
                .filter(color -> color.equalsIgnoreCase("one"))
                .map(String::toUpperCase);

        Flowable<String> sequence = Flowable.just("one", "two", "three");

        sequence = sequence.compose(transformationOperator);
        //sequence = sequence.lift(...);

        sequence.subscribe();
    }

    public void schedulers() {
        Flowable.interval(100, TimeUnit.MILLISECONDS, Schedulers.newThread()).subscribe();

        Flowable.range(1, 100)
                .filter(value -> value > 20)
                .map(value -> value * 2)
                .subscribeOn(Schedulers.io())
                .subscribe();

        Flowable.range(1, 100)
                .subscribeOn(Schedulers.computation())
                .filter(value -> value > 20)
                .map(value -> value * 2)
                .subscribeOn(Schedulers.io())
                .subscribe();

        Flowable.range(1, 100)
                .filter(value -> value > 20)
                .observeOn(Schedulers.computation())
                .map(value -> value * 2)
                .observeOn(Schedulers.io())
                .map(value -> value * 2)
                .subscribeOn(Schedulers.io())
                .subscribe();
    }
}

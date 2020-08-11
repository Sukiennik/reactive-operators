package pl.edu.agh.sukiennik.thesis.examples;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class ReactorExample {

    public void abstraction() {
        Mono<String> noData = Mono.empty();
        Mono<String> singleString = Mono.just("foo");

        List<String> iterable = Arrays.asList("one", "two", "three");
        Flux<String> sequencerArray = Flux.fromIterable(iterable);

        Flux<Integer> sequenceOfNumbers = Flux.range(0, 100);

        noData.subscribe();
        singleString.subscribe(string -> {/* handle result*/});
        //sequencerArray.subscribe(subscriberReference);
        sequenceOfNumbers.subscribe(
                result -> {/* handle result*/},
                throwable -> {/* handle error*/},
                () -> {/* handle completion*/},
                subscription -> subscription.request(10));
    }

    public void composition() {
        Function<Flux<String>, Publisher<String>> transformationOperator = flux -> flux
                .filter(color -> color.equalsIgnoreCase("one"))
                .map(String::toUpperCase);

        Flux<String> sequence = Flux.just("one", "two", "three");

        sequence = sequence.transform(transformationOperator);
        sequence = sequence.transformDeferred(transformationOperator);

        sequence.subscribe();
    }

    public void schedulers() {
        Flux.interval(Duration.ofMillis(100), Schedulers.single()).subscribe();

        Flux.range(1, 100)
                .filter(value -> value > 20)
                .map(value -> value * 2)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();

        Flux.range(1, 100)
                .subscribeOn(Schedulers.parallel())
                .filter(value -> value > 20)
                .map(value -> value * 2)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();

        Flux.range(1, 100)
                .filter(value -> value > 20)
                .publishOn(Schedulers.parallel())
                .map(value -> value * 2)
                .publishOn(Schedulers.boundedElastic())
                .map(value -> value * 2)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }
}

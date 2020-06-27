package pl.edu.agh.sukiennik.thesis.operators.filtering.ofType;

import io.reactivex.rxjava3.core.Flowable;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import pl.edu.agh.sukiennik.thesis.operators.PerformanceSubscriber;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class RxJavaOfType {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flowable<Number> singleOfType;

    @Setup
    public void setup() {
        Stream<Integer> streamOfInts = Arrays.stream(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        Stream<Double> streamOfDoubles = Arrays.stream(IntStream.rangeClosed(0, times).asDoubleStream().boxed().toArray(Double[]::new));
        singleOfType = Flowable.fromArray(Stream.concat(streamOfInts, streamOfDoubles).toArray(Number[]::new));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 1)
    public void singleOfType(Blackhole bh) {
        singleOfType
                .ofType(Double.class)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    public static void main(String[] args) {
        //RxJavaOfType ofTypeBenchmark = new RxJavaOfType();
        //ofTypeBenchmark.singleOfType();
    }

}


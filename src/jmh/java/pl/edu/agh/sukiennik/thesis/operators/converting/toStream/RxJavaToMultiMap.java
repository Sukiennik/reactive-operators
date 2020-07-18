package pl.edu.agh.sukiennik.thesis.operators.converting.toStream;

import io.reactivex.rxjava3.core.Flowable;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import pl.edu.agh.sukiennik.thesis.operators.PerformanceSubscriber;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class RxJavaToMultiMap {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flowable<Integer> singleToMultiMap;

    @Setup
    public void setup() {
        singleToMultiMap = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleToMultiMap(Blackhole bh) {
        try (Stream<Integer> stream = singleToMultiMap.blockingStream()) {
            long c = stream.count();
        }
    }

    public static void main(String[] args) {
        //RxJavaToMultiMap toMultiMapBenchmark = new RxJavaToMultiMap();
        //toMultiMapBenchmark.setup();
        //toMultiMapBenchmark.singleToMultiMap();
    }

}


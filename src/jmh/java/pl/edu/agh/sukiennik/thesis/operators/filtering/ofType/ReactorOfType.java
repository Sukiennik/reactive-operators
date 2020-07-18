package pl.edu.agh.sukiennik.thesis.operators.filtering.ofType;

import org.openjdk.jmh.annotations.*;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class ReactorOfType {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Number> singleOfType;

    @Setup
    public void setup() {
        Stream<Integer> streamOfInts = Arrays.stream(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        Stream<Double> streamOfDoubles = Arrays.stream(IntStream.rangeClosed(0, times).asDoubleStream().boxed().toArray(Double[]::new));
        singleOfType = Flux.fromArray(Stream.concat(streamOfInts, streamOfDoubles).toArray(Number[]::new));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleOfType() {
        singleOfType
                .ofType(Double.class)
                .then()
                .block();
    }

    public static void main(String[] args) {
        //ReactorOfType ofTypeBenchmark = new ReactorOfType();
        //ofTypeBenchmark.setup();
        //ofTypeBenchmark.singleOfType();
    }

}


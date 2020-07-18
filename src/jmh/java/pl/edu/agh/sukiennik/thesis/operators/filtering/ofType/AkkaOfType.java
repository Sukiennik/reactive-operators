package pl.edu.agh.sukiennik.thesis.operators.filtering.ofType;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaOfType {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Source<Number, NotUsed> singleOfType;
    private ActorSystem singleOfTypeSystem;

    @Setup
    public void setup() {
        Stream<Integer> streamOfInts = Arrays.stream(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        Stream<Double> streamOfDoubles = Arrays.stream(IntStream.rangeClosed(0, times).asDoubleStream().boxed().toArray(Double[]::new));
        singleOfType = Source.fromJavaStream(() -> Stream.concat(streamOfInts, streamOfDoubles));
        singleOfTypeSystem = ActorSystem.create("singleOfTypeSystem");
    }

    @TearDown
    public void cleanup() {
        singleOfTypeSystem.terminate();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleOfType() throws ExecutionException, InterruptedException {
        singleOfType
                .collectType(Double.class)
                .run(singleOfTypeSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //AkkaOfType ofTypeBenchmark = new AkkaOfType();
        //ofTypeBenchmark.setup();
        //ofTypeBenchmark.singleOfType();
        //ofTypeBenchmark.cleanup();
    }

}


package pl.edu.agh.sukiennik.thesis.operators.converting.toMap;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.StreamConverters;
import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.utils.ForcedGcMemoryProfiler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaToMap {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Source<Integer, NotUsed> singleToMap;
    private ActorSystem singleToMapSystem;

    @Setup
    public void setup() {
        singleToMap = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
        singleToMapSystem = ActorSystem.create("singleToMapSystem");
    }

    @TearDown
    public void cleanup() {
        singleToMapSystem.terminate();
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleToMap() throws ExecutionException, InterruptedException {
        singleToMap
                .runWith(StreamConverters.javaCollector(() -> Collectors.toMap(t -> t % 5, Function.identity(), (a, b) -> b)), singleToMapSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //AkkaToMap ToMapBenchmark = new AkkaToMap();
        //ToMapBenchmark.setup();
        //ToMapBenchmark.singleToMap();
    }
}


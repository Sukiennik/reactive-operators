package pl.edu.agh.sukiennik.thesis.operators.transforming.flatMap;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaFlatMap {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    @State(Scope.Thread)
    public static class SingleFlatMapState {
        private Source<String, NotUsed> characters;
        private Source<Integer, NotUsed> singleFlatMapSource;
        private ActorSystem singleFlatMapSystem;

        @Setup
        public void setup() {
            characters = Source.from(Arrays.asList("A", "B"));
            singleFlatMapSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            singleFlatMapSystem = ActorSystem.create("singleFlatMapSystem");
        }

        @TearDown
        public void cleanup() {
            singleFlatMapSystem.terminate();
        }
    }

    @State(Scope.Thread)
    public static class MultiFlatMapState {
        private Source<String, NotUsed> characters;
        private Source<Integer, NotUsed> multiFlatMapSource;
        private ActorSystem multiFlatMapSystem;

        @Setup
        public void setup() {
            characters = Source.from(Arrays.asList("A", "B"));
            multiFlatMapSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            multiFlatMapSystem = ActorSystem.create("multiFlatMapSystem");
        }

        @TearDown
        public void cleanup() {
            multiFlatMapSystem.terminate();
        }
    }

    @State(Scope.Thread)
    public static class MultiFlatMapEachOnIoState {
        private Source<String, NotUsed> characters;
        private Source<Integer, NotUsed> multiFlatMapEachOnIoSource;
        private ActorSystem multiFlatMapEachOnIoSystem;

        @Setup
        public void setup() {
            characters = Source.from(Arrays.asList("A", "B"));
            multiFlatMapEachOnIoSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            multiFlatMapEachOnIoSystem = ActorSystem.create("multiFlatMapEachOnIoSystem");
        }

        @TearDown
        public void cleanup() {
            multiFlatMapEachOnIoSystem.terminate();
        }
    }

    @Benchmark
    @Measurement(iterations = 5, time = 5)
    public void singleFlatMap() throws ExecutionException, InterruptedException {
        Source<Integer, NotUsed> singleFlatMapSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, 10));
        ActorSystem singleFlatMapSystem = ActorSystem.create("singleFlatMapSystem");

        singleFlatMapSource
                .flatMapMerge(4, param -> Source.from(Arrays.asList("A", "B")).map(param1 -> param1 + param))
                .run(singleFlatMapSystem)
                .toCompletableFuture()
                .get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 10)
    public void multiFlatMap(MultiFlatMapState state) throws ExecutionException, InterruptedException {
        Source<Integer, NotUsed> range = state.multiFlatMapSource;
        Source<String, NotUsed> results = null;
        for (int i = 0; i < 10; i++) {
            if(results == null) {
                results = state.multiFlatMapSource.flatMapMerge(4, integer -> state.characters.map(character -> character + integer.toString()));
            } else {
                results = results.flatMapMerge(4, string -> state.characters.map(character -> character + string));
            }
        }
        results.run(state.multiFlatMapSystem).toCompletableFuture().get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiFlatMapEachOnIo(MultiFlatMapEachOnIoState state) throws ExecutionException, InterruptedException {
        Source<Integer, NotUsed> range = state.multiFlatMapEachOnIoSource;
        Source<String, NotUsed> results = null;
        for (int i = 0; i < 10; i++) {
            if(results == null) {
                results = state.multiFlatMapEachOnIoSource.flatMapMerge(4, integer -> state.characters.map(character -> character + integer.toString())).async();
            } else {
                results = results.flatMapMerge(4, string -> state.characters.map(character -> character + string));
            }
        }
        results.run(state.multiFlatMapEachOnIoSystem).toCompletableFuture().get();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        AkkaFlatMap flatMapBenchmark = new AkkaFlatMap();
        flatMapBenchmark.singleFlatMap();
    }
}




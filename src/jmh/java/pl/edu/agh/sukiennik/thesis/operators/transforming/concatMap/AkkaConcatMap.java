package pl.edu.agh.sukiennik.thesis.operators.transforming.concatMap;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.utils.ForcedGcMemoryProfiler;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaConcatMap {

    @Param({"1", "1000", "100000", "1000000"})
    private static int times;

    @State(Scope.Thread)
    public static class SingleConcatMapState {
        private Source<String, NotUsed> characters;
        private Source<Integer, NotUsed> singleConcatMapSource;
        private ActorSystem singleConcatMapSystem;

        @Setup
        public void setup() {
            characters = Source.from(Arrays.asList("A", "B"));
            singleConcatMapSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            singleConcatMapSystem = ActorSystem.create("singleConcatMapSystem");
        }

        @TearDown
        public void cleanup() {
            singleConcatMapSystem.terminate();
        }

        @TearDown(Level.Iteration)
        public void cleanup2() {
            ForcedGcMemoryProfiler.recordUsedMemory();
        }
    }

    @State(Scope.Thread)
    public static class MultiConcatMapState {
        private Source<String, NotUsed> characters;
        private Source<Integer, NotUsed> multiConcatMapSource;
        private ActorSystem multiConcatMapSystem;

        @Setup
        public void setup() {
            characters = Source.from(Arrays.asList("A", "B"));
            multiConcatMapSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            multiConcatMapSystem = ActorSystem.create("multiConcatMapSystem");
        }

        @TearDown
        public void cleanup() {
            multiConcatMapSystem.terminate();
        }

        @TearDown(Level.Iteration)
        public void cleanup2() {
            ForcedGcMemoryProfiler.recordUsedMemory();
        }
    }

    @State(Scope.Thread)
    public static class MultiConcatMapEachOnIoState {
        private Source<String, NotUsed> characters;
        private Source<Integer, NotUsed> multiConcatMapEachOnIoSource;
        private ActorSystem multiConcatMapEachOnIoSystem;

        @Setup
        public void setup() {
            characters = Source.from(Arrays.asList("A", "B"));
            multiConcatMapEachOnIoSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            multiConcatMapEachOnIoSystem = ActorSystem.create("multiConcatMapEachOnIoSystem");
        }

        @TearDown
        public void cleanup() {
            multiConcatMapEachOnIoSystem.terminate();
        }

        @TearDown(Level.Iteration)
        public void cleanup2() {
            ForcedGcMemoryProfiler.recordUsedMemory();
        }
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleConcatMap(SingleConcatMapState state) throws ExecutionException, InterruptedException {
        state.singleConcatMapSource
                .flatMapConcat(param -> state.characters.map(character -> character + param.toString()))
                .run(state.singleConcatMapSystem)
                .toCompletableFuture()
                .get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiConcatMap(MultiConcatMapState state) throws ExecutionException, InterruptedException {
        Source<String, NotUsed> results = null;
        for (int i = 0; i < 10; i++) {
            if(results == null) {
                results = state.multiConcatMapSource.flatMapConcat(integer -> state.characters.map(character -> character + integer.toString()));
            } else {
                results = results.flatMapConcat(string -> state.characters.map(character -> character + string));
            }
        }
        state.characters.run(state.multiConcatMapSystem).toCompletableFuture().get();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiConcatMapEachOnIo(MultiConcatMapEachOnIoState state) throws ExecutionException, InterruptedException {
        Source<String, NotUsed> results = null;
        for (int i = 0; i < 10; i++) {
            if(results == null) {
                results = state.multiConcatMapEachOnIoSource.flatMapConcat(integer -> state.characters.map(character -> character + integer.toString())).async();
            } else {
                results = results.flatMapConcat(string -> state.characters.map(character -> character + string)).async();
            }
        }
        results.run(state.multiConcatMapEachOnIoSystem).toCompletableFuture().get();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //AkkaConcatMap concatMapBenchmark = new AkkaConcatMap();
        //concatMapBenchmark.singleConcatMap();
    }
}




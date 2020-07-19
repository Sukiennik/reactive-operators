package pl.edu.agh.sukiennik.thesis.operators.transforming.map;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.operators.ForcedGcMemoryProfiler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaMap {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    @State(Scope.Thread)
    public static class SingleMapState {
        private Source<Integer, NotUsed> singleMapSource;
        private ActorSystem singleMapSystem;

        @Setup
        public void setup() {
            singleMapSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            singleMapSystem = ActorSystem.create("singleMapSystem");
        }

        @TearDown
        public void cleanup() {
            singleMapSystem.terminate();
        }

        @TearDown(Level.Iteration)
        public void cleanup2() {
            ForcedGcMemoryProfiler.recordUsedMemory();
        }
    }

    @State(Scope.Thread)
    public static class MultiMapState {
        private Source<Integer, NotUsed> multiMapSource;
        private ActorSystem multiMapSystem;

        @Setup
        public void setup() {
            multiMapSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            multiMapSystem = ActorSystem.create("multiMapSystem");
        }

        @TearDown
        public void cleanup() {
            multiMapSystem.terminate();
        }

        @TearDown(Level.Iteration)
        public void cleanup2() {
            ForcedGcMemoryProfiler.recordUsedMemory();
        }
    }

    @State(Scope.Thread)
    public static class MultiMapEachOnIoState {
        private Source<Integer, NotUsed> multiMapEachOnIoSource;
        private ActorSystem multiMapEachOnIoSystem;

        @Setup
        public void setup() {
            multiMapEachOnIoSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            multiMapEachOnIoSystem = ActorSystem.create("multiMapEachOnIoSystem");
        }

        @TearDown
        public void cleanup() {
            multiMapEachOnIoSystem.terminate();
        }

        @TearDown(Level.Iteration)
        public void cleanup2() {
            ForcedGcMemoryProfiler.recordUsedMemory();
        }
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleMap(SingleMapState state) throws ExecutionException, InterruptedException {
        state.singleMapSource
                .map(element -> element + 1)
                .run(state.singleMapSystem)
                .toCompletableFuture()
                .get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiMap(MultiMapState state) throws ExecutionException, InterruptedException {
        Source<Integer, NotUsed> range = state.multiMapSource;
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            range = range.map(element -> element + finalI);
        }
        range.run(state.multiMapSystem).toCompletableFuture().get();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiMapEachOnIo(MultiMapEachOnIoState state) throws ExecutionException, InterruptedException {
        Source<Integer, NotUsed> range = state.multiMapEachOnIoSource;
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            range = range.map(element -> element + finalI).async();
        }
        range.run(state.multiMapEachOnIoSystem).toCompletableFuture().get();
    }

    public static void main(String[] args) {
        //AkkaMap mapBenchmark = new AkkaMap();
        //mapBenchmark.multiMapEachOnIo();
    }
}




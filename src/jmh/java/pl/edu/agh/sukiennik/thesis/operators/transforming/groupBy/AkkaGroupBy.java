package pl.edu.agh.sukiennik.thesis.operators.transforming.groupBy;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.utils.ForcedGcMemoryProfiler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaGroupBy {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    @State(Scope.Thread)
    public static class SingleGroupByState {
        private Source<Integer, NotUsed> singleGroupBySource;
        private ActorSystem singleGroupBySystem;

        @Setup
        public void setup() {
            singleGroupBySource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            singleGroupBySystem = ActorSystem.create("singleGroupBySystem");
        }

        @TearDown
        public void cleanup() {
            singleGroupBySystem.terminate();
        }

        @TearDown(Level.Iteration)
        public void cleanup2() {
            ForcedGcMemoryProfiler.recordUsedMemory();
        }
    }

    @State(Scope.Thread)
    public static class SingleGroupByThenFlattenIndexedState {
        private Source<Integer, NotUsed> singleGroupByThenFlattenIndexedSource;
        private ActorSystem singleGroupBySystem;

        @Setup
        public void setup() {
            singleGroupByThenFlattenIndexedSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            singleGroupBySystem = ActorSystem.create("singleGroupBySystem");
        }

        @TearDown
        public void cleanup() {
            singleGroupBySystem.terminate();
        }

        @TearDown(Level.Iteration)
        public void cleanup2() {
            ForcedGcMemoryProfiler.recordUsedMemory();
        }
    }

    @State(Scope.Thread)
    public static class SingleGroupByEachOnIoState {
        private Source<Integer, NotUsed> singleGroupByEachOnIoSource;
        private ActorSystem singleGroupByEachOnIoSystem;

        @Setup
        public void setup() {
            singleGroupByEachOnIoSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            singleGroupByEachOnIoSystem = ActorSystem.create("singleGroupByEachOnIoSystem");
        }

        @TearDown
        public void cleanup() {
            singleGroupByEachOnIoSystem.terminate();
        }

        @TearDown(Level.Iteration)
        public void cleanup2() {
            ForcedGcMemoryProfiler.recordUsedMemory();
        }
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleGroupBy(SingleGroupByState state) throws ExecutionException, InterruptedException {
        state.singleGroupBySource
                .groupBy(5, param -> param % 5)
                .mergeSubstreams()
                .run(state.singleGroupBySystem)
                .toCompletableFuture()
                .get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleGroupByThenFlattenIndexed(SingleGroupByThenFlattenIndexedState state) throws ExecutionException, InterruptedException {
        state.singleGroupByThenFlattenIndexedSource
                .groupBy(5, param -> param % 5)
                .zipWithIndex()
                .mergeSubstreams()
                .run(state.singleGroupBySystem)
                .toCompletableFuture()
                .get();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleGroupByOnIo(SingleGroupByEachOnIoState state) throws ExecutionException, InterruptedException {
        state.singleGroupByEachOnIoSource
                .groupBy(5, param -> param % 5).async()
                .mergeSubstreams()
                .run(state.singleGroupByEachOnIoSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //AkkaGroupBy groupByBenchmark = new AkkaGroupBy();
        //groupByBenchmark.singleGroupBy();
    }
}




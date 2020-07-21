package pl.edu.agh.sukiennik.thesis.operators.conditional.takeWhile;

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
public class AkkaTakeWhile {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    @State(Scope.Thread)
    public static class SingleTakeWhileState {
        private Source<Integer, NotUsed> singleTakeWhile;
        private ActorSystem singleTakeWhileSystem;

        @Setup
        public void setup() {
            singleTakeWhile = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            singleTakeWhileSystem = ActorSystem.create("singleTakeWhileSystem");
        }

        @TearDown
        public void cleanup() {
            singleTakeWhileSystem.terminate();
        }

        @TearDown(Level.Iteration)
        public void cleanup2() {
            ForcedGcMemoryProfiler.recordUsedMemory();
        }
    }

    @State(Scope.Thread)
    public static class MultiTakeWhileState {
        private Source<Integer, NotUsed> multiTakeWhileSource;
        private ActorSystem multiTakeWhileSystem;

        @Setup
        public void setup() {
            multiTakeWhileSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            multiTakeWhileSystem = ActorSystem.create("multiTakeWhileSystem");
        }

        @TearDown
        public void cleanup() {
            multiTakeWhileSystem.terminate();
        }

        @TearDown(Level.Iteration)
        public void cleanup2() {
            ForcedGcMemoryProfiler.recordUsedMemory();
        }
    }

    @State(Scope.Thread)
    public static class MultiTakeWhileEachOnIoState {
        private Source<Integer, NotUsed> multiTakeWhileEachOnIoSource;
        private ActorSystem multiTakeWhileEachOnIoSystem;

        @Setup
        public void setup() {
            multiTakeWhileEachOnIoSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            multiTakeWhileEachOnIoSystem = ActorSystem.create("multiTakeWhileEachOnIoSystem");
        }

        @TearDown
        public void cleanup() {
            multiTakeWhileEachOnIoSystem.terminate();
        }

        @TearDown(Level.Iteration)
        public void cleanup2() {
            ForcedGcMemoryProfiler.recordUsedMemory();
        }
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleTakeWhile(SingleTakeWhileState state) throws ExecutionException, InterruptedException {
        state.singleTakeWhile
                .takeWhile(val -> val <= times / 2)
                .run(state.singleTakeWhileSystem)
                .toCompletableFuture()
                .get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiTakeWhile(MultiTakeWhileState state) throws ExecutionException, InterruptedException {
        Source<Integer, NotUsed> range = state.multiTakeWhileSource;
        int condition = times;
        for (int i = 0; i < 10; i++) {
            condition = condition / 2;
            int finalCondition = condition;
            range = range.takeWhile(val -> val <= finalCondition);
        }
        range.run(state.multiTakeWhileSystem).toCompletableFuture().get();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiTakeWhileEachOnIo(MultiTakeWhileEachOnIoState state) throws ExecutionException, InterruptedException {
        Source<Integer, NotUsed> range = state.multiTakeWhileEachOnIoSource;
        int condition = times;
        for (int i = 0; i < 10; i++) {
            condition = condition / 2;
            int finalCondition = condition;
            range = range.takeWhile(val -> val <= finalCondition).async();
        }
        range.run(state.multiTakeWhileEachOnIoSystem).toCompletableFuture().get();
    }

    public static void main(String[] args) {
        //AkkaTakeWhile takeWhileBenchmark = new AkkaTakeWhile();
        //takeWhileBenchmark.singleTakeWhile();
    }
}


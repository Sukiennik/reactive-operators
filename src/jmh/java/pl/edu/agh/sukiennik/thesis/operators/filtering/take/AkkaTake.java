package pl.edu.agh.sukiennik.thesis.operators.filtering.take;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaTake {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    @State(Scope.Thread)
    public static class SingleTakeState {
        private Source<Integer, NotUsed> singleTake;
        private ActorSystem singleTakeSystem;

        @Setup
        public void setup() {
            singleTake = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            singleTakeSystem = ActorSystem.create("singleTakeSystem");
        }

        @TearDown
        public void cleanup() {
            singleTakeSystem.terminate();
        }
    }

    @State(Scope.Thread)
    public static class MultiTakeState {
        private Source<Integer, NotUsed> multiTakeSource;
        private ActorSystem multiTakeSystem;

        @Setup
        public void setup() {
            multiTakeSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            multiTakeSystem = ActorSystem.create("multiTakeSystem");
        }

        @TearDown
        public void cleanup() {
            multiTakeSystem.terminate();
        }
    }

    @State(Scope.Thread)
    public static class MultiTakeEachOnIoState {
        private Source<Integer, NotUsed> multiTakeEachOnIoSource;
        private ActorSystem multiTakeEachOnIoSystem;

        @Setup
        public void setup() {
            multiTakeEachOnIoSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            multiTakeEachOnIoSystem = ActorSystem.create("multiTakeEachOnIoSystem");
        }

        @TearDown
        public void cleanup() {
            multiTakeEachOnIoSystem.terminate();
        }
    }

    @Benchmark
    @Measurement(iterations = 5, time = 5)
    public void singleTake(SingleTakeState state) throws ExecutionException, InterruptedException {
        state.singleTake
                .take(times / 2)
                .run(state.singleTakeSystem)
                .toCompletableFuture()
                .get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 10)
    public void multiFilter(MultiTakeState state) throws ExecutionException, InterruptedException {
        Source<Integer, NotUsed> range = state.multiTakeSource;
        int elements = times;
        for (int i = 0; i < 10; i++) {
            elements = elements / 2;
            int takeCount = elements;
            range = range.take(takeCount);
        }
        range.run(state.multiTakeSystem).toCompletableFuture().get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiFilterEachOnIo(MultiTakeEachOnIoState state) throws ExecutionException, InterruptedException {
        Source<Integer, NotUsed> range = state.multiTakeEachOnIoSource;
        int elements = times;
        for (int i = 0; i < 10; i++) {
            elements = elements / 2;
            int takeCount = elements;
            range = range.take(takeCount).async();
        }
        range.run(state.multiTakeEachOnIoSystem).toCompletableFuture().get();
    }

    public static void main(String[] args) {
        //AkkaTake takeBenchmark = new AkkaTake();
        //takeBenchmark.singleTake();
    }
}


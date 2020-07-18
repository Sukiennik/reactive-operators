package pl.edu.agh.sukiennik.thesis.operators.filtering.takeLast;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaTakeLast {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    @State(Scope.Thread)
    public static class SingleTakeLastState {
        private Source<Integer, NotUsed> singleTakeLast;
        private ActorSystem singleTakeLastSystem;

        @Setup
        public void setup() {
            singleTakeLast = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            singleTakeLastSystem = ActorSystem.create("singleTakeLastSystem");
        }

        @TearDown
        public void cleanup() {
            singleTakeLastSystem.terminate();
        }
    }

    @State(Scope.Thread)
    public static class MultiTakeLastState {
        private Source<Integer, NotUsed> multiTakeLastSource;
        private ActorSystem multiTakeLastSystem;

        @Setup
        public void setup() {
            multiTakeLastSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            multiTakeLastSystem = ActorSystem.create("multiTakeLastSystem");
        }

        @TearDown
        public void cleanup() {
            multiTakeLastSystem.terminate();
        }
    }

    @State(Scope.Thread)
    public static class MultiTakeLastEachOnIoState {
        private Source<Integer, NotUsed> multiTakeLastEachOnIoSource;
        private ActorSystem multiTakeLastEachOnIoSystem;

        @Setup
        public void setup() {
            multiTakeLastEachOnIoSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            multiTakeLastEachOnIoSystem = ActorSystem.create("multiTakeLastEachOnIoSystem");
        }

        @TearDown
        public void cleanup() {
            multiTakeLastEachOnIoSystem.terminate();
        }
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleTakeLast(SingleTakeLastState state) throws ExecutionException, InterruptedException {
        state.singleTakeLast
                .runWith(Sink.takeLast(times / 3), state.singleTakeLastSystem)
                .toCompletableFuture()
                .get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiTakeLast(MultiTakeLastState state) throws ExecutionException, InterruptedException {
        Source<Integer, NotUsed> range = state.multiTakeLastSource;
        int elements = times;
        for (int i = 0; i < 10; i++) {
            elements = elements / 2;
            int takeLastCount = elements;
            if (takeLastCount > 0) {
                range = Source
                        .completionStage(range
                                .runWith(Sink.takeLast(takeLastCount), state.multiTakeLastSystem))
                        .mapConcat(e -> e);
            }
        }
        range.run(state.multiTakeLastSystem).toCompletableFuture().get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiTakeEachOnIo(MultiTakeLastEachOnIoState state) throws ExecutionException, InterruptedException {
        Source<Integer, NotUsed> range = state.multiTakeLastEachOnIoSource;
        int elements = times;
        for (int i = 0; i < 10; i++) {
            elements = elements / 2;
            int takeLastCount = elements;
            if (takeLastCount > 0) {
                range = Source
                        .completionStage(range
                                .runWith(Sink.takeLast(takeLastCount), state.multiTakeLastEachOnIoSystem))
                        .mapConcat(e -> e)
                        .async();
            }
        }
        range.run(state.multiTakeLastEachOnIoSystem).toCompletableFuture().get();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //AkkaTakeLast takeLastBenchmark = new AkkaTakeLast();
        //takeLastBenchmark.multiFilter();
    }
}


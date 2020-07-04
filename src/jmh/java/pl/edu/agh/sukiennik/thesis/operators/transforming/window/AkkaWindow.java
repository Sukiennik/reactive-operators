package pl.edu.agh.sukiennik.thesis.operators.transforming.window;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
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
public class AkkaWindow {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    @State(Scope.Thread)
    public static class SingleWindowState {
        private Source<Integer, NotUsed> singleWindowSource;
        private ActorSystem singleWindowSystem;

        @Setup
        public void setup() {
            singleWindowSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            singleWindowSystem = ActorSystem.create("singleWindowSystem");
        }

        @TearDown
        public void cleanup() {
            singleWindowSystem.terminate();
        }
    }

    @State(Scope.Thread)
    public static class SingleWindowThenFlattenIndexedState {
        private Source<Integer, NotUsed> singleWindowThenFlattenIndexedSource;
        private ActorSystem singleWindowThenFlattenIndexedSystem;

        @Setup
        public void setup() {
            singleWindowThenFlattenIndexedSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            singleWindowThenFlattenIndexedSystem = ActorSystem.create("singleWindowThenFlattenIndexedSystem");
        }

        @TearDown
        public void cleanup() {
            singleWindowThenFlattenIndexedSystem.terminate();
        }
    }

    @State(Scope.Thread)
    public static class MultiWindowState {
        private Source<Integer, NotUsed> multiWindowSource;
        private ActorSystem multiWindowSystem;

        @Setup
        public void setup() {
            multiWindowSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            multiWindowSystem = ActorSystem.create("multiWindowSystem");
        }

        @TearDown
        public void cleanup() {
            multiWindowSystem.terminate();
        }
    }

    @State(Scope.Thread)
    public static class MultiWindowEachOnIoState {
        private Source<Integer, NotUsed> multiWindowEachOnIoSource;
        private ActorSystem multiWindowEachOnIoSystem;

        @Setup
        public void setup() {
            multiWindowEachOnIoSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            multiWindowEachOnIoSystem = ActorSystem.create("multiWindowEachOnIoSystem");
        }

        @TearDown
        public void cleanup() {
            multiWindowEachOnIoSystem.terminate();
        }
    }

    @Benchmark
    @Measurement(iterations = 5, time = 5)
    public void singleWindow(SingleWindowState state) throws ExecutionException, InterruptedException {
        state.singleWindowSource
                .splitWhen(param -> param != 0 && param % 5 == 0)
                .mergeSubstreams()
                .run(state.singleWindowSystem)
                .toCompletableFuture()
                .get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 5)
    public void singleWindowThenFlattenIndexed(SingleWindowThenFlattenIndexedState state) throws ExecutionException, InterruptedException {
        state.singleWindowThenFlattenIndexedSource
                .splitWhen(param -> param != 0 && param % 5 == 0)
                .zipWithIndex()
                .mergeSubstreams()
                .run(state.singleWindowThenFlattenIndexedSystem)
                .toCompletableFuture()
                .get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 10)
    public void multiWindow(MultiWindowState state) throws ExecutionException, InterruptedException {
        state.multiWindowSource
                .splitWhen(param -> param != 0 && param % 5 == 0)
                .mergeSubstreams()
                .splitWhen(param -> param != 0 && param % 5 == 0)
                .mergeSubstreams()
                .splitWhen(param -> param != 0 && param % 5 == 0)
                .mergeSubstreams()
                .splitWhen(param -> param != 0 && param % 5 == 0)
                .mergeSubstreams()
                .splitWhen(param -> param != 0 && param % 5 == 0)
                .mergeSubstreams()
                .splitWhen(param -> param != 0 && param % 5 == 0)
                .mergeSubstreams()
                .splitWhen(param -> param != 0 && param % 5 == 0)
                .mergeSubstreams()
                .splitWhen(param -> param != 0 && param % 5 == 0)
                .mergeSubstreams()
                .splitWhen(param -> param != 0 && param % 5 == 0)
                .mergeSubstreams()
                .splitWhen(param -> param != 0 && param % 5 == 0)
                .mergeSubstreams()
                .splitWhen(param -> param != 0 && param % 5 == 0)
                .mergeSubstreams()
                .run(state.multiWindowSystem).toCompletableFuture().get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiWindowEachOnIo(MultiWindowEachOnIoState state) throws ExecutionException, InterruptedException {
        state.multiWindowEachOnIoSource
                .splitWhen(param -> param != 0 && param % 5 == 0).async()
                .mergeSubstreams()
                .splitWhen(param -> param != 0 && param % 5 == 0).async()
                .mergeSubstreams()
                .splitWhen(param -> param != 0 && param % 5 == 0).async()
                .mergeSubstreams()
                .splitWhen(param -> param != 0 && param % 5 == 0).async()
                .mergeSubstreams()
                .splitWhen(param -> param != 0 && param % 5 == 0).async()
                .mergeSubstreams()
                .splitWhen(param -> param != 0 && param % 5 == 0).async()
                .mergeSubstreams()
                .splitWhen(param -> param != 0 && param % 5 == 0).async()
                .mergeSubstreams()
                .splitWhen(param -> param != 0 && param % 5 == 0).async()
                .mergeSubstreams()
                .splitWhen(param -> param != 0 && param % 5 == 0).async()
                .mergeSubstreams()
                .splitWhen(param -> param != 0 && param % 5 == 0).async()
                .mergeSubstreams()
                .run(state.multiWindowEachOnIoSystem).toCompletableFuture().get();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //AkkaWindow windowBenchmark = new AkkaWindow();
        //SingleWindowState state = new SingleWindowState();
        //state.setup();
        //windowBenchmark.singleWindow(state);
    }
}




package pl.edu.agh.sukiennik.thesis.operators.filtering.skipWhile;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class AkkaDropWhile {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    @State(Scope.Thread)
    public static class SingleSkipWhileState {
        private Source<Integer, NotUsed> singleSkipWhile;
        private ActorSystem singleSkipWhileSystem;

        @Setup
        public void setup() {
            singleSkipWhile = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            singleSkipWhileSystem = ActorSystem.create("singleSkipWhileSystem");
        }

        @TearDown
        public void cleanup() {
            singleSkipWhileSystem.terminate();
        }
    }

    @State(Scope.Thread)
    public static class MultiSkipWhileState {
        private Source<Integer, NotUsed> multiSkipWhileSource;
        private ActorSystem multiSkipWhileSystem;

        @Setup
        public void setup() {
            multiSkipWhileSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            multiSkipWhileSystem = ActorSystem.create("multiSkipWhileSystem");
        }

        @TearDown
        public void cleanup() {
            multiSkipWhileSystem.terminate();
        }
    }

    @State(Scope.Thread)
    public static class MultiSkipWhileEachOnIoState {
        private Source<Integer, NotUsed> multiSkipWhileEachOnIoSource;
        private ActorSystem multiSkipWhileEachOnIoSystem;

        @Setup
        public void setup() {
            multiSkipWhileEachOnIoSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            multiSkipWhileEachOnIoSystem = ActorSystem.create("multiSkipWhileEachOnIoSystem");
        }

        @TearDown
        public void cleanup() {
            multiSkipWhileEachOnIoSystem.terminate();
        }
    }

    @Benchmark
    @Measurement(iterations = 5, time = 5)
    public void singleSkipWhile(SingleSkipWhileState state) throws ExecutionException, InterruptedException {
        state.singleSkipWhile
                .dropWhile(val -> val <= times / 2)
                .run(state.singleSkipWhileSystem)
                .toCompletableFuture()
                .get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 10)
    public void multiSkipWhile(MultiSkipWhileState state) throws ExecutionException, InterruptedException {
        Source<Integer, NotUsed> range = state.multiSkipWhileSource;
        int condition = times;
        for (int i = 0; i < 10; i++) {
            condition = condition / 2;
            int finalCondition = condition;
            range = range.dropWhile(val -> val <= finalCondition);
        }
        range.run(state.multiSkipWhileSystem).toCompletableFuture().get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiSkipWhileEachOnIo(MultiSkipWhileEachOnIoState state) throws ExecutionException, InterruptedException {
        Source<Integer, NotUsed> range = state.multiSkipWhileEachOnIoSource;
        int condition = times;
        for (int i = 0; i < 10; i++) {
            condition = condition / 2;
            int finalCondition = condition;
            range = range.dropWhile(val -> val <= finalCondition).async();
        }
        range.run(state.multiSkipWhileEachOnIoSystem).toCompletableFuture().get();
    }

    public static void main(String[] args) {
        //AkkaSkipWhile skipWhileBenchmark = new AkkaSkipWhile();
        //skipWhileBenchmark.singleSkipWhile();
    }
}


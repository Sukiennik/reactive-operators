package pl.edu.agh.sukiennik.thesis.operators.combining.startWith;

import akka.NotUsed;
import akka.actor.ActorSystem;
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
public class AkkaStartWith {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;
    
    @State(Scope.Thread)
    public static class SingleStartWithState {
        private Source<String, NotUsed> singleStartWithSource;
        private Source<String, NotUsed> startWithSource;
        private ActorSystem singleStartWithSystem;

        @Setup
        public void setup() {
            singleStartWithSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times).mapToObj(String::valueOf));
            startWithSource = Source.fromJavaStream(() -> IntStream.rangeClosed(times, times * 2 / 3).mapToObj(String::valueOf));
            singleStartWithSystem = ActorSystem.create("singleStartWithSystem");
        }

        @TearDown
        public void cleanup() {
            singleStartWithSystem.terminate();
        }
    }

    @State(Scope.Thread)
    public static class MultiStartWithState {
        private Source<String, NotUsed> multiStartWithSource;
        private Source<String, NotUsed> startWithSource;
        private ActorSystem multiStartWithSystem;

        @Setup
        public void setup() {
            multiStartWithSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times).mapToObj(String::valueOf));
            startWithSource = Source.fromJavaStream(() -> IntStream.rangeClosed(times, times * 2 / 3).mapToObj(String::valueOf));
            multiStartWithSystem = ActorSystem.create("multiStartWithSystem");
        }

        @TearDown
        public void cleanup() {
            multiStartWithSystem.terminate();
        }
    }

    @State(Scope.Thread)
    public static class MultiStartWithEachOnIoState {
        private Source<String, NotUsed> multiStartWithEachOnIoSource;
        private Source<String, NotUsed> startWithSource;
        private ActorSystem multiStartWithEachOnIoSystem;

        @Setup
        public void setup() {
            multiStartWithEachOnIoSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times).mapToObj(String::valueOf));
            startWithSource = Source.fromJavaStream(() -> IntStream.rangeClosed(times, times * 2 / 3).mapToObj(String::valueOf));
            multiStartWithEachOnIoSystem = ActorSystem.create("multiStartWithEachOnIoSystem");
        }

        @TearDown
        public void cleanup() {
            multiStartWithEachOnIoSystem.terminate();
        }
    }
    

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleStartWith(SingleStartWithState state) throws ExecutionException, InterruptedException {
        state.singleStartWithSource
                .prepend(state.startWithSource)
                .run(state.singleStartWithSystem)
                .toCompletableFuture()
                .get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiStartWith(MultiStartWithState state) throws ExecutionException, InterruptedException {
        Source<String, NotUsed> range = state.multiStartWithSource;
        for (int i = 0; i < 10; i++) {
            range = range.prepend(state.startWithSource);
        }
        range.run(state.multiStartWithSystem).toCompletableFuture().get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiStartWithEachOnIo(MultiStartWithEachOnIoState state) throws ExecutionException, InterruptedException {
        Source<String, NotUsed> range = state.multiStartWithEachOnIoSource;
        for (int i = 0; i < 10; i++) {
            range = range.prepend(state.startWithSource).async();
        }
        range.run(state.multiStartWithEachOnIoSystem).toCompletableFuture().get();
    }


    public static void main(String[] args) {
        //AkkaStartWith startWithBenchmark = new AkkaStartWith();
        //startWithBenchmark.singleStartWith();
    }

}


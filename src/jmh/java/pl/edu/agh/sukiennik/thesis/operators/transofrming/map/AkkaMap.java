package pl.edu.agh.sukiennik.thesis.operators.transofrming.map;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaMap {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    @State(Scope.Thread)
    public static class SingleMapState {
        private ActorSystem singleMapSystem;

        @Setup
        public void setup() {
            singleMapSystem = ActorSystem.create("singleMapSystem");
        }

        @TearDown
        public void cleanup() {
            singleMapSystem.terminate();
        }
    }

    @State(Scope.Thread)
    public static class MultiMapState {
        private ActorSystem multiMapSystem;

        @Setup
        public void setup() {
            multiMapSystem = ActorSystem.create("multiMapSystem");
        }

        @TearDown
        public void cleanup() {
            multiMapSystem.terminate();
        }
    }

    @State(Scope.Thread)
    public static class MultiMapEachOnIoState {
        private ActorSystem multiMapEachOnIoSystem;

        @Setup
        public void setup() {
            multiMapEachOnIoSystem = ActorSystem.create("multiMapEachOnIoSystem");
        }

        @TearDown
        public void cleanup() {
            multiMapEachOnIoSystem.terminate();
        }
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 5)
    public void singleMap(SingleMapState state) {
        Source.range(1, times)
                .map(element -> element + 1)
                .run(state.singleMapSystem);
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 10)
    public void multiMap(MultiMapState state) throws ExecutionException, InterruptedException {
        Source<Integer, NotUsed> range = Source.range(1, times);
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            range = range.map(element -> element + finalI);
        }
        range.run(state.multiMapSystem).toCompletableFuture().get();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiMapEachOnIo(MultiMapEachOnIoState state) {
        Source<Integer, NotUsed> range = Source.range(1, times);
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            range = range.map(element -> element + finalI).async();
        }
        range.run(state.multiMapEachOnIoSystem);
    }

    public static void main(String[] args) {
        //AkkaMap mapBenchmark = new AkkaMap();
        //mapBenchmark.multiMapEachOnIo();
    }
}




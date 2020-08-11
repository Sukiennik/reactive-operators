package pl.edu.agh.sukiennik.thesis.examples;

import akka.NotUsed;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.FlowShape;
import akka.stream.Materializer;
import akka.stream.UniformFanInShape;
import akka.stream.UniformFanOutShape;
import akka.stream.javadsl.*;
import akka.util.Timeout;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class AkkaExample {


    public void abstraction() {
        ActorSystem system = ActorSystem.create("system");
        Materializer materializer = Materializer.createMaterializer(system);

        Source<String, NotUsed> noData = Source.empty();
        Source<String, NotUsed> singleString = Source.single("foo");

        List<String> iterable = Arrays.asList("one", "two", "three");
        Source<String, NotUsed> sequencerArray = Source.from(iterable);

        Source<Integer, NotUsed> sequenceOfNumbers = Source.range(0, 100);

        noData.run(system);
        singleString.runForeach(string -> {/* handle result*/}, system);
        sequencerArray.runWith(Sink.foreach(param -> {/* handle result*/}), materializer);
        sequenceOfNumbers.runWith(Sink.head(), system);
    }

    public void askPattern() {
        ActorSystem system = ActorSystem.create("system");
        Timeout askTimeout = Timeout.create(Duration.ofSeconds(5));

        Source<Integer, NotUsed> numbers = Source.from(Arrays.asList(1, 2, 3));

        ActorRef ref = system.actorOf(Props.create(SimpleActor.class));
        numbers
                .ask(5, ref, Integer.class, askTimeout)
                .map(number -> number / 2)
                .run(system);
    }

    static class SimpleActor extends AbstractActor {
        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(
                            Integer.class,
                            number -> {
                                Integer reply = number * 10;
                                getSender().tell(reply, getSelf());
                            })
                    .build();
        }
    }


    private static class PartOfWork {
    }

    private static class HalfDoneWork {
    }

    private static class CompleteWork {
    }

    public void parallel() {
        ActorSystem system = ActorSystem.create("system");
        Source<PartOfWork, NotUsed> workSource =  Source.from(Arrays.asList(new PartOfWork(), new PartOfWork()));

        Flow<PartOfWork, HalfDoneWork, NotUsed> partialWork =
                Flow.of(PartOfWork.class).map(batter -> new HalfDoneWork());

        Flow<HalfDoneWork, CompleteWork, NotUsed> remainingWork =
                Flow.of(HalfDoneWork.class).map(halfCooked -> new CompleteWork());

        Flow<PartOfWork, CompleteWork, NotUsed> allWork = partialWork.async().via(remainingWork.async());

        workSource.via(allWork).run(system);

        @SuppressWarnings("unchecked")
        Flow<PartOfWork, CompleteWork, NotUsed> parallelPipelinedWork =
                Flow.fromGraph(
                        GraphDSL.create(
                                block -> {
                                    final UniformFanInShape<CompleteWork, CompleteWork> mergeWork =
                                            block.add(Merge.create(2));
                                    final UniformFanOutShape<PartOfWork, PartOfWork> partWorkDispatch =
                                            block.add(Balance.create(2));

                                    block.from(partWorkDispatch.out(0))
                                            .via(block.add(partialWork.async()))
                                            .via(block.add(remainingWork.async()))
                                            .toInlet(mergeWork.in(0));

                                    block.from(partWorkDispatch.out(1))
                                            .via(block.add(partialWork.async()))
                                            .via(block.add(remainingWork.async()))
                                            .toInlet(mergeWork.in(1));

                                    return FlowShape.of(partWorkDispatch.in(), mergeWork.out());
                                }));

        workSource.via(parallelPipelinedWork).run(system);

    }
}

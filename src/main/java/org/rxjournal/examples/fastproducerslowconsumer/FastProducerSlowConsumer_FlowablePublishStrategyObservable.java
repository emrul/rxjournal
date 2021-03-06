package org.rxjournal.examples.fastproducerslowconsumer;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import java.io.IOException;

/**
 * The problem here is that the items could get lost on a crash or you could run out of memory.
 * PublishSubject without a Flowable will throw an exception
 */
public class FastProducerSlowConsumer_FlowablePublishStrategyObservable {

    public static void main(String[] args) throws IOException {
        SlowConsumerObserver slowMarketDataConsumer = new SlowConsumerObserver("MKT1", 1000);

        Subject<MarketData> marketDataSubject = PublishSubject.create();
        FastProducer marketDataFastProducer = new FastProducer("MKT1", marketDataSubject);
        marketDataFastProducer.startPublishing(1);

        //Back pressure causes the program to fail if PublisherSubject
        //marketDataSubject.observeOn(Schedulers.io()).subscribe(slowMarketDataConsumer);
        marketDataSubject.subscribe(slowMarketDataConsumer);
    }
}

import Consumer.FileConsumer;
import Producer.FileProducer;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Starter {
    private static final Logger logger = LoggerFactory.getLogger(Starter.class);

    public static void main(String[] args) throws InterruptedException {
        if (args.length != 3) {
            System.err.println("arguments: (1) filename, (2) dir path, (3) partitions number");
            System.exit(1);
        }

        logger.debug("args : {}", Arrays.asList(args));

        // make init partitions
        final List<BlockingQueue<String>> partitions = new ArrayList<>();
        for (int i = 0; i <= Integer.parseInt(args[2]); i++) {
            partitions.add(new LinkedBlockingQueue<>());
        }

        // producer thread
        Executors.newSingleThreadExecutor().execute(new FileProducer(partitions, args[0]));

        final int processors = Runtime.getRuntime().availableProcessors();
        logger.debug("available processors : {}", processors);

        // consumer threads
        final ExecutorService consumers = Executors.newFixedThreadPool(processors);
        partitions.forEach(partition -> consumers.execute(new FileConsumer(partition, args[1])));

        consumers.shutdown();

        while(!consumers.isTerminated()) {
            logger.debug("terminating consumers..");
            Thread.sleep(2000);
        }

        logger.debug("terminated");
    }
}

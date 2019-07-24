package Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FileConsumer implements Consumer {
    private static final Logger logger = LoggerFactory.getLogger(FileConsumer.class);

    public static AtomicInteger duplicatedCounter = new AtomicInteger(0);
    public static AtomicInteger consumeCounter = new AtomicInteger(0);

    private final Map<String, BufferedWriter> writers = new HashMap<>();
    private final Set<String> words = new HashSet<>();
    private BlockingQueue<String> partition;
    private String outputDirPath;

    public FileConsumer(BlockingQueue<String> partition, String outputDirPath) {
        this.partition = partition;
        this.outputDirPath = outputDirPath;
    }

    @Override
    public void run() {
        try {
            String word;
            while ((word = partition.poll(1000, TimeUnit.MILLISECONDS)) != null) {
                if (words.contains(word.toLowerCase())) {
                    logger.debug("duplicated word : {} ", word);
                    duplicatedCounter.incrementAndGet();
                    continue;
                }
                words.add(word.toLowerCase());
                consumeCounter.incrementAndGet();

                String prefix = String.valueOf(word.charAt(0) >= '0' && word.charAt(0) <= '9' ? "number" : word.charAt(0)).toLowerCase();

                BufferedWriter writer = writers.computeIfAbsent(prefix, r -> {
                    try {
                        return new BufferedWriter(new FileWriter(outputDirPath + File.separator + r + ".txt"));
                    } catch (IOException e) {
                        logger.error("error occurred on writer path {}", outputDirPath + File.separator + r + ".txt");
                        throw new RuntimeException(e);
                    }
                });

                writer.write(word + '\n');
            }

            for (BufferedWriter writer : writers.values()) {
                writer.close();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

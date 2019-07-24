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

public class FileConsumer implements Consumer {
    private static final Logger logger = LoggerFactory.getLogger(FileConsumer.class);

    private BlockingQueue<String> partition;
    private String outputDirPath;

    public FileConsumer(BlockingQueue<String> partition, String outputDirPath) {
        this.partition = partition;
        this.outputDirPath = outputDirPath;
    }

    @Override
    public void run() {
        try {
            final Map<String, BufferedWriter> writers = new HashMap<>();
            final Set<String> duplicated = new HashSet<>();
            String word;
            while ((word = partition.poll(3000, TimeUnit.MILLISECONDS)) != "EOF") {
                if (word == null) {
                    break;
                }

                if (duplicated.contains(word.toLowerCase())) {
                    logger.debug("duplicated word : {} ", word);
                    continue;
                }
                duplicated.add(word.toLowerCase());

                String prefix = String.valueOf(word.charAt(0) >= '0' && word.charAt(0) <= '9' ? "number" : word.charAt(0)).toLowerCase();
                BufferedWriter writer = writers.computeIfAbsent(prefix, r -> {
                    try {
                        return new BufferedWriter(new FileWriter(outputDirPath + File.separator + r + ".txt"));
                    } catch (IOException e) {
                       throw new RuntimeException(e);
                    }
                });

                writer.write(word + '\n');
            }

            writers.forEach((key , value) -> {
                try {
                    value.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

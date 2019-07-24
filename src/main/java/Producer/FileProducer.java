package Producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileProducer implements Producer {
    private static final String REGEX = "^(\\d|[a-zA-z])";
    private static final Logger logger = LoggerFactory.getLogger(FileProducer.class);

    private List<BlockingQueue<String>> partitions;
    private String inputFilePath;

    public FileProducer(List<BlockingQueue<String>> partitions, String inputFilePath) {
        this.partitions = partitions;
        this.inputFilePath = inputFilePath;
    }

    @Override
    public void produce() {
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilePath)))) {
            final Pattern pattern = Pattern.compile(REGEX, Pattern.MULTILINE);
            final List<String> produced = br.lines().parallel().filter(l -> pattern.matcher(l).find()).collect(Collectors.toList());

            for (String word : produced) {
                final int index = word.charAt(0) >= '0' && word.charAt(0) <= '9' ? 0 : (String.valueOf(word.charAt(0)).toLowerCase().charAt(0) - 'a') % partitions.size();
                partitions.get(index).put(word);
            }

            logger.debug("produced size : " + produced.size());

        } catch (FileNotFoundException e) {
            logger.error("file path : {}", inputFilePath);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            logger.error("current thread : {}", Thread.currentThread().getName());
            throw new RuntimeException(e);
        } catch (IOException e) {
            logger.error("file path : {}", inputFilePath);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        produce();
    }
}

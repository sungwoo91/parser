package Producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public void run() {

        try {
            final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilePath)));
            final Pattern pattern = Pattern.compile(REGEX, Pattern.MULTILINE);

            List<String> list = br.lines().parallel().filter(l -> pattern.matcher(l).find()).collect(Collectors.toList());
            logger.debug("produced size : " + list.size());

            for (String word : list) {
                int index = word.charAt(0) >= '0' && word.charAt(0) <= '9' ? 0 : (String.valueOf(word.charAt(0)).toLowerCase().charAt(0) - 'a') % 20;
                partitions.get(index).put(word);
            }

            for (BlockingQueue<String> partition : partitions) {
                partition.put("EOF");
            }

            br.close();

            logger.debug("end producer");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

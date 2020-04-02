package story.kafka.producer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Partition {
    public static void main(String[] args) {
        List<String> keys = Arrays.asList("Hello", "hadoop", "map", "reduce");
        Collections.sort(keys);
        keys.stream().forEach(key -> {
            String format = "key %s, hashcode %d, partition %d";
            System.out.println(String.format(format, key, key.hashCode(), Math.abs(key.hashCode()) % 2));
        });
    }
}

//    key Hello, hashcode 69609650, partition 0
//    key hadoop, hashcode -1224864731, partition 1
//    key map, hashcode 107868, partition 0
//    key reduce, hashcode -934873754, partition 0

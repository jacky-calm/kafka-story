package story.kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class StoryProducer {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "kafka.cluster:9092");
//        props.put("broker.address.family", "v4");
        /**
         * The acks config controls the criteria under which requests are considered complete.
         * The "all" setting we have specified will result in blocking on the full commit of the record,
         * the slowest but most durable setting.
         */
        props.put("acks", "all");
        /**
         * how to turn the key and value objects the user provides with their ProducerRecord into bytes.
         */
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        // The producer is thread safe and sharing a single producer instance across threads will generally be faster than having multiple instances.
        Producer<String, String> producer = new KafkaProducer<>(props);
        for (int i = 0; i < 10; i++) {
            String topic = "kafka-story";
            String key = Integer.toString(i);
            String value = Integer.toString(i);
            ProducerRecord<String, String> message = new ProducerRecord<>(topic, key, value);
            // The send() method is asynchronous.
            // This allows the producer to batch together individual records for efficiency.
            producer.send(message);
            /**
             * If the request fails, the producer can automatically retry,
             * though since we have specified retries as 0 it won't.
             * Enabling retries also opens up the possibility of duplicates
             * The idempotent producer promises exactly once delivery. enable.idempotence == retries=Integer.MAX_VALUE && acks=all
             * The transactional producer allows an application to send messages to multiple partitions (and topics!) atomically.
             */
        }
        // Failure to close the producer after use will leak these resources.
        producer.close();
    }
}

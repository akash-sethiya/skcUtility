package com.akash.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

public class KafkaConsumer extends Thread {
    final static String clientId="consumerClient";
    String topic;
    ConsumerConnector consumerConnector;
    private static Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);
    
    public KafkaConsumer(String zkHost,String groupId,String offsetReset,String topic,String rebalanceBackOffTime){
        logger.info("setting up some properties for kafkaConsumer");
        Properties properties=null;
        try{
            properties = new Properties();
            properties.put("zookeeper.connect",zkHost);
            properties.put("group.id",groupId);
            properties.put("auto.offset.reset", offsetReset);
            properties.put("rebalance.backoff.ms",rebalanceBackOffTime);
            properties.put("rebalance.max.retries","10");
            
        }
        catch(Exception e){
            logger.error("Exception occured during setting up the properties");
        }
        ConsumerConfig consumerConfig = new ConsumerConfig(properties);
        
        consumerConnector = Consumer.createJavaConsumerConnector(consumerConfig);
        this.topic=topic;
    }
    
    @Override
    public void run() {
        logger.debug("Starting Kafka Consumer");
        Map<String,Integer> topicCountMap = new HashMap<String,Integer>();
        topicCountMap.put(topic,new Integer(1));
        
        Map<String,List<KafkaStream<byte[],byte[]>>> consumerMap=consumerConnector.createMessageStreams(topicCountMap);
        KafkaStream<byte[],byte[]> stream = consumerMap.get(topic).get(0);
        ConsumerIterator<byte[], byte[]> iterator=stream.iterator();
        while(iterator.hasNext()){
            logger.info("TUPLE FETCHED:"+new String(iterator.next().message()));
        }
    }

}

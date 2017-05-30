package com.akash.topology;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.thrift7.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;
import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;

import com.akash.bolts.CassandraBolt;
import com.akash.kafka.KafkaConsumer;


public class Topology {
    private static String ZK_HOST;
    private static String TOPIC;
    private static String CONSUMER_GROUP_ID;
    private static String OFFSET_RESET;
    private static String CASSANDRA_NODE_IP;
    private static Integer CASSANDRA_PORT;
    private static String SPOUT_NAME;
    private static String BOLT_NAME;
    private static String TOPOLOGY_NAME;
    private static String REBALANCE_BACKOFF_TIME;
    
    private final static String propertyFileName="config.properties";
    
    private static Logger logger = LoggerFactory.getLogger(Topology.class);
    
    public static void main(String args[]) throws AlreadyAliveException, TException, InterruptedException{
        try{
            logger.debug("Setting the configuration of hosts from property file.");
            setProperties(propertyFileName);
        }
        catch(IOException e){
            logger.error("IOException occured during configure the hosts from property file.\nERROR:"+e);
            e.printStackTrace();
        }
        catch(Exception e){
            logger.error("Exception occured during configure the hosts from property file.\nERROR:"+e);
            e.printStackTrace();
        }
        
        ZkHosts zookeeperHost=new ZkHosts(ZK_HOST);
        SpoutConfig kafkaConfig=new SpoutConfig(zookeeperHost, TOPIC, "", CONSUMER_GROUP_ID);
        kafkaConfig.forceFromStart=true;
        kafkaConfig.socketTimeoutMs=10000;
        kafkaConfig.scheme=new SchemeAsMultiScheme(new StringScheme());
        KafkaSpout kafkaSpout=new KafkaSpout(kafkaConfig);
        
        KafkaConsumer consumer = new KafkaConsumer(ZK_HOST, CONSUMER_GROUP_ID, OFFSET_RESET, TOPIC,REBALANCE_BACKOFF_TIME);
        try{
            logger.debug("starting the consumer to fetch data from producer.");
            logger.info("Consumer Details:\n ZOOKEEPER HOST:"+ZK_HOST+"\nCONSUMER GROUP ID:"+CONSUMER_GROUP_ID+"\nTOPIC:"+TOPIC);
            consumer.start();
        }
        catch(Exception e){
            logger.error("Exception occured in starting the kafka consumer.ERROR:"+e);
            e.printStackTrace();
        }
        
        TopologyBuilder builder=new TopologyBuilder();
        builder.setSpout(SPOUT_NAME,kafkaSpout,2);
        logger.debug("Spout is set for the topology.SPOUT="+SPOUT_NAME+",TOPOLOGY="+TOPOLOGY_NAME);
        builder.setBolt(BOLT_NAME,new CassandraBolt(CASSANDRA_NODE_IP,CASSANDRA_PORT),2).globalGrouping("KafkaSpout");
        logger.debug("Bolt is set for Topology.BOLT="+BOLT_NAME+",TOPOLOGY="+TOPOLOGY_NAME);
        //LocalCluster cluster= new LocalCluster();
        Config config=new Config();
        config.setNumWorkers(4);
        config.setMaxSpoutPending(1000);
        config.setMessageTimeoutSecs(60);
        config.setNumAckers(0);
        config.setMaxTaskParallelism(50);
        /*config.put(Config.NIMBUS_HOST,"172.26.41.56");
        config.put(Config.NIMBUS_THRIFT_PORT,6627);
        config.put(Config.STORM_ZOOKEEPER_PORT, 2181);
        config.put(Config.STORM_ZOOKEEPER_SESSION_TIMEOUT,1000000000);
        config.setDebug(true);*/
        
        //Map storm_conf=Utils.readStormConfig();
        //storm_conf.put("nimbus.host","172.26.41.56");
        //Client client=NimbusClient.getConfiguredClient(storm_conf).getClient();
        //NimbusClient nimbus=new NimbusClient(storm_conf, "172.26.41.56",6627);
        
        try{
            //String uploadJarLocation=StormSubmitter.submitJar(config, "C:\\Users\\akash.sethiya\\Desktop\\sethiyaji.akash\\skcUtility\\target\\skcUtility-1.0-SNAPSHOT-jar-with-dependencies.jar");
            //String jsonConf=JSONValue.toJSONString(storm_conf);
            //nimbus.getClient().submitTopology(TOPOLOGY_NAME, uploadJarLocation, jsonConf, builder.createTopology());
            //StormSubmitter.submitJar(config, "C:\\Users\\akash.sethiya\\Desktop\\sethiyaji.akash\\skcUtility\\target\\skcUtility-1.0-SNAPSHOT-jar-with-dependencies.jar");
            StormSubmitter.submitTopology(TOPOLOGY_NAME, config, builder.createTopology());
            //Thread.sleep(1000);
        }
        catch(InvalidTopologyException e){
            logger.error("Invalid Topology : "+TOPOLOGY_NAME);
            e.printStackTrace();
        }
        /*cluster.submitTopology(TOPOLOGY_NAME, config, builder.createTopology());
        try {
            Thread.sleep(3600000);
        } catch (InterruptedException e) {
            logger.error("Error Occured due to threading.ERROR:"+e);
            e.printStackTrace();
        }
        KillOptions killOpts=new KillOptions();
        killOpts.set_wait_secs(1);
        cluster.killTopologyWithOpts(TOPOLOGY_NAME,killOpts);
        cluster.shutdown();
        logger.info("TOPOLOGY="+TOPOLOGY_NAME+" has been shutdown.");*/
    }
    
    private static void setProperties(String propertyFileName) throws IOException{
        Properties property=new Properties();
        InputStream inStream=null;
        try{
            logger.info("fetching data from property file.File:"+propertyFileName);
            inStream=Topology.class.getClassLoader().getResourceAsStream(propertyFileName);
            property.load(inStream);
            ZK_HOST=property.getProperty("zookeeper.host")+":"+property.getProperty("zookeeper.port");
            TOPIC=property.getProperty("kafka.topic");
            CONSUMER_GROUP_ID=property.getProperty("consumer.group.id");
            OFFSET_RESET=property.getProperty("kafka.consumer.offset.reset");
            CASSANDRA_NODE_IP=property.getProperty("cassandra.nodeIp");
            CASSANDRA_PORT=Integer.parseInt(property.getProperty("cassandra.port"));
            SPOUT_NAME=property.getProperty("storm.spout.name");
            BOLT_NAME=property.getProperty("storm.bolt.name");
            TOPOLOGY_NAME=property.getProperty("storm.topology.name");
            REBALANCE_BACKOFF_TIME=property.getProperty("rebalance.backoff.time.ms");
            logger.info("Properties fetched Successfully");
        }
        catch(IOException e){
            logger.error("IOException occured in fetching properties from property File.ERROR:"+e);
            e.printStackTrace();
        }
        catch(Exception e){
            logger.error("Exception occured in fetching properties from property file.ERROR:"+e);
            e.printStackTrace();
        }
        finally{
            logger.debug("Closing the inputStream.");
            inStream.close();
        }
        
    }
}

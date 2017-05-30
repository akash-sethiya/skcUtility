package com.akash.bolts;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akash.cassandra.CassandraOperations;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

@SuppressWarnings("serial")
public class CassandraBolt extends BaseBasicBolt {
    CassandraOperations cassandraOperations;
    String nodeIp;
    int port;
    private static Logger logger = LoggerFactory.getLogger(CassandraBolt.class);
    public CassandraBolt(String nodeIp,int port){
        this.nodeIp=nodeIp;
        this.port=port;
        logger.info("Cassadra:::: Port:"+port+",nodeIp:"+nodeIp);
    }
    
    @SuppressWarnings("rawtypes")
    public void prepare(Map stormConf,TopologyContext context){
        cassandraOperations = new CassandraOperations(nodeIp, port);
    }
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        cassandraOperations.persist(tuple);
    }
    
    public void cleanup(){
        cassandraOperations.close();
    }

}

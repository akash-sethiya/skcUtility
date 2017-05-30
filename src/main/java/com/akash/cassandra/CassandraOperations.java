package com.akash.cassandra;

import java.util.Map;

import backtype.storm.tuple.Tuple;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.akash.json.JParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CassandraOperations {
    private CassandraConnector connector;
    private PreparedStatement statement;
    
    static int i=1;
    
    private Logger logger = LoggerFactory.getLogger(CassandraOperations.class);
    public CassandraOperations(String nodeIp,int port){
        logger.debug("Setting the CassandraConnector Object with cassandra nodeIP:"+nodeIp+",port:"+port+".");
        connector=new CassandraConnector();
        connector.connect(nodeIp, port);
    }
    
    public void persist(Tuple tuple){
        System.out.println("TUPLE:"+tuple.getString(0));
        logger.info("fetching the tuple from kafka consumer.and send it to JsonParser class for processing and parsing.");
        Map<String,Object> rowValues=JParser.parseAndFilter(tuple.getString(0));
        statement=getSession().prepare("INSERT INTO test.storm_kafka_data(timestamp,name,price,symbol) VALUES(?,?,?,?)");
        BoundStatement boundStatement=new BoundStatement(statement);
        this.getSession().execute(boundStatement.bind((Long)rowValues.get("ts"),(String)rowValues.get("name"),(String)rowValues.get("price"),(String)rowValues.get("symbol")));
        logger.info("TUPLE INSERTED SUCCESSFULLY");
    }
    public void close(){
        logger.info("closing the connection to the cassandra");
        connector.close();
    }
    
    public Session getSession(){
        logger.info("Get the current session to connect to the cassandra host");
        return connector.getSession();
    }

}



package com.akash.cassandra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;

public class CassandraConnector {
    private Cluster cluster;
    private Session session;
    private static Logger logger = LoggerFactory.getLogger(CassandraConnector.class);
    public void connect(final String nodeIp,final int port){
        this.cluster=Cluster.builder().addContactPoint(nodeIp).withPort(port).build();
        final Metadata metadata=cluster.getMetadata();
        logger.info("Connect to Cluster:"+metadata.getClusterName());
        for(final Host host:metadata.getAllHosts()){
            logger.info("DataCenter:"+host.getDatacenter()+",Host:"+host.getAddress()+",Rack:"+host.getRack());
        }
        session=cluster.connect();
    }
    
    public Session getSession(){
        logger.debug("Get Session of cassandra.");
        return this.session;
    }
    
    public void close(){
        logger.debug("Close the cassandra cluster.");
        cluster.close();
    }

}

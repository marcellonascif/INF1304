package main;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ckafka.data.Swap;
import main.StaticLibrary;
import main.java.ckafka.GroupDefiner;
import main.java.ckafka.GroupSelection;

public class mainGD implements GroupSelection{

    final Logger logger = LoggerFactory.getLogger(GroupDefiner.class);

    public mainGD() {
        ObjectMapper objectMapper = new ObjectMapper();
        Swap swap = new Swap(objectMapper);
        new GroupDefiner(this, swap);
    }
    public static void main(String[] args) {
    	// creating missing environment variable
		Map<String,String> env = new HashMap<String, String>();
		env.putAll(System.getenv());
		if(System.getenv("gd.one.consumer.topics") == null) 			env.put("gd.one.consumer.topics", "GroupReportTopic");
		if(System.getenv("gd.one.consumer.auto.offset.reset") == null) 	env.put("gd.one.consumer.auto.offset.reset", "latest");
		if(System.getenv("gd.one.consumer.bootstrap.servers") == null) 	env.put("gd.one.consumer.bootstrap.servers", "127.0.0.1:9092");
		if(System.getenv("gd.one.consumer.group.id") == null) 			env.put("gd.one.consumer.group.id", "gw-gd");
		if(System.getenv("gd.one.producer.bootstrap.servers") == null) 	env.put("gd.one.producer.bootstrap.servers", "127.0.0.1:9092");
		if(System.getenv("gd.one.producer.retries") == null) 			env.put("gd.one.producer.retries", "3");
		if(System.getenv("gd.one.producer.enable.idempotence") == null)	env.put("gd.one.producer.enable.idempotence", "true");
		if(System.getenv("gd.one.producer.linger.ms") == null) 			env.put("gd.one.producer.linger.ms", "1");
		try {
			StaticLibrary.setEnv(env);
		} catch (Exception e) {
			e.printStackTrace();
		}
        new mainGD();

    }

    /**
     * groupsIdentification<br>
     * @return a set containing all groups (a group is an Integer)
     */
    public Set<Integer> groupsIdentification() {
        Set<Integer> setOfGroups = new HashSet<Integer>();
        setOfGroups.add(1000);	// Mobile Node default group
        return setOfGroups;
    }

    /**
     * getNodesGroupByContext<br>
     * @return a set of groups representing the node groups
     */
    public Set<Integer> getNodesGroupByContext(ObjectNode contextInfo) {
        Set<Integer> setOfGroups = new HashSet<Integer>();
        double latitude = Double.parseDouble(String.valueOf(contextInfo.get("latitude")));
        double longitude = Double.parseDouble(String.valueOf(contextInfo.get("longitude")));

        setOfGroups.add(1000);	// Mobile Node default group
        Coordinate coordinate = new Coordinate(latitude, longitude);
        logger.info(String.format("[MainGD] lista de grupos para %s = %s.", String.valueOf(contextInfo.get("ID")), setOfGroups));
        return setOfGroups;
    }

    public String kafkaConsumerPrefix() {
        return "gd.one.consumer";
    }

    public String kafkaProducerPrefix() {
        return "gd.one.producer";
    }
}

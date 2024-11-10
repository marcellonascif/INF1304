package main;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Properties;
import java.util.Collections;
import java.util.Base64;


import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ckafka.data.Swap;
import main.StaticLibrary;
import main.java.ckafka.GroupDefiner;
import main.java.ckafka.GroupSelection;
import ckafka.data.SwapData;


import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

public class MainGD implements GroupSelection{

    final Logger logger = LoggerFactory.getLogger(GroupDefiner.class);
    private Swap swap;

    public MainGD() {
        ObjectMapper objectMapper = new ObjectMapper();
        this.swap = new Swap(objectMapper);
        new GroupDefiner(this, swap);
        System.out.println("GroupDefiner iniciado.");
        receiveMessagesFromProcessingNode();
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
            System.out.println("Iniciando GroupDefiner...");
            new MainGD();
		} catch (Exception e) {
			e.printStackTrace();
		}
        

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

    /**
     * Método para configurar o consumidor Kafka e escutar o tópico "GroupReportTopic"
     */
    public void receiveMessagesFromProcessingNode() {
        System.out.println("Recebendo mensagens do ProcessingNode...");
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "127.0.0.1:9092"); // Servidor Kafka
        properties.put("group.id", "gw-gd"); // Grupo de consumidores para o GroupDefiner
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");

        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singletonList("GroupReportTopic"));

        // Loop para consumir mensagens do tópico
        new Thread(() -> {
            try {
                while (true) {
                    ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord<String, byte[]> record : records) {
                        processMessage(record); // Chama a função para processar cada mensagem
                    }
                }
            } finally {
                consumer.close(); 
            }
        }).start();
    }

    /**
     * Método para processar a mensagem recebida do ProcessingNode
     * @param record O registro da mensagem recebida
     */
    private void processMessage(ConsumerRecord<String, byte[]> record) {
        System.out.println(String.format("Mensagem recebida de %s", record.key()));
        try {
            // String message = new String(record.value(), StandardCharsets.UTF_8);
            // System.out.println("Mensagem recebida = " + message);
            SwapData data = swap.SwapDataDeserialization((byte[]) record.value());
            String text = new String(data.getMessage(), StandardCharsets.UTF_8);
            System.out.println("Conteúdo da mensagem = " + text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

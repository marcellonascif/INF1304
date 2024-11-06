package main;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


import main.ProcessingNode;
import main.MainCKMobileNode;
import ckafka.data.Swap;
import main.java.ckafka.GroupDefiner;
import main.java.ckafka.GroupSelection;

public class MainGD implements GroupSelection {
    /** Logger */
    final Logger logger = LoggerFactory.getLogger(GroupDefiner.class);
	/** Lista de inspetores do SeFaz */
	private List<MainCKMobileNode> inspectorList;

	/**
     * Construtor
     */
    public MainGD() {
		// cria uma lista vazia de inspetores
		this.inspectorList = new ArrayList<MainCKMobileNode>();

		/*
		 * Cria o GroupDefiner
		 */
        ObjectMapper objectMapper = new ObjectMapper();
        Swap swap = new Swap(objectMapper);
        new GroupDefiner(this, swap);
    }

	/**
	 * Método principal
	 * @param args argumentos de linha de comando
	 */
    public static void main(String[] args) {
    	// criando variáveis de ambiente caso faltem
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
			ProcessingNode.setEnv(env);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// criação do novo GroupDefiner
        new MainGD();
    }

    /**
     * groupsIdentification<br>
     * @return um conjunto contendo todos os grupos (um grupo é um Integer)
     */
    public Set<Integer> groupsIdentification() {
        Set<Integer> setOfGroups = new HashSet<Integer>();
        setOfGroups.add(1000);	// Grupo padrão para nós móveis
        // Adiciona mais grupos se necessário
        // Exemplo: setOfGroups.add(2000);
        return setOfGroups;
    }

    /**
     * getNodesGroupByContext<br>
     * @return um conjunto de grupos representando os grupos do nó
     */
    public Set<Integer> getNodesGroupByContext(ObjectNode contextInfo) {
    	MainCKMobileNode inspector = null;
        Set<Integer> setOfGroups = new HashSet<Integer>();
        double latitude = Double.parseDouble(String.valueOf(contextInfo.get("latitude")));
        double longitude = Double.parseDouble(String.valueOf(contextInfo.get("longitude")));

        // Atualiza a posição do inspetor (nó móvel)
        // try {
		// 	inspector = new MainCKMobileNode(String.valueOf(contextInfo.get("date")), latitude, longitude,
		// 			String.valueOf(contextInfo.get("ID")));
		// } catch (NumberFormatException | ParseException e) {
		// 	e.printStackTrace();
		// }
        inspectorList.add(inspector);
        
        setOfGroups.add(1000);	// Grupo padrão para nós móveis
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

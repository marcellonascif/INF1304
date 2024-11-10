package main;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Properties;
import java.util.Collections;
import java.util.Base64;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.time.Duration;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ckafka.data.Swap;
import ckafka.data.SwapData;

import main.StaticLibrary;
import main.java.ckafka.GroupDefiner;
import main.java.ckafka.GroupSelection;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

public class MainGD implements GroupSelection{

    final Logger logger = LoggerFactory.getLogger(GroupDefiner.class);
    private Swap swap;
    private Set<PontoDeOnibus> pontosDeOnibus = new HashSet<PontoDeOnibus>();
    private final double RAIO_DO_PONTO = 4000.0; // 1 km

    public MainGD() {
        ObjectMapper objectMapper = new ObjectMapper();
        this.swap = new Swap(objectMapper);
        new GroupDefiner(this, swap);
        System.out.println("GroupDefiner iniciado.");
        
        // Carrega os pontos de ônibus do arquivo CSV
        pontosDeOnibus = carregarPontosDeOnibus();

        System.out.println("Pontos de ônibus carregados:");

        // Itera sobre os pontos de ônibus carregados e imprime cada um
        for (PontoDeOnibus ponto : pontosDeOnibus) {
            System.out.println(ponto.toString());
        }
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
        for (PontoDeOnibus ponto : pontosDeOnibus) {
            setOfGroups.add(ponto.getNumeroGrupo());
        }
       
        return setOfGroups;
    }

    /**
     * getNodesGroupByContext<br>
     * @return a set of groups representing the node groups
     */
    public Set<Integer> getNodesGroupByContext(ObjectNode contextInfo) {
        System.out.println("[MainGD] getNodesGroupByContext chamado");
        Set<Integer> setOfGroups = new HashSet<Integer>();


        String ID = String.valueOf(contextInfo.get("ID"));
        double latitude = Double.parseDouble(String.valueOf(contextInfo.get("latitude")));
        double longitude = Double.parseDouble(String.valueOf(contextInfo.get("longitude")));
        System.out.println(String.format("[MainGD] ID: %s, latitude = %f, longitude = %f", ID, latitude, longitude));

        Coordinate coordenadas = new Coordinate(latitude, longitude);
        
        for (PontoDeOnibus ponto : this.pontosDeOnibus) {
            if(ponto.estaDentroDoRaio(coordenadas, this.RAIO_DO_PONTO)){
                setOfGroups.add(ponto.getNumeroGrupo());
                System.out.println(String.format("[MainGD] A coordenada está dentro do raio do ponto %s. Adicionando o Mobile Node %s ao grupo de numero %d.", ponto.getNomePonto(), ID, ponto.getNumeroGrupo()));
            }
            else if(setOfGroups.contains(ponto.getNumeroGrupo())){
                setOfGroups.remove(ponto.getNumeroGrupo());
                System.out.println(String.format("[MainGD] A coordenada não está mais dentro do raio do ponto %s. Removendo o Mobile Node %s do grupo de numero %d.", ponto.getNomePonto(), ID, ponto.getNumeroGrupo()));
            }
        }
        
        setOfGroups.add(1000);	// Mobile Node default group
        logger.info(String.format("[MainGD] lista de grupos para %s = %s.", ID, setOfGroups));
        return setOfGroups;
    }

    public String kafkaConsumerPrefix() {
        return "gd.one.consumer";
    }

    public String kafkaProducerPrefix() {
        return "gd.one.producer";
    }

    // Método para carregar pontos de ônibus do arquivo CSV
    public Set<PontoDeOnibus> carregarPontosDeOnibus() {
        Set<PontoDeOnibus> pontosDeOnibus = new HashSet<>();

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("csv/pontosDeOnibusCoordenadas.csv");
             CSVReader reader = new CSVReader(new InputStreamReader(is))) {
            
            String[] line;
            reader.readNext(); // Pular o cabeçalho
            
            while ((line = reader.readNext()) != null) {
                try {
                    // Parse dos valores de cada coluna
                    int numeroGrupo = Integer.parseInt(line[0]);
                    String nomePonto = line[1];
                    double latitude = Double.parseDouble(line[2]);
                    double longitude = Double.parseDouble(line[3]);

                    // Criação do objeto PontoDeOnibus e adição ao HashSet
                    PontoDeOnibus ponto = new PontoDeOnibus(numeroGrupo, nomePonto, latitude, longitude);
                    pontosDeOnibus.add(ponto);
                    
                } catch (NumberFormatException e) {
                    System.err.println("Erro ao converter um valor numérico na linha: " + String.join(",", line));
                    e.printStackTrace();
                }
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }

        return pontosDeOnibus;
    }
    
}

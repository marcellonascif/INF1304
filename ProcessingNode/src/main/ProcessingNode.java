package main;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

import org.openstreetmap.gui.jmapviewer.Coordinate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import ckafka.data.Swap;
import ckafka.data.SwapData;
import main.java.application.ModelApplication;

/**
 * Processing Node
 */
public class ProcessingNode extends ModelApplication {
    private ObjectMapper objectMapper;
    private Swap swap;

    // Opções válidas de entrada do usuário
    private static final String OPTION_EXIT = "Z";
    private boolean fim = false;

    /**
     * Constructor
     */
    public ProcessingNode() {
        this.objectMapper = new ObjectMapper();
        this.swap = new Swap(objectMapper);
    }


    /**
     * Main
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // creating missing environment variable
        Map<String,String> env = new HashMap<String, String>();
        env.putAll(System.getenv());
        if(System.getenv("app.consumer.topics") == null) env.put("app.consumer.topics", "AppModel");
        if(System.getenv("app.consumer.auto.offset.reset") == null) env.put("app.consumer.auto.offset.reset", "latest");
        if(System.getenv("app.consumer.bootstrap.servers") == null) env.put("app.consumer.bootstrap.servers", "127.0.0.1:9092");
        if(System.getenv("app.consumer.group.id") == null) env.put("app.consumer.group.id", "gw-consumer");
        if(System.getenv("app.producer.bootstrap.servers") == null) env.put("app.producer.bootstrap.servers", "127.0.0.1:9092");
        if(System.getenv("app.producer.retries") == null) env.put("app.producer.retries", "3");
        if(System.getenv("app.producer.enable.idempotence") == null)env.put("app.producer.enable.idempotence", "true");
        if(System.getenv("app.producer.linger.ms") == null) env.put("app.producer.linger.ms", "1");
        if(System.getenv("app.producer.acks") == null) env.put("app.producer.acks", "all");
       try {
           StaticLibrary.setEnv(env);
       }
       catch (Exception e) {
           e.printStackTrace();
       }
        Scanner keyboard = new Scanner(System.in);
        ProcessingNode pn = new ProcessingNode();
        pn.runPN(keyboard);
    }


    public void runPN(Scanner keyboard) {
        Map<String, Consumer<Scanner>> optionsMap = new HashMap<>();

        // Mapeia as opções para as funções correspondentes
        optionsMap.put(OPTION_EXIT, scanner -> fim = true);

        while(!fim) {
            System.out.print("(Z para terminar)? \n\n");
            String linha = keyboard.nextLine().trim().toUpperCase();

            System.out.printf("Sua opção foi %s.\n", linha);
            if(optionsMap.containsKey(linha)) optionsMap.get(linha).accept(keyboard);
            else System.out.printf("Opção %s inválida.\n", linha);
        }

        keyboard.close();
        System.out.println("FIM!");
        System.exit(0);
    }

    /**
     * Activated when a record is received
     * @param record
     */
    @Override
    public void recordReceived(ConsumerRecord record) {

        System.out.println(String.format("Mensagem recebida de %s", record.key()));
        try {
            SwapData data = swap.SwapDataDeserialization((byte[]) record.value());
            // System.out.println(data.getClass());
            String message = new String(data.getMessage(), StandardCharsets.UTF_8);
            System.out.println("Mensagem recebida = " + message);

            processMessage(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Process message
     * @param message
     */
    private void processMessage(String message) {

        try{
            ObjectNode rootNode = (ObjectNode) objectMapper.readTree(message);

            final String id = rootNode.get("id").asText();
            final double velocidadeOnibus = rootNode.get("velocidade").asDouble();

            final Coordinate coordOnibus = new Coordinate(
                rootNode.get("latitude").asDouble(),
                rootNode.get("longitude").asDouble()
            );

            final ArrayNode proximosPontosAN = (ArrayNode) rootNode.get("proximosPontos");

            List<PontoDeOnibus> proximosPontos = new ArrayList<>();
            for (JsonNode pontoNode : proximosPontosAN) {
                proximosPontos.add(new PontoDeOnibus(
                    pontoNode.get("numeroGrupo").asInt(),
                    id,
                    pontoNode.get("latitude").asDouble(),
                    pontoNode.get("longitude").asDouble()
                ));
            }

            System.out.println("ID: " + id);
            System.out.println("Coordenadas do ônibus: " + coordOnibus);

            this.sendToGroups(coordOnibus, velocidadeOnibus, proximosPontos);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error reading JSON in processMessage", e);
        }
    }

    private void sendToGroups(Coordinate coordOnibus, double velocidadeOnibus, List<PontoDeOnibus> proximosPontos) {
        for (PontoDeOnibus ponto : proximosPontos) {
            Coordinate coordPonto = ponto.getCoordenada();
            double distancia = StaticLibrary.calcularDistancia(coordOnibus, coordPonto);
            double tempo = StaticLibrary.calcularTempo(distancia, velocidadeOnibus);

            int group = ponto.getNumeroGrupo();

            String message = "Tempo para chegar ao ponto de ônibus: " + tempo + " minuto(s)";
            System.out.println("Mensagem enviada para grupo " + group + ": " + message);

            this.sendGroupcastMessage(message, String.valueOf(group));
        }
    }

    /**
     * Send groupcast message
     * @param message
     * @param group
     */
    private void sendGroupcastMessage(String message, String group) {
        try {
            sendRecord(createRecord("GroupMessageTopic", group, swap.SwapDataSerialization(createSwapData(message))));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error SendGroupCastMessage", e);
        }
    }
}

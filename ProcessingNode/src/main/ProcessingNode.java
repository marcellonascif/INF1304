package main;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.openstreetmap.gui.jmapviewer.Coordinate;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.fasterxml.jackson.databind.ObjectMapper;

import ckafka.data.Swap;
import ckafka.data.SwapData;
import main.java.application.ModelApplication;


// import br.com.meslin.auxiliar.StaticLibrary;

public class ProcessingNode extends ModelApplication {
    private ObjectMapper objectMapper;
    private Swap swap;

    // Opções válidas de entrada do usuário
    private static final String OPTION_GROUPCAST = "G";
    private static final String OPTION_PN = "P";
    private static final String OPTION_EXIT = "Z";

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
        // pn.runPN(keyboard);

        keyboard.close();
    }

    /**
     *
     */
//     public void runPN(Scanner keyboard) {
//         Map<String, Consumer<Scanner>> optionsMap = new HashMap<>();

//         // Mapeia as opções para as funções correspondentes
//         optionsMap.put(OPTION_GROUPCAST, this::sendGroupcastMessage);
//         optionsMap.put(OPTION_UNICAST, this::sendUnicastMessage);
// //        optionsMap.put(OPTION_PN, this::sendMessageToPN);
//         optionsMap.put(OPTION_EXIT, scanner -> fim = true);

//         while(!fim) {
//             System.out.print("Mensagem para (G)rupo ou (I)ndivíduo (P)rocessing Node (Z para terminar)? \n\n");
//             String linha = keyboard.nextLine().trim().toUpperCase();
//             System.out.printf("Sua opção foi %s.\n", linha);
//             if(optionsMap.containsKey(linha)) optionsMap.get(linha).accept(keyboard);
//             else System.out.printf("Opção %s inválida.\n", linha);
//         }
//         keyboard.close();
//         System.out.println("FIM!");
//         System.exit(0);
//     }

    /**
     *
     */
    @Override
    public void recordReceived(ConsumerRecord record) {

        System.out.println(String.format("Mensagem recebida de %s", record.key()));
        try {
            SwapData data = swap.SwapDataDeserialization((byte[]) record.value());
            // System.out.println(data.getClass());
            String text = new String(data.getMessage(), StandardCharsets.UTF_8);
            System.out.println("Mensagem recebida = " + text);

            sendGroupcastMessage(text);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Send groupcast message
     * @param message
     */
    private void sendGroupcastMessage(String message) {

        String group = extractGroup(message);
        Coordinate coordBus = extractCoordinate(message);
        Coordinate coordStop = new Coordinate(10, 10);

        double distance = StaticLibrary.calcularDistancia(coordBus, coordStop);

        double speedBus = 50;
        double tempo = StaticLibrary.calcularTempo(distance, speedBus);

        String messageText = "Tempo para chegar ao ponto de ônibus: " + tempo + " minuto(s)";
        System.out.println("Mensagem enviada para grupo" + group + ": " + messageText);

        try {
            sendRecord(createRecord("GroupMessageTopic", group, swap.SwapDataSerialization(createSwapData(messageText))));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error SendGroupCastMessage", e);
        }
    }

    public String extractGroup (String text){
        String[] parts = text.split(",");
        return parts[0];
    }

    public Coordinate extractCoordinate (String text){
        String[] parts = text.split(",");
        double latitude = Double.parseDouble(parts[1]);
        double longitude = Double.parseDouble(parts[2]);
        return new Coordinate(latitude, longitude);
    }
}

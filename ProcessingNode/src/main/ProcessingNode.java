package main;

import main.StaticLibrary;

import java.nio.charset.StandardCharsets;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.fasterxml.jackson.databind.ObjectMapper;

import ckafka.data.Swap;
import ckafka.data.SwapData;
import main.java.application.ModelApplication;


// import br.com.meslin.auxiliar.StaticLibrary;

public class ProcessingNode extends ModelApplication {
    private Swap swap;
    private ObjectMapper objectMapper;

    // Opções válidas de entrada do usuário
    private static final String OPTION_GROUPCAST = "G";
    private static final String OPTION_UNICAST = "I";
    private static final String OPTION_PN = "P";
    private static final String OPTION_EXIT = "Z";

    // a variável não pode ser local porque está sendo usada em uma função lambda
    // Controle do loop eterno até que ele termine
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

    /**
     * TODO
     */
    public void runPN(Scanner keyboard) {
        Map<String, Consumer<Scanner>> optionsMap = new HashMap<>();

        // Mapeia as opções para as funções correspondentes
        optionsMap.put(OPTION_GROUPCAST, this::sendGroupcastMessage);
        optionsMap.put(OPTION_UNICAST, this::sendUnicastMessage);
//        optionsMap.put(OPTION_PN, this::sendMessageToPN);
        optionsMap.put(OPTION_EXIT, scanner -> fim = true);

        while(!fim) {
            System.out.print("Mensagem para (G)rupo ou (I)ndivíduo (P)rocessing Node (Z para terminar)? ");
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
     *
     */
    @Override
    public void recordReceived(ConsumerRecord record) {
        System.out.println(String.format("Mensagem recebida de %s", record.key()));
        try {
            SwapData data = swap.SwapDataDeserialization((byte[]) record.value());
            String text = new String(data.getMessage(), StandardCharsets.UTF_8);
            System.out.println("Mensagem recebida = " + text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param keyboard
     */
    private void sendUnicastMessage(Scanner keyboard) {
        System.out.println("UUID:\nHHHHHHHH-HHHH-HHHH-HHHH-HHHHHHHHHHHH");
        String uuid = keyboard.nextLine();
        System.out.print("Message: ");
        String messageText = keyboard.nextLine();
        System.out.println(String.format("Sending |%s| to %s.", messageText, uuid));

        try {
            sendRecord(createRecord("PrivateMessageTopic", uuid, swap.SwapDataSerialization(createSwapData(messageText))));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Send groupcast message
     * @param keyboard
     */
    private void sendGroupcastMessage(Scanner keyboard) {
        System.out.print("Mensagem groupcast. Entre com o número do grupo: ");
        String group = keyboard.nextLine();
        System.out.print("Entre com a mensagem: ");
        String messageText = keyboard.nextLine();
        System.out.println(String.format("Enviando mensagem %s para o grupo %s.",
                messageText, group));
        try {
            sendRecord(createRecord("GroupMessageTopic", group,
                    swap.SwapDataSerialization(createSwapData(messageText))));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error SendGroupCastMessage", e);
        }
    }

}

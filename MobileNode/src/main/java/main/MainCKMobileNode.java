package main;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.node.ObjectNode;

import ckafka.data.SwapData;
import lac.cnclib.net.NodeConnection;
import lac.cnclib.sddl.message.ApplicationMessage;
import lac.cnclib.sddl.message.Message;
import main.java.ckafka.mobile.CKMobileNode;
import main.java.ckafka.mobile.tasks.SendLocationTask;

/**
 * @authors João Biscaia, Thomas de Mello, Marcello Nascif
 *
 */
public class MainCKMobileNode extends CKMobileNode {
    private double latitude;
    private double longitude;
    private UUID uuid;
    private int stepNumber = 0;

    // Executor service para agendar tarefas de envio de localização
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> locationTask;

    public MainCKMobileNode() {}

    public MainCKMobileNode(UUID uuid, double latitude, double longitude) {
        super();
        this.uuid = uuid;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    private void sendLocation() {
        ApplicationMessage message = createDefaultApplicationMessage();
        SwapData locationData = newLocation(stepNumber);

        if (locationData != null) {
            message.setContentObject(locationData);
            sendMessageToGateway(message);
            System.out.println("Localização enviada ao Processing Node.");
        }
    }

    private void startLocationTask() {
        locationTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                sendLocation();
            } catch (Exception e) {
                logger.error("Erro ao enviar localização periodicamente.", e);
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {
        MainCKMobileNode node = new MainCKMobileNode();
        node.startLocationTask();  // Inicia a tarefa de localização periódica

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (node.locationTask != null && !node.locationTask.isCancelled()) {
                node.locationTask.cancel(true);
            }
            node.scheduler.shutdown();
            System.out.println("Encerrando a aplicação.");
        }));
    }

    @Override
    public SwapData newLocation(Integer messageCounter) {
        logger.debug("Gerando nova localização");

        ObjectNode location = objectMapper.createObjectNode();
        double stepX = (-43.23232376069340 - (-43.18559736525978)) / 10;
        double stepY = (-22.978883470478085 - (-22.936826006961283)) / 10;
        Double amountX = -43.18559736525978 + stepX * this.stepNumber;
        Double amountY = -22.936826006961283 + stepY * this.stepNumber;
        this.stepNumber = (this.stepNumber + 1) % 10;

        location.put("ID", this.mnID.toString());
        location.put("messageCount", messageCounter);
        location.put("longitude", amountX);
        location.put("latitude", amountY);
        location.put("date", new Date().toString());

        try {
            SwapData locationData = new SwapData();
            locationData.setContext(location);
            locationData.setDuration(60);  // Tempo de vida da mensagem em segundos
            return locationData;
        } catch (Exception e) {
            logger.error("Não foi possível criar o SwapData de localização", e);
            return null;
        }
    }

    @Override
    public void connected(NodeConnection nodeConnection) {
        logger.debug("Conectado ao Gateway");
    }

    @Override
    public void newMessageReceived(NodeConnection nodeConnection, Message message) {
        logger.debug("Nova mensagem recebida");
        try {
            SwapData swp = fromMessageToSwapData(message);
            if (swp.getTopic().equals("Ping")) {
                message.setSenderID(this.mnID);
                sendMessageToGateway(message);
            } else {
                String str = new String(swp.getMessage(), StandardCharsets.UTF_8);
                logger.info(String.format("Mensagem recebida de %s: %s", message.getRecipientID(), str));
            }
        } catch (Exception e) {
            logger.error("Erro ao ler a nova mensagem recebida");
        }
    }

    @Override
    public void disconnected(NodeConnection nodeConnection) {}

    @Override
    public void unsentMessages(NodeConnection nodeConnection, List<Message> list) {}

    @Override
    public void internalException(NodeConnection nodeConnection, Exception e) {}
}

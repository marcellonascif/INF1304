package main;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.node.ObjectNode;

import ckafka.data.SwapData;
import lac.cnclib.sddl.message.ApplicationMessage;

public class OnibusNode extends MobileNode {
    private Map<String, double[]> pontosDeOnibus;
    private int stepNumber = 0;

    public OnibusNode(String nome, double latitude, double longitude){
        super(nome, latitude, longitude); 
        this.pontosDeOnibus = pontosDeOnibus;
        this.mnID = generateCustomUUID();
    }

    private UUID generateCustomUUID() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        uuid = "0" + uuid.substring(1);
        return UUID.fromString(uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20));
    }

    public Map<String, double[]> getPontosDeOnibus(){
        return pontosDeOnibus;
    }

    @Override
    public SwapData newLocation(Integer messageCounter) {
        logger.debug("Getting new location");

        // creates an empty json {}
        ObjectNode location = objectMapper.createObjectNode();

        // 3 parameters that composes
        // Origem: -43.18559736525978 -22.936826006961283
        // Destino -43.23232376069340 -22.978883470478085
        double stepX = (-43.23232376069340 - (this.longitude)) / 10;
        double stepY = (-22.978883470478085 - (this.latitude)) / 10;
        this.latitude += stepY;
        this.longitude += stepX;
        this.stepNumber = (this.stepNumber+1) % 10;

        // we write the data to the json document
        location.put("ID", this.mnID.toString());
        location.put("messageCount", messageCounter);
        location.put("longitude", this.longitude);
        location.put("latitude", this.latitude);
        location.put("date", new Date().toString());

        try {
            // Converte o ObjectNode para uma string JSON
            String locationJson = objectMapper.writeValueAsString(location);
            SwapData data = new SwapData();
            ApplicationMessage message = createDefaultApplicationMessage();

            data.setMessage(locationJson.getBytes(StandardCharsets.UTF_8));
            data.setTopic("AppModel");
            message.setContentObject(data);
            
            // sendMessageToGateway(message);

            return data;

        } catch (Exception e) {
            logger.error("Location Swap Data could not be created", e);
            return null;
        }
    }

}

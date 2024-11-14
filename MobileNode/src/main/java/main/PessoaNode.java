package main;

import java.util.Date;
import java.util.UUID;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ckafka.data.SwapData;

public class PessoaNode extends MobileNode {
    /** used to move this MN */
    private double latitude;
    private double longitude;

    public PessoaNode(String nome, double latitude, double longitude){
        super(nome);
        this.mnID = generateCustomUUID();
        this.latitude = latitude;
        this.longitude = longitude;
        System.out.println("UUID: " + this.mnID.toString());
    }


    private UUID generateCustomUUID() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        uuid = "1" + uuid.substring(1);
        return UUID.fromString(uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20));
    }

    /**
     * Get the Location (in simulation it generates a new location)
     *
     * @pre MessageCounter
     * @post ShippableData containing location as Context information
     *
     */
    @Override
    public SwapData newLocation(Integer messageCounter) {
        logger.debug("Getting new location");

        // creates an empty json {}
        ObjectNode location = objectMapper.createObjectNode();

        // we write the data to the json document
        location.put("ID", this.mnID.toString());
        location.put("messageCount", messageCounter);
        location.put("longitude", this.longitude);
        location.put("latitude", this.latitude);
        location.put("date", new Date().toString());

        try {
            SwapData locationData = new SwapData();
            locationData.setContext(location);
            locationData.setDuration(60);			// tempo em segundos de vida da mensagem
            return locationData;
        } catch (Exception e) {
            logger.error("Location Swap Data could not be created", e);
            return null;
        }
    }
}

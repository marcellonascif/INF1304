package main;

import java.util.Map;
import java.util.Date;
import java.util.UUID;

public class OnibusNode extends MainCKMobileNode {
    private Map<String, double[]> pontosDeOnibus;

    public OnibusNode(UUID uuid, double latitude, double longitude, Map<String, double[]> pontosDeOnibus){
        super(uuid, latitude, longitude);
        this.pontosDeOnibus = pontosDeOnibus;
    }

}

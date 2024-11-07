package main;

import java.util.Map;
import java.util.Date;
import java.util.UUID;

public class OnibusNode extends MainCKMobileNode {
    private Map<String, double[]> pontosDeOnibus;

    public OnibusNode(Date date, double latitude, double longitude, UUID uuid, Map<String, double[]> pontosDeOnibus){
        super(date, latitude, longitude, uuid); 
        this.pontosDeOnibus = pontosDeOnibus;
    }

    

}

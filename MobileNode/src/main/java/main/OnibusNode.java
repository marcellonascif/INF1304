package main;

import java.util.Map;
import java.util.UUID;

public class OnibusNode extends MainCKMobileNode {
    private Map<String, double[]> pontosDeOnibus;

    public OnibusNode(double latitude, double longitude, UUID uuid, Map<String, double[]> pontosDeOnibus){
        super(uuid, latitude, longitude); 
        this.pontosDeOnibus = pontosDeOnibus;
    }

    public Map<String, double[]> getPontosDeOnibus(){
        return pontosDeOnibus;
    }



}

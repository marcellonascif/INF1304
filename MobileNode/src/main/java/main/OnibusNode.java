package main;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.openstreetmap.gui.jmapviewer.Coordinate;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import ckafka.data.SwapData;
import lac.cnclib.sddl.message.ApplicationMessage;

public class OnibusNode extends MobileNode {

    private String linha;
    private List<PontoDeOnibus> proximosPontos;
    private OnibusInfo info;
    private int stepNumber = 0;
    private double velocidade = 0;


    public OnibusNode(String nomeNode){
        super(nomeNode);
        this.info = new OnibusInfo();

        this.mnID = generateCustomUUID();
        this.linha = info.getLinha(nomeNode);
        this.proximosPontos = info.getPontosDeOnibus(nomeNode);
        this.latitude = proximosPontos.get(0).getLatitude();
        this.longitude = proximosPontos.get(0).getLongitude();

        // Remove o ponto atual da lista de próximos pontos pois o onibus inicializa no primeiro ponto
        proximosPontos.remove(0);


        System.out.println(this.toString());
    }


    private UUID generateCustomUUID() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        uuid = "0" + uuid.substring(1);
        return UUID.fromString(uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20));
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Onibus %s da linha %s (mnID: %s)\n", this.nomeNode, this.linha, mnID.toString()));
        sb.append(String.format("Latitude: %.7f | Longitude: %.7f\n", this.latitude, this.longitude));

        sb.append("Próximos pontos:\n");
        for (PontoDeOnibus ponto : proximosPontos) {
            sb.append(String.format("  Grupo: %d, Nome: %s, Latitude: %.7f, Longitude: %.7f\n",
                    ponto.getNumeroGrupo(), ponto.getNomePonto(), ponto.getLatitude(), ponto.getLongitude()));
        }

        return sb.toString();
    }


    @Override
    public SwapData newLocation(Integer messageCounter) {
        logger.debug("Mandando as infos do onibus pro PN");

        ObjectMapper objectMapper = new ObjectMapper();

        // creates an empty json {}
        ObjectNode onibus = objectMapper.createObjectNode();

        PontoDeOnibus pontoAtual = this.proximosPontos.get(0);

        if (this.stepNumber == 0){
            double distancia = StaticLibrary.calcularDistancia(new Coordinate(this.latitude, this.longitude), new Coordinate(pontoAtual.getLatitude(), pontoAtual.getLongitude()));
            double tempo = (1.0 / 6.0) ;// 10 minutos em hora
            this.velocidade = (distancia / 1000) / tempo ; // km/h
        }
        double stepY = (pontoAtual.getLatitude() - (this.latitude)) / 10;
        double stepX = (pontoAtual.getLongitude() - (this.longitude)) / 10;

        this.latitude += stepY;
        this.longitude += stepX;

        if (this.stepNumber == 10){
            this.proximosPontos.remove(0);
        }

        this.stepNumber = (this.stepNumber+1) % 10;


        ArrayNode proximosPontosAN = objectMapper.createArrayNode();
        for (PontoDeOnibus ponto : this.proximosPontos) {
            ObjectNode pontoNode = objectMapper.createObjectNode();
            pontoNode.put("numeroGrupo", ponto.getNumeroGrupo());
            pontoNode.put("latitude", ponto.getLatitude());
            pontoNode.put("longitude", ponto.getLongitude());

            proximosPontosAN.add(pontoNode);
        }

        // we write the data to the json document
        onibus.put("id", this.nomeNode);
        onibus.put("velocidade", this.velocidade);
        onibus.put("latitude", this.latitude);
        onibus.put("longitude", this.longitude);
        // Adiciona o ArrayNode ao ObjectNode principal
        onibus.set("proximosPontos", proximosPontosAN);

        try {
            // Converte o ObjectNode para uma string JSON
            // String onibusJson = objectMapper.writeValueAsString(onibus);
            String onibusJson = objectMapper.writeValueAsString(onibus);
            SwapData data = new SwapData();
            ApplicationMessage message = createDefaultApplicationMessage();

            data.setMessage(onibusJson.getBytes(StandardCharsets.UTF_8));
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

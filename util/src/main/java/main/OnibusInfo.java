package main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class OnibusInfo {
    private static final String onibusPath = "json/onibus.json";
    private static final String linhasPath = "json/linhas.json";
    private static final String pontosPath = "json/pontos.json";
    
    private ObjectMapper objectMapper;
    
    public OnibusInfo() {
        this.objectMapper = new ObjectMapper();
    }

    // Dado o id de um onibus retorna os pontos de onibus que ele atende (Grupo, Nome, Latitude, Longitude)
    public List<PontoDeOnibus> getPontosDeOnibus(String id){
        List<PontoDeOnibus> pontos = new ArrayList<>();
        String linha = getLinha(id);
        List<Integer> grupos = getGrupos(linha);

        try {
            JsonNode jsonNode = objectMapper.readTree(new File(pontosPath));
            for (JsonNode node : jsonNode) {
                if (grupos.contains(node.get("grupo").asInt())) {
                    pontos.add(new PontoDeOnibus(node.get("grupo").asInt(), node.get("nome").asText(), node.get("latitude").asDouble(), node.get("longitude").asDouble()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pontos;

    }

    // dado uma linha retorna os grupos de onibus que a atendem
    public List<Integer> getGrupos(String linha){
        List<Integer> grupos = new ArrayList<>();
        try {
            JsonNode jsonNode = objectMapper.readTree(new File(linhasPath));
            for (JsonNode node : jsonNode) {
                if (node.get("linha").asText() == linha) {
                    grupos.add(node.get("grupo").asInt());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return grupos;
    }

    // Dado um onibus retorna a sua linha
    public String getLinha(String id){
        try {
            JsonNode jsonNode = objectMapper.readTree(new File(onibusPath));
            for (JsonNode node : jsonNode) {
                if (node.get("id").asText() == id) {
                    return node.get("linha").asText();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}

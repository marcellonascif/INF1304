package main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OnibusInfo {
    private static final String ONIBUS_PATH = "json/onibus.json";
    private static final String LINHAS_PATH = "json/linhas.json";
    private static final String PONTOS_PATH = "json/pontos.json";

    private ObjectMapper objectMapper;

    public OnibusInfo() {
        this.objectMapper = new ObjectMapper();
    }


    // Carrega todos os pontos de onibus do arquivo JSON
    public List<PontoDeOnibus> carregarPontosDeOnibus() {
        List<PontoDeOnibus> pontos = new ArrayList<>();
        try (InputStream pontosStream = getClass().getClassLoader().getResourceAsStream(PONTOS_PATH)) {
            if (pontosStream == null) {
                System.err.println("Arquivo " + PONTOS_PATH + " não encontrado no classpath!");
                return null;
            }

            JsonNode jsonNode = objectMapper.readTree(pontosStream);
            for (JsonNode node : jsonNode) {
                pontos.add(new PontoDeOnibus(
                        node.get("grupo").asInt(),
                        node.get("nome").asText(),
                        node.get("latitude").asDouble(),
                        node.get("longitude").asDouble()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pontos;
    }


    // Dado um onibus retorna a sua linha
    public String getLinha(String id) {
        try (InputStream onibusStream = getClass().getClassLoader().getResourceAsStream(ONIBUS_PATH)) {
            if (onibusStream == null) {
                System.err.println("Arquivo " + ONIBUS_PATH + " não encontrado no classpath!");
                return null;
            }
            JsonNode jsonNode = objectMapper.readTree(onibusStream);
            for (JsonNode node : jsonNode) {
                if (node.get("id").asText().equals(id)) {
                    return node.get("linha").asText();
                }
            }
            System.err.println("ID " + id + " não encontrado no arquivo " + ONIBUS_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    // dado uma linha retorna os grupos de onibus que a atendem
    public List<Integer> getGrupos(String linha) {
        List<Integer> grupos = new ArrayList<>();
        try (InputStream linhasStream = getClass().getClassLoader().getResourceAsStream(LINHAS_PATH)) {
            if (linhasStream == null) {
                System.err.println("Arquivo " + LINHAS_PATH + " não encontrado no classpath!");
                return null;
            }
            JsonNode jsonNode = objectMapper.readTree(linhasStream);

            for (JsonNode node : jsonNode) {
                if (node.get("linha").asText().equals(linha)) {
                    JsonNode paradasNode = node.get("paradas");
                    if (paradasNode != null) {
                        for (JsonNode parada : paradasNode) {
                            grupos.add(parada.get("grupo").asInt());
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return grupos;
    }


    // Dado o id de um onibus retorna os pontos de onibus que ele atende (Grupo, Nome, Latitude, Longitude)
    public List<PontoDeOnibus> getPontosDeOnibus(String id) {
        List<PontoDeOnibus> pontos = new ArrayList<>();
        String linha = getLinha(id);
        List<Integer> grupos = getGrupos(linha);

        try (InputStream pontosStream = getClass().getClassLoader().getResourceAsStream(PONTOS_PATH)) {
            JsonNode jsonNode = objectMapper.readTree(pontosStream);
            if (pontosStream == null) {
                System.err.println("Arquivo " + PONTOS_PATH + " não encontrado no classpath!");
                return null;
            }
            for (JsonNode node : jsonNode) {
                if (grupos.contains(node.get("grupo").asInt())) {
                    pontos.add(new PontoDeOnibus(
                            node.get("grupo").asInt(),
                            node.get("nome").asText(),
                            node.get("latitude").asDouble(),
                            node.get("longitude").asDouble()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pontos;
    }
}

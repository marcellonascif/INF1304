package main;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import java.util.HashSet;
import java.util.Set;

public class PontoDeOnibus {
    private int numeroGrupo;
    private String nomePonto;
    private Coordinate coordenada;

    // Constante para o raio da Terra em metros
    private static final double RAIO_DA_TERRA_METROS = 6371e3;

    public PontoDeOnibus(int numeroGrupo, String nomePonto, double latitude, double longitude) {
        this.numeroGrupo = numeroGrupo;
        this.nomePonto = nomePonto;
        this.coordenada = new Coordinate(latitude, longitude);
    }

    public int getNumeroGrupo() {
        return numeroGrupo;
    }

    public String getNomePonto() {
        return nomePonto;
    }

    public Coordinate getCoordenada() {
        return coordenada;
    }

    @Override
    public String toString() {
        return "PontoDeOnibus{" +
                "numeroGrupo=" + numeroGrupo +
                ", nomePonto='" + nomePonto + '\'' +
                ", coordenada=" + coordenada +
                '}';
    }


    /**
     * Método que verifica se a coordenada dada está dentro de um raio de qualquer ponto de ônibus no HashSet
     *
     * @param coord_mn Coordenada do Mobile Node
     * @param raio Em metros, distância máxima para incluir a coordenada
     * @return boolean, Se a coordenada está dentro do raio
     */
    public boolean estaDentroDoRaio(Coordinate coord_mn, double raio) {

        double distancia = calcularDistancia(coord_mn, this.coordenada);
        System.out.printf("Distância até o ponto de ônibus %s(%d): %.2f metros\n", this.nomePonto, this.numeroGrupo, distancia);
        if (distancia <= raio) {
            return true;
        }

        return false;
    }

    /**
     * Método que calcula a distância entre duas coordenadas usando a fórmula de Haversine
     *
     * @param coord1 Primeira coordenada
     * @param coord2 Segunda coordenada
     * @return Distância entre as duas coordenadas em metros
     */
    static public double calcularDistancia(Coordinate coord1, Coordinate coord2) {
        double latitude1 = Math.toRadians(coord1.getLat());
        double longitude1 = Math.toRadians(coord1.getLon());
        double latitude2 = Math.toRadians(coord2.getLat());
        double longitude2 = Math.toRadians(coord2.getLon());

        double deltaLat = latitude2 - latitude1;
        double deltaLon = longitude2 - longitude1;

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(latitude1) * Math.cos(latitude2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Distância final em metros
        return RAIO_DA_TERRA_METROS * c;
    }

    static public double calcularTempo(Double distancia, Double velocidade) {
        double tempo = (distancia / (velocidade / 3.6)) / 60;

        if (tempo < 1) {
            return 1;
        }

        return tempo;
    }

}

package main;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import java.util.HashSet;
import java.util.Set;
import main.StaticLibrary;

public class PontoDeOnibus {
    private int numeroGrupo;
    private String nomePonto;
    private Coordinate coordenada;

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

        double distancia = StaticLibrary.calcularDistancia(coord_mn, this.coordenada);
        System.out.printf("Distância até o ponto de ônibus %s(%d): %.2f metros\n", this.nomePonto, this.numeroGrupo, distancia);
        if (distancia <= raio) {
            return true;
        }

        return false;
    }
}

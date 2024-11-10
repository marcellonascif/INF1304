package main;

import org.openstreetmap.gui.jmapviewer.Coordinate;

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
}
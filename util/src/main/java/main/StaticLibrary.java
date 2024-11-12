package main;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import org.openstreetmap.gui.jmapviewer.Coordinate;

public class StaticLibrary {
    private static final double RAIO_DA_TERRA_METROS = 6371e3;

    public static void setEnv(Map<String, String> newenv) throws Exception {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        }
        catch (NoSuchFieldException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for (Class cl : classes) {
                if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.putAll(newenv);
                }
            }
        }
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

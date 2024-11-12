package main;

public class Main {
    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Uso: java -jar MobileNodeProject.jar <tipoNode> <nomeNode> <latitude> <longitude>");
            System.out.println("tipoNode: 'pessoa' ou 'onibus'");
            return;
        }

        String tipoNode = args[0].toLowerCase();
        String nomeNode = args[1];

        double latitude;
        double longitude;

        try {
            latitude = Double.parseDouble(args[2]);
            longitude = Double.parseDouble(args[3]);

        } catch (NumberFormatException e) {
            System.out.println("[Erro]: Latitude e longitude devem ser números. Exemplo: -22.936826006961283 -43.18559736525978");
            return;
        }

        switch (tipoNode) {
            case "pessoa":
                PessoaNode pessoa = new PessoaNode(nomeNode, latitude, longitude);
                pessoa.fazTudo();
                break;
            case "onibus":
                OnibusNode onibus = new OnibusNode(nomeNode, latitude, longitude);
                onibus.fazTudo();
                break;
            default:
                System.out.println("[Erro]: tipoNode deve ser 'pessoa' ou 'onibus'");
        }

    }
}
# INF1304 - Trabalho 1

## Como rodar

### Iniciar Gateways

Clonar o projeto ContextNet Kafka Core:

```bash
git clone https://gitlab.com/lac-puc/context-net-kafka-core.git
```

Levantar a imagem Docker do gateway:

```bash
cd context-net-kafka-core
sudo docker-compose -f docker-start-gw.yml up
```

Para verificar se as imagens estão rodando:

```bash
sudo docker ps -a
```

### Compilar e rodar o ProcessingNode

```bash
cd ProcessingNode
mvn clean install
mvn exec:java -Dexec.mainClass="main.ProcessingNode"
```

### Compilar e rodar o MobileNode

```bash
cd MobileNode
mvn clean install
mvn exec:java -Dexec.mainClass="main.MainCKMobileNode"
```


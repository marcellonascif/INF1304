# INF1304 - Trabalho 1

## Como rodar

Iniciar Gateways
Clonar o projeto ContextNet Kafka Core
```git clone https://gitlab.com/lac-puc/context-net-kafka-core.git```

Levantar a imagem Docker do gateway:
```cd context-net-kafka-core``` 
```docker-start-gw.yml up```


Para verificar se as imagens estão rodando
```sudo docker ps -a```

Compilar e rodar o ProcessingNode
```cd ProcessingNode``` 
```mvn clean install```
```mvn exec:java -Dexec.mainClass="main.ProcessingNode"```

Compilar e rodar o MobileNode
```cd MobileNode```
```mvn clean install```
```mvn exec:java -Dexec.mainClass="main.MainCKMobileNode"```
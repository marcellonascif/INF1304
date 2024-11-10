# INF1304 - Trabalho 1
Autores: Thomas de Mello, Marcello Nascif e João Biscaia
## Pré-requisitos
- [Docker](https://docs.docker.com/get-docker/) e [Docker Compose](https://docs.docker.com/compose/install/)
- [Git](https://git-scm.com/downloads)
- [Java JDK 8+](https://www.oracle.com/java/technologies/javase-downloads.html)
- [Maven](https://maven.apache.org/install.html)

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

### Para compilar os projetos maven, execute o script 'compile_all.sh'

Conceder permissão de execução ao shell 
```bash
chmod +x compile_all.sh 
```
Rodar o script
```bash
./compile_all.sh
```


### Rodar o ProcessingNode

```bash
cd ProcessingNode
mvn exec:java -Dexec.mainClass="main.ProcessingNode"
```

### Rodar o MobileNode

```bash
cd MobileNode
mvn exec:java -Dexec.mainClass="main.MainCKMobileNode"
```

### Rodar o GroupDefiner

```bash
cd GroupDefiner
mvn exec:java -Dexec.mainClass="main.MainGD"
``` 

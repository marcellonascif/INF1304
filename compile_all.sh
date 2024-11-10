#!/bin/bash

# projetos a serem compilados
projetos=("util" "GroupDefiner" "MobileNode" "ProcessingNode")

# Loop através de cada diretório de projeto e executa o Maven clean install
for projeto in "${projetos[@]}"; do
    echo "Compilando projeto: $projeto"
    cd $projeto || exit
    mvn clean install
    if [ $? -ne 0 ]; then
        echo "Falha ao compilar o projeto: $projeto"
        exit 1
    fi
    cd ..
done

echo "Todos os projetos foram compilados com sucesso."
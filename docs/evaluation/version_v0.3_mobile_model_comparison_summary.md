\# Versão v0.3 — Comparação mobile de modelos TFLite



\## Objetivo da versão



A versão v0.3 registra a evolução do aplicativo Android após a integração de múltiplos modelos TensorFlow Lite e a criação de um fluxo de comparação local entre modelos.



Até a versão v0.2, o aplicativo executava inferência on-device usando principalmente o modelo YOLO26n Float32. Nesta versão, foi adicionado o modelo YOLOv8n Float32 ao app, permitindo comparar os dois modelos diretamente na interface Android.



\## Principais mudanças



As principais mudanças desta etapa foram:



1\. adição do modelo YOLOv8n Float32 como asset do aplicativo;

2\. criação de configurações para múltiplos modelos TFLite;

3\. atualização do detector local para carregar diferentes modelos;

4\. adição de seletor de modelo na interface;

5\. manutenção do YOLO26n Float32 como modelo padrão;

6\. exibição do modelo usado no resultado visual;

7\. documentação da primeira comparação mobile entre YOLO26n e YOLOv8n.



\## Modelos disponíveis no app



Após esta etapa, o aplicativo possui os seguintes modelos locais:



```text

YOLO26n Float32

YOLOv8n Float32

````



Arquivos no app:



```text

app/src/main/assets/classroom\_yolo26n\_e50\_best\_float32.tflite

app/src/main/assets/classroom\_yolov8n\_e50\_best\_float32.tflite

app/src/main/assets/labels.txt

```



Tamanhos dos modelos:



```text

YOLO26n Float32: 9.935.120 bytes

YOLOv8n Float32: 12.354.257 bytes

```



\## Interface do aplicativo



A interface foi atualizada para permitir a escolha do modelo local usado na inferência.



O modo de teste e comparação possui os botões:



```text

Usar YOLO26n Float32

Usar YOLOv8n Float32

Selecionar imagem para teste

Analisar com o modelo local selecionado

Analisar via backend

```



O fluxo principal também utiliza o modelo local atualmente selecionado.



\## Comparação inicial



Foi feita uma comparação inicial usando 5 imagens do dataset no emulador Android.



Imagens testadas:



1\. mochila/bolsa;

2\. régua;

3\. cadeira;

4\. tesoura;

5\. apontador.



Resumo dos resultados:



```text

YOLO26n Float32

Acertos: 5/5

Confiança média: 93,4%

Tempo médio aproximado: 455,8 ms

```



```text

YOLOv8n Float32

Acertos: 5/5

Confiança média: 91,8%

Tempo médio aproximado: 676,0 ms

```



\## Interpretação



Neste teste inicial, os dois modelos acertaram todos os objetos avaliados.



O YOLO26n Float32 apresentou menor tempo médio de inferência e menor tamanho de arquivo. O YOLOv8n Float32 também apresentou resultados corretos, mas foi mais lento no emulador.



Com base nessa comparação inicial, o YOLO26n Float32 continua sendo uma opção adequada como modelo principal do app, especialmente considerando o uso on-device em uma aplicação assistiva, onde tempo de resposta e eficiência são importantes.



\## Limitações da comparação



A comparação ainda é inicial e não deve ser tratada como uma avaliação definitiva.



Limitações:



\* uso de apenas 5 imagens;

\* imagens provenientes do dataset;

\* teste realizado em emulador;

\* ausência de comparação em dispositivo físico nesta etapa;

\* ausência de fotos reais controladas e não controladas nesta comparação específica.



\## Estado atual do app



Ao final da versão v0.3, o aplicativo possui:



\* inferência on-device com YOLO26n Float32;

\* inferência on-device com YOLOv8n Float32;

\* seletor de modelo local;

\* modo principal separado do modo de teste/debug;

\* resposta curta por Text-to-Speech;

\* descrições para TalkBack;

\* feedback por vibração;

\* documentação da comparação inicial entre modelos.



\## Próximos passos



Os próximos passos recomendados são:



1\. gerar ou adicionar uma versão YOLO26n Float16;

2\. comparar YOLO26n Float32 e YOLO26n Float16;

3\. verificar se a versão Float16 reduz tamanho e tempo de inferência sem perda relevante de detecção;

4\. melhorar o pré-processamento de imagem, especialmente com letterbox;

5\. avançar para CameraX e detecção semi-contínua;

6\. futuramente, testar o protótipo com usuários ou especialistas em acessibilidade.


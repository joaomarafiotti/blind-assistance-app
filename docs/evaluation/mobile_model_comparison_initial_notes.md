\# Comparação inicial de modelos TFLite no aplicativo Android



\## Objetivo



Registrar a primeira etapa de comparação entre modelos TFLite executados diretamente no aplicativo Android.



Nesta etapa, o objetivo não foi realizar ainda uma avaliação completa com várias imagens, mas sim preparar o aplicativo para permitir a comparação prática entre modelos locais usando a mesma interface.



\## Modelos comparados



Foram adicionados ao aplicativo os seguintes modelos:



```text

YOLO26n Float32

YOLOv8n Float32

````



O modelo YOLO26n Float32 já estava integrado ao app como modelo local principal. O modelo YOLOv8n Float32 foi adicionado como novo asset TFLite para permitir comparação direta no dispositivo.



\## Arquivos de modelo no app



```text

app/src/main/assets/classroom\_yolo26n\_e50\_best\_float32.tflite

app/src/main/assets/classroom\_yolov8n\_e50\_best\_float32.tflite

app/src/main/assets/labels.txt

```



Tamanhos dos arquivos:



```text

YOLO26n Float32: 9.935.120 bytes

YOLOv8n Float32: 12.354.257 bytes

```



\## Alterações no aplicativo



O app foi atualizado para permitir seleção do modelo local usado na inferência.



Foram adicionadas as opções:



```text

Usar YOLO26n Float32

Usar YOLOv8n Float32

```



O modelo selecionado aparece na interface e também é usado pelo botão principal de captura.



O modo de teste e comparação permite:



```text

Selecionar uma imagem

Escolher o modelo local

Analisar a mesma imagem com YOLO26n Float32

Analisar a mesma imagem com YOLOv8n Float32

Comparar confiança e tempo aproximado de inferência

```



\## Primeiro teste manual



Foi realizado um teste manual usando a mesma imagem contendo uma mochila/bolsa.



Resultado observado:



```text

YOLO26n Float32

Classe detectada: bolsa

Confiança: 91%

Tempo aproximado: 776 ms

```



```text

YOLOv8n Float32

Classe detectada: bolsa

Confiança: 97%

Tempo aproximado: 1152 ms

```



\## Interpretação inicial



Neste primeiro teste manual, os dois modelos detectaram corretamente o objeto como bolsa.



O YOLOv8n apresentou maior confiança na detecção, mas também apresentou maior tempo de inferência no dispositivo.



O YOLO26n foi mais rápido e também obteve uma detecção correta com alta confiança.



Esse resultado inicial sugere um possível trade-off entre:



```text

qualidade/confiança da detecção

tempo de inferência no dispositivo

tamanho do modelo

```



\## Estado atual



O aplicativo agora permite executar e comparar dois modelos TFLite locais:



```text

YOLO26n Float32

YOLOv8n Float32

```



A comparação ainda é inicial e precisa ser complementada com uma avaliação mais sistemática usando múltiplas imagens.



\## Próximos passos



Os próximos passos são:



1\. selecionar um conjunto fixo de imagens para comparação;

2\. testar YOLO26n e YOLOv8n nas mesmas imagens;

3\. registrar classe detectada, confiança, tempo aproximado e status do resultado;

4\. criar uma tabela comparativa;

5\. avaliar qual modelo é mais adequado para uso on-device no contexto assistivo.


\# Notas iniciais — YOLO26n Float16 no aplicativo Android



\## Objetivo



Registrar a exportação e integração inicial do modelo YOLO26n Float16 no aplicativo Android.



A versão Float16 foi gerada para avaliar se é possível reduzir o tamanho do modelo mantendo resultados semelhantes ao YOLO26n Float32 já usado no app.



\## Modelo de origem



O modelo Float16 foi exportado a partir do modelo YOLO26n treinado:



```text

/content/drive/MyDrive/IC\_Object\_Recognition/04\_models/yolo26n\_e50/classroom\_yolo26n\_e50\_best.pt

````



\## Arquivo exportado



O arquivo TFLite Float16 foi exportado e salvo em:



```text

/content/drive/MyDrive/IC\_Object\_Recognition/06\_exports\_mobile/yolo26n\_e50\_float16\_end2end/classroom\_yolo26n\_e50\_best\_float16.tflite

```



Depois, o arquivo foi adicionado ao app em:



```text

app/src/main/assets/classroom\_yolo26n\_e50\_best\_float16.tflite

```



\## Tamanho dos modelos



```text

YOLO26n Float32: 9.935.120 bytes

YOLO26n Float16: 5.100.303 bytes

```



A versão Float16 reduziu o tamanho do modelo em aproximadamente 48,7%.



\## Integração no app



O aplicativo foi atualizado para incluir uma terceira opção de modelo local:



```text

YOLO26n Float32

YOLO26n Float16

YOLOv8n Float32

```



A interface agora permite selecionar o modelo YOLO26n Float16 no modo de teste e comparação.



\## Primeiro teste manual



Foi realizado um teste rápido com uma imagem de mochila/bolsa.



Resultado observado:



```text

YOLO26n Float32

Classe detectada: bolsa

Confiança: 91%

Tempo aproximado: 465 ms

```



```text

YOLO26n Float16

Classe detectada: bolsa

Confiança: 91%

Tempo aproximado: 512 ms

```



Também foi observado um tempo inicial maior na primeira execução do Float16, aproximadamente 881 ms, provavelmente relacionado ao aquecimento/carregamento inicial do modelo.



\## Interpretação inicial



No primeiro teste, os dois modelos detectaram corretamente o objeto como bolsa, com a mesma confiança de 91%.



A versão Float16 apresentou tamanho muito menor, mas não foi mais rápida nesse teste em emulador.



Esse resultado é compatível com a possibilidade de que o TensorFlow Lite execute parte da inferência em Float32 na CPU, mesmo com pesos armazenados em Float16.



\## Estado atual



O YOLO26n Float16 está integrado e funcional no app.



Ainda é necessário realizar uma comparação um pouco mais organizada entre YOLO26n Float32 e YOLO26n Float16 usando as mesmas imagens de teste.



\## Próximos passos



1\. testar YOLO26n Float32 e YOLO26n Float16 nas mesmas 5 imagens usadas na comparação anterior;

2\. registrar classe detectada, confiança e tempo aproximado;

3\. comparar acerto, confiança média, tempo médio e tamanho de arquivo;

4\. decidir se o Float16 deve ser mantido apenas como opção experimental ou se pode substituir o Float32 como modelo principal.


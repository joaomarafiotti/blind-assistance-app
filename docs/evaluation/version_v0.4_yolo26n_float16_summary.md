\# Versão v0.4 — Integração e avaliação inicial do YOLO26n Float16



\## Objetivo da versão



A versão v0.4 registra a integração do modelo YOLO26n Float16 ao aplicativo Android e a comparação inicial com a versão YOLO26n Float32.



O objetivo principal desta etapa foi avaliar se uma versão mais leve do modelo poderia manter comportamento semelhante ao modelo Float32, reduzindo o tamanho do arquivo e potencialmente melhorando o uso em dispositivos móveis.



\## Contexto



Até a versão v0.3, o aplicativo já possuía:



\- inferência on-device com YOLO26n Float32;

\- inferência on-device com YOLOv8n Float32;

\- seletor de modelo local;

\- comparação inicial entre YOLO26n Float32 e YOLOv8n Float32;

\- feedback por voz, vibração e suporte a TalkBack;

\- separação entre fluxo principal e modo de teste/debug.



Na versão v0.4, foi adicionada uma terceira opção de modelo:



```text

YOLO26n Float16

````



\## Modelo de origem



O modelo Float16 foi exportado a partir do modelo treinado YOLO26n:



```text

/content/drive/MyDrive/IC\_Object\_Recognition/04\_models/yolo26n\_e50/classroom\_yolo26n\_e50\_best.pt

```



\## Arquivo exportado



O arquivo exportado foi salvo no Google Drive em:



```text

/content/drive/MyDrive/IC\_Object\_Recognition/06\_exports\_mobile/yolo26n\_e50\_float16\_end2end/classroom\_yolo26n\_e50\_best\_float16.tflite

```



E integrado ao app em:



```text

app/src/main/assets/classroom\_yolo26n\_e50\_best\_float16.tflite

```



\## Modelos disponíveis no aplicativo



Após esta etapa, o app possui três modelos locais:



```text

YOLO26n Float32

YOLO26n Float16

YOLOv8n Float32

```



Arquivos nos assets:



```text

app/src/main/assets/classroom\_yolo26n\_e50\_best\_float32.tflite

app/src/main/assets/classroom\_yolo26n\_e50\_best\_float16.tflite

app/src/main/assets/classroom\_yolov8n\_e50\_best\_float32.tflite

app/src/main/assets/labels.txt

```



\## Tamanho dos modelos



```text

YOLO26n Float32: 9.935.120 bytes

YOLO26n Float16: 5.100.303 bytes

YOLOv8n Float32: 12.354.257 bytes

```



A versão YOLO26n Float16 reduziu o tamanho do modelo em aproximadamente 48,7% em relação à versão YOLO26n Float32.



\## Alterações no app



O aplicativo foi atualizado para permitir selecionar o YOLO26n Float16 na interface.



O modo de teste e comparação agora possui as opções:



```text

Usar YOLO26n Float32

Usar YOLO26n Float16

Usar YOLOv8n Float32

```



O modelo selecionado é usado tanto no modo de teste quanto no fluxo principal de captura.



\## Comparação inicial entre Float32 e Float16



A comparação inicial foi feita no emulador Android usando 5 imagens do dataset:



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

Tempo médio aproximado: 458,0 ms

Tamanho: 9.935.120 bytes

```



```text

YOLO26n Float16

Acertos: 5/5

Confiança média: 93,4%

Tempo médio aproximado: 519,2 ms

Tamanho: 5.100.303 bytes

```



\## Interpretação



Nos testes iniciais, o YOLO26n Float16 manteve o mesmo comportamento de detecção do YOLO26n Float32 nas imagens avaliadas.



Os dois modelos acertaram os 5 objetos testados e obtiveram a mesma confiança média da detecção principal.



A principal vantagem observada no Float16 foi a redução significativa do tamanho do arquivo.



Em relação ao tempo de inferência, o Float16 não apresentou ganho consistente no emulador. Em alguns casos foi mais rápido, mas em um caso específico, cadeira, apresentou tempo maior. Por isso, ainda não é possível afirmar que o Float16 é mais rápido de forma geral.



\## Conclusão parcial



O YOLO26n Float16 é uma opção promissora para o aplicativo porque:



\* manteve os acertos do Float32 nos testes iniciais;

\* manteve a mesma confiança média da detecção principal;

\* reduziu quase pela metade o tamanho do modelo;

\* funcionou corretamente no aplicativo Android.



Mesmo assim, o YOLO26n Float32 deve continuar como modelo padrão por enquanto, pois já foi mais testado em diferentes cenários.



O YOLO26n Float16 deve permanecer integrado como opção experimental para novas comparações, especialmente em dispositivo físico e em fotos reais.



\## Estado atual do app após a v0.4



Ao final desta versão, o aplicativo possui:



\* inferência on-device com YOLO26n Float32;

\* inferência on-device com YOLO26n Float16;

\* inferência on-device com YOLOv8n Float32;

\* seletor de modelo local;

\* fluxo principal separado do modo de teste/debug;

\* resposta curta por Text-to-Speech;

\* suporte a TalkBack;

\* feedback por vibração;

\* documentação das comparações entre modelos.



\## Próximos passos



Os próximos passos recomendados são:



1\. manter YOLO26n Float32 como modelo padrão;

2\. testar YOLO26n Float16 em dispositivo físico;

3\. comparar Float32 e Float16 em fotos reais controladas;

4\. implementar melhoria de pré-processamento com letterbox;

5\. avaliar se o letterbox melhora o desempenho em fotos reais;

6\. depois avançar para CameraX e detecção semi-contínua.


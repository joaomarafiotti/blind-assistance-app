\# Avaliação final consolidada



\## Objetivo



Este documento consolida os principais resultados técnicos obtidos durante o desenvolvimento do aplicativo Android para reconhecimento de objetos em aplicações assistivas.



O objetivo do projeto foi investigar e implementar uma solução capaz de reconhecer objetos comuns em ambiente escolar/doméstico, com foco em uso por pessoas cegas ou com deficiência visual, priorizando inferência local, resposta por voz, vibração e funcionamento no dispositivo móvel.



\## Evolução da implementação



O desenvolvimento evoluiu em etapas incrementais:



| Versão | Marco |

|---|---|

| v0.1 | Baseline cliente-servidor com backend FastAPI |

| v0.2 | Inferência on-device com TensorFlow Lite |

| v0.4 | Comparação inicial com YOLO26n Float16 |

| v0.5 | Pré-processamento com letterbox |

| v0.6 | Validação física com fotos reais capturadas pelo app |

| v0.7 | Detecção semi-contínua com CameraX |



Essa evolução permitiu migrar de uma arquitetura dependente de backend para uma solução mais adequada ao contexto assistivo, com execução local e menor dependência de conexão com a internet.



\## Dataset e classes



Foi utilizado o dataset Objects in the Classroom, composto por 20 classes de objetos comuns em ambiente escolar:



```text

table, chair, whiteboard, bookshelf, clock, wall-magazine, trash-can,

eraser, sharpener, pen, book, ruler, scissor, fan, laptop,

remote-control, bag, pants, shoes, hat

````



Essas classes foram escolhidas por estarem alinhadas a um cenário de apoio à navegação e identificação de objetos em ambientes internos.



\## Modelos avaliados



Foram avaliados modelos da família YOLO em versões compactas:



\* YOLOv8n;

\* YOLO26n.



Ambos foram utilizados a partir de pesos pré-treinados e posteriormente ajustados ao dataset do projeto por meio de fine-tuning.



Também foram avaliadas exportações TensorFlow Lite em diferentes precisões:



\* YOLO26n Float32;

\* YOLO26n Float16;

\* YOLOv8n Float32.



\## Modelo final adotado



O modelo adotado como padrão final do aplicativo foi:



```text

YOLO26n Float32

```



Esse modelo está presente no app como:



```text

classroom\_yolo26n\_e50\_best\_float32.tflite

```



Ele foi escolhido porque apresentou bom equilíbrio entre acurácia, tempo de inferência, estabilidade e compatibilidade com o modo contínuo implementado com CameraX.



\## Justificativa da escolha do YOLO26n Float32



A escolha do YOLO26n Float32 foi baseada em três fatores principais:



1\. desempenho adequado em dispositivo móvel;

2\. integração estável com TensorFlow Lite;

3\. melhor alinhamento com a proposta de inferência on-device.



Embora o YOLOv8n também tenha apresentado bons resultados, ele não demonstrou vantagem prática suficiente nos testes finais para substituir o YOLO26n.



A versão YOLO26n Float16 apresentou resultados promissores e arquivo menor, mas foi mantida como alternativa experimental por exigir validação mais ampla no modo contínuo.



\## Comparação final curta entre modelos



Foi realizado um teste curto em dispositivo físico com três objetos reais:



\* caneta;

\* controle remoto;

\* bolsa/mochila.



Resultados:



| Objeto          | YOLO26n Float32 | YOLO26n Float16 | YOLOv8n Float32 |

| --------------- | --------------: | --------------: | --------------: |

| Caneta          |    88% / 189 ms |    88% / 205 ms |    90% / 262 ms |

| Controle remoto |    93% / 133 ms |    94% / 105 ms |    90% / 149 ms |

| Bolsa/mochila   |     97% / 94 ms |    97% / 126 ms |    93% / 172 ms |



Resumo por modelo:



| Modelo          | Acertos | Confiança média | Tempo médio |

| --------------- | ------: | --------------: | ----------: |

| YOLO26n Float32 |     3/3 |           92,7% |    138,7 ms |

| YOLO26n Float16 |     3/3 |           93,0% |    145,3 ms |

| YOLOv8n Float32 |     3/3 |           91,0% |    194,3 ms |



Com base nesse teste, o YOLO26n Float32 foi mantido como modelo padrão por apresentar o menor tempo médio e por já ter sido mais validado ao longo do desenvolvimento.



\## Testes com imagens do dataset



Nos testes iniciais com imagens do dataset em ambiente controlado, o modelo on-device apresentou bons resultados.



Na avaliação com 20 imagens do dataset no emulador, foram observados:



| Métrica                | Resultado |

| ---------------------- | --------: |

| Imagens avaliadas      |        20 |

| Acertos                |        17 |

| Parcialmente correto   |         1 |

| Erros                  |         1 |

| Sem detecção           |         1 |

| Acurácia simples       |       85% |

| Acertos + parciais     |       90% |

| Tempo médio aproximado |    368 ms |



Esses resultados indicaram que o modelo era capaz de reconhecer objetos do conjunto treinado em condições semelhantes às do dataset.



\## Teste em dispositivo físico com imagens do dataset



O app também foi testado em um Samsung S25 FE com imagens do dataset.



Resumo:



| Métrica                | Resultado |

| ---------------------- | --------: |

| Imagens avaliadas      |        10 |

| Acertos                |         8 |

| Erros                  |         1 |

| Sem detecção           |         1 |

| Tempo médio aproximado |  167,3 ms |



Esse teste mostrou que a execução em dispositivo físico foi mais rápida que no emulador, reforçando a viabilidade da inferência local.



\## Testes com fotos reais



Foram realizados testes com fotos reais capturadas pelo app.



No primeiro teste físico com fotos reais, foram avaliadas 7 imagens capturadas diretamente pela câmera do celular.



Resumo:



| Métrica                | Resultado |

| ---------------------- | --------: |

| Imagens avaliadas      |         7 |

| Acertos                |         3 |

| Erros                  |         1 |

| Sem detecção           |         3 |

| Tempo médio aproximado |  289,9 ms |



Esse teste mostrou que o app funcionava tecnicamente no celular, mas também evidenciou limitações do modelo em fotos reais fora das condições do dataset.



\## Pré-processamento com letterbox



Foi implementado pré-processamento com letterbox para preservar a proporção da imagem antes da inferência.



Antes disso, as imagens eram redimensionadas diretamente para 640x640, o que poderia distorcer objetos em imagens não quadradas.



Na validação curta com 5 imagens do dataset:



| Métrica                | Resize direto | Letterbox |

| ---------------------- | ------------: | --------: |

| Acertos                |           5/5 |       5/5 |

| Confiança média        |         93,4% |     93,4% |

| Tempo médio aproximado |      458,0 ms |  476,6 ms |



O letterbox foi mantido porque preserva melhor a geometria da imagem e não prejudicou os resultados no teste inicial.



\## Detecção semi-contínua com CameraX



A versão v0.7 adicionou o modo de detecção semi-contínua com CameraX.



O fluxo implementado foi:



```text

abrir câmera no app

capturar frames periodicamente

converter frame para Bitmap

aplicar letterbox

executar inferência TFLite

mostrar resultado no overlay

falar resultado por Text-to-Speech

emitir vibração

controlar repetição com cooldown

```



Esse modo foi adicionado para reduzir a dependência de uma única foto perfeita, o que é importante em uma aplicação voltada para usuários cegos ou com deficiência visual.



\## Teste funcional do CameraX



O modo CameraX foi testado em dispositivo físico com objetos reais.



Foram registradas 10 observações:



| Métrica                           | Resultado |

| --------------------------------- | --------: |

| Observações registradas           |        10 |

| Detecções semanticamente corretas |        10 |

| Tempo médio aproximado            |  214,2 ms |

| Menor tempo observado             |    111 ms |

| Maior tempo observado             |    467 ms |

| Confiança média aproximada        |     60,9% |



O app conseguiu abrir a câmera, processar frames, executar inferência local, atualizar o overlay, falar resultados, vibrar e reiniciar o modo contínuo sem travar.



\## Ajustes de acessibilidade



Durante os testes, foi observado que a fala inicial era longa demais, pois incluía a porcentagem exata de confiança.



Por isso, o feedback por voz foi ajustado para categorias:



| Faixa         | Categoria falada         |

| ------------- | ------------------------ |

| 80% a 100%    | confiança alta           |

| 50% a 79%     | confiança média          |

| 30% a 49%     | confiança baixa          |

| abaixo de 30% | sem confiança suficiente |



A porcentagem continua visível no overlay para depuração, mas a fala ficou mais curta e adequada para uso real.



\## Funcionalidades finais implementadas



Ao final da implementação, o app contém:



\* inferência local com TensorFlow Lite;

\* suporte a múltiplos modelos TFLite;

\* modelo padrão YOLO26n Float32;

\* alternativa YOLO26n Float16;

\* alternativa YOLOv8n Float32;

\* pré-processamento com letterbox;

\* captura de foto pelo app;

\* seleção de imagem para teste;

\* modo CameraX com detecção semi-contínua;

\* Text-to-Speech em português;

\* feedback por vibração;

\* labels traduzidos para português;

\* cooldown para reduzir repetição no modo contínuo;

\* documentação incremental dos testes.



\## Limitações



Apesar dos avanços, o sistema ainda possui limitações:



\* o modelo ainda apresenta dificuldade em algumas fotos reais;

\* objetos fora das classes treinadas podem ser confundidos com classes conhecidas;

\* o app ainda não usa bounding boxes para orientação espacial;

\* cenas com múltiplos objetos ainda não possuem feedback por voz avançado;

\* os testes reais foram feitos com amostras pequenas;

\* ainda não foi realizada validação formal com usuários cegos ou com deficiência visual;

\* o modo Float16 ainda precisa de validação mais ampla no CameraX.



\## Trabalhos futuros



Como trabalhos futuros, recomenda-se:



1\. validar o app com usuários reais;

2\. ampliar a base de testes com fotos reais;

3\. testar YOLO26n Float16 no modo CameraX;

4\. avaliar consumo de bateria e aquecimento;

5\. usar bounding boxes para orientação espacial;

6\. implementar frases como “aproxime o celular” ou “mova um pouco para a esquerda”;

7\. melhorar o feedback para múltiplos objetos;

8\. treinar com mais imagens reais capturadas em condições variadas;

9\. comparar modelos maiores em backend com modelos menores on-device;

10\. separar melhor o modo assistivo final e o modo de depuração.



\## Conclusão



O projeto demonstrou a viabilidade de um aplicativo Android assistivo com reconhecimento de objetos on-device.



A evolução do sistema mostrou que é possível executar modelos YOLO exportados para TensorFlow Lite em dispositivo móvel, com resposta por voz e vibração. A implementação do CameraX tornou o fluxo mais adequado ao uso assistivo, pois permite análise semi-contínua em vez de depender de uma única foto capturada manualmente.



O modelo YOLO26n Float32 foi mantido como escolha final por apresentar bom desempenho, estabilidade e melhor validação ao longo do desenvolvimento.



Assim, a implementação final fornece uma base funcional para reconhecimento de objetos em ambiente interno, com potencial de evolução para validação com usuários reais e melhorias de orientação espacial.


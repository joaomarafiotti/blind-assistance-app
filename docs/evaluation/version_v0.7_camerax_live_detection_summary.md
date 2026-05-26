\# Resumo da versão v0.7 — CameraX Live Detection



\## Marco da versão



A versão v0.7 marca a primeira implementação funcional do modo de detecção semi-contínua com CameraX no aplicativo Android.



Até a versão v0.6, o app já realizava inferência local com TensorFlow Lite a partir de fotos capturadas ou selecionadas. Na v0.7, o app passou a manter a câmera aberta, processar frames periodicamente e fornecer feedback contínuo por texto, voz e vibração.



\## Funcionalidades implementadas



Nesta versão foram adicionados:



\- integração com CameraX;

\- preview da câmera dentro do app;

\- uso de ImageAnalysis para processar frames da câmera;

\- conversão de frames para Bitmap;

\- execução do detector YOLO26n Float32 nos frames capturados;

\- overlay visual com classe detectada, confiança, tempo de inferência e índice da análise;

\- feedback por Text-to-Speech no modo contínuo;

\- feedback por vibração no modo contínuo;

\- cooldown para reduzir repetição de fala e vibração;

\- tradução dos labels para português no modo contínuo;

\- ajustes de fala para usar categorias de confiança em vez de porcentagem numérica.



\## Modelo usado



O modelo usado no modo contínuo foi:



```text

YOLO26n Float32

````



Esse modelo foi escolhido porque já era o modelo padrão do app, já havia sido validado em testes anteriores e apresentou bom equilíbrio entre tamanho, desempenho e precisão.



\## Fluxo da detecção contínua



O fluxo implementado é:



```text

abrir câmera com CameraX

capturar frames periodicamente

converter frame para Bitmap

aplicar pré-processamento com letterbox

executar inferência TFLite on-device

mostrar resultado no overlay

falar resultado por TTS

emitir vibração

evitar repetição excessiva com cooldown

```



\## Ajustes de acessibilidade



A versão inicial falava a porcentagem exata de confiança, por exemplo:



```text

Detectado controle remoto. Confiança 94 por cento.

```



Durante o teste físico, foi observado que essa fala era longa demais para o modo contínuo e podia ser interrompida por novas detecções.



Por isso, a fala foi ajustada para mensagens mais curtas:



```text

sapato detectado. Confiança alta.

possível objeto: caneta. Confiança média.

possível objeto: livro. Confiança baixa.

```



A porcentagem continua visível no overlay para fins de teste e depuração, mas a resposta por voz ficou mais adequada para o usuário final.



\## Thresholds e categorias de confiança



A versão passou a usar categorias de confiança:



| Faixa         | Categoria                |

| ------------- | ------------------------ |

| 80% a 100%    | confiança alta           |

| 50% a 79%     | confiança média          |

| 30% a 49%     | confiança baixa          |

| abaixo de 30% | sem confiança suficiente |



\## Teste físico



O modo CameraX foi testado em dispositivo físico com objetos reais.



Foram observados resultados positivos para objetos como:



\* sapato;

\* caneta;

\* livro;

\* bolsa/mochila;

\* cadeira;

\* notebook.



O app conseguiu:



\* abrir a câmera dentro da interface;

\* processar frames sem travar;

\* atualizar o overlay;

\* executar inferência local;

\* falar os resultados;

\* vibrar conforme detecção;

\* parar e iniciar novamente o modo contínuo.



\## Resultados observados



Na amostra documentada, foram registradas 10 observações com detecções semanticamente corretas.



Resumo:



| Métrica                           | Resultado |

| --------------------------------- | --------: |

| Observações registradas           |        10 |

| Detecções semanticamente corretas |        10 |

| Tempo médio aproximado            |  214,2 ms |

| Menor tempo observado             |    111 ms |

| Maior tempo observado             |    467 ms |

| Confiança média aproximada        |     60,9% |



\## Importância para o projeto



A v0.7 é um marco importante porque transforma o app de um fluxo baseado em foto única para um fluxo assistivo mais interativo.



Em uma aplicação para usuários cegos ou com deficiência visual, depender de uma foto perfeita pode ser limitante. O modo contínuo permite que o usuário mova o celular, ajuste o enquadramento e receba novos feedbacks ao longo do tempo.



\## Limitações atuais



A versão ainda possui limitações:



\* não usa bounding boxes para orientar espacialmente o usuário;

\* não diferencia múltiplos objetos por voz de forma avançada;

\* ainda depende da robustez do modelo treinado;

\* algumas detecções aparecem com confiança média ou baixa;

\* objetos fora das classes treinadas podem ser confundidos com classes conhecidas;

\* o modo contínuo usa o objeto mais confiante como principal resultado.



\## Possíveis melhorias futuras



Melhorias futuras incluem:



1\. validar bounding boxes no preview;

2\. orientar o usuário por áudio com frases como "mova um pouco para a esquerda" ou "aproxime o celular";

3\. testar estratégia de múltiplos objetos;

4\. comparar YOLO26n Float32 e YOLO26n Float16 no modo contínuo;

5\. ajustar thresholds com uma amostra maior de fotos reais;

6\. separar visualmente modo assistivo e modo de depuração;

7\. avaliar consumo de bateria e aquecimento.



\## Conclusão



A versão v0.7 demonstra que é viável executar detecção semi-contínua on-device em um app Android assistivo usando CameraX, TensorFlow Lite, Text-to-Speech e vibração.



Esse marco consolida a implementação principal do aplicativo e fornece uma base funcional para a avaliação final da IC.


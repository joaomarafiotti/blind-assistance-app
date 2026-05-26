\# Validação em dispositivo físico com fotos reais capturadas pelo app



\## Objetivo



Registrar uma validação curta do aplicativo Android em dispositivo físico após a implementação do pré-processamento com letterbox.



O objetivo foi testar o fluxo principal do app em um cenário mais próximo de uso real:



```text

tirar foto pelo app -> executar inferência on-device -> receber resposta por voz, texto e vibração

````



\## Configuração do teste



\* Aplicativo: Blind Assistance App

\* Dispositivo: Samsung S25 FE

\* Modelo: YOLO26n Float32

\* Pré-processamento: letterbox 640x640

\* Modo de captura: botão principal "Tirar foto e ouvir resultado"

\* Fonte das imagens: fotos reais capturadas na hora

\* Número de imagens: 7



\## Objetos testados



1\. mochila/bolsa;

2\. régua;

3\. cadeira;

4\. tesoura;

5\. caneta;

6\. livro/mangá;

7\. sapato.



\## Resultados



| Imagem | Objeto esperado | Resultado    | Confiança |  Tempo | Status       |

| ------ | --------------- | ------------ | --------: | -----: | ------------ |

| 01     | mochila/bolsa   | bolsa        |       94% | 179 ms | correto      |

| 02     | régua           | sem detecção |         - | 290 ms | sem detecção |

| 03     | cadeira         | sem detecção |         - | 330 ms | sem detecção |

| 04     | tesoura         | sem detecção |         - | 297 ms | sem detecção |

| 05     | caneta          | caneta       |       89% | 340 ms | correto      |

| 06     | livro/mangá     | mural        |       85% | 336 ms | erro         |

| 07     | sapato          | sapato       |       84% | 257 ms | correto      |



\## Resumo quantitativo



| Métrica                | Resultado |

| ---------------------- | --------: |

| Total de imagens       |         7 |

| Acertos                |         3 |

| Erros                  |         1 |

| Casos sem detecção     |         3 |

| Taxa de acerto simples |     42,9% |

| Tempo médio aproximado |  289,9 ms |

| Menor tempo observado  |    179 ms |

| Maior tempo observado  |    340 ms |



\## Interpretação



O teste confirmou que o fluxo principal do aplicativo funciona corretamente em dispositivo físico.



O app conseguiu capturar fotos pela câmera, executar inferência local com TensorFlow Lite, apresentar resultado visual, falar a resposta por Text-to-Speech e emitir feedback por vibração.



O tempo de inferência observado no celular físico foi bom, com média aproximada de 289,9 ms.



No entanto, os resultados também mostram limitações importantes do modelo em fotos reais. Objetos como régua, cadeira e tesoura não foram reconhecidos com segurança. O livro/mangá foi classificado incorretamente como mural.



Esses resultados reforçam que o problema principal não é apenas a execução mobile, mas também a robustez do modelo em condições reais de captura.



\## Relação com acessibilidade



Para uma aplicação assistiva voltada a usuários cegos ou com deficiência visual, depender de uma única foto pode ser limitante.



O usuário pode não enquadrar corretamente o objeto, pode estar longe demais, pode capturar com ângulo ruim ou iluminação inadequada.



Por isso, os resultados desta validação reforçam a necessidade de evoluir o app para um modo mais interativo, com câmera contínua ou semi-contínua.



\## Conclusão parcial



A validação em dispositivo físico mostrou que:



\* o app funciona no celular real;

\* o fluxo principal de captura está operacional;

\* o tempo de inferência é aceitável;

\* o feedback por voz e vibração é adequado;

\* o modelo ainda apresenta limitações em fotos reais capturadas na hora.



A próxima etapa técnica deve investigar CameraX e detecção semi-contínua, para reduzir a dependência de uma única foto capturada manualmente.



\## Próximos passos



Os próximos passos recomendados são:



1\. planejar a arquitetura da detecção semi-contínua com CameraX;

2\. processar frames em intervalos controlados;

3\. adicionar cooldown de Text-to-Speech para evitar repetição excessiva;

4\. manter feedback por vibração;

5\. futuramente, orientar o usuário por áudio durante a câmera aberta.


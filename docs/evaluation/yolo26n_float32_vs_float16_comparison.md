\# Comparação entre YOLO26n Float32 e YOLO26n Float16 no aplicativo Android



\## Objetivo



Registrar a comparação inicial entre duas versões do mesmo modelo YOLO26n executadas no aplicativo Android com TensorFlow Lite:



\- YOLO26n Float32;

\- YOLO26n Float16.



A versão Float16 foi gerada para avaliar se seria possível reduzir o tamanho do modelo mantendo comportamento semelhante ao Float32.



\## Modelos comparados



Arquivos usados no aplicativo:



```text

app/src/main/assets/classroom\_yolo26n\_e50\_best\_float32.tflite

app/src/main/assets/classroom\_yolo26n\_e50\_best\_float16.tflite

````



Tamanhos dos arquivos:



```text

YOLO26n Float32: 9.935.120 bytes

YOLO26n Float16: 5.100.303 bytes

```



A versão Float16 reduziu o tamanho do modelo em aproximadamente 48,7%.



\## Contexto do teste



\* Aplicativo: Blind Assistance App

\* Execução: on-device com TensorFlow Lite

\* Ambiente: emulador Android

\* Fonte das imagens: imagens do dataset

\* Número de imagens testadas: 5



As imagens testadas foram:



1\. mochila/bolsa;

2\. régua;

3\. cadeira;

4\. tesoura;

5\. apontador.



\## Resultados por imagem



| Imagem | Objeto esperado |              YOLO26n Float32 | Tempo Float32 |              YOLO26n Float16 | Tempo Float16 | Observação                                                     |

| ------ | --------------- | ---------------------------: | ------------: | ---------------------------: | ------------: | -------------------------------------------------------------- |

| 01     | mochila/bolsa   |                    bolsa 91% |        465 ms |                    bolsa 91% |        430 ms | ambos corretos                                                 |

| 02     | régua           |                    régua 90% |        476 ms |                    régua 90% |        376 ms | ambos corretos                                                 |

| 03     | cadeira         |                  cadeira 94% |        439 ms |                  cadeira 94% |        888 ms | ambos corretos; Float16 teve tempo alto neste caso             |

| 04     | tesoura         |                  tesoura 98% |        444 ms |                  tesoura 98% |        435 ms | ambos corretos                                                 |

| 05     | apontador       | apontador 94% + borracha 73% |        466 ms | apontador 94% + borracha 72% |        467 ms | ambos acertaram o objeto principal e retornaram detecção extra |



\## Resumo quantitativo



| Métrica                               | YOLO26n Float32 | YOLO26n Float16 |

| ------------------------------------- | --------------: | --------------: |

| Acertos                               |             5/5 |             5/5 |

| Erros                                 |             0/5 |             0/5 |

| Casos sem detecção                    |             0/5 |             0/5 |

| Confiança média da detecção principal |           93,4% |           93,4% |

| Tempo médio aproximado                |        458,0 ms |        519,2 ms |

| Menor tempo observado                 |          439 ms |          376 ms |

| Maior tempo observado                 |          476 ms |          888 ms |

| Tamanho do modelo                     | 9.935.120 bytes | 5.100.303 bytes |



\## Interpretação



Neste teste inicial, o YOLO26n Float16 manteve o mesmo desempenho de detecção observado no YOLO26n Float32.



Os dois modelos acertaram os 5 objetos avaliados, e a confiança média da detecção principal foi igual: 93,4%.



A principal vantagem do Float16 foi o tamanho do arquivo. A versão Float16 ficou quase metade do tamanho da versão Float32.



Em relação ao tempo de inferência, o Float16 não apresentou ganho consistente no emulador. Em algumas imagens foi mais rápido, como régua e bolsa, mas no caso da cadeira apresentou tempo muito maior. Esse valor pode ter sido influenciado por variação do emulador, aquecimento do modelo ou instabilidade da execução.



Sem o caso da cadeira, o tempo médio do Float16 nas outras quatro imagens foi aproximadamente 427,0 ms, o que fica abaixo da média observada no Float32. Ainda assim, por enquanto não é possível afirmar que o Float16 é consistentemente mais rápido.



\## Conclusão parcial



A versão YOLO26n Float16 é promissora porque:



\* manteve os mesmos acertos do Float32 nos exemplos testados;

\* manteve a mesma confiança média da detecção principal;

\* reduziu bastante o tamanho do modelo;

\* funcionou corretamente no aplicativo Android.



Por outro lado, ela ainda deve ser tratada como opção experimental até ser testada em dispositivo físico e em mais cenários reais.



Com base nos testes atuais, o YOLO26n Float32 continua sendo uma escolha segura como modelo padrão. O YOLO26n Float16 deve permanecer integrado ao app para novas comparações, principalmente em dispositivo físico.



\## Próximos passos



Os próximos passos recomendados são:



1\. testar YOLO26n Float16 em dispositivo físico;

2\. comparar Float32 e Float16 em fotos reais controladas;

3\. avaliar se o Float16 mantém estabilidade fora do dataset;

4\. decidir se o Float16 pode substituir o Float32 como modelo principal;

5\. depois avançar para melhorias de pré-processamento, como letterbox.


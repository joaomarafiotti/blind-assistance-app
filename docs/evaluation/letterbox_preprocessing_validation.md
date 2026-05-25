\# Validação inicial do pré-processamento com letterbox



\## Objetivo



Registrar uma validação curta do pré-processamento com letterbox no aplicativo Android.



Antes desta etapa, o aplicativo redimensionava qualquer imagem diretamente para 640x640. Essa abordagem é simples, mas pode distorcer imagens que não possuem proporção quadrada.



Com o letterbox, a imagem passa a ser redimensionada preservando sua proporção original, e o espaço restante é preenchido com bordas.



\## Configuração do teste



\- Aplicativo: Blind Assistance App

\- Modelo: YOLO26n Float32

\- Ambiente: emulador Android

\- Fonte das imagens: dataset

\- Imagens testadas: 5

\- Comparação:

&#x20; - antes: resize direto para 640x640

&#x20; - depois: letterbox para 640x640 preservando proporção



\## Imagens avaliadas



1\. mochila/bolsa;

2\. régua;

3\. cadeira;

4\. tesoura;

5\. apontador.



\## Resultados



| Imagem | Objeto esperado | Antes do letterbox | Tempo antes | Depois do letterbox | Tempo depois | Observação |

|---|---|---:|---:|---:|---:|---|

| 01 | mochila/bolsa | bolsa 91% | 465 ms | bolsa 91% | 594 ms | ambos corretos |

| 02 | régua | régua 90% | 476 ms | régua 90% | 419 ms | ambos corretos |

| 03 | cadeira | cadeira 94% | 439 ms | cadeira 94% | 444 ms | ambos corretos |

| 04 | tesoura | tesoura 98% | 444 ms | tesoura 98% | 545 ms | ambos corretos |

| 05 | apontador | apontador 94% + borracha 73% | 466 ms | apontador 94% + borracha 73% | 381 ms | ambos acertaram o objeto principal e retornaram detecção extra |



\## Resumo quantitativo



| Métrica | Resize direto | Letterbox |

|---|---:|---:|

| Acertos | 5/5 | 5/5 |

| Erros | 0/5 | 0/5 |

| Casos sem detecção | 0/5 | 0/5 |

| Confiança média da detecção principal | 93,4% | 93,4% |

| Tempo médio aproximado | 458,0 ms | 476,6 ms |

| Menor tempo observado | 439 ms | 381 ms |

| Maior tempo observado | 476 ms | 594 ms |



\## Interpretação



Nesta validação inicial com imagens do dataset, o letterbox não prejudicou a qualidade das detecções.



As mesmas cinco imagens continuaram sendo reconhecidas corretamente, e a confiança média da detecção principal permaneceu igual: 93,4%.



Em relação ao tempo, o letterbox apresentou tempo médio um pouco maior no emulador. A diferença média observada foi de aproximadamente 18,6 ms. Essa diferença é aceitável nesta etapa, considerando que o letterbox adiciona uma etapa extra de pré-processamento para preservar a proporção da imagem.



O comportamento no caso do apontador permaneceu o mesmo: o modelo detectou corretamente apontador, mas também retornou borracha como detecção secundária.



\## Conclusão parcial



Com base nesta validação curta, o letterbox pode ser mantido no app porque:



\- não quebrou a inferência local;

\- manteve os acertos nas imagens avaliadas;

\- manteve a confiança média da detecção principal;

\- preserva melhor a proporção das imagens antes da inferência.



Ainda não é possível afirmar que o letterbox melhora os resultados em todos os cenários. A principal justificativa técnica nesta etapa é evitar distorção geométrica em imagens que não são quadradas.



\## Próximos passos



Os próximos passos recomendados são:



1\. testar o letterbox em fotos reais controladas;

2\. observar se há melhora em objetos alongados ou imagens não quadradas;

3\. testar no celular físico;

4\. documentar os resultados com fotos reais;

5\. depois avançar para CameraX e detecção semi-contínua.


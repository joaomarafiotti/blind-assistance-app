# Comparação mobile entre YOLO26n Float32 e YOLOv8n Float32

## Objetivo

Registrar uma comparação inicial entre os modelos YOLO26n Float32 e YOLOv8n Float32 executados diretamente no aplicativo Android com TensorFlow Lite.

A comparação foi feita no modo de teste do aplicativo, usando as mesmas imagens para os dois modelos.

## Contexto do teste

- Aplicativo: Blind Assistance App
- Execução: on-device com TensorFlow Lite
- Ambiente: emulador Android
- Fonte das imagens: imagens do dataset
- Número de imagens testadas: 5
- Modelos comparados:
  - YOLO26n Float32
  - YOLOv8n Float32

As imagens usadas foram:

1. mochila/bolsa
2. régua
3. cadeira
4. tesoura
5. apontador

## Resultados por imagem

| Imagem | Objeto esperado | YOLO26n Float32 | Tempo YOLO26n | YOLOv8n Float32 | Tempo YOLOv8n | Observação |
|---|---|---:|---:|---:|---:|---|
| 01 | mochila/bolsa | bolsa 91% | 437 ms | bolsa 97% | 617 ms | ambos corretos |
| 02 | régua | régua 90% | 440 ms | régua 89% | 627 ms | ambos corretos |
| 03 | cadeira | cadeira 94% | 480 ms | cadeira 88% | 596 ms | ambos corretos |
| 04 | tesoura | tesoura 98% | 487 ms | tesoura 93% | 747 ms | ambos corretos |
| 05 | apontador | apontador 94% + borracha 73% | 435 ms | apontador 92% | 793 ms | YOLO26n acertou, mas retornou detecção extra |

## Resumo quantitativo

| Métrica | YOLO26n Float32 | YOLOv8n Float32 |
|---|---:|---:|
| Acertos | 5/5 | 5/5 |
| Erros | 0/5 | 0/5 |
| Casos sem detecção | 0/5 | 0/5 |
| Confiança média da detecção principal | 93,4% | 91,8% |
| Tempo médio aproximado | 455,8 ms | 676,0 ms |
| Menor tempo observado | 435 ms | 596 ms |
| Maior tempo observado | 487 ms | 793 ms |

## Interpretação inicial

Neste teste pequeno com imagens do dataset, os dois modelos acertaram todos os objetos avaliados.

O YOLO26n Float32 apresentou melhor desempenho de tempo no emulador, com tempo médio aproximado de 455,8 ms. O YOLOv8n Float32 apresentou tempo médio aproximado de 676,0 ms.

A diferença média foi de aproximadamente 220,2 ms a favor do YOLO26n Float32.

Em termos de confiança, os dois modelos tiveram valores altos. O YOLO26n Float32 obteve confiança média de 93,4%, enquanto o YOLOv8n Float32 obteve confiança média de 91,8%.

No caso do apontador, o YOLO26n detectou corretamente o objeto principal, mas também retornou borracha como detecção secundária. O YOLOv8n retornou apenas apontador nesse caso.

## Conclusão parcial

Com base neste teste inicial em imagens do dataset, o YOLO26n Float32 continua sendo uma boa opção para uso on-device, pois apresentou:

- acerto em todos os casos testados;
- menor tempo médio de inferência;
- menor tamanho de arquivo;
- confiança média alta.

O YOLOv8n Float32 também apresentou bons resultados, mas foi mais lento neste teste inicial.

Ainda assim, esta comparação não é suficiente para uma conclusão final. É necessário complementar com testes em:

- celular físico;
- fotos reais controladas;
- fotos reais não controladas;
- objetos fora do dataset;
- versão otimizada em Float16, se aplicável.

## Próximos passos

Os próximos passos recomendados são:

1. repetir a comparação em dispositivo físico;
2. comparar os dois modelos em fotos reais;
3. avaliar se o YOLOv8n oferece vantagem em cenários mais difíceis;
4. testar versão Float16 do modelo escolhido;
5. decidir o modelo principal considerando acurácia, tempo de inferência, tamanho e comportamento em uso assistivo.

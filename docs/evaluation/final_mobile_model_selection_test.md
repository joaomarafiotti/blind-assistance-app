\# Teste final curto de seleção do modelo mobile



\## Objetivo



Registrar um teste curto para apoiar a escolha do modelo final usado no aplicativo Android.



O objetivo não foi realizar uma avaliação estatística ampla, mas comparar os três modelos disponíveis no app em um cenário real no celular físico, usando os mesmos objetos e o mesmo fluxo de análise por foto.



\## Modelos avaliados



Foram avaliados:



\- YOLO26n Float32;

\- YOLO26n Float16;

\- YOLOv8n Float32.



O modelo YOLO26n Float32 é o modelo padrão atual do aplicativo e também o modelo usado no modo de detecção contínua com CameraX.



\## Dispositivo e cenário



\- Dispositivo: Samsung S25 FE

\- Modo: análise on-device por foto

\- Fonte: fotos reais capturadas pelo app

\- Objetos testados:

&#x20; - caneta;

&#x20; - controle remoto;

&#x20; - bolsa/mochila.



\## Resultados



| Objeto | YOLO26n Float32 | YOLO26n Float16 | YOLOv8n Float32 |

|---|---:|---:|---:|

| Caneta | 88% / 189 ms | 88% / 205 ms | 90% / 262 ms |

| Controle remoto | 93% / 133 ms | 94% / 105 ms | 90% / 149 ms |

| Bolsa/mochila | 97% / 94 ms | 97% / 126 ms | 93% / 172 ms |



\## Resumo por modelo



| Modelo | Acertos | Confiança média | Tempo médio |

|---|---:|---:|---:|

| YOLO26n Float32 | 3/3 | 92,7% | 138,7 ms |

| YOLO26n Float16 | 3/3 | 93,0% | 145,3 ms |

| YOLOv8n Float32 | 3/3 | 91,0% | 194,3 ms |



\## Interpretação



Todos os modelos reconheceram corretamente os três objetos avaliados.



O YOLO26n Float32 apresentou o menor tempo médio no teste curto, com aproximadamente 138,7 ms. Além disso, é o modelo mais validado ao longo do desenvolvimento do aplicativo, tendo sido usado nos testes on-device, nos testes com fotos reais e no modo CameraX.



O YOLO26n Float16 apresentou comportamento muito competitivo. Ele manteve acertos nos três objetos e confiança média ligeiramente superior, além de ter um arquivo menor. No entanto, como foi menos testado no modo contínuo com CameraX, foi mantido como alternativa experimental.



O YOLOv8n Float32 também funcionou corretamente, mas apresentou o maior tempo médio entre os três modelos neste teste. Assim, ele não apresentou vantagem prática suficiente para substituir o YOLO26n como modelo principal do app.



\## Justificativa da escolha final



A escolha final do app foi manter o YOLO26n Float32 como modelo padrão.



Essa decisão se baseia em:



\- boa taxa de acerto nos testes realizados;

\- menor tempo médio neste teste curto;

\- estabilidade ao longo do desenvolvimento;

\- compatibilidade já validada com TensorFlow Lite;

\- uso bem-sucedido no modo CameraX;

\- melhor alinhamento com a proposta de execução on-device.



\## Observação sobre Float16



A versão YOLO26n Float16 permanece relevante como alternativa futura.



Ela reduziu o tamanho do arquivo TFLite e apresentou resultados promissores no celular físico. Porém, antes de virar o modelo padrão, ainda seria necessário avaliá-la melhor no modo contínuo com CameraX e em uma amostra maior de objetos reais.



\## Conclusão



O teste reforça que o YOLO26n Float32 é uma escolha defensável para a versão final do aplicativo.



O YOLOv8n foi mantido como referência comparativa, enquanto o YOLO26n Float16 foi classificado como alternativa promissora para otimização futura.


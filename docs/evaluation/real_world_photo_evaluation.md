\# Avaliação com fotos reais



\## Objetivo



Registrar uma avaliação funcional do modo on-device do aplicativo Android usando fotos reais capturadas com o celular físico.



Diferentemente das avaliações anteriores, que usaram imagens do conjunto de teste do dataset Objects in the Classroom, esta etapa avalia o comportamento do modelo em imagens reais capturadas em ambiente doméstico, com variações de iluminação, fundo, distância, ângulo e composição.



\## Modelo avaliado



\- Modelo: classroom\_yolo26n\_e50\_best\_float32.tflite

\- Arquitetura base: YOLO26n

\- Formato: TensorFlow Lite Float32

\- Execução: on-device no Android

\- Classes: 20 classes do dataset Objects in the Classroom



\## Ambiente



\- App: blind-assistance-app

\- Commit de referência: ababde9

\- Dispositivo: Samsung S25 FE

\- Modelo técnico do dispositivo: SM-S731B

\- Versão Android: 16

\- Backend: não utilizado

\- Entrada: fotos capturadas pela câmera do celular

\- Saída: texto na interface e leitura por Text-to-Speech



\## Protocolo



Foram capturadas 20 fotos reais com o celular. A lista incluiu objetos presentes nas classes do dataset e também objetos fora do conjunto de classes, para observar limitações do modelo.



Objetos testados:



1\. lápis

2\. caneta

3\. livro

4\. caderno

5\. cadeira

6\. mesa

7\. régua

8\. sapato

9\. mochila

10\. lixeira

11\. ventilador

12\. controle remoto

13\. televisão

14\. boné

15\. tesoura

16\. apontador

17\. borracha

18\. notebook

19\. calça

20\. grifador de texto



Os objetos lápis, caderno, televisão e grifador de texto foram considerados fora do conjunto de classes do dataset.



\## Resultados



Os resultados brutos estão no arquivo:



```text

docs/evaluation/real\_world\_photo\_tests.csv

````



\## Resumo geral



Foram testadas 20 fotos reais.



\* total de fotos reais: 20

\* objetos dentro do dataset: 16

\* objetos fora do dataset: 4

\* tempo médio aproximado: 223,5 ms

\* menor tempo: 157 ms

\* maior tempo: 331 ms



\## Resultados para objetos dentro do dataset



Entre os 16 objetos que pertencem às classes do dataset:



\* corretos: 5

\* parciais: 1

\* erros: 4

\* nenhuma detecção: 6

\* acurácia considerando apenas corretos: 31,25%

\* acurácia considerando corretos e parciais: 37,5%



\## Resultados para objetos fora do dataset



Entre os 4 objetos fora do conjunto de classes:



\* sem detecção segura: 2

\* confusão moderada: 2



Os objetos fora do dataset foram:



\* lápis;

\* caderno;

\* televisão;

\* grifador de texto.



\## Principais acertos



O modelo reconheceu corretamente alguns objetos reais:



\* sapato;

\* ventilador;

\* controle remoto;

\* apontador;

\* borracha.



A mochila foi considerada um caso parcial, pois o app apresentou `bolsa` como objeto secundário, mas indicou `sapato` como classe principal.



\## Principais falhas observadas



O modelo apresentou dificuldades em várias fotos reais:



\* não reconheceu caneta real;

\* confundiu livro real com mural;

\* não reconheceu cadeira real;

\* não reconheceu mesa real em cena com múltiplos objetos;

\* não reconheceu régua real;

\* confundiu lixeira real com bolsa;

\* confundiu boné com sapato;

\* não reconheceu tesoura real;

\* não reconheceu notebook real;

\* confundiu calça com sapato.



\## Interpretação



A avaliação com fotos reais mostrou uma queda significativa de desempenho em relação às avaliações com imagens do conjunto de teste do dataset.



Esse comportamento sugere um gap de domínio entre o dataset usado no treinamento e as imagens capturadas em condições reais. As fotos reais apresentam variações de iluminação, fundo, distância, ângulo, escala, textura e composição que podem não estar suficientemente representadas no conjunto de treinamento.



Apesar da queda de desempenho qualitativo, o tempo de inferência permaneceu baixo no celular físico, com média aproximada de 223,5 ms.



\## Conclusão



O modo on-device está funcional em hardware Android real, mas os resultados com fotos reais indicam que o modelo atual ainda não generaliza bem para todos os cenários práticos.



Essa etapa reforça a necessidade de próximos passos técnicos antes de uma aplicação assistiva mais robusta, como:



\* testar outros modelos exportados para TFLite;

\* comparar YOLO26n com YOLOv8n no app;

\* testar versões otimizadas como Float16;

\* coletar ou adicionar fotos reais ao processo de treinamento;

\* melhorar o pré-processamento das imagens;

\* avaliar estratégias de recorte ou foco no objeto principal;

\* melhorar a interface para orientar melhor a captura da foto;

\* considerar uma segunda rodada de fotos reais em condições mais controladas.


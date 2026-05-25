\# Avaliação controlada com fotos reais



\## Objetivo



Registrar uma segunda rodada de avaliação com fotos reais, desta vez em condições mais controladas de captura.



A rodada anterior usou fotos reais em condições mais naturais, com maior variação de fundo, iluminação, distância, ângulo e composição. Nesta rodada, os objetos foram fotografados de forma mais centralizada, com fundo claro e melhor iluminação, buscando verificar se o desempenho do modelo melhora quando a captura é mais simples.



\## Modelo avaliado



\- Modelo: classroom\_yolo26n\_e50\_best\_float32.tflite

\- Arquitetura base: YOLO26n

\- Formato: TensorFlow Lite Float32

\- Execução: on-device no Android

\- Classes: 20 classes do dataset Objects in the Classroom



\## Ambiente



\- App: blind-assistance-app

\- Dispositivo: Samsung S25 FE

\- Modelo técnico do dispositivo: SM-S731B

\- Versão Android: 16

\- Backend: não utilizado

\- Entrada: fotos capturadas pela câmera do celular

\- Saída: texto na interface e leitura por Text-to-Speech



\## Protocolo



Foram capturadas 15 fotos reais em condições mais controladas:



\- fundo claro;

\- objeto centralizado;

\- boa iluminação;

\- objeto ocupando parte relevante da imagem;

\- menor quantidade de objetos ao redor.



Objetos testados:



1\. caneta

2\. livro

3\. régua

4\. tesoura

5\. controle remoto

6\. apontador

7\. borracha

8\. sapato

9\. boné

10\. notebook

11\. mochila

12\. lápis

13\. caderno

14\. grifador de texto

15\. celular



Os objetos lápis, caderno, grifador e celular foram considerados fora do conjunto de classes do dataset.



\## Resultados



Os resultados brutos estão no arquivo:



```text

docs/evaluation/controlled\_real\_world\_photo\_tests.csv

````



\## Resumo geral



Foram testadas 15 fotos reais em condição controlada.



\* total de fotos: 15

\* objetos dentro do dataset: 11

\* objetos fora do dataset: 4

\* tempo médio aproximado: 215,2 ms

\* menor tempo: 98 ms

\* maior tempo: 335 ms



\## Resultados para objetos dentro do dataset



Entre os 11 objetos pertencentes às classes do dataset:



\* corretos: 8

\* erros: 3

\* nenhuma detecção: 0

\* acurácia considerando apenas corretos: 72,7%



Os objetos reconhecidos corretamente foram:



\* caneta;

\* régua;

\* tesoura;

\* controle remoto;

\* apontador;

\* borracha;

\* sapato;

\* notebook.



Os principais erros foram:



\* livro confundido com mural;

\* boné confundido com sapato;

\* mochila confundida com sapato.



\## Resultados para objetos fora do dataset



Entre os 4 objetos fora das classes treinadas:



\* sem detecção segura: 1

\* confusão alta: 1

\* confusão moderada: 2



Casos observados:



\* lápis não foi reconhecido com segurança;

\* caderno foi confundido com mural;

\* grifador foi confundido com caneta;

\* celular foi confundido com controle remoto.



\## Comparação com a rodada real anterior



Na rodada anterior com fotos reais menos controladas, o desempenho foi baixo, com apenas 5 acertos entre 16 objetos pertencentes ao dataset.



Nesta rodada controlada, o desempenho melhorou para 8 acertos entre 11 objetos pertencentes ao dataset.



Isso indica que a qualidade da captura influencia bastante o comportamento do modelo. Fundo mais simples, melhor iluminação e objeto centralizado melhoraram os resultados, embora algumas confusões persistam.



\## Interpretação



A avaliação controlada sugere que parte das falhas observadas na rodada anterior estava relacionada às condições de captura. No entanto, alguns erros persistiram mesmo em cenário controlado.



A confusão recorrente entre livros/cadernos e mural indica que o modelo pode estar associando capas retangulares e coloridas à classe `wall-magazine`.



A confusão entre boné/mochila e sapato indica que o modelo ainda apresenta dificuldades de generalização para alguns objetos reais, mesmo quando a classe existe no dataset.



\## Conclusão



O teste controlado mostrou melhora significativa em relação à primeira rodada com fotos reais, mas também evidenciou limitações persistentes do modelo.



Os resultados reforçam que o aplicativo on-device está funcional e rápido no celular físico, mas que a robustez em cenários reais ainda depende de melhorias no modelo, no dataset, no pré-processamento e possivelmente na orientação de captura da imagem.


\# Avaliação funcional em dispositivo físico



\## Objetivo



Registrar uma avaliação funcional preliminar do modo on-device do aplicativo Android em um celular físico, verificando se o modelo YOLO26n em TensorFlow Lite mantém o comportamento observado no emulador e comparando os tempos aproximados de inferência.



\## Modelo avaliado



\- Modelo: classroom\_yolo26n\_e50\_best\_float32.tflite

\- Arquitetura base: YOLO26n

\- Formato: TensorFlow Lite Float32

\- Execução: on-device no Android

\- Classes: 20 classes do dataset Objects in the Classroom



\## Ambiente



\- App: blind-assistance-app

\- Commit de referência: 0afd179

\- Dispositivo ADB: RQCYA007KJT

\- Modelo comercial do dispositivo: Samsung S25 FE

\- Modelo técnico do dispositivo: SM-S731B

\- Versão Android: 16

\- Backend: não utilizado

\- Entrada: imagens selecionadas pelo Android Photo Picker

\- Saída: texto na interface e leitura por Text-to-Speech



\## Protocolo



Foram selecionadas 10 imagens usadas anteriormente na avaliação do emulador, incluindo casos com acertos fortes e casos problemáticos.



As imagens testadas foram:



\- 01\_table.jpg

\- 02\_chair.jpg

\- 03\_whiteboard.jpg

\- 07\_trash\_can.jpg

\- 08\_eraser.jpg

\- 10\_pen.jpg

\- 11\_book.jpg

\- 15\_laptop.jpg

\- 17\_bag.jpg

\- 19\_shoes.jpg



Para cada imagem, foi registrado:



\- classe esperada;

\- objetos detectados pelo app;

\- confiança principal;

\- tempo aproximado de inferência;

\- status do resultado;

\- observações.



\## Resultados



Os resultados brutos estão no arquivo:



```text

docs/evaluation/physical\_device\_functional\_tests.csv

````



\## Resumo



Foram testadas 10 imagens no celular físico.



\* total de imagens testadas: 10

\* corretas: 8

\* parciais: 0

\* erros: 1

\* nenhuma detecção: 1

\* acurácia considerando apenas corretas: 80%

\* acurácia considerando corretas e parciais: 80%

\* tempo médio aproximado: 167,3 ms

\* menor tempo: 126 ms

\* maior tempo: 207 ms



\## Comparação com o emulador



Para as mesmas 10 imagens, os tempos aproximados observados anteriormente no emulador foram:



\* tempo médio aproximado no emulador: 382,5 ms

\* tempo médio aproximado no celular físico: 167,3 ms



Nesta amostra, o celular físico executou a inferência aproximadamente 2,3 vezes mais rápido que o emulador.



\## Observações



O comportamento qualitativo do modelo foi semelhante ao observado no emulador.



Os principais acertos foram:



\* table;

\* chair;

\* whiteboard;

\* pen;

\* book;

\* laptop;

\* bag;

\* shoes.



Os principais problemas observados foram os mesmos da avaliação anterior:



\* a classe `trash-can` não foi reconhecida com segurança;

\* a classe `eraser` foi confundida com `sharpener`.



Esse resultado reforça que o modo on-device está funcional em hardware Android real e apresenta tempos de inferência menores do que os observados no emulador.


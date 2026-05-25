\# Avaliação funcional on-device



\## Objetivo



Registrar uma avaliação funcional preliminar do aplicativo Android executando inferência local com o modelo YOLO26n em formato TensorFlow Lite.



\## Modelo avaliado



\- Modelo: classroom\_yolo26n\_e50\_best\_float32.tflite

\- Arquitetura base: YOLO26n

\- Formato: TensorFlow Lite Float32

\- Execução: on-device no Android

\- Classes: 20 classes do dataset Objects in the Classroom



\## Ambiente



\- App: blind-assistance-app

\- Branch: feature/on-device-tflite

\- Dispositivo: precisa preencher

\- Backend: não utilizado nos testes on-device

\- Entrada: imagens selecionadas pelo Android Photo Picker

\- Saída: texto na interface e leitura por Text-to-Speech



\## Protocolo



Foram selecionadas 20 imagens do conjunto de teste do dataset Objects in the Classroom, uma imagem por classe.



Para cada imagem, foi registrado:



\- classe esperada;

\- objetos detectados pelo app;

\- confiança principal;

\- tempo aproximado de inferência;

\- status do resultado;

\- observações.



Status possíveis:



\- correto: a classe esperada apareceu entre os objetos detectados;

\- parcial: a classe esperada apareceu, mas junto com confusões relevantes;

\- erro: a classe esperada não apareceu e o modelo indicou outra classe;

\- nenhuma detecção: o app não reconheceu nenhum objeto com segurança.



\## Resultados



Os resultados brutos estão no arquivo:



```text

docs/evaluation/on\_device\_functional\_tests.csv

````



## Resumo

Foram testadas 20 imagens do conjunto de teste do dataset Objects in the Classroom, sendo uma imagem por classe.

- total de imagens testadas: 20
- corretas: 17
- parciais: 1
- erros: 1
- nenhuma detecção: 1
- acurácia considerando apenas corretas: 85%
- acurácia considerando corretas e parciais: 90%
- tempo médio aproximado: 368 ms
- menor tempo: 301 ms
- maior tempo: 713 ms

O resultado indica que a integração on-device com YOLO26n TFLite está funcional no aplicativo Android. A maioria das classes foi reconhecida corretamente, com tempos de inferência geralmente próximos de 300 a 400 ms no ambiente de teste.

O maior tempo registrado ocorreu no primeiro teste, com 713 ms, o que pode estar relacionado ao carregamento inicial ou aquecimento do modelo no emulador.

Os principais casos de limitação observados foram:

- a classe `trash-can`, que não foi reconhecida com segurança;
- a classe `eraser`, que foi confundida com `sharpener`;
- a classe `sharpener`, que foi detectada corretamente, mas também gerou uma detecção adicional de `eraser`.

Esses resultados reforçam que o app já executa inferência local de forma funcional, mas ainda pode apresentar confusões entre objetos visualmente semelhantes ou em imagens com características menos favoráveis.



\## Observações iniciais



O app já foi testado qualitativamente com uma imagem de pessoa usando bolsa, detectando bolsa com 91% de confiança. Também foi testado com uma imagem de lápis fora das classes do dataset, retornando possível régua com 56% de confiança, o que indica limitação em objetos fora do domínio treinado.


\# Plano técnico — CameraX e detecção semi-contínua



\## Objetivo



Planejar a evolução do aplicativo Android para um modo de detecção semi-contínua usando CameraX.



Até a versão v0.6, o aplicativo já permite capturar uma foto, executar inferência local com TensorFlow Lite e retornar o resultado por texto, voz e vibração.



No entanto, os testes com fotos reais capturadas no celular físico mostraram que depender de uma única foto pode ser limitado em um contexto assistivo. O usuário pode não enquadrar corretamente o objeto, pode estar distante demais, pode capturar em ângulo ruim ou pode ter iluminação inadequada.



Por isso, o próximo passo é investigar um fluxo com câmera aberta e análise periódica de frames.



\## Motivação



A validação física com fotos reais mostrou que o app funciona no celular real, mas ainda apresenta limitações em fotos capturadas na hora.



Resumo da validação física:



```text

Total de imagens: 7

Acertos: 3

Sem detecção: 3

Erros: 1

Tempo médio aproximado: 289,9 ms

````



Esses resultados indicam que a execução mobile está viável, mas o fluxo de uso ainda pode ser melhorado.



Em vez de exigir que o usuário tire uma foto perfeita, um modo semi-contínuo pode analisar a câmera ao longo do tempo e fornecer feedback quando encontrar uma detecção relevante.



\## Fluxo esperado



O fluxo proposto é:



```text

abrir câmera no app

capturar frames periodicamente

executar inferência on-device

avaliar confiança da detecção

falar resultado curto por Text-to-Speech

emitir vibração conforme o tipo de resultado

evitar repetir a mesma resposta muitas vezes

```



\## Modelo inicial



O modelo inicial para CameraX deve ser:



```text

YOLO26n Float32

```



Justificativa:



\* é o modelo mais testado até agora;

\* é o modelo padrão atual;

\* apresentou bons resultados em imagens do dataset;

\* já foi validado no app com TFLite;

\* já funciona com TTS, vibração e TalkBack.



O YOLO26n Float16 deve permanecer disponível como alternativa experimental, mas não deve ser o primeiro modelo usado no CameraX até termos mais testes físicos.



\## Estratégia de inferência



A inferência não deve rodar em todos os frames, para evitar sobrecarga.



Estratégia inicial:



```text

processar 1 frame a cada 2 ou 3 segundos

ignorar frames enquanto uma inferência estiver em andamento

rodar o modelo em background

atualizar resultado na interface

```



Essa estratégia reduz consumo de recursos e evita que o app tente falar resultados com frequência excessiva.



\## Feedback por voz



O app deve continuar usando respostas curtas:



```text

Detectado: objeto.

Possível: objeto.

Não consegui reconhecer com segurança.

```



No modo contínuo, a mensagem "Não consegui reconhecer com segurança" não deve ser repetida o tempo todo. Ela pode ser usada apenas em situações específicas, como quando o usuário inicia o modo ou quando passa muito tempo sem nenhuma detecção.



\## Cooldown de fala



Será necessário implementar uma lógica de cooldown para evitar repetição excessiva.



Regras iniciais:



```text

se o mesmo objeto foi falado há menos de 5 segundos, não repetir

se um objeto diferente for detectado com confiança alta, falar

se a confiança for moderada, falar "possível"

se não houver detecção, evitar repetição contínua

```



Exemplo:



```text

t = 0s: Detectado: bolsa.

t = 2s: bolsa detectada novamente -> não fala

t = 6s: bolsa detectada novamente -> pode falar novamente

t = 8s: caneta detectada -> fala, porque mudou o objeto

```



\## Feedback por vibração



A lógica atual de vibração pode ser reaproveitada:



```text

alta confiança: uma vibração curta

confiança moderada: duas vibrações curtas

sem detecção segura: uma vibração mais longa/fraca

```



No modo contínuo, a vibração também deve respeitar cooldown para não incomodar o usuário.



\## Interface inicial



A interface pode manter o fluxo atual e adicionar uma nova seção ou botão:



```text

Iniciar detecção contínua

Parar detecção contínua

```



Ou, em uma primeira implementação, pode ser uma tela simples separada para testes com CameraX.



O objetivo inicial não é criar a interface final, mas validar tecnicamente:



```text

preview da câmera

captura periódica de frame

inferência TFLite no frame

feedback acessível

```



\## Etapas de implementação



A implementação deve ser feita em etapas pequenas.



\### Etapa 1 — Dependências CameraX



Adicionar dependências CameraX ao projeto Android.



Objetivo:



```text

compilar o projeto com CameraX

```



\### Etapa 2 — Preview da câmera



Criar uma tela ou seção simples que abre o preview da câmera.



Objetivo:



```text

visualizar câmera dentro do app

```



\### Etapa 3 — Captura de frames



Adicionar ImageAnalysis para capturar frames da câmera.



Objetivo:



```text

receber frames periodicamente

```



\### Etapa 4 — Conversão de frame para Bitmap



Converter frames da câmera para Bitmap compatível com o detector atual.



Objetivo:



```text

reaproveitar YoloTfliteDetector

```



\### Etapa 5 — Inferência periódica



Executar inferência a cada 2 ou 3 segundos.



Objetivo:



```text

rodar TFLite sem travar a interface

```



\### Etapa 6 — Feedback com cooldown



Adicionar lógica para evitar repetição excessiva de áudio e vibração.



Objetivo:



```text

tornar o modo contínuo mais usável

```



\### Etapa 7 — Teste físico



Testar em dispositivo físico com objetos reais.



Objetivo:



```text

validar funcionamento fora do emulador

```



\## Critérios de sucesso



A primeira versão do CameraX será considerada funcional se:



```text

a câmera abrir dentro do app

o app conseguir processar frames

o modelo rodar sem travar

o resultado aparecer na tela

o TTS funcionar

a vibração funcionar

o app evitar repetição excessiva

```



Não é necessário que a primeira versão seja perfeita. O objetivo inicial é validar a arquitetura.



\## Riscos e limitações



Possíveis dificuldades:



```text

conversão de ImageProxy para Bitmap

rotação/orientação da imagem

desempenho em tempo real

controle de concorrência entre frames

repetição excessiva de áudio

consumo de bateria

aquecimento do dispositivo

```



\## Plano de teste inicial



Objetos para teste inicial:



```text

mochila/bolsa

caneta

sapato

régua

tesoura

cadeira

livro

```



Métricas qualitativas:



```text

detectou corretamente?

falou na hora certa?

repetiu demais?

vibrou corretamente?

travou?

ficou lento?

```



Métricas quantitativas:



```text

tempo aproximado de inferência

número de acertos

número de erros

número de casos sem detecção

```



\## Documentação esperada



Após implementar a primeira versão funcional, criar:



```text

docs/evaluation/camerax\_live\_detection\_functional\_test.md

docs/evaluation/camerax\_live\_detection\_functional\_test.csv

```



\## Próximo marco



O próximo marco do projeto será:



```text

v0.7-camerax-live-detection

```



Esse marco deve representar a primeira versão funcional do aplicativo com detecção semi-contínua baseada em CameraX.


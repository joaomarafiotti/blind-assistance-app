# Blind Assistance App

Aplicativo Android desenvolvido como parte de uma Iniciação Científica voltada ao reconhecimento de objetos em aplicações assistivas, com foco em usuários cegos ou com deficiência visual.

O app permite reconhecer objetos comuns em ambientes internos, especialmente educacionais e domésticos, usando inferência local no próprio dispositivo Android, resposta por voz, vibração e um modo de detecção semi-contínua com CameraX.

## Objetivo

O objetivo do projeto é investigar e implementar uma solução mobile para reconhecimento de objetos que possa auxiliar usuários cegos ou com deficiência visual na identificação de itens do ambiente.

A proposta prioriza:

- inferência on-device;
- baixa latência;
- funcionamento sem depender constantemente de internet;
- privacidade;
- feedback acessível por voz e vibração;
- compatibilidade com uso em dispositivo móvel.

## Contexto do projeto

Este repositório contém o cliente Android da IC.

O projeto possui dois repositórios principais:

- `blind-assistance-app`: aplicativo Android;
- `object-recognition-server`: backend FastAPI usado como baseline cliente-servidor.

Inicialmente, a arquitetura usava um backend FastAPI para receber imagens e executar YOLO no servidor. Ao longo do desenvolvimento, o app evoluiu para execução local com TensorFlow Lite e, posteriormente, para detecção semi-contínua com CameraX.

## Tecnologias utilizadas

- Kotlin
- Android Studio
- Jetpack Compose
- CameraX
- TensorFlow Lite
- Text-to-Speech
- Vibração/haptic feedback
- TalkBack/semantics
- OkHttp
- YOLO/Ultralytics
- FastAPI no backend de comparação

## Arquitetura geral

O projeto evoluiu em três fases principais.

### 1. Baseline cliente-servidor

Fluxo inicial:

```text
imagem capturada ou selecionada
        ↓
app Android
        ↓
backend FastAPI
        ↓
modelo YOLO no servidor
        ↓
resposta JSON
        ↓
resultado no app + Text-to-Speech
````

Esse modo foi mantido como baseline e ferramenta de comparação.

### 2. Inferência on-device

Fluxo local:

```text
imagem capturada ou selecionada
        ↓
pré-processamento com letterbox
        ↓
modelo YOLO TFLite local
        ↓
pós-processamento das detecções
        ↓
resultado visual + voz + vibração
```

Nesse modo, o app não depende do backend para reconhecer objetos.

### 3. Detecção semi-contínua com CameraX

Fluxo atual:

```text
câmera aberta com CameraX
        ↓
captura periódica de frames
        ↓
conversão do frame para Bitmap
        ↓
pré-processamento com letterbox
        ↓
inferência TFLite on-device
        ↓
overlay visual
        ↓
Text-to-Speech
        ↓
vibração
        ↓
cooldown para evitar repetição excessiva
```

Esse modo foi criado para reduzir a dependência de uma única foto perfeita e tornar o fluxo mais adequado ao uso assistivo.

## Modelos disponíveis no app

O app inclui três modelos TFLite em `assets`:

| Modelo          | Arquivo                                     | Uso                      |
| --------------- | ------------------------------------------- | ------------------------ |
| YOLO26n Float32 | `classroom_yolo26n_e50_best_float32.tflite` | modelo padrão            |
| YOLO26n Float16 | `classroom_yolo26n_e50_best_float16.tflite` | alternativa experimental |
| YOLOv8n Float32 | `classroom_yolov8n_e50_best_float32.tflite` | referência comparativa   |

O modelo padrão final do app é:

```text
YOLO26n Float32
```

## Sobre o treinamento dos modelos

Os modelos não foram criados do zero. Foram utilizadas arquiteturas YOLO pré-treinadas como ponto de partida, com fine-tuning no dataset do projeto.

Fluxo geral:

```text
modelo YOLO pré-treinado
        ↓
fine-tuning com Objects in the Classroom
        ↓
best.pt
        ↓
exportação para TensorFlow Lite
        ↓
arquivo .tflite usado no Android
```

Assim, os modelos usados no app são modelos customizados para o problema da IC, treinados/fine-tuned com o dataset Objects in the Classroom.

## Dataset

Foi utilizado o dataset Objects in the Classroom, com 20 classes de objetos comuns em ambientes internos:

```text
table, chair, whiteboard, bookshelf, clock, wall-magazine, trash-can,
eraser, sharpener, pen, book, ruler, scissor, fan, laptop,
remote-control, bag, pants, shoes, hat
```

No app, os labels são traduzidos para português durante a apresentação dos resultados.

## Funcionalidades atuais

O app possui:

* captura de foto pela câmera;
* seleção de imagem pelo Android Photo Picker;
* inferência local com TensorFlow Lite;
* suporte a múltiplos modelos TFLite;
* modelo padrão YOLO26n Float32;
* alternativa YOLO26n Float16;
* alternativa YOLOv8n Float32;
* modo backend para comparação;
* pré-processamento com letterbox;
* detecção semi-contínua com CameraX;
* overlay visual com objeto, confiança e tempo;
* Text-to-Speech em português;
* feedback por vibração;
* cooldown para reduzir repetição no modo contínuo;
* suporte a TalkBack via semantics;
* documentação de avaliações em `docs/evaluation`.

## Modos do aplicativo

### Fluxo principal

Permite capturar uma foto e receber o resultado por voz, texto e vibração.

```text
tirar foto
    ↓
executar YOLO26n TFLite local
    ↓
mostrar resultado
    ↓
falar resultado
    ↓
vibrar conforme confiança
```

### Detecção contínua assistiva

Mantém a câmera aberta e analisa frames periodicamente.

O app fala apenas quando há uma detecção relevante, usando cooldown para evitar repetição excessiva.

Exemplos de fala:

```text
sapato detectado. Confiança alta.
possível objeto: caneta. Confiança média.
possível objeto: livro. Confiança baixa.
```

A porcentagem de confiança continua visível no overlay para fins de teste e depuração.

### Modo de teste e comparação

Permite selecionar imagens e comparar os modelos disponíveis:

* YOLO26n Float32;
* YOLO26n Float16;
* YOLOv8n Float32;
* backend FastAPI.

## Categorias de confiança

No modo contínuo, o app usa categorias de confiança para tornar a fala mais curta e acessível:

| Faixa         | Categoria                |
| ------------- | ------------------------ |
| 80% a 100%    | confiança alta           |
| 50% a 79%     | confiança média          |
| 30% a 49%     | confiança baixa          |
| abaixo de 30% | sem confiança suficiente |

## Resultados principais

### Teste com imagens do dataset no emulador

| Métrica                | Resultado |
| ---------------------- | --------: |
| Imagens avaliadas      |        20 |
| Acertos                |        17 |
| Parcialmente correto   |         1 |
| Erros                  |         1 |
| Sem detecção           |         1 |
| Acurácia simples       |       85% |
| Acertos + parciais     |       90% |
| Tempo médio aproximado |    368 ms |

### Teste físico com imagens do dataset

Dispositivo: Samsung S25 FE.

| Métrica                | Resultado |
| ---------------------- | --------: |
| Imagens avaliadas      |        10 |
| Acertos                |         8 |
| Erros                  |         1 |
| Sem detecção           |         1 |
| Tempo médio aproximado |  167,3 ms |

### Teste físico com fotos reais capturadas pelo app

| Métrica                | Resultado |
| ---------------------- | --------: |
| Imagens avaliadas      |         7 |
| Acertos                |         3 |
| Erros                  |         1 |
| Sem detecção           |         3 |
| Tempo médio aproximado |  289,9 ms |

Esse teste mostrou que o app funcionava no celular real, mas também evidenciou limitações em fotos reais fora das condições do dataset.

### Validação do letterbox

| Métrica                | Resize direto | Letterbox |
| ---------------------- | ------------: | --------: |
| Acertos                |           5/5 |       5/5 |
| Confiança média        |         93,4% |     93,4% |
| Tempo médio aproximado |      458,0 ms |  476,6 ms |

O letterbox foi mantido porque preserva melhor a proporção da imagem e não prejudicou os resultados avaliados.

### Teste funcional do CameraX

| Métrica                           | Resultado |
| --------------------------------- | --------: |
| Observações registradas           |        10 |
| Detecções semanticamente corretas |        10 |
| Tempo médio aproximado            |  214,2 ms |
| Menor tempo observado             |    111 ms |
| Maior tempo observado             |    467 ms |
| Confiança média aproximada        |     60,9% |

O modo CameraX conseguiu abrir a câmera, processar frames, executar inferência local, atualizar o overlay, falar resultados, vibrar e reiniciar sem travar.

### Teste final curto de seleção do modelo

| Objeto          | YOLO26n Float32 | YOLO26n Float16 | YOLOv8n Float32 |
| --------------- | --------------: | --------------: | --------------: |
| Caneta          |    88% / 189 ms |    88% / 205 ms |    90% / 262 ms |
| Controle remoto |    93% / 133 ms |    94% / 105 ms |    90% / 149 ms |
| Bolsa/mochila   |     97% / 94 ms |    97% / 126 ms |    93% / 172 ms |

Resumo:

| Modelo          | Acertos | Confiança média | Tempo médio |
| --------------- | ------: | --------------: | ----------: |
| YOLO26n Float32 |     3/3 |           92,7% |    138,7 ms |
| YOLO26n Float16 |     3/3 |           93,0% |    145,3 ms |
| YOLOv8n Float32 |     3/3 |           91,0% |    194,3 ms |

Com base nesses resultados e na estabilidade ao longo do desenvolvimento, o YOLO26n Float32 foi mantido como modelo padrão.

## Justificativa da escolha do modelo final

O YOLO26n Float32 foi escolhido como modelo padrão porque:

* apresentou bom desempenho em dispositivo móvel;
* teve o menor tempo médio no teste final curto;
* foi o modelo mais validado ao longo do desenvolvimento;
* funcionou no fluxo de foto única;
* funcionou no modo CameraX;
* manteve compatibilidade com TensorFlow Lite;
* apresentou estabilidade suficiente para a versão final da IC.

O YOLO26n Float16 apresentou resultados promissores e arquivo menor, mas foi mantido como alternativa experimental por exigir validação mais ampla no modo contínuo.

O YOLOv8n Float32 também funcionou, mas não apresentou vantagem prática suficiente para substituir o YOLO26n como padrão.

## Estrutura do projeto

```text
blind-assistance-app/
├── app/
│   └── src/
│       └── main/
│           ├── assets/
│           │   ├── classroom_yolo26n_e50_best_float32.tflite
│           │   ├── classroom_yolo26n_e50_best_float16.tflite
│           │   ├── classroom_yolov8n_e50_best_float32.tflite
│           │   └── labels.txt
│           ├── java/com/joaomarafiotti/blindassistanceapp/
│           │   ├── CameraPreview.kt
│           │   ├── MainActivity.kt
│           │   ├── YoloTfliteDetector.kt
│           │   └── ui/theme/
│           ├── res/
│           └── AndroidManifest.xml
├── docs/
│   └── evaluation/
├── gradle/
├── README.md
├── build.gradle.kts
└── settings.gradle.kts
```

## Arquivos principais

### `MainActivity.kt`

Contém a tela principal do app, fluxo de captura de foto, seleção de imagem, integração com backend, integração on-device, Text-to-Speech, vibração e organização dos modos de uso.

### `CameraPreview.kt`

Implementa o modo de detecção semi-contínua com CameraX. É responsável por abrir a câmera, capturar frames, converter para Bitmap, executar o detector local, atualizar o overlay e controlar feedback por voz/vibração.

### `YoloTfliteDetector.kt`

Carrega o modelo TFLite, aplica letterbox, prepara o buffer de entrada, executa inferência local e interpreta a saída do modelo.

### `docs/evaluation/`

Contém os documentos de avaliação funcional, comparação de modelos, validação física e resumo final consolidado.

## Como executar

1. Abra o projeto no Android Studio.
2. Sincronize o Gradle.
3. Conecte um celular Android ou abra um emulador.
4. Execute o app com o botão Run.

Para testar o modo on-device e o modo CameraX, não é necessário rodar o backend.

Para testar o modo backend, rode também o servidor do repositório `object-recognition-server`.

## Backend local

Durante testes com emulador Android, o app acessa o backend local usando:

```text
http://10.0.2.2:8000
```

Esse endereço permite que o emulador acesse o servidor rodando na máquina host.

Para testes em celular físico, é necessário usar o IP local da máquina na rede Wi-Fi.

## Histórico de versões

| Tag                                   | Descrição                                        |
| ------------------------------------- | ------------------------------------------------ |
| v0.1-client-server-baseline           | baseline cliente-servidor                        |
| v0.2-on-device-tflite                 | primeira versão com TFLite on-device             |
| v0.4-yolo26n-float16                  | inclusão e comparação inicial do YOLO26n Float16 |
| v0.5-letterbox-preprocessing          | pré-processamento com letterbox                  |
| v0.6-physical-real-capture-validation | validação física com fotos reais                 |
| v0.7-camerax-live-detection           | CameraX com detecção semi-contínua               |

## Status atual

* app Android funcional;
* inferência on-device funcionando;
* CameraX funcionando;
* Text-to-Speech funcionando;
* vibração funcionando;
* modo backend preservado;
* avaliação final consolidada documentada;
* modelo padrão final: YOLO26n Float32;
* versão atual: v0.7-camerax-live-detection.

## Limitações

O app ainda é um protótipo de pesquisa e possui limitações:

* reconhece apenas as 20 classes do dataset;
* pode confundir objetos fora das classes treinadas;
* ainda não possui validação formal com usuários cegos;
* os testes reais foram feitos com amostras pequenas;
* ainda não usa bounding boxes para orientar espacialmente o usuário;
* ainda não possui feedback avançado para múltiplos objetos;
* o modo Float16 ainda precisa de validação mais ampla no CameraX;
* consumo de bateria e aquecimento ainda não foram avaliados formalmente.

## Trabalhos futuros

Possíveis evoluções:

* validar com usuários cegos ou com deficiência visual;
* ampliar testes com fotos reais;
* testar YOLO26n Float16 no modo CameraX;
* avaliar consumo de bateria e aquecimento;
* usar bounding boxes para orientação espacial;
* implementar mensagens como “mova um pouco para a esquerda” ou “aproxime o celular”;
* melhorar feedback para múltiplos objetos;
* treinar com mais imagens reais;
* comparar modelos maiores em backend com modelos menores on-device;
* separar modo assistivo final e modo de depuração.

## Repositório relacionado

Backend do projeto:

```text
object-recognition-server
```

## Autor

João Pedro Piccino Marafiotti
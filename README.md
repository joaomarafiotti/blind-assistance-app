# blind-assistance-app

Cliente Android da iniciação científica voltado para reconhecimento de objetos em contexto educacional, com foco em acessibilidade para usuários cegos.

## Objetivo

Este repositório contém o aplicativo Android do projeto. O app permite capturar ou selecionar uma imagem, executar reconhecimento de objetos e apresentar o resultado de forma acessível, incluindo leitura em voz alta.

O projeto começou com uma arquitetura cliente-servidor, em que o app enviava imagens para um backend FastAPI. Atualmente, o app também possui uma versão inicial com inferência on-device usando um modelo YOLO exportado para TensorFlow Lite.

## Contexto do projeto

Este app faz parte da iniciação científica sobre reconhecimento de objetos para auxílio a usuários cegos em ambientes educacionais.

A arquitetura do projeto possui dois repositórios principais:

- `blind-assistance-app`: cliente Android
- `object-recognition-server`: backend responsável por receber imagens, rodar YOLO e retornar detecções em JSON

## Tecnologias utilizadas

- Kotlin
- Android Studio
- Jetpack Compose
- OkHttp
- Text-to-Speech (TTS)
- TensorFlow Lite
- YOLO26n exportado para TFLite

## Funcionalidades atuais

O app possui dois modos principais de funcionamento.

### 1. Modo on-device

Neste modo, o reconhecimento acontece localmente no Android, sem depender do backend.

Fluxo:

```text
foto ou imagem selecionada → YOLO26n TFLite local → resultado na tela → Text-to-Speech
````

Funcionalidades:

* captura de foto pela câmera
* seleção de imagem pelo Android Photo Picker
* execução local do modelo TFLite
* pós-processamento da saída do modelo
* tradução das classes para português
* exibição dos objetos detectados
* exibição das confianças principais
* leitura do resultado em voz alta
* tratamento de confiança alta, média e baixa

O app diferencia resultados de acordo com a confiança:

* confiança alta: informa o objeto detectado
* confiança média: informa como possível detecção
* confiança baixa ou ausência de detecção: informa que nenhum objeto foi reconhecido com segurança

### 2. Modo backend

Neste modo, o app envia a imagem para o backend FastAPI.

Fluxo:

```text
foto ou imagem selecionada → backend FastAPI → YOLO no servidor → JSON → resultado na tela → Text-to-Speech
```

Esse modo foi mantido para comparação com a versão on-device e para preservar o baseline cliente-servidor do projeto.

## Modelo on-device atual

Modelo local utilizado no app:

```text
classroom_yolo26n_e50_best_float32.tflite
```

Características:

* arquitetura base: YOLO26n
* formato: TensorFlow Lite Float32
* dataset: Objects in the Classroom
* classes: 20
* tamanho aproximado: 9.47 MB
* execução: local no Android

Classes do modelo:

```text
table, chair, whiteboard, bookshelf, clock, wall-magazine, trash-can,
eraser, sharpener, pen, book, ruler, scissor, fan, laptop,
remote-control, bag, pants, shoes, hat
```

## Exemplos observados

Durante testes iniciais no app:

* imagem de pessoa usando bolsa: detectou `bolsa` com 91% de confiança
* imagem de lápis fora do conjunto de classes: detectou possível `régua` com 56% de confiança
* imagem aleatória fora do domínio: retornou “Nenhum objeto reconhecido com segurança”

Esses exemplos indicam que o modelo on-device está funcional, mas ainda possui limitações quando o objeto não pertence às classes treinadas ou quando a imagem está fora do domínio do dataset.

## Estrutura do projeto

```text
blind-assistance-app/
├── app/
│   └── src/
│       └── main/
│           ├── assets/
│           │   ├── classroom_yolo26n_e50_best_float32.tflite
│           │   └── labels.txt
│           ├── java/com/joaomarafiotti/blindassistanceapp/
│           │   ├── MainActivity.kt
│           │   ├── YoloTfliteDetector.kt
│           │   └── ui/theme/
│           ├── res/
│           └── AndroidManifest.xml
├── gradle/
├── README.md
├── build.gradle.kts
└── settings.gradle.kts
```

## Arquivos principais

### `MainActivity.kt`

Contém a tela principal do app, os botões de captura/seleção de imagem, integração com backend, integração on-device, exibição dos resultados e Text-to-Speech.

### `YoloTfliteDetector.kt`

Classe responsável por carregar o modelo TFLite a partir de `assets`, preparar a imagem, executar inferência local e interpretar a saída do modelo.

### `assets/classroom_yolo26n_e50_best_float32.tflite`

Modelo YOLO26n treinado no dataset Objects in the Classroom e exportado para TensorFlow Lite Float32.

### `assets/labels.txt`

Lista das 20 classes usadas pelo modelo.

## Como executar

1. Abra o projeto no Android Studio.
2. Sincronize o Gradle.
3. Conecte um celular Android ou abra um emulador.
4. Execute o app com o botão Run.

Para testar o modo on-device, não é necessário rodar o backend.

Para testar o modo backend, rode o servidor do repositório `object-recognition-server`.

## Backend local

Durante testes com emulador Android, o app acessa o backend local usando:

```text
http://10.0.2.2:8000
```

Esse endereço permite que o emulador acesse o servidor rodando na máquina host.

Para testes em celular físico, é necessário usar o IP local da máquina na rede Wi-Fi, por exemplo:

```text
http://192.168.x.x:8000
```

## Status atual

* app Android funcional
* captura de foto implementada
* seleção de imagem implementada
* integração com backend implementada
* Text-to-Speech implementado
* modelo YOLO26n TFLite incluído no app
* inferência on-device funcionando
* pós-processamento inicial das detecções funcionando
* mensagens ajustadas por nível de confiança
* branch atual de desenvolvimento: `feature/on-device-tflite`

## Limitações atuais

* o app ainda é um protótipo de pesquisa
* o modelo reconhece apenas as 20 classes do dataset Objects in the Classroom
* objetos fora dessas classes podem ser confundidos com classes visualmente parecidas
* ainda não há validação formal com usuários cegos
* ainda não há detecção em tempo real com fluxo contínuo de câmera
* ainda não há comparação on-device sistemática entre YOLOv8n TFLite e YOLO26n TFLite no celular
* os tempos de inferência variam conforme dispositivo, imagem e primeira execução do modelo

## Próximos passos

* realizar avaliação funcional on-device com imagens do dataset
* testar fotos reais capturadas em ambiente educacional
* registrar tempos de inferência no celular
* comparar o comportamento do modelo local com o backend
* avaliar exportações otimizadas, como Float16
* melhorar mensagens acessíveis e suporte ao TalkBack
* documentar resultados para monografia, relatório ou artigo
* considerar CameraX em tempo real como etapa futura

## Repositório relacionado

Backend do projeto:

```text
object-recognition-server
```

## Autor

João Pedro Piccino Marafiotti
# blind-assistance-app

Cliente Android da iniciação científica voltado para reconhecimento de objetos em contexto educacional, com foco em acessibilidade para usuários cegos.

Este aplicativo faz parte de uma solução cliente-servidor para reconhecimento de objetos. O app captura ou seleciona uma imagem, envia essa imagem para um backend de detecção de objetos, recebe os objetos detectados e apresenta o resultado por texto e voz.

## Objetivo

O objetivo deste repositório é implementar o cliente Android do projeto de iniciação científica sobre reconhecimento de objetos para auxílio a usuários cegos em ambientes educacionais.

A versão atual permite testar o fluxo ponta-a-ponta:

```text
Android app → imagem → backend FastAPI → modelo YOLO → JSON → texto/TTS
````

## Contexto do projeto

A arquitetura atual do projeto está organizada em dois repositórios principais:

* `blind-assistance-app`: cliente Android
* `object-recognition-server`: backend responsável por receber a imagem, executar o modelo de detecção e retornar as detecções

O backend atual utiliza um modelo YOLOv8n fine-tuned no dataset Objects in the Classroom.

## Tecnologias utilizadas

* Kotlin
* Android Studio
* Jetpack Compose
* Android Activity Result API
* Android Photo Picker
* OkHttp
* Coil
* Text-to-Speech (TTS)

## Funcionalidades atuais

* captura de foto usando a câmera do sistema
* seleção de imagem pelo Android Photo Picker
* preview da imagem capturada ou selecionada
* envio da imagem para o backend via HTTP multipart/form-data
* recebimento da resposta do endpoint `/detect`
* extração dos objetos detectados a partir do JSON retornado
* tradução básica dos labels para português
* exibição do resultado de forma amigável
* exibição dos objetos detectados em chips
* leitura do resultado em voz alta usando Text-to-Speech em português
* botão para ouvir o resultado novamente
* estados básicos de carregamento e erro

## Fluxo principal

O fluxo principal atual é:

1. o usuário toca em `Tirar foto e analisar`
2. o app abre a câmera do sistema
3. o usuário captura uma foto
4. a imagem capturada aparece no app
5. o app envia a imagem para o backend
6. o backend executa a inferência com YOLO
7. o backend retorna um JSON com as detecções
8. o app interpreta os objetos detectados
9. o app mostra o resultado na tela
10. o app lê o resultado em voz alta

Também existe um fluxo secundário para selecionar imagem da galeria/testes:

1. o usuário toca em `Selecionar imagem para teste`
2. o app abre o Android Photo Picker
3. o usuário seleciona uma imagem
4. o app exibe o preview
5. o usuário toca em `Analisar imagem selecionada`
6. o app envia a imagem ao backend e apresenta o resultado

## Estrutura do projeto

* `app/`: código principal do aplicativo Android
* `gradle/`: configuração de build e catálogo de versões
* `app/src/main/java/.../MainActivity.kt`: tela principal, captura/seleção de imagem, envio ao backend e feedback por TTS
* `app/src/main/AndroidManifest.xml`: permissões e configuração principal do app
* `app/build.gradle.kts`: dependências e configuração do módulo Android
* `gradle/libs.versions.toml`: catálogo de versões das dependências

## Backend esperado

O app espera que o backend esteja rodando com o endpoint:

```text
POST /detect
```

O backend relacionado está no repositório:

```text
object-recognition-server
```

O backend atual retorna uma resposta no formato:

```json
{
  "model": "classroom_yolov8n_e50_best.pt",
  "filename": "selected_image.jpg",
  "num_detections": 1,
  "inference_ms": 251.61,
  "detections": [
    {
      "class_id": 14,
      "class_name": "laptop",
      "confidence": 0.9592918157577515
    }
  ]
}
```

## Como executar com emulador Android

1. Abra o projeto no Android Studio.
2. Sincronize o Gradle.
3. Inicie o backend localmente no repositório `object-recognition-server`.
4. Execute o app em um emulador Android.
5. Use o app para capturar ou selecionar uma imagem e enviar ao servidor.

Durante o desenvolvimento com emulador Android, o app acessa o backend local usando:

```text
http://10.0.2.2:8000/detect
```

Esse endereço permite que o emulador acesse o servidor rodando na máquina host.

## Como executar com celular físico

Para testar em um celular Android físico, o backend precisa aceitar conexões da rede local.

No backend, execute:

```powershell
$env:MODEL_PATH="models/classroom_yolov8n_e50_best.pt"
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

Depois, descubra o IP local do computador:

```powershell
ipconfig
```

No celular, conectado à mesma rede Wi-Fi, teste no navegador:

```text
http://IP_DO_PC:8000/health
```

Exemplo usado em teste local:

```text
http://192.168.15.5:8000/health
```

Para testar pelo app no celular físico, a URL no código deve apontar temporariamente para:

```text
http://IP_DO_PC:8000/detect
```

Exemplo:

```text
http://192.168.15.5:8000/detect
```

Observação: esse IP é local da rede e não deve ser mantido como configuração fixa versionada. Para a versão padrão do emulador, o app usa `10.0.2.2`.

## Teste em dispositivo físico

A versão atual foi testada em:

* emulador Android
* celular físico Samsung S25 FE

No teste com celular físico, o app conseguiu:

* abrir a câmera do sistema
* capturar uma foto real
* enviar a imagem para o backend local
* receber a detecção
* exibir o resultado
* falar o resultado usando Text-to-Speech

## Observação sobre câmera

A versão atual usa a câmera do sistema por meio da Android Activity Result API, com `ActivityResultContracts.TakePicture()`.

Ela ainda não usa CameraX.

CameraX é uma possibilidade futura para permitir preview dentro do app, maior controle da câmera e detecção em tempo real por meio de análise de frames.

## Estado atual

O app já possui uma integração funcional com o backend. Atualmente ele consegue:

* tirar foto
* selecionar imagem
* mostrar preview
* enviar imagem ao backend
* receber resposta do servidor
* mostrar resultado na interface
* traduzir labels básicas para português
* falar o resultado
* funcionar em emulador
* funcionar em celular físico na mesma rede do backend

## Limitações atuais

* a inferência ainda ocorre no backend, não no próprio celular
* o app depende de rede local/servidor para detectar objetos
* o app ainda não usa CameraX
* não há detecção em tempo real
* o parsing do JSON ainda é feito por regex
* o app ainda não foi validado formalmente com usuários cegos
* a acessibilidade foi implementada inicialmente com TTS, mas ainda precisa de avaliação formal

## Próximos passos

* comparar YOLOv8n com YOLO26n no backend
* escolher o modelo principal com base em métricas, tamanho, tempo e viabilidade mobile
* exportar o modelo selecionado para TFLite/LiteRT
* integrar inferência on-device no app
* permitir detecção sem backend
* comparar a versão cliente-servidor com a versão on-device
* futuramente avaliar CameraX e detecção em tempo real

## Autor

João Pedro Piccino Marafiotti

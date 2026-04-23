# blind-assistance-app

Cliente Android da iniciação científica voltado para reconhecimento de objetos em contexto educacional, com foco em acessibilidade para usuários cegos.

## Objetivo

Este repositório contém o aplicativo Android da solução cliente-servidor do projeto. O app permite selecionar uma imagem, enviá-la para o backend de detecção de objetos e apresentar o resultado de forma acessível, incluindo leitura em voz alta.

## Contexto do projeto

Este app faz parte da iniciação científica sobre reconhecimento de objetos para auxílio a usuários cegos em ambientes educacionais.

A arquitetura atual do projeto está organizada em dois blocos principais:

- **blind-assistance-app**: cliente Android
- **object-recognition-server**: backend responsável por receber a imagem, rodar o modelo e retornar as detecções

## Tecnologias utilizadas

- Kotlin
- Android Studio
- Jetpack Compose
- OkHttp
- Text-to-Speech (TTS)

## Funcionalidades atuais

- seleção de imagem pelo Android Photo Picker
- envio da imagem para o backend
- recebimento da resposta do endpoint `/detect`
- exibição do resultado de forma amigável
- leitura do resultado em voz alta
- preview da imagem selecionada

## Fluxo atual

1. o usuário seleciona uma imagem
2. o app envia essa imagem para o backend
3. o backend processa a imagem com o modelo de detecção
4. o backend retorna um JSON com as detecções
5. o app interpreta esse resultado e exibe os objetos detectados
6. o app também pode ler o resultado em voz alta

## Estrutura do projeto

- `app/`: código principal do aplicativo Android
- `gradle/`: configuração de build e catálogo de versões
- `app/src/main/java/.../MainActivity.kt`: tela principal e fluxo atual do app

## Estado atual

O app já possui uma primeira integração funcional com o backend. Atualmente ele consegue:

- escolher imagem
- enviar imagem
- receber resposta do servidor
- mostrar resultado na interface
- falar o resultado

## Próximos passos

- melhorar ainda mais a interface do app
- tornar a saída mais adequada ao usuário final
- integrar melhor com o modelo treinado do projeto
- evoluir a experiência acessível
- consolidar a integração com o backend para demonstração e avaliação

## Como executar

1. abra o projeto no Android Studio
2. sincronize o Gradle
3. execute o app em um emulador Android
4. certifique-se de que o backend esteja rodando localmente
5. use o app para selecionar uma imagem e enviar para o servidor

## Observação sobre backend local

Durante o desenvolvimento com emulador Android, o app acessa o backend local usando:

`http://10.0.2.2:8000`

Esse endereço permite que o emulador acesse o servidor rodando na máquina host.

## Repositório relacionado

Backend do projeto:

- `object-recognition-server`

## Autor

João Pedro Piccino Marafiotti

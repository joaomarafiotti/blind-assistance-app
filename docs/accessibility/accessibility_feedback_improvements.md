# Melhorias de acessibilidade e feedback ao usuário

## Objetivo

Registrar as melhorias adicionadas ao aplicativo Android para tornar o retorno da detecção mais adequado a um cenário assistivo.

Após a integração do modelo YOLO26n em TensorFlow Lite, o aplicativo já conseguia executar inferência on-device, exibir os resultados na tela e falar o resultado por Text-to-Speech. No entanto, as primeiras versões falavam mensagens muito longas, com informações técnicas como confiança e tempo de inferência.

Nesta etapa, o foco foi melhorar o feedback acessível ao usuário, especialmente considerando o público-alvo do projeto: usuários cegos ou com deficiência visual.

## Melhorias implementadas

Foram implementadas quatro melhorias principais:

1. mensagens faladas mais curtas por Text-to-Speech;
2. descrições de acessibilidade para TalkBack;
3. feedback tátil por vibração;
4. separação entre fluxo principal de uso e modo de teste/debug.

## Text-to-Speech mais curto

Antes, o aplicativo falava a mesma mensagem exibida na tela, por exemplo:

```text
Possível detecção on-device: caneta. Confiança moderada: caneta 54%. Tente confirmar com outra foto. Tempo aproximado: 309 ms.
```

Esse formato é útil para depuração e avaliação técnica, mas é longo demais para uso assistivo.

Agora, o aplicativo mantém a mensagem completa na tela, mas usa uma versão curta para o áudio.

Exemplos:

```text
Detectado: borracha.
Possível: caneta.
Não consegui reconhecer com segurança.
```

Com isso, o app mantém duas camadas de informação:

- interface visual completa para testes, depuração e avaliação;
- resposta falada curta e objetiva para uso assistivo.

## Separação entre mensagem visual e mensagem falada

Foi adicionada uma variável separada para armazenar a mensagem falada:

```text
spokenResult
```

Assim, o botão "Ouvir resultado novamente" também reproduz a versão curta da resposta, em vez da mensagem técnica completa.

## Labels para TalkBack

Foram adicionadas descrições semânticas em elementos importantes da interface, incluindo:

- título da tela;
- botões principais;
- imagem selecionada ou capturada;
- seção de resultado;
- chips de objetos detectados;
- botão para ouvir o resultado novamente.

Exemplos de descrições adicionadas:

```text
Tirar foto e analisar objeto no dispositivo.
Selecionar imagem da galeria para teste de reconhecimento de objetos.
Resultado da análise: ...
Ouvir novamente o resultado da análise.
```

Também foi utilizado `liveRegion` na área de resultado, para indicar que essa região pode ser atualizada dinamicamente após a análise.

## Feedback tátil por vibração

Foi adicionado feedback por vibração após a inferência local.

O objetivo é fornecer uma camada tátil além da resposta por voz.

O padrão atual é:

```text
alta confiança: uma vibração curta
confiança moderada: duas vibrações curtas
sem detecção segura: uma vibração mais longa e fraca
```

A lógica usa três estados:

```text
HIGH_CONFIDENCE
MEDIUM_CONFIDENCE
NO_SAFE_DETECTION
```

Esse feedback foi testado em um celular físico Samsung S25 FE e as vibrações ficaram perceptíveis.

## Separação entre fluxo principal e modo de teste/debug

A interface foi reorganizada para diferenciar melhor o uso assistivo principal das ferramentas de avaliação técnica.

Antes, os botões ficavam misturados na mesma sequência:

```text
Tirar foto e analisar on-device
Selecionar imagem para teste
Analisar on-device
Analisar via backend
```

Agora, a tela foi dividida em seções:

```text
Fluxo principal
Resultado
Imagem atual
Modo de teste e comparação
```

O fluxo principal contém o botão:

```text
Tirar foto e ouvir resultado
```

Esse botão representa o caminho assistivo principal:

```text
capturar imagem -> inferência on-device -> resposta por voz -> vibração -> resultado visual
```

O modo de teste e comparação mantém as ferramentas necessárias para pesquisa:

```text
Selecionar imagem para teste
Analisar imagem on-device
Analisar via backend
```

Essa separação preserva os recursos úteis para avaliação da IC, mas deixa o fluxo principal mais claro e menos confuso.

## Relação com os testes reais

As avaliações com fotos reais mostraram que as condições de captura influenciam bastante a qualidade da detecção. No entanto, como o público-alvo inclui usuários cegos, o aplicativo não deve depender principalmente de instruções visuais, como cards pedindo para centralizar o objeto ou melhorar a iluminação.

Por isso, as melhorias priorizadas foram:

- áudio curto;
- suporte ao TalkBack;
- vibração;
- feedback de incerteza;
- fluxo principal mais simples;
- preparação para futuras formas de orientação por câmera.

## Estado atual do feedback ao usuário

Atualmente, após uma análise on-device, o app fornece:

- resposta visual completa;
- lista visual de objetos detectados;
- resposta falada curta;
- feedback por vibração;
- descrições de acessibilidade para leitores de tela;
- botão para ouvir novamente a resposta curta;
- fluxo principal separado do modo de teste/debug.

## Limitações atuais

Apesar das melhorias, o app ainda não possui:

- orientação por áudio durante a captura;
- detecção contínua com câmera;
- indicação espacial do objeto;
- validação formal com usuários cegos;
- lógica para evitar repetição excessiva de áudio em modo contínuo.

## Próximos passos técnicos

Os próximos passos relacionados à acessibilidade são:

1. melhorar o modo de captura para reduzir dependência de instruções visuais;
2. investigar CameraX para detecção semi-contínua;
3. adicionar lógica para evitar repetição excessiva de áudio;
4. futuramente, orientar o usuário por voz durante a câmera aberta;
5. futuramente, avaliar o protótipo com usuários cegos ou especialistas em acessibilidade.
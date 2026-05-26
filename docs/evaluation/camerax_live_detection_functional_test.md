\# Teste funcional — CameraX com detecção semi-contínua



\## Objetivo



Registrar o teste funcional da primeira versão do modo de detecção semi-contínua com CameraX no aplicativo Android.



Nesta etapa, o app passou a usar a câmera aberta dentro da aplicação, processar frames periodicamente, executar inferência local com TensorFlow Lite e fornecer feedback por texto, voz e vibração.



\## Configuração do teste



\- Aplicativo: Blind Assistance App

\- Dispositivo: Samsung S25 FE

\- Modelo: YOLO26n Float32

\- Modo: CameraX live detection

\- Inferência: on-device com TensorFlow Lite

\- Feedback: overlay visual, Text-to-Speech e vibração

\- Pré-processamento: letterbox 640x640

\- Intervalo de análise ajustado: aproximadamente 3,5 segundos

\- Cooldown de fala para o mesmo objeto: aproximadamente 10 segundos



\## Objetos e cenas testadas



Foram testados objetos reais em ambiente doméstico, incluindo:



\- mochila/bolsa;

\- sapato;

\- caneta;

\- livro;

\- cadeira;

\- notebook.



Também foram observadas cenas com mais de um objeto visível.



\## Resultados observados



| Teste | Objeto esperado | Resultado | Confiança | Tempo | Status |

|---|---|---|---:|---:|---|

| 01 | mochila/bolsa | bolsa | 41% | 121 ms | correto com baixa confiança |

| 02 | sapato | sapato | 93% | 111 ms | correto com alta confiança |

| 03 | caneta | caneta | 67% | 184 ms | correto com média confiança |

| 04 | caneta | caneta | 59% | 394 ms | correto com média confiança |

| 05 | livro | livro | 55% | 162 ms | correto com média confiança |

| 06 | livro | livro | 48% | 133 ms | correto com baixa confiança |

| 07 | livro | livro | 68% | 179 ms | correto com média confiança |

| 08 | mochila/bolsa | bolsa | 64% | 467 ms | correto com média confiança |

| 09 | cadeira | cadeira | 50% | 189 ms | correto com média confiança |

| 10 | notebook | notebook | 64% | 202 ms | correto com média confiança |



\## Resumo quantitativo da amostra



| Métrica | Resultado |

|---|---:|

| Total de observações registradas | 10 |

| Detecções semanticamente corretas | 10 |

| Erros registrados nos prints analisados | 0 |

| Alta confiança | 1 |

| Média confiança | 7 |

| Baixa confiança | 2 |

| Tempo médio aproximado | 214,2 ms |

| Menor tempo observado | 111 ms |

| Maior tempo observado | 467 ms |

| Confiança média aproximada | 60,9% |



\## Observações qualitativas



O modo CameraX funcionou corretamente no dispositivo físico.



O app conseguiu:



\- abrir a câmera dentro da interface;

\- processar frames periodicamente;

\- executar o modelo YOLO26n Float32 on-device;

\- atualizar o overlay com resultado, confiança e tempo;

\- falar o resultado por Text-to-Speech;

\- emitir vibração;

\- parar e reiniciar o modo contínuo sem travar.



Durante o primeiro teste, foi observado que a fala estava longa demais porque incluía o valor numérico da confiança. Além disso, quando o mesmo objeto permanecia parado na frente da câmera, o app podia repetir a fala antes da frase anterior terminar.



Por isso, foi feito um ajuste posterior no feedback por voz:



\- a porcentagem de confiança permanece visível no overlay para teste;

\- a fala passou a usar categorias: confiança alta, média ou baixa;

\- o intervalo entre análises foi aumentado;

\- o cooldown para repetir o mesmo objeto foi aumentado;

\- a vibração foi suavizada.



\## Interpretação



O teste físico indica que a arquitetura CameraX + TensorFlow Lite é viável para o aplicativo.



A execução em dispositivo físico apresentou tempos de inferência adequados para um modo semi-contínuo. A média observada na amostra foi de aproximadamente 214,2 ms, embora o app processe os frames em intervalos maiores para evitar sobrecarga e repetição excessiva de fala.



Os resultados também reforçam que a aplicação assistiva não deve depender apenas de uma única foto. O modo contínuo permite que o usuário mova o celular, ajuste o enquadramento e receba novos feedbacks ao longo do tempo.



\## Limitações observadas



Apesar do funcionamento correto, ainda existem limitações:



\- algumas detecções aparecem com confiança baixa ou média;

\- objetos fora do conjunto de classes podem ser confundidos com classes conhecidas;

\- o app ainda não usa bounding boxes visuais ou orientação espacial por áudio;

\- o modo contínuo ainda considera principalmente o objeto mais confiante;

\- cenas com múltiplos objetos podem exigir uma estratégia de fala mais cuidadosa.



\## Possíveis melhorias futuras



Melhorias futuras incluem:



1\. usar bounding boxes para orientar o usuário, como "mova o celular um pouco para a esquerda" ou "aproxime o objeto";

2\. validar o mapeamento das caixas com rotação, preview e letterbox;

3\. explorar feedback para múltiplos objetos;

4\. comparar YOLO26n Float32 e Float16 no modo CameraX;

5\. ajustar thresholds de confiança com base em mais testes reais;

6\. criar um modo assistivo final e um modo de depuração separados.



\## Conclusão parcial



A versão com CameraX representa um avanço importante em relação ao fluxo baseado em foto única.



O aplicativo agora possui uma primeira versão funcional de detecção semi-contínua com:



\- câmera integrada;

\- inferência local;

\- pré-processamento com letterbox;

\- feedback por voz;

\- feedback por vibração;

\- controle de repetição por cooldown;

\- execução em dispositivo físico.



Esse marco pode ser registrado como a versão v0.7 do projeto.


# Roteiro de testes

Versão de teste: 0.2.0 (partículas e correção do alinhamento).

## Antes de começar

- Minecraft 1.21.4 com Fabric, Fabric API e CPM para Fabric 1.21.4.
- `compelium-cpm-plus-0.2.0.jar` na pasta `mods`, no lugar da versão anterior.
- Para os testes com dois jogadores: um amigo ou uma segunda conta.

## 1. O mod carrega

- [ ] O jogo abre sem erro.
- [ ] No arquivo `logs/latest.log` aparece a linha `[Compelium's CPM+] Loaded`.
- [ ] Em **Editar → Ferramentas** aparecem **Gatilhos do CPM+** e **Partículas do CPM+**.

## 2. Alinhamento dos 50%

1. Abra um modelo com uma animação de dois lados que já existia (como `cplus_lados`, com três quadros).
2. Abra **Gatilhos do CPM+** e clique em **Corrigir alinhamento de todas**.

- [ ] Aparece a mensagem dizendo quantas animações foram corrigidas.
- [ ] No controle de teste da animação, 50% mostra exatamente o quadro do meio.
- [ ] No jogo, parado, a peça fica na pose de repouso.
- [ ] Uma animação nova criada pelo assistente já sai alinhada, sem precisar corrigir.
- [ ] As descrições dos gatilhos de dois lados aparecem normais (antes podiam aparecer como "Format error").

## 3. Primeiro emissor: fogo na mão

1. Selecione o braço direito (ou um cajado) e abra **Partículas do CPM+**.
2. Deixe **Chama**, clique em **Criar emissor** e mova o ponto criado até a ponta da mão.
3. Salve, aplique o modelo e entre num mundo em terceira pessoa (F5).

- [ ] Sai fogo da ponta da mão, para cima.
- [ ] Mexendo o braço (andando, batendo), o fogo acompanha a mão.
- [ ] Correndo, o fogo deixa um rastro contínuo, sem buracos.
- [ ] No inventário (a prévia do personagem), não sai fogo na tela.
- [ ] No arquivo de log aparece `Found ... particle marker(s) in a model`.

## 4. Ajustes das partículas

- [ ] Girar o ponto muda a direção das partículas.
- [ ] `qtd`, `vel` e `esp` mudam o que se espera (mais partículas, mais rápidas, mais espalhadas).
- [ ] **Poeira colorida** com uma cor (por exemplo `ff0000`) sai na cor certa, e o tamanho muda.
- [ ] Uma partícula digitada à mão (por exemplo `soul` ou `sculk_soul`) funciona.
- [ ] Esconder o ponto numa animação (um gesto, por exemplo) desliga o emissor durante o gesto.
- [ ] Criar outro emissor com o ponto selecionado junta as duas partículas no mesmo lugar.
- [ ] Em **Configurações de vídeo → Partículas: Mínimas**, quase nada aparece.

## 5. Emissores com gatilho

- [ ] `gatilho=velocidade`: parado não sai nada; andando sai pouco; correndo sai mais.
- [ ] `gatilho=pouso`: uma leva de partículas ao cair de 2 ou mais blocos, maior quanto mais alta a queda.
- [ ] `gatilho=pulo`: uma leva a cada pulo, e nenhuma ao subir degraus.

## 6. Com outro jogador

- [ ] Seu amigo, **com** o mod, vê as suas partículas.
- [ ] Seu amigo, **sem** o mod, vê o seu modelo normal, sem partículas e sem pontos aparecendo.
- [ ] Os gatilhos de movimento continuam funcionando como na versão 0.1.0.

## 7. Desempenho

- [ ] Com vários emissores (por exemplo 5 emissores com `qtd=100`), o jogo não trava.

## Modelo de relatório

Copie, preencha e mande:

```
Versão do Compelium's CPM+:
Versão do CPM:
Versão do Minecraft e do Fabric Loader:
Outros mods instalados:

Funcionou:
Não funcionou:
O que achou das partículas e do movimento:

Prints ou vídeo:
Linhas do logs/latest.log com "Compelium" (ou o arquivo inteiro, se deu erro):
```

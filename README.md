# Compelium's CPM+

Expansão para o [Customizable Player Models](https://github.com/tom5454/CustomPlayerModels) (CPM) com dois recursos:

- **Gatilhos de movimento:** capas, cabelos, rabos, orelhas e acessórios que balançam ao cair, correr, virar, pular e pousar.
- **Partículas presas ao modelo:** fogo na ponta de um cajado, fumaça saindo de uma chaminé nas costas, brilho nas mãos.

O mod é opcional e roda só no cliente. Quem tem o mod vê os efeitos; quem não tem vê o modelo CPM normal, parado na pose de repouso e sem partículas. O servidor não precisa de nada.

## Instalação

Para Minecraft Java 1.21.4 com Fabric:

1. [Fabric Loader](https://fabricmc.net/use/) e [Fabric API](https://modrinth.com/mod/fabric-api).
2. [Customizable Player Models](https://modrinth.com/plugin/custom-player-models) para Fabric 1.21.4.
3. O arquivo `compelium-cpm-plus-<versão>.jar`, da página de [lançamentos](https://github.com/snocscompelium/compelium-cpm-plus/releases), na pasta `mods`.

Os gatilhos de movimento funcionam em qualquer versão a partir da 1.21.4. As partículas foram feitas para a 1.21.4; nas outras versões elas ficam desligadas, sem erro.

## Gatilhos de movimento

Cada gatilho é uma animação comum do CPM com um nome especial. O mod lê o movimento de cada jogador na sua tela e move a animação de acordo, como a fala faz com a voz.

### Jeito fácil: o assistente

No editor do CPM, abra **Editar → Ferramentas → Gatilhos do CPM+**. Escolha o gatilho e os ajustes e clique em **Criar nova**. O assistente cria a animação com o nome certo e já marcada do jeito que o mod precisa.

Depois é só animar:

- **Um lado** (a maioria dos gatilhos): o primeiro quadro é o repouso, e o último é o movimento mais forte.
- **Dois lados** (vertical, frente e trás, lados, giro): use três quadros. O primeiro é um lado, o do meio é o repouso e o último é o outro lado.

Para testar no editor, mexa no controle deslizante de teste da animação. A física de verdade só roda no jogo.

**Interpolação:** use **Linear (única)**, que o assistente já escolhe. Com as interpolações em loop e com a Polinomial (única), o CPM não espalha os quadros por igual, e 50% cai entre o quadro do meio e o último. O botão **Corrigir alinhamento de todas**, no mesmo assistente, ajusta de uma vez todas as animações `cplus_` do modelo.

### Escrevendo o nome à mão

O nome começa com `cplus_` e o gatilho, por exemplo `cplus_queda`. Os ajustes vêm depois de um espaço:

```
cplus_queda peso=4 mola=6
```

Use uma animação do tipo **Value layer** para seguir a força do movimento, ou **Layer** para ligar e desligar. Marque **Command activated** para ela não aparecer no menu de gestos, e **Additive** para somar com as outras animações.

Para várias animações usarem o mesmo gatilho, use o `#` do próprio CPM: `cplus_queda#capa` e `cplus_queda#cabelo` andam juntas.

### Gatilhos

| Nome | O que faz |
| --- | --- |
| `cplus_queda` | Aumenta enquanto o personagem cai |
| `cplus_subida` | Aumenta enquanto sobe (pulo, bolhas) |
| `cplus_frente` | Andar para frente |
| `cplus_tras` | Andar para trás |
| `cplus_esquerda` | Andar de lado, para a esquerda |
| `cplus_direita` | Andar de lado, para a direita |
| `cplus_velocidade` | Rapidez no chão, em qualquer direção |
| `cplus_giro_esquerda` | Virar o corpo para a esquerda |
| `cplus_giro_direita` | Virar o corpo para a direita |
| `cplus_pouso` | Um tranco ao chegar no chão |
| `cplus_pulo` | Um tranco no momento do pulo |
| `cplus_agua` | Liga dentro da água |
| `cplus_parado` | Aumenta devagar enquanto fica parado |
| `cplus_vertical` | Dois lados: subindo, parado, caindo |
| `cplus_frente_tras` | Dois lados: trás, parado, frente |
| `cplus_lados` | Dois lados: esquerda, parado, direita |
| `cplus_giro` | Dois lados: virando para a esquerda, parado, para a direita |

### Ajustes

Todos vão de 0 a 10. Decimais podem usar vírgula ou ponto (`peso=2,5`).

| Ajuste | Padrão | O que muda |
| --- | --- | --- |
| `peso` | 3 | Quanto a parte demora para reagir. 0 é na hora, 10 é bem devagar. |
| `mola` | 2 | Quanto ela passa do ponto e volta balançando. 0 é nada, 10 é muito. |
| `forca` | 5 | Sensibilidade. Com 10, chega ao fim com metade do movimento. |
| `limite` | 10 | Até onde a animação vai. Com 5, para na metade. |
| `limiar` | 5 | Só para liga e desliga: a força que liga a animação. |

Exemplo de capa: `cplus_queda peso=3 mola=2#capa` levanta a capa ao cair, e `cplus_frente peso=4#capa` joga a capa para trás ao correr.

## Partículas

### Jeito fácil: o assistente

1. No editor, selecione a peça de onde as partículas vão sair (a ponta do cajado, por exemplo).
2. Abra **Editar → Ferramentas → Partículas do CPM+**, escolha a partícula e os ajustes e clique em **Criar emissor**.
3. O assistente cria um **ponto invisível** dentro da peça e já o seleciona. Mova o ponto até o lugar certo.

As partículas saem para cima do ponto. Gire o ponto para mudar a direção.

O ponto é igual aos pontos de segurar item do próprio CPM: ninguém vê o ponto, nem quem está sem o mod. Para ligar e desligar o emissor, mostre ou esconda o ponto numa animação. Por exemplo: deixe o ponto escondido no modelo e visível só no gesto "magia", e as partículas aparecem só durante o gesto.

Para juntar mais de uma partícula no mesmo lugar (fogo e fumaça, por exemplo), selecione o ponto que já existe e crie outro emissor: ele é adicionado ao mesmo ponto.

### O que o assistente cria

Além do ponto, o assistente cria uma animação do tipo **Layer** com os ajustes no nome, por exemplo:

```
cplus_particula flame qtd=10 vel=1 esp=2
```

Essa animação só guarda os ajustes e aponta para o ponto; ela não precisa ser tocada. Para mudar os ajustes depois, selecione a animação na lista, abra o assistente e clique em **Aplicar**.

### Ajustes

| Ajuste | Padrão | O que muda |
| --- | --- | --- |
| partícula | `flame` | Qualquer partícula do Minecraft: `flame`, `smoke`, `end_rod`, `heart`, `dust`... |
| `qtd` | 10 | Partículas por segundo, até 200. Com pouso ou pulo, partículas por vez. |
| `vel` | 1 | Velocidade de 0 a 10. Com 0, ficam paradas no lugar. |
| `esp` | 2 | Espalhamento de 0 (um jato fino) a 10 (todos os lados). |
| `cor` | `ffffff` | Só para `dust`: cor no formato RRGGBB, como `ff8800`. |
| `tam` | 1 | Só para `dust`: tamanho de 0,1 a 4. |
| `gatilho` | sempre | Liga o emissor a um gatilho de movimento. Com `velocidade`, solta mais partículas quanto mais rápido você anda; com `pouso` ou `pulo`, solta uma leva a cada pouso ou pulo. |

Exemplos:

- Rastro de poeira ao correr: `cplus_particula dust cor=c8b48c tam=1,5 qtd=30 esp=6 gatilho=velocidade`
- Poeira nos pés ao pousar: `cplus_particula poof qtd=12 vel=2 esp=8 gatilho=pouso`

### Limites

- As partículas seguem a configuração de partículas do Minecraft (Todas, Reduzidas, Mínimas).
- Em primeira pessoa você não vê as partículas do seu próprio modelo; os outros jogadores com o mod veem.

## Testes

O roteiro de testes e o modelo de relatório estão em [docs/TESTES.md](docs/TESTES.md).

## Compilar

O projeto usa Gradle com Fabric Loom e compila contra a API do CPM. A cada envio para a branch `main`, o GitHub compila e publica o arquivo na página de lançamentos; a branch `dev` só compila, para testes.

```
./gradlew build
```

O arquivo sai em `build/libs`.

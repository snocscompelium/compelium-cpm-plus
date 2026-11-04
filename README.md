# Compelium's CPM+

Expansão para o [Customizable Player Models](https://github.com/tom5454/CustomPlayerModels) (CPM) que faz partes do modelo reagirem ao movimento do personagem: capas, cabelos, rabos, orelhas e acessórios que balançam ao cair, correr, virar, pular e pousar.

O mod é opcional e roda só no cliente. Quem tem o mod vê os efeitos; quem não tem vê o modelo CPM normal, parado na pose de repouso. O servidor não precisa de nada.

## Instalação

Para Minecraft Java 1.21.4 com Fabric:

1. [Fabric Loader](https://fabricmc.net/use/) e [Fabric API](https://modrinth.com/mod/fabric-api).
2. [Customizable Player Models](https://modrinth.com/plugin/custom-player-models) para Fabric 1.21.4.
3. O arquivo `compelium-cpm-plus-<versão>.jar`, da página de [lançamentos](https://github.com/snocscompelium/compelium-cpm-plus/releases), na pasta `mods`.

## Como funciona

Cada gatilho é uma animação comum do CPM com um nome especial. O mod lê o movimento de cada jogador na sua tela e move a animação de acordo, como a fala faz com a voz.

### Jeito fácil: o assistente

No editor do CPM, abra **Editar → Ferramentas → Gatilhos do CPM+**. Escolha o gatilho e os ajustes e clique em **Criar nova**. O assistente cria a animação com o nome certo e já marcada do jeito que o mod precisa.

Depois é só animar:

- **Um lado** (a maioria dos gatilhos): o primeiro quadro é o repouso, e o último é o movimento mais forte.
- **Dois lados** (vertical, frente e trás, lados, giro): use três quadros. O primeiro é um lado, o do meio é o repouso e o último é o outro lado.

Para testar no editor, mexa no controle deslizante de teste da animação. A física de verdade só roda no jogo.

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

Todos vão de 0 a 10.

| Ajuste | Padrão | O que muda |
| --- | --- | --- |
| `peso` | 3 | Quanto a parte demora para reagir. 0 é na hora, 10 é bem devagar. |
| `mola` | 2 | Quanto ela passa do ponto e volta balançando. 0 é nada, 10 é muito. |
| `forca` | 5 | Sensibilidade. Com 10, chega ao fim com metade do movimento. |
| `limite` | 10 | Até onde a animação vai. Com 5, para na metade. |
| `limiar` | 5 | Só para liga e desliga: a força que liga a animação. |

Exemplo de capa: `cplus_queda peso=3 mola=2#capa` levanta a capa ao cair, e `cplus_frente peso=4#capa` joga a capa para trás ao correr.

## Testes

O roteiro de testes e o modelo de relatório estão em [docs/TESTES.md](docs/TESTES.md).

## Compilar

O projeto usa Gradle e compila contra a API do CPM, sem depender de uma versão específica do Minecraft:

```
gradle build
```

O arquivo sai em `build/libs`. A cada envio para a branch `main`, o GitHub compila e publica o arquivo na página de lançamentos.

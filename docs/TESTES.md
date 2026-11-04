# Roteiro de testes

Versão de teste: fases 1 e 2 (gatilhos de física e assistente no editor).

## Antes de começar

- Minecraft 1.21.4 com Fabric, Fabric API e CPM para Fabric 1.21.4.
- `compelium-cpm-plus-<versão>.jar` na pasta `mods`.
- Para os testes com dois jogadores: um amigo ou uma segunda conta.

## 1. O mod carrega

- [ ] O jogo abre sem erro.
- [ ] No arquivo `logs/latest.log` aparece a linha `[Compelium's CPM+] Loaded`.

## 2. Assistente no editor

- [ ] Em **Editar → Ferramentas** aparece **Gatilhos do CPM+**.
- [ ] O assistente abre, a lista de gatilhos mostra os nomes em português e os números mudam a prévia do nome.
- [ ] **Criar nova** cria uma animação com o nome mostrado na prévia.
- [ ] Com uma animação selecionada, **Aplicar** troca o nome dela e mantém o que vem depois do `#`.

## 3. Primeiro gatilho: a capa ao cair

1. Crie `cplus_queda` pelo assistente.
2. Selecione a capa (ou outra peça), adicione um segundo quadro e, nele, gire a peça para cima.
3. Mexa no controle de teste da animação: a peça deve ir do repouso até o quadro 2.
4. Salve, aplique o modelo e entre num mundo.

- [ ] Pulando de um lugar alto, a peça sobe enquanto cai.
- [ ] Ao chegar no chão, ela volta ao repouso.
- [ ] No arquivo de log aparecem `Found ... trigger animation(s)` e `Physics triggers are running`.

## 4. Outros gatilhos

Teste os que puder e anote o que achou do movimento:

- [ ] `cplus_frente`: andar e correr para frente.
- [ ] `cplus_lados` com três quadros: andar de lado para os dois lados.
- [ ] `cplus_giro` com três quadros: virar rápido para os dois lados.
- [ ] `cplus_pouso`: cair de dois ou mais blocos.
- [ ] `cplus_pulo`: pular parado e correndo. Subir escadas e degraus **não** deve contar como pulo.
- [ ] `cplus_agua` e `cplus_parado`.

## 5. Ajustes

- [ ] `peso=0` reage na hora; `peso=9` reage bem devagar.
- [ ] `mola=8` faz a peça passar do ponto e balançar.
- [ ] `limite=5` para a animação na metade.
- [ ] Uma animação do tipo **Layer** (liga e desliga) com `cplus_queda` liga só durante a queda.

## 6. Com outro jogador

- [ ] Seu amigo, **com** o mod, vê as suas peças reagindo.
- [ ] Seu amigo, **sem** o mod, vê o seu modelo normal, parado no repouso, sem erro.
- [ ] Num servidor **sem** CPM, funciona igual.
- [ ] Num servidor **com** CPM, funciona igual.

## Modelo de relatório

Copie, preencha e mande:

```
Versão do Compelium's CPM+:
Versão do CPM:
Versão do Minecraft e do Fabric Loader:
Outros mods instalados:

Funcionou:
Não funcionou:
O que achou do movimento (rápido demais, lento demais, estranho):

Prints ou vídeo:
Linhas do logs/latest.log com "Compelium" (ou o arquivo inteiro, se deu erro):
```

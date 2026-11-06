Versão de teste 0.2.1 do Compelium's CPM+.

Correção:
- Com shaders ligados (Iris), as partículas também apareciam em pontos extras, que mudavam de lugar ao girar a câmera. Os shaders desenham cada jogador uma segunda vez, do ponto de vista do sol, para fazer as sombras, e o mod soltava partículas também nesse desenho, com a posição errada. Agora ele ignora o desenho das sombras.

Continua tudo da 0.2.0:
- Partículas presas ao modelo, com o assistente Partículas do CPM+.
- Dobras do Emotecraft (o CPM Emote Bends, agora dentro do CPM+, com os mesmos nomes), com o assistente Dobras do CPM+.
- Gatilhos de movimento com Trigonométrico Único como padrão, e o botão "Corrigir alinhamento de todas".

Instalação: coloque o arquivo .jar na pasta mods, junto com o CPM para Fabric 1.21.4, no lugar da versão anterior. Para as dobras, o Emotecraft.
Roteiro de testes: docs/TESTES.md no repositório.

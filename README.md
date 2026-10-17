# Código de Huffman

Código de Huffman do zero em Java 21, com o algoritmo de 1952 de um lado e **todos os códigos de prefixo possíveis** do outro, enumerados um por um.

"O algoritmo de Huffman é ótimo" é um teorema com prova conhecida. Aqui ele não é provado nem citado: para cada distribuição de frequências, a força bruta gera todo vetor de comprimentos que satisfaz a desigualdade de Kraft e escolhe o mais barato. Se o algoritmo errasse, a varredura teria encontrado o código melhor.

Ele não erra. O que ele não é, e muita gente acha que é, é **único**.

```
mvn -q exec:java -Dexec.args=unicidade
```

## O que torna isso enumerável

O custo de um código não depende de quais bits ele usa, só do **comprimento** de cada palavra. Então o objeto de estudo não é a árvore, é o vetor de comprimentos.

E a desigualdade de Kraft, que soma `2^-comprimento`, é a tradução exata de "dá para montar um código de prefixo com estes comprimentos". Num alfabeto de `n` símbolos nenhum código ótimo precisa passar de `n - 1` bits, porque um nó interno com um filho só pode ser cortado, encurtando um código sem alongar nenhum outro.

Com teto e com Kraft, os códigos de prefixo de um alfabeto pequeno viram um conjunto finito que cabe num laço.

## As medidas

### 1. Huffman contra todos os códigos de prefixo possíveis

| símbolos | total | distribuições | divergências |
|---:|---:|---:|---:|
| 2 | 20 | 19 | 0 |
| 3 | 20 | 171 | 0 |
| 4 | 20 | 969 | 0 |
| 5 | 20 | 3.876 | 0 |
| 6 | 20 | 11.628 | 0 |
| 7 | 12 | 462 | 0 |

17.125 distribuições varridas, nenhuma divergência.

E o juiz tem dente, de dois jeitos diferentes: um algoritmo que junta sempre as duas árvores mais **pesadas**, em vez das mais leves, perde do ótimo na maioria das distribuições; e o código de comprimento fixo, que é legal e cheio, perde também. A varredura acusa os dois.

### 2. Quantos vetores de comprimento são código de verdade

| símbolos | vetores | passam no Kraft | cheios | fração que passa |
|---:|---:|---:|---:|---:|
| 3 | 8 | 4 | 3 | 50,0% |
| 4 | 81 | 32 | 13 | 39,5% |
| 5 | 1.024 | 392 | 75 | 38,3% |
| 6 | 15.625 | 6.154 | 525 | 39,4% |
| 7 | 279.936 | 114.937 | 4.347 | 41,1% |

Entre "passa no Kraft" e "é cheio" mora o código legal que ninguém deveria usar: ele funciona, decodifica certo, e desperdiça ramos da árvore, ou seja, gasta bits à toa. Com sete símbolos são 114.937 códigos válidos e só 4.347 sem desperdício.

Há um caso em que o desperdício é inevitável, e é o menor de todos: com **um símbolo só**, o código é um bit e o outro valor daquele bit não nomeia nada.

### 3. Quantas árvores ótimas existem

| símbolos | distribuições | com mais de um ótimo | fração | máximo |
|---:|---:|---:|---:|---:|
| 3 | 105 | 6 | 5,7% | 2 |
| 4 | 455 | 108 | 23,7% | 3 |
| 5 | 1.365 | 775 | 56,8% | 7 |
| 6 | 3.003 | 2.088 | 69,5% | 15 |

Falar em "a árvore de Huffman" sugere que o resultado é único. Com seis símbolos, **sete em cada dez distribuições têm mais de um código ótimo**, e o recorde são quinze códigos diferentes com exatamente o mesmo custo, nas frequências `2, 2, 2, 2, 2, 6`.

Quem decide qual sai não é o algoritmo: é o critério de desempate da implementação, que o livro não especifica. A consequência prática é direta: dois programas corretos comprimem o mesmo arquivo em saídas diferentes, do mesmo tamanho, e nenhum dos dois está errado. Comparar saída byte a byte entre implementações testa o desempate, não a compressão.

### 4. O quanto Huffman passa do piso de Shannon

| frequências | entropia | Huffman | folga | quantas vezes |
|---|---:|---:|---:|---:|
| 1, 1 | 1,0000 | 1,0000 | 0,0000 | 1,0 |
| 3, 1 | 0,8113 | 1,0000 | 0,1887 | 1,2 |
| 1, 31 | 0,2006 | 1,0000 | 0,7994 | 5,0 |
| 97, 1, 1, 1 | 0,2419 | 1,0500 | 0,8081 | 4,3 |
| 999, 1 | 0,0114 | 1,0000 | 0,9886 | **87,7** |
| 8, 4, 2, 1, 1 | 1,8750 | 1,8750 | 0,0000 | 1,0 |

A entropia é o piso: nenhum código gasta menos que ela, em média. Huffman é ótimo, mas ótimo **entre códigos que dão um número inteiro de bits a cada símbolo**, e o piso não respeita essa restrição.

A folga nunca chega a um bit por símbolo, e a maior que a varredura achou foi 0,7994. Só que um bit é muito quando a entropia é pequena: com frequências `999, 1`, Huffman gasta **87 vezes** o piso, e continua sendo ótimo. Essa é a distância entre "ótimo" e "bom", e é por isso que compressores de verdade não param no Huffman.

Quando as frequências são potências de dois, a folga some inteira.

### 5. "Mais frequente ganha código menor"

| símbolos | distribuições | Huffman viola | ordenados legais | piores que o ótimo |
|---:|---:|---:|---:|---:|
| 3 | 78 | 0 | 162 | 78 |
| 4 | 286 | 0 | 2.394 | 1.984 |
| 5 | 715 | 0 | 29.905 | **28.395** |

A primeira coluna confirma a metade boa da regra: o Huffman nunca dá código mais longo a um símbolo mais frequente, em nenhuma distribuição varrida. Isso é necessário.

A última coluna derruba a regra como receita. Entre os vetores de comprimento que satisfazem Kraft **e** respeitam a ordem das frequências, 28.395 dos 29.905 custam mais que o ótimo. Passar no teste de bolso não quer dizer quase nada: 95% dos que passam são piores do que precisavam ser.

## O que as medidas me corrigiram

**"A árvore de Huffman" quase nunca é uma só.** Com seis símbolos, 69,5% das distribuições têm empate, e até quinze códigos ótimos diferentes. O algoritmo não determina a árvore; o desempate da fila determina.

**Ótimo não quer dizer perto do piso.** Com `999, 1`, Huffman gasta 87 vezes a entropia e continua ótimo, porque a restrição é dar um número inteiro de bits por símbolo. O teorema é sobre uma família de códigos, não sobre compressão em geral.

**A regra de bolso que eu usaria para conferir um código à mão não confere nada.** Respeitar "mais frequente, código menor" elimina pouco: 95% dos códigos ordenados e legais ainda gastam mais que o ótimo.

**E um bug meu, achado antes de qualquer teste cobrir.** O gerador de palavras de código devolvia string **vazia** para o primeiro símbolo, porque eu tratava o zero como caso especial quando `Long.toBinaryString(0)` já devolve `"0"`. Um código de prefixo com palavra vazia não é código de prefixo: a palavra vazia é prefixo de todas as outras. Agora existe um teste que confere, em toda distribuição varrida, que nenhuma palavra é prefixo de outra.

## Rodar

```bash
mvn -q verify                              # 28 testes
mvn -q exec:java                           # todas as medidas
mvn -q exec:java -Dexec.args=correcao      # 1
mvn -q exec:java -Dexec.args=kraft         # 2
mvn -q exec:java -Dexec.args=unicidade     # 3
mvn -q exec:java -Dexec.args=entropia      # 4
mvn -q exec:java -Dexec.args=ordem         # 5
```

Java 21, sem dependência fora do JUnit. Nenhum número deste README foi digitado à mão: todos saem de `Medidas`, e os testes conferem os que aparecem escritos aqui.

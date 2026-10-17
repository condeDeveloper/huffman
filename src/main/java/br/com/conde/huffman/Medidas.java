package br.com.conde.huffman;

import java.util.ArrayList;
import java.util.List;

/**
 * AS MEDIDAS: tudo o que o repositório afirma sai de varrer todas as distribuições de
 * frequência com um total, contra todos os códigos de prefixo possíveis.
 */
public final class Medidas {

    private Medidas() {}

    /* ===================== 1. correcao ===================== */

    public record Correcao(int simbolos, int total, int distribuicoes, int divergencias) {}

    private static List<Correcao> correcao;

    /**
     * HUFFMAN CONTRA TODOS OS CÓDIGOS DE PREFIXO POSSÍVEIS.
     *
     * <p>"O algoritmo é ótimo" é um teorema com prova conhecida. Aqui ele não é provado nem
     * citado: para cada distribuição, a força bruta enumera todo vetor de comprimentos que
     * satisfaz Kraft e escolhe o melhor. Se o algoritmo errasse em algum caso deste domínio,
     * a varredura teria achado o código melhor.
     */
    public static List<Correcao> correcao() {
        if (correcao != null) return correcao;

        List<Correcao> saida = new ArrayList<>();

        /* O caso de sete simbolos com total 20 sozinho faria 7,6 bilhoes de comparacoes
         * (27 mil distribuicoes vezes 280 mil vetores cada) e levava quase dois minutos. Com
         * total 12 o alfabeto de sete continua coberto, por 462 distribuicoes. */
        int[][] casos = {{2, 20}, {3, 20}, {4, 20}, {5, 20}, {6, 20}, {7, 12}};

        for (int[] caso : casos) {
            List<long[]> todas = ForcaBruta.todasAsFrequencias(caso[0], caso[1]);

            int divergencias = 0;

            for (long[] frequencias : todas)
                if (Huffman.custo(frequencias) != ForcaBruta.melhor(frequencias).custo())
                    divergencias++;

            saida.add(new Correcao(caso[0], caso[1], todas.size(), divergencias));
        }

        return correcao = List.copyOf(saida);
    }

    public static int totalDeDistribuicoes() {
        int soma = 0;

        for (Correcao linha : correcao()) soma += linha.distribuicoes();

        return soma;
    }

    public static int totalDeDivergencias() {
        int soma = 0;

        for (Correcao linha : correcao()) soma += linha.divergencias();

        return soma;
    }

    /* ===================== 2. kraft ===================== */

    public record Kraft(int simbolos, long vetores, long passam, long cheios) {

        public double fracaoQuePassa() {
            return passam / (double) vetores;
        }
    }

    private static List<Kraft> kraft;

    /**
     * QUANTOS VETORES DE COMPRIMENTO SÃO CÓDIGO DE VERDADE.
     *
     * <p>A desigualdade de Kraft é a tradução exata de "dá para montar um código de prefixo
     * com estes comprimentos". A tabela mostra quantos vetores passam por ela, e quantos
     * deles são CHEIOS, ou seja, não desperdiçam nenhum ramo da árvore.
     *
     * <p>A diferença entre as duas colunas é o espaço de códigos legais que ninguém deveria
     * usar: eles funcionam, e gastam bits à toa.
     */
    public static List<Kraft> kraft() {
        if (kraft != null) return kraft;

        List<Kraft> saida = new ArrayList<>();

        for (int simbolos = 2; simbolos <= 7; simbolos++) {
            ForcaBruta.Espaco espaco = ForcaBruta.espaco(simbolos);

            saida.add(new Kraft(simbolos, espaco.testados(), espaco.passamNoKraft(),
                    espaco.cheios()));
        }

        return kraft = List.copyOf(saida);
    }

    public static Kraft kraft(int simbolos) {
        return kraft().stream().filter(x -> x.simbolos() == simbolos).findFirst().orElseThrow();
    }

    /* ===================== 3. unicidade ===================== */

    public record Unicidade(int simbolos, int distribuicoes, int comMaisDeUm, int maximo,
            long[] ondeDaOMaximo) {

        public double fracao() {
            return comMaisDeUm / (double) distribuicoes;
        }
    }

    private static List<Unicidade> unicidade;

    /**
     * QUANTAS ÁRVORES ÓTIMAS EXISTEM, que quase nunca é uma.
     *
     * <p>Falar em "a árvore de Huffman" sugere que o resultado é único. Não é: a cada empate
     * de peso na fila, duas junções diferentes levam a árvores diferentes com o MESMO custo
     * total, e o que decide qual sai é o critério de desempate da implementação.
     *
     * <p>A consequência prática é que dois programas corretos produzem arquivos comprimidos
     * diferentes, do mesmo tamanho, e nenhum dos dois está errado. Quem compara saída byte a
     * byte entre implementações está testando o desempate, não a compressão.
     */
    public static List<Unicidade> unicidade() {
        if (unicidade != null) return unicidade;

        List<Unicidade> saida = new ArrayList<>();

        for (int simbolos = 3; simbolos <= 6; simbolos++) {
            List<long[]> todas = ForcaBruta.todasAsFrequencias(simbolos, 16);

            int comMaisDeUm = 0;
            int maximo = 0;

            long[] onde = null;

            for (long[] frequencias : todas) {
                int empatam = ForcaBruta.melhor(frequencias).quantosEmpatam();

                if (empatam > 1) comMaisDeUm++;

                if (empatam > maximo) {
                    maximo = empatam;
                    onde = frequencias;
                }
            }

            saida.add(new Unicidade(simbolos, todas.size(), comMaisDeUm, maximo, onde));
        }

        return unicidade = List.copyOf(saida);
    }

    public static Unicidade unicidade(int simbolos) {
        return unicidade().stream().filter(x -> x.simbolos() == simbolos)
                .findFirst().orElseThrow();
    }

    /* ===================== 4. entropia ===================== */

    public record Folga(long[] frequencias, double entropia, double huffman) {

        public double folga() {
            return huffman - entropia;
        }

        public double vezes() {
            return entropia == 0 ? Double.POSITIVE_INFINITY : huffman / entropia;
        }
    }

    private static Folga piorFolga;

    /**
     * O QUANTO HUFFMAN PASSA DO PISO DE SHANNON.
     *
     * <p>A entropia é o piso: nenhum código de prefixo gasta menos que ela, em média. Huffman
     * é ótimo, mas ótimo entre códigos que dão um número INTEIRO de bits a cada símbolo, e o
     * piso não respeita essa restrição.
     *
     * <p>A folga é sempre menor que um bit por símbolo, e um bit é muito quando a entropia é
     * pequena. Essa é a diferença entre "ótimo" e "bom".
     */
    public static Folga piorFolga() {
        if (piorFolga != null) return piorFolga;

        Folga pior = null;

        for (int simbolos = 2; simbolos <= 5; simbolos++)
            for (int total : new int[] {10, 16, 24, 32})
                for (long[] frequencias : ForcaBruta.todasAsFrequencias(simbolos, total)) {
                    Folga atual = new Folga(frequencias, Huffman.entropia(frequencias),
                            Huffman.bitsPorSimbolo(frequencias));

                    if (pior == null || atual.folga() > pior.folga()) pior = atual;
                }

        return piorFolga = pior;
    }

    /** Casos escolhidos para a tabela, onde a diferença entre ótimo e bom aparece. */
    public static List<Folga> folgas() {
        long[][] casos = {{1, 1}, {3, 1}, {1, 31}, {97, 1, 1, 1}, {999, 1}, {8, 4, 2, 1, 1},
            {1, 1, 1, 1}};

        List<Folga> saida = new ArrayList<>();

        for (long[] frequencias : casos)
            saida.add(new Folga(frequencias, Huffman.entropia(frequencias),
                    Huffman.bitsPorSimbolo(frequencias)));

        return saida;
    }

    /** A folga nunca chega a um bit por símbolo. */
    public static boolean folgaSempreAbaixoDeUm() {
        for (int simbolos = 2; simbolos <= 5; simbolos++)
            for (int total : new int[] {10, 16, 24})
                for (long[] frequencias : ForcaBruta.todasAsFrequencias(simbolos, total))
                    if (Huffman.bitsPorSimbolo(frequencias) - Huffman.entropia(frequencias) >= 1)
                        return false;

        return true;
    }

    /* ===================== 5. ordem ===================== */

    public record Ordem(int simbolos, int distribuicoes, int violacoesDoHuffman,
            int ordenadosLegais, int ordenadosPioresQueOOtimo) {}

    private static List<Ordem> ordem;

    /**
     * "MAIS FREQUENTE GANHA CÓDIGO MENOR" é necessário e NÃO é suficiente.
     *
     * <p>A primeira coluna confere a parte necessária: o Huffman nunca dá código mais longo
     * para um símbolo mais frequente, em nenhuma distribuição varrida.
     *
     * <p>A segunda é a parte que derruba a regra como receita. Entre os vetores de
     * comprimento que satisfazem Kraft E respeitam a ordem, muitos custam mais que o ótimo.
     * Respeitar a ordem não basta para o código ser bom: é um teste que o ótimo passa e que
     * muita coisa ruim passa junto.
     */
    public static List<Ordem> ordem() {
        if (ordem != null) return ordem;

        List<Ordem> saida = new ArrayList<>();

        for (int simbolos = 3; simbolos <= 5; simbolos++) {
            List<long[]> todas = ForcaBruta.todasAsFrequencias(simbolos, 14);

            int violacoes = 0;
            int legais = 0;
            int piores = 0;

            for (long[] frequencias : todas) {
                int[] comprimentos = Huffman.comprimentos(frequencias);

                for (int a = 0; a < simbolos; a++)
                    for (int b = 0; b < simbolos; b++)
                        if (frequencias[a] > frequencias[b] && comprimentos[a] > comprimentos[b])
                            violacoes++;

                long otimo = ForcaBruta.melhor(frequencias).custo();

                for (int[] candidato : ForcaBruta.todosOsOrdenados(frequencias)) {
                    legais++;

                    if (Huffman.custo(frequencias, candidato) > otimo) piores++;
                }
            }

            saida.add(new Ordem(simbolos, todas.size(), violacoes, legais, piores));
        }

        return ordem = List.copyOf(saida);
    }

    public static Ordem ordem(int simbolos) {
        return ordem().stream().filter(x -> x.simbolos() == simbolos).findFirst().orElseThrow();
    }
}

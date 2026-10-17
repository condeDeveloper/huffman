package br.com.conde.huffman;

import java.util.ArrayList;
import java.util.List;

/**
 * O JUIZ: todos os códigos de prefixo possíveis, enumerados.
 *
 * <p>Esta classe não sabe o que é Huffman. Ela gera todo vetor de comprimentos que satisfaz a
 * desigualdade de Kraft e escolhe o de menor custo. Como Kraft é exatamente a condição para um
 * vetor de comprimentos virar um código de prefixo de verdade, enumerar vetores é enumerar
 * códigos, sem perder nenhum e sem inventar nenhum.
 *
 * <p>O domínio é finito: num alfabeto de {@code n} símbolos, nenhum código ótimo precisa passar
 * de {@code n - 1} bits, porque uma árvore binária com {@code n} folhas tem profundidade no
 * máximo {@code n - 1}. Então os vetores a testar são no máximo {@code (n-1)^n}, e para os
 * tamanhos deste repositório isso cabe de sobra.
 *
 * <p>É esse teto que transforma "o algoritmo é ótimo" de teorema citado em fato conferido.
 */
public final class ForcaBruta {

    private ForcaBruta() {}

    /** O melhor custo possível, e quantos vetores de comprimentos empatam nele. */
    public record Melhor(long custo, int[] comprimentos, int quantosEmpatam) {}

    /**
     * Varre todos os códigos de prefixo e devolve o melhor.
     *
     * <p>O teto de comprimento é {@code n - 1}: com mais que isso a árvore teria um nó interno
     * com um filho só, e cortar esse nó encurta um código sem alongar nenhum outro. Ou seja,
     * nenhum código acima do teto pode ser estritamente melhor, então ignorá-los não perde o
     * ótimo.
     */
    public static Melhor melhor(long[] frequencias) {
        int quantos = frequencias.length;

        if (quantos == 0) return new Melhor(0, new int[0], 1);

        if (quantos == 1) return new Melhor(frequencias[0], new int[] {1}, 1);

        int teto = quantos - 1;

        int[] atual = new int[quantos];
        int[] melhor = new int[quantos];

        long[] melhorCusto = {Long.MAX_VALUE};
        int[] empates = {0};

        varrer(frequencias, atual, 0, teto, melhor, melhorCusto, empates);

        return new Melhor(melhorCusto[0], melhor.clone(), empates[0]);
    }

    private static void varrer(long[] frequencias, int[] atual, int posicao, int teto,
            int[] melhor, long[] melhorCusto, int[] empates) {
        if (posicao == atual.length) {
            if (!Huffman.kraft(atual)) return;

            long custo = Huffman.custo(frequencias, atual);

            if (custo < melhorCusto[0]) {
                melhorCusto[0] = custo;
                empates[0] = 1;

                System.arraycopy(atual, 0, melhor, 0, atual.length);
            } else if (custo == melhorCusto[0]) {
                empates[0]++;
            }

            return;
        }

        for (int comprimento = 1; comprimento <= teto; comprimento++) {
            atual[posicao] = comprimento;

            varrer(frequencias, atual, posicao + 1, teto, melhor, melhorCusto, empates);
        }
    }

    /** Todos os vetores de comprimentos que empatam no custo ótimo. */
    public static List<int[]> todosOsOtimos(long[] frequencias) {
        List<int[]> saida = new ArrayList<>();

        int quantos = frequencias.length;

        if (quantos <= 1) {
            saida.add(quantos == 0 ? new int[0] : new int[] {1});

            return saida;
        }

        long otimo = melhor(frequencias).custo();

        int[] atual = new int[quantos];

        colher(frequencias, atual, 0, quantos - 1, otimo, saida);

        return saida;
    }

    private static void colher(long[] frequencias, int[] atual, int posicao, int teto,
            long otimo, List<int[]> saida) {
        if (posicao == atual.length) {
            if (Huffman.kraft(atual) && Huffman.custo(frequencias, atual) == otimo)
                saida.add(atual.clone());

            return;
        }

        for (int comprimento = 1; comprimento <= teto; comprimento++) {
            atual[posicao] = comprimento;

            colher(frequencias, atual, posicao + 1, teto, otimo, saida);
        }
    }

    /**
     * Os vetores de comprimento que satisfazem Kraft E respeitam a ordem das frequencias:
     * simbolo mais frequente nunca com codigo mais longo.
     *
     * <p>Sao os candidatos que passam no teste de bolso "mais frequente, codigo menor". A
     * quinta medida conta quantos deles, mesmo passando, custam mais que o otimo.
     */
    public static List<int[]> todosOsOrdenados(long[] frequencias) {
        List<int[]> saida = new ArrayList<>();

        int quantos = frequencias.length;

        if (quantos <= 1) {
            saida.add(quantos == 0 ? new int[0] : new int[] {1});

            return saida;
        }

        ordenados(frequencias, new int[quantos], 0, quantos - 1, saida);

        return saida;
    }

    private static void ordenados(long[] frequencias, int[] atual, int posicao, int teto,
            List<int[]> saida) {
        if (posicao == atual.length) {
            if (!Huffman.kraft(atual)) return;

            for (int a = 0; a < atual.length; a++)
                for (int b = 0; b < atual.length; b++)
                    if (frequencias[a] > frequencias[b] && atual[a] > atual[b]) return;

            saida.add(atual.clone());

            return;
        }

        for (int comprimento = 1; comprimento <= teto; comprimento++) {
            atual[posicao] = comprimento;

            ordenados(frequencias, atual, posicao + 1, teto, saida);
        }
    }

    /** Quantos vetores de comprimentos satisfazem Kraft, de todos os testados. */
    public record Espaco(long testados, long passamNoKraft, long cheios) {}

    public static Espaco espaco(int simbolos) {
        int teto = Math.max(1, simbolos - 1);

        long[] contagem = {0, 0, 0};

        contar(new int[simbolos], 0, teto, contagem);

        return new Espaco(contagem[0], contagem[1], contagem[2]);
    }

    private static void contar(int[] atual, int posicao, int teto, long[] contagem) {
        if (posicao == atual.length) {
            contagem[0]++;

            if (Huffman.kraft(atual)) {
                contagem[1]++;

                if (Huffman.kraftCheia(atual)) contagem[2]++;
            }

            return;
        }

        for (int comprimento = 1; comprimento <= teto; comprimento++) {
            atual[posicao] = comprimento;

            contar(atual, posicao + 1, teto, contagem);
        }
    }

    /**
     * TODOS OS VETORES DE FREQUÊNCIA com este tanto de símbolos e esta soma.
     *
     * <p>É o que torna as medidas varredura em vez de exemplo: não um punhado de distribuições
     * escolhidas, mas todas as que existem com aquele total.
     */
    public static List<long[]> todasAsFrequencias(int simbolos, int total) {
        List<long[]> saida = new ArrayList<>();

        montar(new long[simbolos], 0, total, saida);

        return saida;
    }

    private static void montar(long[] atual, int posicao, int resta, List<long[]> saida) {
        if (posicao == atual.length - 1) {
            if (resta < 1) return;

            atual[posicao] = resta;

            saida.add(atual.clone());

            return;
        }

        for (int valor = 1; valor <= resta - (atual.length - posicao - 1); valor++) {
            atual[posicao] = valor;

            montar(atual, posicao + 1, resta - valor, saida);
        }
    }
}

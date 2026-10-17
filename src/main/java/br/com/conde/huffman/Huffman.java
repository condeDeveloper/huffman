package br.com.conde.huffman;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * O ALGORITMO DE HUFFMAN, de 1952: junte sempre os dois menores.
 *
 * <p>Cada símbolo começa como uma árvore sozinha, com o peso da sua frequência. Junte as duas
 * árvores mais leves numa só, cujo peso é a soma. Repita até sobrar uma. A profundidade de cada
 * folha na árvore final é o comprimento do código daquele símbolo.
 *
 * <p>É um algoritmo guloso, e guloso quase nunca dá ótimo. Este dá, e a prova é curta: os dois
 * símbolos menos frequentes podem sempre ser postos como irmãos no nível mais fundo sem piorar
 * nada, e a partir daí o problema fica menor.
 *
 * <p>Aqui a prova não é usada. O que este repositório faz é CONFERIR o resultado contra todos
 * os códigos de prefixo possíveis, que é um conjunto finito e enumerável para alfabetos
 * pequenos. Se o algoritmo errasse, a varredura acharia o código melhor.
 *
 * <p>O que importa para o custo é só o COMPRIMENTO de cada código, não quais bits ele usa.
 * Por isso tudo aqui trabalha com vetores de comprimentos, e não com as palavras de código.
 */
public final class Huffman {

    private Huffman() {}

    /** Um nó da árvore durante a junção: peso e profundidade das folhas que ele carrega. */
    private record No(long peso, int[] folhas, int ordem) {}

    /**
     * O comprimento do código de cada símbolo, pela junção dos dois menores.
     *
     * <p>Com um símbolo só o comprimento é 1, e não 0: um arquivo de um símbolo repetido ainda
     * precisa de um bit por ocorrência para existir como código de prefixo. Zero bits não
     * distinguem nada de nada.
     */
    public static int[] comprimentos(long[] frequencias) {
        int quantos = frequencias.length;

        if (quantos == 0) return new int[0];

        if (quantos == 1) return new int[] {1};

        int[] saida = new int[quantos];

        /* O desempate pela ordem de criação deixa o algoritmo determinístico. Sem ele, duas
         * árvores de mesmo peso saem em ordem imprevisível, e o custo total não muda, mas os
         * comprimentos de cada símbolo mudam. A terceira medida é exatamente sobre isso. */
        PriorityQueue<No> fila = new PriorityQueue<>((a, b) ->
                a.peso() != b.peso() ? Long.compare(a.peso(), b.peso())
                        : Integer.compare(a.ordem(), b.ordem()));

        int ordem = 0;

        for (int simbolo = 0; simbolo < quantos; simbolo++)
            fila.add(new No(frequencias[simbolo], new int[] {simbolo}, ordem++));

        while (fila.size() > 1) {
            No menor = fila.poll();
            No outro = fila.poll();

            /* Descer um nível custa um bit a mais para toda folha dos dois lados. */
            for (int folha : menor.folhas()) saida[folha]++;
            for (int folha : outro.folhas()) saida[folha]++;

            int[] juntas = new int[menor.folhas().length + outro.folhas().length];

            System.arraycopy(menor.folhas(), 0, juntas, 0, menor.folhas().length);
            System.arraycopy(outro.folhas(), 0, juntas, menor.folhas().length,
                    outro.folhas().length);

            fila.add(new No(menor.peso() + outro.peso(), juntas, ordem++));
        }

        return saida;
    }

    /** Quantos bits o arquivo inteiro ocupa com estes comprimentos. */
    public static long custo(long[] frequencias, int[] comprimentos) {
        long total = 0;

        for (int simbolo = 0; simbolo < frequencias.length; simbolo++)
            total += frequencias[simbolo] * (long) comprimentos[simbolo];

        return total;
    }

    public static long custo(long[] frequencias) {
        return custo(frequencias, comprimentos(frequencias));
    }

    /**
     * A DESIGUALDADE DE KRAFT: a soma de {@code 2^-comprimento} não passa de 1.
     *
     * <p>Ela é a tradução exata de "dá para montar um código de prefixo com estes
     * comprimentos". Cada código de comprimento {@code c} reserva uma fatia {@code 2^-c} do
     * espaço de todas as sequências infinitas de bits, e as fatias não podem se sobrepor,
     * porque nenhum código pode ser prefixo de outro.
     *
     * <p>Trabalhar com denominador comum evita ponto flutuante: a soma vira inteiro.
     */
    public static boolean kraft(int[] comprimentos) {
        int maior = 0;

        for (int comprimento : comprimentos) maior = Math.max(maior, comprimento);

        if (maior > 30) throw new IllegalArgumentException("comprimento grande demais: " + maior);

        long soma = 0;

        for (int comprimento : comprimentos) soma += 1L << (maior - comprimento);

        return soma <= 1L << maior;
    }

    /** A soma de Kraft dá exatamente 1 quando a árvore não desperdiça nenhum ramo. */
    public static boolean kraftCheia(int[] comprimentos) {
        int maior = 0;

        for (int comprimento : comprimentos) maior = Math.max(maior, comprimento);

        long soma = 0;

        for (int comprimento : comprimentos) soma += 1L << (maior - comprimento);

        return soma == 1L << maior;
    }

    /** As palavras de código de verdade, para quem quiser ver que elas existem. */
    public static List<String> palavras(int[] comprimentos) {
        Integer[] ordem = new Integer[comprimentos.length];

        for (int i = 0; i < ordem.length; i++) ordem[i] = i;

        java.util.Arrays.sort(ordem, (a, b) -> comprimentos[a] != comprimentos[b]
                ? Integer.compare(comprimentos[a], comprimentos[b]) : Integer.compare(a, b));

        String[] saida = new String[comprimentos.length];

        long codigo = 0;
        int anterior = 0;

        for (int indice : ordem) {
            codigo <<= comprimentos[indice] - anterior;

            anterior = comprimentos[indice];

            String bits = Long.toBinaryString(codigo);

            /* O zero tambem e uma palavra: Long.toBinaryString(0) ja da "0", e tratar esse
             * caso a parte produzia palavra VAZIA para o primeiro codigo. */
            saida[indice] = "0".repeat(Math.max(0, anterior - bits.length())) + bits;

            codigo++;
        }

        return List.of(saida);
    }

    /** A entropia de Shannon, em bits por símbolo: o piso de qualquer código. */
    public static double entropia(long[] frequencias) {
        long total = 0;

        for (long frequencia : frequencias) total += frequencia;

        if (total == 0) return 0;

        double soma = 0;

        for (long frequencia : frequencias) {
            if (frequencia == 0) continue;

            double p = frequencia / (double) total;

            soma -= p * (Math.log(p) / Math.log(2));
        }

        return soma;
    }

    /** Bits por símbolo que o código de Huffman gasta de verdade. */
    public static double bitsPorSimbolo(long[] frequencias) {
        long total = 0;

        for (long frequencia : frequencias) total += frequencia;

        return total == 0 ? 0 : custo(frequencias) / (double) total;
    }

    /** Todas as frequências somadas. */
    public static long total(long[] frequencias) {
        long soma = 0;

        for (long frequencia : frequencias) soma += frequencia;

        return soma;
    }

    /** As frequências ordenadas da maior para a menor, sem mexer no vetor original. */
    public static long[] ordenadas(long[] frequencias) {
        long[] copia = frequencias.clone();

        java.util.Arrays.sort(copia);

        long[] saida = new long[copia.length];

        for (int i = 0; i < copia.length; i++) saida[i] = copia[copia.length - 1 - i];

        return saida;
    }

    /** Converte um vetor de contagens em lista, para as mensagens de teste. */
    public static List<Long> comoLista(long[] frequencias) {
        List<Long> saida = new ArrayList<>();

        for (long frequencia : frequencias) saida.add(frequencia);

        return saida;
    }
}

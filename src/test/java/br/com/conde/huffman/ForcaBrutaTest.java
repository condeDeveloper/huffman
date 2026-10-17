package br.com.conde.huffman;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * O juiz julgado.
 *
 * <p>Dizer "Huffman concorda com a força bruta em 17.125 distribuições" só vale alguma coisa
 * se a força bruta discordasse de um algoritmo QUEBRADO. Os dois últimos testes plantam
 * defeitos de propósito e conferem que ela acusa os dois.
 */
class ForcaBrutaTest {

    @Test
    @DisplayName("a geracao de frequencias nao repete nem pula distribuicao")
    void geracaoCompleta() {
        List<long[]> todas = ForcaBruta.todasAsFrequencias(3, 10);

        Set<String> unicas = new HashSet<>();

        for (long[] frequencias : todas) {
            assertEquals(10, Huffman.total(frequencias), java.util.Arrays.toString(frequencias));

            for (long frequencia : frequencias)
                assertTrue(frequencia >= 1, "frequencia zero nao e simbolo");

            unicas.add(java.util.Arrays.toString(frequencias));
        }

        assertEquals(todas.size(), unicas.size());

        /* Tres partes positivas somando dez: C(9,2) = 36. */
        assertEquals(36, todas.size());
        assertEquals(1, ForcaBruta.todasAsFrequencias(2, 2).size());
    }

    @Test
    @DisplayName("o melhor achado satisfaz kraft e e mesmo o mais barato que passa")
    void melhorEhMelhor() {
        for (long[] frequencias : ForcaBruta.todasAsFrequencias(4, 12)) {
            ForcaBruta.Melhor melhor = ForcaBruta.melhor(frequencias);

            assertTrue(Huffman.kraft(melhor.comprimentos()));

            assertEquals(melhor.custo(),
                    Huffman.custo(frequencias, melhor.comprimentos()));

            for (int[] candidato : ForcaBruta.todosOsOtimos(frequencias))
                assertEquals(melhor.custo(), Huffman.custo(frequencias, candidato));
        }
    }

    @Test
    @DisplayName("a contagem de empates bate com a lista de otimos")
    void empatesBatemComALista() {
        for (int simbolos = 2; simbolos <= 5; simbolos++)
            for (long[] frequencias : ForcaBruta.todasAsFrequencias(simbolos, simbolos + 6))
                assertEquals(ForcaBruta.melhor(frequencias).quantosEmpatam(),
                        ForcaBruta.todosOsOtimos(frequencias).size(),
                        java.util.Arrays.toString(frequencias));
    }

    @Test
    @DisplayName("O JUIZ PEGA: um huffman que junta os DOIS MAIORES erra quase sempre")
    void juizPegaOsDoisMaiores() {
        int errou = 0;
        int total = 0;

        for (long[] frequencias : ForcaBruta.todasAsFrequencias(4, 14)) {
            total++;

            if (custoJuntandoOsMaiores(frequencias) > ForcaBruta.melhor(frequencias).custo())
                errou++;
        }

        assertTrue(errou > total / 2, "so " + errou + " de " + total);
    }

    /** O defeito plantado: juntar sempre as duas arvores mais PESADAS. */
    private long custoJuntandoOsMaiores(long[] frequencias) {
        int quantos = frequencias.length;

        int[] comprimentos = new int[quantos];

        List<long[]> arvores = new java.util.ArrayList<>();

        for (int simbolo = 0; simbolo < quantos; simbolo++)
            arvores.add(new long[] {frequencias[simbolo], simbolo});

        List<List<Integer>> folhas = new java.util.ArrayList<>();

        for (int simbolo = 0; simbolo < quantos; simbolo++)
            folhas.add(new java.util.ArrayList<>(List.of(simbolo)));

        while (arvores.size() > 1) {
            int maior = 0;
            int segundo = 1;

            for (int i = 0; i < arvores.size(); i++)
                if (arvores.get(i)[0] > arvores.get(maior)[0]) maior = i;

            for (int i = 0; i < arvores.size(); i++)
                if (i != maior && (segundo == maior || arvores.get(i)[0] > arvores.get(segundo)[0]))
                    segundo = i;

            for (int folha : folhas.get(maior)) comprimentos[folha]++;
            for (int folha : folhas.get(segundo)) comprimentos[folha]++;

            long peso = arvores.get(maior)[0] + arvores.get(segundo)[0];

            List<Integer> juntas = new java.util.ArrayList<>(folhas.get(maior));

            juntas.addAll(folhas.get(segundo));

            int primeiro = Math.min(maior, segundo);
            int ultimo = Math.max(maior, segundo);

            arvores.remove(ultimo);
            arvores.remove(primeiro);
            folhas.remove(ultimo);
            folhas.remove(primeiro);

            arvores.add(new long[] {peso, -1});
            folhas.add(juntas);
        }

        return Huffman.custo(frequencias, comprimentos);
    }

    @Test
    @DisplayName("E PEGA TAMBEM um codigo de comprimento fixo, que e legal e caro")
    void juizPegaOComprimentoFixo() {
        int errou = 0;
        int total = 0;

        for (long[] frequencias : ForcaBruta.todasAsFrequencias(4, 14)) {
            total++;

            /* Dois bits para cada um dos quatro simbolos: satisfaz kraft, e cheio, e e o
             * que se usa quando nao se comprime nada. */
            int[] fixo = {2, 2, 2, 2};

            assertTrue(Huffman.kraft(fixo));

            if (Huffman.custo(frequencias, fixo) > ForcaBruta.melhor(frequencias).custo())
                errou++;
        }

        assertTrue(errou > total / 2, "so " + errou + " de " + total);
    }

    @Test
    @DisplayName("os ordenados sao um pedaco dos que passam no kraft")
    void ordenadosSaoUmPedaco() {
        long[] frequencias = {5, 4, 3, 2};

        List<int[]> ordenados = ForcaBruta.todosOsOrdenados(frequencias);

        assertTrue(ordenados.size() > 0);

        for (int[] candidato : ordenados) {
            assertTrue(Huffman.kraft(candidato));

            for (int a = 0; a < 4; a++)
                for (int b = 0; b < 4; b++)
                    if (frequencias[a] > frequencias[b])
                        assertTrue(candidato[a] <= candidato[b],
                                java.util.Arrays.toString(candidato));
        }

        assertTrue(ordenados.size() <= ForcaBruta.espaco(4).passamNoKraft());
    }
}

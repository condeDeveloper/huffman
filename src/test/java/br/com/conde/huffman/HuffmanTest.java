package br.com.conde.huffman;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * O algoritmo sozinho, e as propriedades que o código de prefixo precisa ter para ser código
 * de prefixo: Kraft, e nenhuma palavra sendo prefixo de outra.
 */
class HuffmanTest {

    @Test
    @DisplayName("um simbolo so ainda custa um bit por ocorrencia")
    void umSimbolo() {
        assertArrayEquals(new int[] {1}, Huffman.comprimentos(new long[] {7}));

        /* Zero bits nao distinguem nada de nada: nao existe codigo de comprimento zero. */
        assertEquals(7, Huffman.custo(new long[] {7}));
    }

    @Test
    @DisplayName("alfabeto vazio nao custa nada")
    void semSimbolo() {
        assertArrayEquals(new int[0], Huffman.comprimentos(new long[0]));
        assertEquals(0, Huffman.custo(new long[0]));
    }

    @Test
    @DisplayName("o exemplo que se faz a mao: 8, 4, 1, 1 vira 1, 2, 3, 3")
    void exemploDeMao() {
        long[] frequencias = {8, 4, 1, 1};

        assertArrayEquals(new int[] {1, 2, 3, 3}, Huffman.comprimentos(frequencias));

        assertEquals(8 + 8 + 3 + 3, Huffman.custo(frequencias));
    }

    @Test
    @DisplayName("frequencias iguais dao comprimentos iguais, quando sao potencia de dois")
    void todosIguais() {
        for (int expoente = 1; expoente <= 4; expoente++) {
            int quantos = 1 << expoente;

            long[] frequencias = new long[quantos];

            java.util.Arrays.fill(frequencias, 1);

            for (int comprimento : Huffman.comprimentos(frequencias))
                assertEquals(expoente, comprimento, quantos + " simbolos");
        }
    }

    @Test
    @DisplayName("O CODIGO SEMPRE SATISFAZ KRAFT, e sem desperdicar ramo")
    void sempreKraft() {
        for (int simbolos = 1; simbolos <= 6; simbolos++)
            for (long[] frequencias : ForcaBruta.todasAsFrequencias(simbolos, simbolos + 6)) {
                int[] comprimentos = Huffman.comprimentos(frequencias);

                assertTrue(Huffman.kraft(comprimentos),
                        java.util.Arrays.toString(frequencias));

                /* Com DOIS ou mais simbolos a arvore nao sobra ramo. Com um simbolo so
                 * sobra, e nao da para evitar: o codigo e um bit, e o outro valor daquele
                 * bit nao nomeia nada. */
                if (simbolos > 1)
                    assertTrue(Huffman.kraftCheia(comprimentos),
                            java.util.Arrays.toString(frequencias) + " desperdica ramo");
                else
                    assertFalse(Huffman.kraftCheia(comprimentos),
                            "com um simbolo o ramo sobrando e inevitavel");
            }
    }

    @Test
    @DisplayName("kraft reprova o que nao da para montar")
    void kraftReprova() {
        assertTrue(Huffman.kraft(new int[] {1, 2, 3, 3}));
        assertTrue(Huffman.kraft(new int[] {2, 2, 2, 2}));

        /* Tres codigos de um bit nao cabem: so existem dois. */
        assertFalse(Huffman.kraft(new int[] {1, 1, 1}));
        assertFalse(Huffman.kraft(new int[] {1, 2, 2, 2}));

        assertFalse(Huffman.kraftCheia(new int[] {1, 2, 3}));
        assertTrue(Huffman.kraft(new int[] {1, 2, 3}));
    }

    @Test
    @DisplayName("NENHUMA PALAVRA E PREFIXO DE OUTRA, que e o ponto de tudo isto")
    void nenhumaEhPrefixo() {
        for (int simbolos = 1; simbolos <= 6; simbolos++)
            for (long[] frequencias : ForcaBruta.todasAsFrequencias(simbolos, simbolos + 5)) {
                List<String> palavras = Huffman.palavras(Huffman.comprimentos(frequencias));

                Set<String> unicas = new HashSet<>(palavras);

                assertEquals(palavras.size(), unicas.size(),
                        java.util.Arrays.toString(frequencias) + " repetiu palavra");

                for (String uma : palavras) {
                    assertFalse(uma.isEmpty(), "palavra vazia");

                    for (String outra : palavras)
                        if (uma != outra)
                            assertFalse(outra.startsWith(uma),
                                    uma + " e prefixo de " + outra + " em "
                                            + java.util.Arrays.toString(frequencias));
                }
            }
    }

    @Test
    @DisplayName("as palavras tem o comprimento que o algoritmo mandou")
    void comprimentoDasPalavras() {
        int[] comprimentos = {1, 2, 3, 3};

        List<String> palavras = Huffman.palavras(comprimentos);

        assertEquals(List.of("0", "10", "110", "111"), palavras);

        for (int indice = 0; indice < comprimentos.length; indice++)
            assertEquals(comprimentos[indice], palavras.get(indice).length());
    }

    @Test
    @DisplayName("a entropia de uma moeda honesta e um bit, e a de um resultado certo e zero")
    void entropia() {
        assertEquals(1.0, Huffman.entropia(new long[] {1, 1}), 1e-12);
        assertEquals(2.0, Huffman.entropia(new long[] {1, 1, 1, 1}), 1e-12);
        assertEquals(3.0, Huffman.entropia(new long[] {1, 1, 1, 1, 1, 1, 1, 1}), 1e-12);

        assertEquals(0.0, Huffman.entropia(new long[] {5}), 1e-12);
        assertEquals(0.0, Huffman.entropia(new long[] {5, 0}), 1e-12);
    }

    @Test
    @DisplayName("quando as frequencias sao potencias de dois, huffman encosta no piso")
    void encostaNoPiso() {
        long[][] casos = {{1, 1}, {2, 1, 1}, {4, 2, 1, 1}, {8, 4, 2, 1, 1}};

        for (long[] frequencias : casos)
            assertEquals(Huffman.entropia(frequencias), Huffman.bitsPorSimbolo(frequencias),
                    1e-12, java.util.Arrays.toString(frequencias));
    }

    @Test
    @DisplayName("ordenar as frequencias nao muda o custo total")
    void ordemNaoMudaOCusto() {
        for (int simbolos = 2; simbolos <= 6; simbolos++)
            for (long[] frequencias : ForcaBruta.todasAsFrequencias(simbolos, simbolos + 6))
                assertEquals(Huffman.custo(frequencias),
                        Huffman.custo(Huffman.ordenadas(frequencias)),
                        java.util.Arrays.toString(frequencias));
    }
}

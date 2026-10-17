package br.com.conde.huffman;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Os números que aparecem escritos no README, travados em teste. */
class MedidasTest {

    /* ===================== 1. correcao ===================== */

    @Test
    @DisplayName("zero divergencias em 17.125 distribuicoes")
    void correcao() {
        assertEquals(17_125, Medidas.totalDeDistribuicoes());
        assertEquals(0, Medidas.totalDeDivergencias());

        for (Medidas.Correcao linha : Medidas.correcao())
            assertEquals(0, linha.divergencias(),
                    linha.simbolos() + " simbolos, total " + linha.total());
    }

    /* ===================== 2. kraft ===================== */

    @Test
    @DisplayName("com sete simbolos, 114.937 dos 279.936 vetores sao codigo de verdade")
    void kraft() {
        Medidas.Kraft sete = Medidas.kraft(7);

        assertEquals(279_936, sete.vetores());
        assertEquals(114_937, sete.passam());
        assertEquals(4_347, sete.cheios());

        /* O vetor de dois simbolos so pode ser 1,1: um unico codigo possivel. */
        assertEquals(1, Medidas.kraft(2).vetores());
        assertEquals(1, Medidas.kraft(2).passam());
    }

    @Test
    @DisplayName("a maioria dos codigos legais desperdica ramo da arvore")
    void legaisMasGastoes() {
        for (Medidas.Kraft linha : Medidas.kraft()) {
            assertTrue(linha.cheios() <= linha.passam(), linha.simbolos() + " simbolos");

            if (linha.simbolos() >= 4)
                assertTrue(linha.cheios() * 2 < linha.passam(),
                        linha.simbolos() + " simbolos: " + linha.cheios() + " de "
                                + linha.passam());
        }
    }

    /* ===================== 3. unicidade ===================== */

    @Test
    @DisplayName("A ARVORE OTIMA QUASE NUNCA E UNICA: 69,5% com seis simbolos")
    void unicidade() {
        assertEquals(105, Medidas.unicidade(3).distribuicoes());
        assertEquals(6, Medidas.unicidade(3).comMaisDeUm());

        assertEquals(3003, Medidas.unicidade(6).distribuicoes());
        assertEquals(2088, Medidas.unicidade(6).comMaisDeUm());

        assertTrue(Medidas.unicidade(6).fracao() > 0.69);

        /* A fracao cresce a cada simbolo novo. */
        for (int simbolos = 4; simbolos <= 6; simbolos++)
            assertTrue(Medidas.unicidade(simbolos).fracao()
                    > Medidas.unicidade(simbolos - 1).fracao(), simbolos + " simbolos");
    }

    @Test
    @DisplayName("o recorde sao quinze codigos otimos para a mesma distribuicao")
    void recordeDeEmpates() {
        Medidas.Unicidade seis = Medidas.unicidade(6);

        assertEquals(15, seis.maximo());
        assertArrayEquals(new long[] {2, 2, 2, 2, 2, 6}, seis.ondeDaOMaximo());

        assertEquals(2, Medidas.unicidade(3).maximo());
        assertEquals(3, Medidas.unicidade(4).maximo());
        assertEquals(7, Medidas.unicidade(5).maximo());
    }

    /* ===================== 4. entropia ===================== */

    @Test
    @DisplayName("a folga nunca chega a um bit por simbolo")
    void folgaAbaixoDeUm() {
        assertTrue(Medidas.folgaSempreAbaixoDeUm());

        assertTrue(Medidas.piorFolga().folga() < 1.0);
        assertEquals(0.7994, Medidas.piorFolga().folga(), 1e-4);
        assertArrayEquals(new long[] {1, 31}, Medidas.piorFolga().frequencias());
    }

    @Test
    @DisplayName("mas um bit e muito: com 999 e 1, huffman gasta 87 vezes a entropia")
    void umBitEhMuito() {
        long[] quaseTudo = {999, 1};

        assertEquals(1.0, Huffman.bitsPorSimbolo(quaseTudo), 1e-12);
        assertEquals(0.0114, Huffman.entropia(quaseTudo), 1e-4);

        assertTrue(Huffman.bitsPorSimbolo(quaseTudo) / Huffman.entropia(quaseTudo) > 85);
    }

    @Test
    @DisplayName("quando as frequencias sao potencias de dois, a folga some")
    void folgaZero() {
        for (Medidas.Folga linha : Medidas.folgas()) {
            assertTrue(linha.folga() >= -1e-12, java.util.Arrays.toString(linha.frequencias()));
            assertTrue(linha.folga() < 1.0, java.util.Arrays.toString(linha.frequencias()));
        }

        assertEquals(0.0, Huffman.bitsPorSimbolo(new long[] {8, 4, 2, 1, 1})
                - Huffman.entropia(new long[] {8, 4, 2, 1, 1}), 1e-12);
    }

    /* ===================== 5. ordem ===================== */

    @Test
    @DisplayName("o huffman NUNCA da codigo mais longo para simbolo mais frequente")
    void huffmanNuncaViolaAOrdem() {
        for (Medidas.Ordem linha : Medidas.ordem())
            assertEquals(0, linha.violacoesDoHuffman(), linha.simbolos() + " simbolos");
    }

    @Test
    @DisplayName("mas respeitar a ordem nao basta: 28.395 dos 29.905 ordenados gastam mais")
    void ordemNaoBasta() {
        Medidas.Ordem cinco = Medidas.ordem(5);

        assertEquals(29_905, cinco.ordenadosLegais());
        assertEquals(28_395, cinco.ordenadosPioresQueOOtimo());

        assertTrue(cinco.ordenadosPioresQueOOtimo() > 0.94 * cinco.ordenadosLegais());

        Medidas.Ordem tres = Medidas.ordem(3);

        assertEquals(162, tres.ordenadosLegais());
        assertEquals(78, tres.ordenadosPioresQueOOtimo());
    }

    @Test
    @DisplayName("e um exemplo concreto, de cabeca: 5, 4, 3 e 2")
    void exemploConcreto() {
        long[] frequencias = {5, 4, 3, 2};

        /* 1, 2, 3, 3 respeita a ordem e satisfaz kraft. */
        int[] ordenado = {1, 2, 3, 3};

        assertTrue(Huffman.kraft(ordenado));

        assertEquals(5 + 8 + 9 + 6, Huffman.custo(frequencias, ordenado));

        /* E o otimo sao 28, que o comprimento fixo tambem alcanca. */
        assertEquals(28, ForcaBruta.melhor(frequencias).custo());
        assertEquals(28, Huffman.custo(frequencias));
        assertArrayEquals(new int[] {2, 2, 2, 2}, Huffman.comprimentos(frequencias));
    }
}

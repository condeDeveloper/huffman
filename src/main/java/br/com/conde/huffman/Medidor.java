package br.com.conde.huffman;

import java.util.Locale;

/** O MEDIDOR: roda as varreduras e imprime as tabelas do README. */
public final class Medidor {

    private Medidor() {}

    public static void main(String[] argumentos) {
        Locale.setDefault(Locale.US);

        String passo = argumentos.length > 0 ? argumentos[0] : "tudo";

        switch (passo) {
            case "correcao" -> correcao();
            case "kraft" -> kraft();
            case "unicidade" -> unicidade();
            case "entropia" -> entropia();
            case "ordem" -> ordem();
            default -> {
                correcao();
                kraft();
                unicidade();
                entropia();
                ordem();
            }
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println(texto);
        System.out.println("=".repeat(texto.length()));
        System.out.println();
    }

    static void correcao() {
        titulo("1. huffman contra todos os codigos de prefixo possiveis");

        System.out.println("simbolos  total  distribuicoes  divergencias");

        for (Medidas.Correcao linha : Medidas.correcao())
            System.out.printf("%8d  %5d  %13d  %12d%n", linha.simbolos(), linha.total(),
                    linha.distribuicoes(), linha.divergencias());

        System.out.println();
        System.out.printf("%d distribuicoes varridas, %d divergencias.%n",
                Medidas.totalDeDistribuicoes(), Medidas.totalDeDivergencias());
        System.out.println("a forca bruta nao sabe o que e huffman: ela enumera todo vetor de");
        System.out.println("comprimentos que satisfaz kraft e pega o mais barato.");
    }

    static void kraft() {
        titulo("2. quantos vetores de comprimento sao codigo de verdade");

        System.out.println("simbolos  vetores  passam no kraft  cheios  fracao que passa");

        for (Medidas.Kraft linha : Medidas.kraft())
            System.out.printf("%8d  %7d  %15d  %6d  %15.1f%%%n", linha.simbolos(),
                    linha.vetores(), linha.passam(), linha.cheios(),
                    100 * linha.fracaoQuePassa());

        System.out.println();
        System.out.println("entre passar no kraft e ser cheio mora o codigo legal que ninguem");
        System.out.println("deveria usar: ele funciona e desperdica ramo da arvore.");
    }

    static void unicidade() {
        titulo("3. quantas arvores otimas existem");

        System.out.println("simbolos  distribuicoes  com mais de um otimo  fracao  maximo");

        for (Medidas.Unicidade linha : Medidas.unicidade())
            System.out.printf("%8d  %13d  %20d  %5.1f%%  %6d%n", linha.simbolos(),
                    linha.distribuicoes(), linha.comMaisDeUm(), 100 * linha.fracao(),
                    linha.maximo());

        Medidas.Unicidade seis = Medidas.unicidade(6);

        System.out.println();
        System.out.printf("o recorde e %d codigos otimos, em %s.%n", seis.maximo(),
                java.util.Arrays.toString(seis.ondeDaOMaximo()));
        System.out.println("falar em A arvore de huffman sugere unicidade que nao existe:");
        System.out.println("quem decide qual sai e o criterio de desempate da implementacao.");
    }

    static void entropia() {
        titulo("4. o quanto huffman passa do piso de shannon");

        System.out.println("frequencias        entropia  huffman   folga  quantas vezes");

        for (Medidas.Folga linha : Medidas.folgas())
            System.out.printf("%-18s %8.4f  %7.4f  %6.4f  %13s%n",
                    java.util.Arrays.toString(linha.frequencias()), linha.entropia(),
                    linha.huffman(), linha.folga(),
                    Double.isInfinite(linha.vezes()) ? "-"
                            : String.format("%.1f", linha.vezes()));

        Medidas.Folga pior = Medidas.piorFolga();

        System.out.println();
        System.out.printf("pior folga varrida: %.4f bits por simbolo, em %s.%n", pior.folga(),
                java.util.Arrays.toString(pior.frequencias()));
        System.out.println("otimo quer dizer melhor entre os codigos que dao um numero INTEIRO");
        System.out.println("de bits por simbolo. o piso de shannon nao respeita essa regra.");
    }

    static void ordem() {
        titulo("5. mais frequente ganha codigo menor");

        System.out.println("simbolos  distribuicoes  huffman viola  ordenados legais  piores que o otimo");

        for (Medidas.Ordem linha : Medidas.ordem())
            System.out.printf("%8d  %13d  %13d  %16d  %18d%n", linha.simbolos(),
                    linha.distribuicoes(), linha.violacoesDoHuffman(), linha.ordenadosLegais(),
                    linha.ordenadosPioresQueOOtimo());

        System.out.println();
        System.out.println("o huffman nunca viola a ordem: essa parte da regra e necessaria.");
        System.out.println("mas respeitar a ordem nao basta, e a ultima coluna conta quantos");
        System.out.println("codigos passam no teste de bolso e mesmo assim gastam mais.");
    }
}

package testes;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

// Gera arquivos de contatos (nome;telefone) para os benchmarks empiricos, com telefones unicos.
public class GeradorDadosContatos {
    private static final String[] PRIMEIROS_NOMES = {
        "Ana", "Bruno", "Carla", "Daniel", "Elisa", "Fabio", "Gabriela", "Hugo", "Isabela", "Joao",
        "Karina", "Lucas", "Marina", "Nicolas", "Olivia", "Pedro", "Queila", "Rafael", "Sofia", "Thiago",
        "Ursula", "Victor", "Wesley", "Ximena", "Yasmin", "Zeca", "Aline", "Bernardo", "Camila", "Diego"
    };
    private static final String[] SOBRENOMES = {
        "Silva", "Souza", "Oliveira", "Santos", "Pereira", "Costa", "Rodrigues", "Almeida", "Nascimento", "Lima",
        "Araujo", "Fernandes", "Carvalho", "Gomes", "Martins", "Rocha", "Ribeiro", "Alves", "Monteiro", "Cardoso"
    };

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Uso: java testes.GeradorDadosContatos <quantidade> <arquivoSaida> [seed]");
            return;
        }
        int quantidade = Integer.parseInt(args[0]);
        if (quantidade < 1) throw new IllegalArgumentException("Quantidade deve ser positiva");
        String arquivoSaida = args[1];
        long seed = args.length >= 3 ? Long.parseLong(args[2]) : 42L;

        Random random = new Random(seed);
        Set<String> telefonesUsados = new HashSet<>();

        try (BufferedWriter bw = Files.newBufferedWriter(Path.of(arquivoSaida), StandardCharsets.UTF_8)) {
            // Primeiro no arquivo e maior nas duas chaves: termina na cauda nos dois modos.
            // Os demais nomes continuam aleatorios e podem se repetir.
            bw.write("ZZZ Contato Final;99999999999");
            bw.newLine();
            telefonesUsados.add("99999999999");
            for (int i = 1; i < quantidade; i++) {
                String nome = nomeAleatorio(random);
                String telefone = telefoneUnico(random, telefonesUsados);
                bw.write(nome + ";" + telefone);
                bw.newLine();
            }
        }
        System.out.println(quantidade + " contatos gerados em " + arquivoSaida);
    }

    private static String nomeAleatorio(Random random) {
        String primeiro = PRIMEIROS_NOMES[random.nextInt(PRIMEIROS_NOMES.length)];
        String sobrenome1 = SOBRENOMES[random.nextInt(SOBRENOMES.length)];
        String sobrenome2 = SOBRENOMES[random.nextInt(SOBRENOMES.length)];
        return primeiro + " " + sobrenome1 + " " + sobrenome2;
    }

    private static String telefoneUnico(Random random, Set<String> usados) {
        String telefone;
        do {
            int ddd = 27 + random.nextInt(3);
            int numero = random.nextInt(100_000_000);
            telefone = String.format("%d9%08d", ddd, numero);
        } while (!usados.add(telefone));
        return telefone;
    }
}

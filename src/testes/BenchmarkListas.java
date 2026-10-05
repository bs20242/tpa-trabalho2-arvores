package testes;

import app.CadastroContatos;
import dominio.Contato;
import java.nio.file.Path;
import java.io.IOException;

// Uma execucao por processo. Mesma carga do menu e mesmo metodo de remocao medido.
public class BenchmarkListas {
    public static void main(String[] args) throws IOException {
        if (args.length != 2 || !(args[1].equals("true") || args[1].equals("false"))) {
            throw new IllegalArgumentException("Uso: BenchmarkListas <arquivo> <true|false>");
        }
        boolean ordenada = Boolean.parseBoolean(args[1]);
        CadastroContatos cadastro = new CadastroContatos(ordenada);
        long inicio = System.nanoTime();
        int n = cadastro.carregar(Path.of(args[0]));
        double montagem = (System.nanoTime() - inicio) / 1_000_000.0;

        // A selecao e a verificacao do alvo ficam fora das medicoes.
        Contato alvo = cadastro.ultimoPorTelefone();
        if (alvo == null || cadastro.ultimoPorNome() != alvo
                || cadastro.porNome().pesquisar(new Contato(alvo.getNome(), "")) != alvo) {
            throw new IllegalArgumentException("Use o gerador: alvo unico deve ser a cauda das duas listas");
        }
        Contato chaveTelefone = new Contato("", alvo.getTelefone());
        Contato chaveNome = new Contato(alvo.getNome(), "");

        inicio = System.nanoTime();
        Contato porTelefone = cadastro.porTelefone().pesquisar(chaveTelefone);
        double buscaTelefone = (System.nanoTime() - inicio) / 1_000_000.0;
        inicio = System.nanoTime();
        Contato porNome = cadastro.porNome().pesquisar(chaveNome);
        double buscaNome = (System.nanoTime() - inicio) / 1_000_000.0;

        // Mede so o metodo da biblioteca; a sincronizacao da outra lista vem depois.
        inicio = System.nanoTime();
        boolean removido = cadastro.porTelefone().remover(chaveTelefone);
        double remocao = (System.nanoTime() - inicio) / 1_000_000.0;
        boolean removidoNome = cadastro.removerPorNome(alvo);
        if (porTelefone != alvo || porNome != alvo || !removido || !removidoNome
                || cadastro.porNome().quantidadeNos() != n - 1
                || cadastro.porTelefone().quantidadeNos() != n - 1) {
            throw new IllegalStateException("Resultado incorreto no benchmark");
        }
        System.out.println((ordenada ? "ordenada" : "nao-ordenada") + "," + n + ","
                + montagem + "," + buscaTelefone + "," + buscaNome + "," + remocao);
    }
}

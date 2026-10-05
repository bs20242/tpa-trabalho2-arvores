package app;

import colecao.IColecao;
import colecao.ListaEncadeada;
import dominio.Contato;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

// Regras usadas tanto pelo menu quanto pelo benchmark, sem imprimir mensagens.
public class CadastroContatos {
    private final IColecao<Contato> porNome;
    private final IColecao<Contato> porTelefone;
    private final Set<String> telefones;

    public CadastroContatos(boolean ordenada) {
        porNome = new ListaEncadeada<>(new Contato.ComparadorPorNome(), ordenada);
        porTelefone = new ListaEncadeada<>(new Contato.ComparadorPorTelefone(), ordenada);
        telefones = new HashSet<>();
    }

    public IColecao<Contato> porNome() { return porNome; }
    public IColecao<Contato> porTelefone() { return porTelefone; }

    // As listas foram criadas neste construtor; o cast acessa apenas metodos auxiliares.
    // IColecao permanece igual a interface fornecida na disciplina.
    public boolean removerPorNome(Contato contato) {
        boolean r = ((ListaEncadeada<Contato>) porNome).removerReferencia(contato);
        if (r && contato != null && contato.getTelefone() != null) {
            telefones.remove(contato.getTelefone());
        }
        return r;
    }

    public Contato ultimoPorNome() {
        return ((ListaEncadeada<Contato>) porNome).obterUltimo();
    }

    public Contato ultimoPorTelefone() {
        return ((ListaEncadeada<Contato>) porTelefone).obterUltimo();
    }

    public boolean adicionar(Contato contato) {
        if (contato == null || contato.getNome() == null || contato.getTelefone() == null
                || contato.getNome().isEmpty() || contato.getTelefone().isEmpty()
                || !telefones.add(contato.getTelefone())) {
            return false;
        }
        try {
            porNome.adicionar(contato);
            porTelefone.adicionar(contato);
        } catch (RuntimeException e) {
            telefones.remove(contato.getTelefone());
            throw e;
        }
        return true;
    }

    public int carregar(Path arquivo) throws IOException {
        int total = 0;
        try (BufferedReader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                String[] campos = linha.split(";", -1);
                if (campos.length == 2
                        && adicionar(new Contato(campos[0].trim(), campos[1].trim()))) {
                    total++;
                }
            }
        }
        return total;
    }

    // O contato foi localizado antes desta chamada. Remove a mesma pessoa das duas listas.
    public boolean remover(Contato contato) {
        if (!removerPorNome(contato)) {
            return false;
        }
        if (!porTelefone.remover(contato)) {
            throw new IllegalStateException("Listas inconsistentes");
        }
        telefones.remove(contato.getTelefone());
        return true;
    }
}

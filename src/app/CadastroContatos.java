package app;

import colecao.IColecao;
import colecao.ListaEncadeada;
import arvorebinaria.ArvoreBinaria;
import dominio.Contato;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class CadastroContatos {
    private final IColecao<Contato> porNome;
    private final IColecao<Contato> porTelefone;
    private final Set<String> telefones;
    private final int tipoEstrutura; 

    // SOLUÇÃO DA QUEBRA DE COMPATIBILIDADE: 
    public CadastroContatos(boolean ordenada) {
        this(ordenada ? 2 : 1);
    }

    // SOLUÇÃO DO ERRO DO FINAL:
    // A validação é feita de forma limpa na atribuição.
    public CadastroContatos(int tipoEscolhido) {
        this.tipoEstrutura = (tipoEscolhido >= 1 && tipoEscolhido <= 3) ? tipoEscolhido : 1;
        this.telefones = new HashSet<>();

        switch (this.tipoEstrutura) {
            case 1:
                porNome = new ListaEncadeada<>(new Contato.ComparadorPorNome(), false);
                porTelefone = new ListaEncadeada<>(new Contato.ComparadorPorTelefone(), false);
                break;
            case 2:
                porNome = new ListaEncadeada<>(new Contato.ComparadorPorNome(), true);
                porTelefone = new ListaEncadeada<>(new Contato.ComparadorPorTelefone(), true);
                break;
            case 3:
                porNome = new ArvoreBinaria<>(new Contato.ComparadorPorNome());
                porTelefone = new ArvoreBinaria<>(new Contato.ComparadorPorTelefone());
                break;
            default:
                porNome = new ListaEncadeada<>(new Contato.ComparadorPorNome(), false);
                porTelefone = new ListaEncadeada<>(new Contato.ComparadorPorTelefone(), false);
                break;
        }
    }

    public IColecao<Contato> porNome() { return porNome; }
    public IColecao<Contato> porTelefone() { return porTelefone; }

    public boolean removerPorNome(Contato contato) {
        boolean r = false;
        
        // SOLUÇÃO DA REMOÇÃO INCOMPLETA DA ÁRVORE:
        if (tipoEstrutura == 1 || tipoEstrutura == 2) {
            r = ((ListaEncadeada<Contato>) porNome).removerReferencia(contato);
        } else if (tipoEstrutura == 3) {
            r = ((ArvoreBinaria<Contato>) porNome).removerReferencia(contato); 
        }

        if (r && contato != null && contato.getTelefone() != null) {
            telefones.remove(contato.getTelefone());
        }
        return r;
    }

    public Contato ultimoPorNome() {
        if (tipoEstrutura == 1 || tipoEstrutura == 2) {
            return ((ListaEncadeada<Contato>) porNome).obterUltimo();
        }
        return null;
    }

    public Contato ultimoPorTelefone() {
        if (tipoEstrutura == 1 || tipoEstrutura == 2) {
            return ((ListaEncadeada<Contato>) porTelefone).obterUltimo();
        }
        return null;
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

    public boolean remover(Contato contato) {
        if (!removerPorNome(contato)) {
            return false;
        }
        if (!porTelefone.remover(contato)) {
            throw new IllegalStateException("Listas inconsistentes");
        }
        return true;
    }
}

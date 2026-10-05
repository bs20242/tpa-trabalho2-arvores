package app;

import colecao.IColecao;
import colecao.ListaEncadeada;
import arvorebinaria.ArvoreBinaria; // <- Adicione o import da sua Arvore
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
    private final int tipoEstrutura; // Guarda o tipo para sabermos como fazer casts específicos

    // Construtor alterado para receber a escolha (1: Lista Não-Ordenada, 2: Lista Ordenada, 3: Árvore)
    public CadastroContatos(int tipoEstrutura) {
        this.tipoEstrutura = tipoEstrutura;
        this.telefones = new HashSet<>();

        switch (tipoEstrutura) {
            case 1: // Lista Não-Ordenada
                porNome = new ListaEncadeada<>(new Contato.ComparadorPorNome(), false);
                porTelefone = new ListaEncadeada<>(new Contato.ComparadorPorTelefone(), false);
                break;
            case 2: // Lista Ordenada
                porNome = new ListaEncadeada<>(new Contato.ComparadorPorNome(), true);
                porTelefone = new ListaEncadeada<>(new Contato.ComparadorPorTelefone(), true);
                break;
            case 3: // Árvore Binária (Nova inclusão do Trabalho 2)
                porNome = new ArvoreBinaria<>(new Contato.ComparadorPorNome());
                porTelefone = new ArvoreBinaria<>(new Contato.ComparadorPorTelefone());
                break;
            default: // Fallback padrão
                porNome = new ListaEncadeada<>(new Contato.ComparadorPorNome(), false);
                porTelefone = new ListaEncadeada<>(new Contato.ComparadorPorTelefone(), false);
                this.tipoEstrutura = 1;
                break;
        }
    }

    public IColecao<Contato> porNome() { return porNome; }
    public IColecao<Contato> porTelefone() { return porTelefone; }

    public boolean removerPorNome(Contato contato) {
        boolean r = false;
        
        // Verifica que tipo de estrutura está rodando por trás do IColecao para chamar o método específico (se houver)
        if (tipoEstrutura == 1 || tipoEstrutura == 2) {
            r = ((ListaEncadeada<Contato>) porNome).removerReferencia(contato);
        } else if (tipoEstrutura == 3) {
            // Na árvore, usamos o remover padrão da interface
            r = porNome.remover(contato); 
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
        // Para a árvore, obter o "último" não faz o mesmo sentido prático da lista para fins de pior caso no benchmark.
        // O benchmark da etapa 5 focará na carga e na contagem de nós, ou buscará elementos específicos.
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
        // A remoção do HashSet de telefones já foi garantida no removerPorNome (se r for true)
        // telefones.remove(contato.getTelefone()); -> removi para não tentar remover duas vezes.
        return true;
    }
}

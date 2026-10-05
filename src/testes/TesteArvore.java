package testes;

import arvorebinaria.ArvoreBinaria;
import colecao.IColecao;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

/** Verificacoes da biblioteca, sem dependencia do aplicativo de contatos. */
public class TesteArvore {
    private static int verificacoes;

    private static void verificar(boolean condicao, String caso) {
        verificacoes++;
        if (!condicao) throw new AssertionError(caso);
    }

    private record Pessoa(String nome, int codigo) {}

    public static void main(String[] args) {
        ArvoreBinaria<Integer> arvore = new ArvoreBinaria<>(Integer::compare);
        verificar(arvore.altura() == -1 && arvore.quantidadeNos() == 0, "Arvore vazia");
        verificar(arvore.toString().equals("[]") && arvore.caminharEmNivel().equals("[]"), "Percursos vazios");
        verificar(!arvore.adicionar(null) && arvore.pesquisar(null) == null && !arvore.remover(null), "Valores nulos");
        for (int valor : new int[]{4, 2, 6, 1, 3, 5, 7}) arvore.adicionar(valor);
        verificar(arvore.altura() == 2 && arvore.quantidadeNos() == 7, "Arvore perfeita");
        verificar(arvore.caminharEmOrdem().equals("[1, 2, 3, 4, 5, 6, 7]"), "Em ordem");
        verificar(arvore.caminharEmNivel().equals("[4\n2, 6\n1, 3, 5, 7]"), "Um nivel por linha");
        for (int valor : new int[]{1, 2, 4, 7, 6, 3, 5}) {
            verificar(arvore.remover(valor), "Remocao de folha, um filho ou dois filhos");
            verificar(arvore.pesquisar(valor) == null, "Chave removida");
        }
        verificar(arvore.altura() == -1 && !arvore.remover(99), "Ultima raiz e chave ausente");

        // O sucessor da raiz tem um filho direito: esse filho precisa ser preservado.
        for (int valor : new int[]{10, 5, 20, 15, 17, 30}) arvore.adicionar(valor);
        verificar(arvore.remover(10) && arvore.toString().equals("[5, 15, 17, 20, 30]"), "Sucessor com filho");

        Pessoa ana1 = new Pessoa("Ana", 1), ana2 = new Pessoa("Ana", 2);
        ArvoreBinaria<Pessoa> porNome = new ArvoreBinaria<>(Comparator.comparing(Pessoa::nome));
        IColecao<Pessoa> porCodigo = new ArvoreBinaria<>(Comparator.comparingInt(Pessoa::codigo));
        for (Pessoa pessoa : List.of(ana1, ana2, new Pessoa("Bia", 3))) {
            porNome.adicionar(pessoa);
            porCodigo.adicionar(pessoa);
        }
        verificar(porCodigo.pesquisar(new Pessoa("", 2)) == ana2, "Mesmo tipo com outro comparador");
        verificar(porNome.removerReferencia(ana2) && porNome.pesquisar(ana1) == ana1, "Nomes iguais e identidade");
        verificar(!porNome.removerReferencia(new Pessoa("Ana", 1)), "Referencia diferente nao remove");
        ArvoreBinaria<String> invertida = new ArvoreBinaria<>(Comparator.<String>reverseOrder());
        for (String valor : List.of("a", "c", "b")) invertida.adicionar(valor);
        verificar(invertida.toString().equals("[c, b, a]") && invertida.pesquisar("b").equals("b"), "Tipo String e comparador reverso");

        arvore = new ArvoreBinaria<>(Integer::compare);
        TreeMap<Integer, Integer> modelo = new TreeMap<>();
        Random aleatorio = new Random(20261005);
        for (int i = 0; i < 10000; i++) {
            int valor = aleatorio.nextInt(100);
            if (aleatorio.nextBoolean()) {
                arvore.adicionar(valor);
                modelo.merge(valor, 1, Integer::sum);
            } else {
                boolean existe = modelo.containsKey(valor);
                verificar(arvore.remover(valor) == existe, "Remocao comparada com modelo");
                if (existe) {
                    if (modelo.get(valor) == 1) modelo.remove(valor);
                    else modelo.put(valor, modelo.get(valor) - 1);
                }
            }
            verificar((arvore.pesquisar(valor) != null) == modelo.containsKey(valor), "Pesquisa comparada com modelo");
            verificar(arvore.quantidadeNos() == modelo.values().stream().mapToInt(Integer::intValue).sum(), "Contagem comparada com modelo");
            if (i % 100 == 0) {
                List<Integer> esperado = new ArrayList<>();
                modelo.forEach((chave, quantidade) -> {
                    for (int j = 0; j < quantidade; j++) esperado.add(chave);
                });
                verificar(arvore.toString().equals(esperado.toString()), "Ordem e repeticoes comparadas com modelo");
            }
        }

        arvore = new ArvoreBinaria<>(Integer::compare);
        for (int i = 0; i < 10000; i++) arvore.adicionar(i);
        verificar(arvore.altura() == 9999 && arvore.quantidadeNos() == 10000, "Arvore degenerada profunda");
        verificar(arvore.folhaMaisDistante() == 9999 && arvore.profundidade(9999) == 9999, "Alvo mais profundo");
        verificar(arvore.toString().endsWith("9999]") && arvore.caminharEmNivel().endsWith("9999]"), "Percursos sem estouro de pilha");
        verificar(arvore.remover(9999) && arvore.quantidadeNos() == 9999, "Remocao na cadeia profunda");
        System.out.println("OK: " + verificacoes + " verificacoes da biblioteca.");
    }
}

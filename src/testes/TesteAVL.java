package testes;

import arvorebinaria.ArvoreAVL;
import arvorebinaria.ArvoreBinaria;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

/** Verificacoes da arvore AVL: rotacoes, invariante de balanceamento e comparacao com um modelo. */
public class TesteAVL {
    private static int verificacoes;

    private static void verificar(boolean condicao, String caso) {
        verificacoes++;
        if (!condicao) throw new AssertionError(caso);
    }

    private record Pessoa(String nome, int codigo) {}

    private static ArvoreAVL<Integer> montar(int... valores) {
        ArvoreAVL<Integer> arvore = new ArvoreAVL<>(Integer::compare);
        for (int valor : valores) arvore.adicionar(valor);
        return arvore;
    }

    public static void main(String[] args) {
        ArvoreAVL<Integer> arvore = new ArvoreAVL<>(Integer::compare);
        verificar(arvore instanceof ArvoreBinaria<Integer>, "AVL herda de ArvoreBinaria");
        verificar(arvore.altura() == -1 && arvore.quantidadeNos() == 0 && arvore.toString().equals("[]"), "AVL vazia");
        verificar(!arvore.adicionar(null) && !arvore.remover(null), "Valores nulos");

        // Os quatro casos de desbalanceamento terminam com 2 na raiz.
        verificar(montar(3, 2, 1).caminharEmNivel().equals("[2\n1, 3]"), "Rotacao simples a direita (LL)");
        verificar(montar(1, 2, 3).caminharEmNivel().equals("[2\n1, 3]"), "Rotacao simples a esquerda (RR)");
        verificar(montar(3, 1, 2).caminharEmNivel().equals("[2\n1, 3]"), "Rotacao dupla esquerda-direita (LR)");
        verificar(montar(1, 3, 2).caminharEmNivel().equals("[2\n1, 3]"), "Rotacao dupla direita-esquerda (RL)");

        // Remocao que desbalanceia a raiz.
        arvore = montar(2, 1, 3, 4);
        verificar(arvore.remover(1) && arvore.caminharEmNivel().equals("[3\n2, 4]") && arvore.estaBalanceada(), "Rebalanceia apos remover");
        arvore = montar(5, 3, 8, 2, 4, 7, 9, 1);
        verificar(arvore.remover(5) && arvore.toString().equals("[1, 2, 3, 4, 7, 8, 9]") && arvore.estaBalanceada(), "Remove raiz com dois filhos");

        // Insercao crescente: a binaria comum degenera, a AVL fica perfeita para n = 2^k - 1.
        arvore = new ArvoreAVL<>(Integer::compare);
        for (int i = 1; i <= 16383; i++) arvore.adicionar(i);
        verificar(arvore.altura() == 13 && arvore.estaBalanceada() && arvore.quantidadeNos() == 16383, "Entrada crescente balanceada");
        verificar(arvore.profundidade(arvore.folhaMaisDistante()) == 13, "Folha mais distante na AVL");

        // Mesmo tipo, comparadores diferentes e nomes iguais com rotacoes.
        ArvoreAVL<Pessoa> porNome = new ArvoreAVL<>(Comparator.comparing(Pessoa::nome));
        List<Pessoa> anas = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Pessoa ana = new Pessoa("Ana", i);
            anas.add(ana);
            porNome.adicionar(ana);
        }
        verificar(porNome.altura() <= 6 && porNome.estaBalanceada(), "Chaves iguais tambem sao balanceadas");
        for (int i = 49; i >= 0; i -= 3) {
            verificar(porNome.removerReferencia(anas.get(i)), "Remove a instancia certa entre nomes iguais");
            verificar(porNome.estaBalanceada() && !porNome.toString().contains("codigo=" + i + "]"), "Instancia removida");
        }
        verificar(!porNome.removerReferencia(new Pessoa("Ana", 1)), "Referencia diferente nao remove");

        arvore = new ArvoreAVL<>(Integer::compare);
        TreeMap<Integer, Integer> modelo = new TreeMap<>();
        Random aleatorio = new Random(20261026);
        for (int i = 0; i < 20000; i++) {
            int valor = aleatorio.nextInt(200);
            if (aleatorio.nextInt(3) > 0) {
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
            if (i % 100 == 0) {
                int total = modelo.values().stream().mapToInt(Integer::intValue).sum();
                List<Integer> esperado = new ArrayList<>();
                modelo.forEach((chave, quantidade) -> {
                    for (int j = 0; j < quantidade; j++) esperado.add(chave);
                });
                verificar(arvore.quantidadeNos() == total && arvore.toString().equals(esperado.toString()), "Ordem e contagem");
                verificar(arvore.estaBalanceada(), "Invariante AVL apos operacoes aleatorias");
                verificar(arvore.altura() <= 1.4405 * (Math.log(total + 2) / Math.log(2)), "Altura dentro do limite AVL");
            }
        }
        System.out.println("OK: " + verificacoes + " verificacoes da arvore AVL.");
    }
}

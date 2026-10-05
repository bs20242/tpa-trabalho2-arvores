package testes;

import colecao.IColecao;
import colecao.ListaEncadeada;
import java.util.Comparator;

public class TesteLista {
    public static void main(String[] args) {
        boolean assertionsAtivas = false;
        assert assertionsAtivas = true;
        if (!assertionsAtivas) {
            throw new IllegalStateException("Execute com -ea para habilitar os testes");
        }
        testarListaNaoOrdenada();
        testarListaOrdenada();
        testarToStringEQuantidade();
        System.out.println("TODOS_OS_TESTES_PASSARAM");
    }

    private static void testarListaNaoOrdenada() {
        Comparator<Integer> comp = Integer::compareTo;
        IColecao<Integer> lista = new ListaEncadeada<>(comp, false);

        assert lista.quantidadeNos() == 0;
        assert lista.adicionar(10);
        assert lista.adicionar(20);
        assert lista.adicionar(5);
        assert lista.quantidadeNos() == 3;

        assert lista.pesquisar(20) == 20;
        assert lista.pesquisar(100) == null;

        assert lista.remover(20);
        assert lista.quantidadeNos() == 2;
        assert lista.pesquisar(20) == null;

        assert lista.remover(5);
        assert lista.remover(10);
        assert lista.quantidadeNos() == 0;
        assert !lista.remover(10);
    }

    private static void testarListaOrdenada() {
        Comparator<Integer> comp = Integer::compareTo;
        IColecao<Integer> lista = new ListaEncadeada<>(comp, true);

        lista.adicionar(30);
        lista.adicionar(10);
        lista.adicionar(20);
        lista.adicionar(5);
        lista.adicionar(40);

        assert lista.quantidadeNos() == 5;
        assert lista.toString().equals("[5, 10, 20, 30, 40]");

        assert lista.pesquisar(5) == 5;
        assert lista.pesquisar(20) == 20;
        assert lista.pesquisar(40) == 40;
        assert lista.pesquisar(15) == null;

        assert lista.remover(5);
        assert lista.toString().equals("[10, 20, 30, 40]");

        assert lista.remover(30);
        assert lista.toString().equals("[10, 20, 40]");

        assert lista.remover(40);
        assert lista.toString().equals("[10, 20]");

        assert !lista.remover(99);
        assert lista.quantidadeNos() == 2;
    }

    private static void testarToStringEQuantidade() {
        Comparator<String> comp = String::compareTo;
        IColecao<String> lista = new ListaEncadeada<>(comp, false);

        assert lista.toString().equals("[]");
        lista.adicionar("alpha");
        assert lista.toString().equals("[alpha]");
        lista.adicionar("beta");
        assert lista.toString().equals("[beta, alpha]");
    }
}

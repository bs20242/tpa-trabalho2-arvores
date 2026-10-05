package arvorebinaria;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.Objects;

public class ArvoreBinaria<T> extends ArvoreBinariaBase<T> {
    private static class No<T> {
        T valor;
        No<T> esq, dir;
        No(T valor) { this.valor = valor; }
    }
    private No<T> raiz;
    public ArvoreBinaria(Comparator<T> comparador) {
        super(Objects.requireNonNull(comparador));
    }

    @Override
    public boolean adicionar(T valor) {
        if (valor == null) return false;
        No<T> novo = new No<>(valor);
        if (raiz == null) { raiz = novo; return true; }
        No<T> atual = raiz;
        while (true) {
            if (comparador.compare(valor, atual.valor) < 0) {
                if (atual.esq == null) { atual.esq = novo; return true; }
                atual = atual.esq;
            } else {
                if (atual.dir == null) { atual.dir = novo; return true; }
                atual = atual.dir;
            }
        }
    }

    @Override
    public T pesquisar(T valor) {
        if (valor == null) return null;
        No<T> atual = raiz;
        while (atual != null) {
            int c = comparador.compare(valor, atual.valor);
            if (c == 0) return atual.valor;
            atual = c < 0 ? atual.esq : atual.dir;
        }
        return null;
    }

    @Override
    public boolean remover(T valor) {
        return removerInterno(valor, false);
    }
    public boolean removerReferencia(T valor) {
        return removerInterno(valor, true);
    }
    private boolean removerInterno(T valor, boolean referencia) {
        if (valor == null) return false;
        No<T> pai = null, atual = raiz;
        while (atual != null) {
            int c = comparador.compare(valor, atual.valor);
            if (c == 0 && (!referencia || atual.valor == valor)) break;
            pai = atual;
            atual = c < 0 ? atual.esq : atual.dir;
        }
        if (atual == null) return false;
        if (atual.esq != null && atual.dir != null) {
            No<T> paiSucessor = atual, sucessor = atual.dir;
            while (sucessor.esq != null) {
                paiSucessor = sucessor;
                sucessor = sucessor.esq;
            }
            atual.valor = sucessor.valor;
            if (paiSucessor == atual) paiSucessor.dir = sucessor.dir;
            else paiSucessor.esq = sucessor.dir;
        } else {
            No<T> filho = atual.esq != null ? atual.esq : atual.dir;
            if (pai == null) raiz = filho;
            else if (pai.esq == atual) pai.esq = filho;
            else pai.dir = filho;
        }
        return true;
    }

    @Override
    public int quantidadeNos() {
        if (raiz == null) return 0;
        Deque<No<T>> pilha = new ArrayDeque<>();
        pilha.push(raiz);
        int total = 0;
        while (!pilha.isEmpty()) {
            No<T> atual = pilha.pop();
            total++;
            if (atual.dir != null) pilha.push(atual.dir);
            if (atual.esq != null) pilha.push(atual.esq);
        }
        return total;
    }

    @Override
    public int altura() {
        if (raiz == null) return -1;
        Deque<No<T>> fila = new ArrayDeque<>();
        fila.add(raiz);
        int altura = -1;
        while (!fila.isEmpty()) {
            int largura = fila.size();
            altura++;
            for (int i = 0; i < largura; i++) {
                No<T> atual = fila.remove();
                if (atual.esq != null) fila.add(atual.esq);
                if (atual.dir != null) fila.add(atual.dir);
            }
        }
        return altura;
    }
    @Override
    public String caminharEmOrdem() {
        StringBuilder s = new StringBuilder("[");
        Deque<No<T>> pilha = new ArrayDeque<>();
        No<T> atual = raiz;
        boolean primeiro = true;
        while (atual != null || !pilha.isEmpty()) {
            while (atual != null) { pilha.push(atual); atual = atual.esq; }
            atual = pilha.pop();
            if (!primeiro) s.append(", ");
            s.append(atual.valor);
            primeiro = false;
            atual = atual.dir;
        }
        return s.append("]").toString();
    }
    @Override
    public String caminharEmNivel() {
        StringBuilder s = new StringBuilder("[");
        Deque<No<T>> fila = new ArrayDeque<>();
        if (raiz != null) fila.add(raiz);
        while (!fila.isEmpty()) {
            int largura = fila.size();
            for (int i = 0; i < largura; i++) {
                No<T> atual = fila.remove();
                if (i > 0) s.append(", ");
                s.append(atual.valor);
                if (atual.esq != null) fila.add(atual.esq);
                if (atual.dir != null) fila.add(atual.dir);
            }
            if (!fila.isEmpty()) s.append('\n');
        }
        return s.append("]").toString();
    }
    public T folhaMaisDistante() {
        if (raiz == null) return null;
        Deque<No<T>> fila = new ArrayDeque<>();
        fila.add(raiz);
        No<T> ultimo = raiz;
        while (!fila.isEmpty()) {
            ultimo = fila.remove();
            if (ultimo.esq != null) fila.add(ultimo.esq);
            if (ultimo.dir != null) fila.add(ultimo.dir);
        }
        return ultimo.valor;
    }
    public int profundidade(T valor) {
        No<T> atual = raiz;
        int nivel = 0;
        while (atual != null) {
            int c = comparador.compare(valor, atual.valor);
            if (c == 0) return nivel;
            atual = c < 0 ? atual.esq : atual.dir;
            nivel++;
        }
        return -1;
    }
}

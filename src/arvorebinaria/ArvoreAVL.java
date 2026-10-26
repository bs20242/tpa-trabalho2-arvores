package arvorebinaria;
import java.util.Comparator;

public class ArvoreAVL<T> extends ArvoreBinaria<T> {
    private boolean removido;
    public ArvoreAVL(Comparator<T> comparador) {
        super(comparador);
    }

    @Override
    public boolean adicionar(T valor) {
        if (valor == null) return false;
        raiz = adicionar(raiz, valor);
        return true;
    }
    private No<T> adicionar(No<T> no, T valor) {
        if (no == null) return new No<>(valor);
        if (comparador.compare(valor, no.valor) < 0) no.esq = adicionar(no.esq, valor);
        else no.dir = adicionar(no.dir, valor);
        return balancear(no);
    }

    @Override
    public boolean remover(T valor) {
        return removerAVL(valor, false);
    }
    @Override
    public boolean removerReferencia(T valor) {
        return removerAVL(valor, true);
    }
    private boolean removerAVL(T valor, boolean referencia) {
        if (valor == null) return false;
        removido = false;
        raiz = remover(raiz, valor, referencia);
        return removido;
    }
    private No<T> remover(No<T> no, T valor, boolean referencia) {
        if (no == null) return null;
        int c = comparador.compare(valor, no.valor);
        if (c == 0 && (!referencia || no.valor == valor)) {
            removido = true;
            if (no.esq == null) return no.dir;
            if (no.dir == null) return no.esq;
            No<T> sucessor = no.dir;
            while (sucessor.esq != null) sucessor = sucessor.esq;
            no.valor = sucessor.valor;
            no.dir = removerMenor(no.dir);
        } else if (c == 0) {
            // Rotacoes podem levar chaves iguais para a esquerda: procura a instancia nos dois lados.
            no.esq = remover(no.esq, valor, true);
            if (!removido) no.dir = remover(no.dir, valor, true);
        } else if (c < 0) {
            no.esq = remover(no.esq, valor, referencia);
        } else {
            no.dir = remover(no.dir, valor, referencia);
        }
        return balancear(no);
    }
    private No<T> removerMenor(No<T> no) {
        if (no.esq == null) return no.dir;
        no.esq = removerMenor(no.esq);
        return balancear(no);
    }

    private int alturaNo(No<T> no) {
        return no == null ? -1 : no.altura;
    }
    private void atualizarAltura(No<T> no) {
        no.altura = 1 + Math.max(alturaNo(no.esq), alturaNo(no.dir));
    }
    private int fatorBalanceamento(No<T> no) {
        return alturaNo(no.esq) - alturaNo(no.dir);
    }
    private No<T> balancear(No<T> no) {
        atualizarAltura(no);
        int fb = fatorBalanceamento(no);
        if (fb > 1) {
            if (fatorBalanceamento(no.esq) < 0) no.esq = rotacionarEsquerda(no.esq);
            return rotacionarDireita(no);
        }
        if (fb < -1) {
            if (fatorBalanceamento(no.dir) > 0) no.dir = rotacionarDireita(no.dir);
            return rotacionarEsquerda(no);
        }
        return no;
    }
    private No<T> rotacionarDireita(No<T> no) {
        No<T> novaRaiz = no.esq;
        no.esq = novaRaiz.dir;
        novaRaiz.dir = no;
        atualizarAltura(no);
        atualizarAltura(novaRaiz);
        return novaRaiz;
    }
    private No<T> rotacionarEsquerda(No<T> no) {
        No<T> novaRaiz = no.dir;
        no.dir = novaRaiz.esq;
        novaRaiz.esq = no;
        atualizarAltura(no);
        atualizarAltura(novaRaiz);
        return novaRaiz;
    }

    @Override
    public int altura() {
        return alturaNo(raiz);
    }
    public boolean estaBalanceada() {
        return verificarNo(raiz) != Integer.MIN_VALUE;
    }
    private int verificarNo(No<T> no) {
        if (no == null) return -1;
        int he = verificarNo(no.esq), hd = verificarNo(no.dir);
        if (he == Integer.MIN_VALUE || hd == Integer.MIN_VALUE || Math.abs(he - hd) > 1
                || no.altura != 1 + Math.max(he, hd)) return Integer.MIN_VALUE;
        return no.altura;
    }
}

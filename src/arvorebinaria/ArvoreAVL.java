package arvorebinaria;

import java.util.Comparator;

/** Arvore de busca que mantem a diferenca de alturas entre -1 e 1. */
public class ArvoreAVL<T> extends ArvoreBinaria<T> {
    private static class NoAVL<T> extends No<T> {
        int altura;

        NoAVL(T valor) {
            super(valor);
        }
    }

    private static class Remocao {
        boolean realizada;
    }

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
        if (no == null) return new NoAVL<>(valor);
        if (comparador.compare(valor, no.valor) < 0) {
            no.esq = adicionar(no.esq, valor);
        } else {
            no.dir = adicionar(no.dir, valor);
        }
        return balancear(no);
    }

    @Override
    public boolean remover(T valor) {
        return remover(valor, false);
    }

    /** Remove uma ocorrencia da mesma instancia, inclusive entre chaves iguais. */
    @Override
    public boolean removerReferencia(T valor) {
        return remover(valor, true);
    }

    private boolean remover(T valor, boolean porReferencia) {
        if (valor == null) return false;
        Remocao resultado = new Remocao();
        raiz = remover(raiz, valor, porReferencia, resultado);
        return resultado.realizada;
    }

    private No<T> remover(No<T> no, T valor, boolean porReferencia, Remocao resultado) {
        if (no == null) return null;
        int comparacao = comparador.compare(valor, no.valor);
        if (comparacao < 0) {
            no.esq = remover(no.esq, valor, porReferencia, resultado);
        } else if (comparacao > 0) {
            no.dir = remover(no.dir, valor, porReferencia, resultado);
        } else if (porReferencia && no.valor != valor) {
            // Rotacoes podem deixar chaves iguais nos dois lados do no.
            no.esq = remover(no.esq, valor, true, resultado);
            if (!resultado.realizada) {
                no.dir = remover(no.dir, valor, true, resultado);
            }
        } else {
            resultado.realizada = true;
            if (no.esq == null) return no.dir;
            if (no.dir == null) return no.esq;
            No<T> sucessor = no.dir;
            while (sucessor.esq != null) sucessor = sucessor.esq;
            no.valor = sucessor.valor;
            no.dir = removerMenor(no.dir);
        }
        return resultado.realizada ? balancear(no) : no;
    }

    private No<T> removerMenor(No<T> no) {
        if (no.esq == null) return no.dir;
        no.esq = removerMenor(no.esq);
        return balancear(no);
    }

    @Override
    public int altura() {
        return altura(raiz);
    }

    private int altura(No<T> no) {
        return no == null ? -1 : ((NoAVL<T>) no).altura;
    }

    private void atualizarAltura(No<T> no) {
        ((NoAVL<T>) no).altura = 1 + Math.max(altura(no.esq), altura(no.dir));
    }

    // Convencao dos slides: altura da direita menos altura da esquerda.
    private int fatorBalanceamento(No<T> no) {
        return altura(no.dir) - altura(no.esq);
    }

    private No<T> balancear(No<T> no) {
        atualizarAltura(no);
        int fator = fatorBalanceamento(no);
        // Filho com fator zero tambem usa rotacao simples na remocao.
        if (fator > 1) {
            if (fatorBalanceamento(no.dir) < 0) return rotacaoDireitaEsquerda(no);
            return rotacaoEsquerda(no);
        }
        if (fator < -1) {
            if (fatorBalanceamento(no.esq) > 0) return rotacaoEsquerdaDireita(no);
            return rotacaoDireita(no);
        }
        return no;
    }

    private No<T> rotacaoEsquerda(No<T> no) {
        No<T> novaRaiz = no.dir;
        no.dir = novaRaiz.esq;
        novaRaiz.esq = no;
        atualizarAltura(no);
        atualizarAltura(novaRaiz);
        return novaRaiz;
    }

    private No<T> rotacaoDireita(No<T> no) {
        No<T> novaRaiz = no.esq;
        no.esq = novaRaiz.dir;
        novaRaiz.dir = no;
        atualizarAltura(no);
        atualizarAltura(novaRaiz);
        return novaRaiz;
    }

    private No<T> rotacaoEsquerdaDireita(No<T> no) {
        no.esq = rotacaoEsquerda(no.esq);
        return rotacaoDireita(no);
    }

    private No<T> rotacaoDireitaEsquerda(No<T> no) {
        no.dir = rotacaoDireita(no.dir);
        return rotacaoEsquerda(no);
    }
}

package colecao;

import java.util.Comparator;

public class ListaEncadeada<T> implements IColecao<T> {
    private No<T> primeiro;
    private final Comparator<T> comparador;
    private final boolean ordenada;

    public ListaEncadeada(Comparator<T> comparador, boolean ordenada) {
        this.primeiro = null;
        this.comparador = comparador;
        this.ordenada = ordenada;
    }

    @Override
    public boolean adicionar(T novoValor) {
        if (novoValor == null) {
            return false;
        }

        No<T> novoNo = new No<>(novoValor);

        if (!ordenada) {
            novoNo.setProximo(primeiro);
            primeiro = novoNo;
            return true;
        }

        if (primeiro == null || comparador.compare(novoValor, primeiro.getValor()) <= 0) {
            novoNo.setProximo(primeiro);
            primeiro = novoNo;
            return true;
        }

        No<T> atual = primeiro;
        while (atual.getProximo() != null && comparador.compare(novoValor, atual.getProximo().getValor()) > 0) {
            atual = atual.getProximo();
        }

        novoNo.setProximo(atual.getProximo());
        atual.setProximo(novoNo);
        return true;
    }

    @Override
    public T pesquisar(T valor) {
        if (valor == null || primeiro == null) {
            return null;
        }

        No<T> atual = primeiro;

        if (ordenada) {
            while (atual != null) {
                int comp = comparador.compare(atual.getValor(), valor);
                if (comp == 0) {
                    return atual.getValor();
                }
                if (comp > 0) {
                    return null;
                }
                atual = atual.getProximo();
            }
        } else {
            while (atual != null) {
                if (comparador.compare(atual.getValor(), valor) == 0) {
                    return atual.getValor();
                }
                atual = atual.getProximo();
            }
        }

        return null;
    }

    @Override
    public boolean remover(T valor) {
        if (valor == null || primeiro == null) {
            return false;
        }

        if (comparador.compare(primeiro.getValor(), valor) == 0) {
            primeiro = primeiro.getProximo();
            return true;
        }

        if (ordenada && comparador.compare(primeiro.getValor(), valor) > 0) {
            return false;
        }

        No<T> anterior = primeiro;
        No<T> atual = primeiro.getProximo();

        while (atual != null) {
            int comp = comparador.compare(atual.getValor(), valor);
            if (comp == 0) {
                anterior.setProximo(atual.getProximo());
                return true;
            }
            if (ordenada && comp > 0) {
                return false;
            }
            anterior = atual;
            atual = atual.getProximo();
        }

        return false;
    }

    @Override
    public int quantidadeNos() {
        int total = 0;
        No<T> atual = primeiro;
        while (atual != null) {
            total++;
            atual = atual.getProximo();
        }
        return total;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        No<T> atual = primeiro;
        while (atual != null) {
            sb.append(atual.getValor());
            if (atual.getProximo() != null) {
                sb.append(", ");
            }
            atual = atual.getProximo();
        }
        sb.append("]");
        return sb.toString();
    }

    // Remove a mesma instancia, mesmo quando o comparador considera outros valores iguais.
    public boolean removerReferencia(T valor) {
        No<T> anterior = null;
        No<T> atual = primeiro;
        while (atual != null) {
            if (atual.getValor() == valor) {
                if (anterior == null) {
                    primeiro = atual.getProximo();
                } else {
                    anterior.setProximo(atual.getProximo());
                }
                return true;
            }
            anterior = atual;
            atual = atual.getProximo();
        }
        return false;
    }

    public T obterUltimo() {
        if (primeiro == null) {
            return null;
        }
        No<T> atual = primeiro;
        while (atual.getProximo() != null) {
            atual = atual.getProximo();
        }
        return atual.getValor();
    }
}

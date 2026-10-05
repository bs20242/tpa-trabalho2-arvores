package dominio;

import java.util.Comparator;
import java.util.Objects;

public class Contato {
    private String nome;
    private String telefone;

    public Contato(String nome, String telefone) {
        this.nome = nome;
        this.telefone = telefone;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    // Sobrescrita obrigatoria: devolve nome e telefone separados por hifen
    @Override
    public String toString() {
        return nome + " - " + telefone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contato contato = (Contato) o;
        return Objects.equals(telefone, contato.telefone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(telefone);
    }

    // Comparador para ordenar/buscar pela lista de Nomes (case-insensitive)
    public static class ComparadorPorNome implements Comparator<Contato> {
        @Override
        public int compare(Contato c1, Contato c2) {
            if (c1 == null || c2 == null || c1.getNome() == null || c2.getNome() == null) {
                return 0;
            }
            return c1.getNome().compareToIgnoreCase(c2.getNome());
        }
    }

    // Comparador para ordenar/buscar pela lista de Telefones
    public static class ComparadorPorTelefone implements Comparator<Contato> {
        @Override
        public int compare(Contato c1, Contato c2) {
            if (c1 == null || c2 == null || c1.getTelefone() == null || c2.getTelefone() == null) {
                return 0;
            }
            return c1.getTelefone().compareTo(c2.getTelefone());
        }
    }
}

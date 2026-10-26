package testes;

import arvorebinaria.ArvoreAVL;
import dominio.Contato;

public class TesteAVL {
    private static void verificar(boolean condicao, String caso) {
        if (!condicao) throw new AssertionError(caso);
    }

    private static ArvoreAVL<Integer> criar(int... valores) {
        ArvoreAVL<Integer> arvore = new ArvoreAVL<>(Integer::compare);
        for (int valor : valores) arvore.adicionar(valor);
        return arvore;
    }

    private static void testarRotacoes() {
        // Direita, esquerda, esquerda-direita e direita-esquerda.
        int[][] insercoes = {{30, 20, 10}, {10, 20, 30}, {30, 10, 20}, {10, 30, 20}};
        for (int[] ordem : insercoes) {
            ArvoreAVL<Integer> arvore = criar(ordem);
            verificar(arvore.caminharEmNivel().equals("[20\n10, 30]"), "Rotacao na insercao");
            verificar(arvore.altura() == 1, "Altura apos insercao");
        }

        int[][] remocoes = {{30, 20, 40, 10}, {20, 10, 30, 40}, {30, 10, 40, 20}, {20, 10, 40, 30}};
        int[] alvos = {40, 10, 40, 10};
        String[] esperados = {"[20\n10, 30]", "[30\n20, 40]", "[20\n10, 30]", "[30\n20, 40]"};
        for (int i = 0; i < remocoes.length; i++) {
            ArvoreAVL<Integer> arvore = criar(remocoes[i]);
            verificar(arvore.remover(alvos[i]), "Remover antes da rotacao");
            verificar(arvore.caminharEmNivel().equals(esperados[i]), "Rotacao na remocao");
            verificar(arvore.altura() == 1, "Altura apos remocao");
        }

        // Na remocao, o filho pode ter fator de balanceamento zero.
        ArvoreAVL<Integer> arvore = criar(4, 2, 5, 1, 3);
        verificar(arvore.remover(5), "Remover 5");
        verificar(arvore.caminharEmNivel().equals("[2\n1, 4\n3]"), "Rotacao direita com filho de fator zero");
        verificar(arvore.altura() == 2, "Altura apos rotacao direita");
        arvore = criar(2, 1, 4, 3, 5);
        verificar(arvore.remover(1), "Remover 1");
        verificar(arvore.caminharEmNivel().equals("[4\n2, 5\n3]"), "Rotacao esquerda com filho de fator zero");
        verificar(arvore.altura() == 2, "Altura apos rotacao esquerda");
    }

    private static void testarOperacoes() {
        ArvoreAVL<Integer> arvore = criar();
        verificar(arvore.altura() == -1 && arvore.quantidadeNos() == 0, "Arvore vazia");
        verificar(arvore.caminharEmNivel().equals("[]"), "Percurso vazio");
        verificar(!arvore.adicionar(null) && !arvore.remover(null), "Valor nulo");
        verificar(arvore.pesquisar(null) == null && !arvore.remover(99), "Busca e remocao em arvore vazia");
        verificar(arvore.adicionar(20) && arvore.altura() == 0, "Primeiro no");
        verificar(arvore.remover(20) && arvore.altura() == -1, "Remover raiz unica");

        arvore = criar(20, 10, 30, 25);
        verificar(arvore.pesquisar(25) == 25 && arvore.pesquisar(99) == null, "Busca");
        verificar(arvore.caminharEmOrdem().equals("[10, 20, 25, 30]"), "Percurso em ordem");
        verificar(arvore.remover(30), "Remover no com um filho");
        verificar(arvore.caminharEmNivel().equals("[20\n10, 25]"), "Filho preservado");
        verificar(arvore.remover(20), "Remover raiz com dois filhos");
        verificar(arvore.caminharEmNivel().equals("[25\n10]"), "Sucessor na raiz");
        verificar(arvore.remover(10), "Remover folha");
        verificar(arvore.quantidadeNos() == 1 && arvore.altura() == 0, "Contagem e altura finais");

        arvore = criar(7, 7, 7);
        verificar(arvore.remover(7), "Remover uma chave repetida");
        verificar(arvore.caminharEmOrdem().equals("[7, 7]"), "Preservar outras ocorrencias");
        verificar(arvore.quantidadeNos() == 2, "Contagem com repeticoes");
    }

    private static void testarNomesIguais() {
        ArvoreAVL<Contato> porNome = new ArvoreAVL<>(new Contato.ComparadorPorNome());
        ArvoreAVL<Contato> porTelefone = new ArvoreAVL<>(new Contato.ComparadorPorTelefone());
        Contato[] contatos = {
            new Contato("Ana", "1111"),
            new Contato("Ana", "2222"),
            new Contato("Ana", "3333")
        };
        for (Contato contato : contatos) {
            porNome.adicionar(contato);
            porTelefone.adicionar(contato);
        }
        verificar(!porNome.removerReferencia(new Contato("Ana", "1111")), "Instancia diferente");
        verificar(!porNome.removerReferencia(null), "Referencia nula");

        // A rotacao deixou um contato com nome igual de cada lado da raiz.
        int[] ordem = {0, 2, 1};
        for (int i : ordem) {
            Contato encontrado = porTelefone.pesquisar(new Contato("", contatos[i].getTelefone()));
            verificar(encontrado == contatos[i], "Contato localizado por telefone");
            verificar(porTelefone.remover(encontrado), "Excluir por telefone");
            verificar(porNome.removerReferencia(encontrado), "Excluir o mesmo contato por nome");
            verificar(!porNome.removerReferencia(encontrado), "Preservar os outros contatos");
            verificar(porNome.quantidadeNos() == porTelefone.quantidadeNos(), "Contagens dos dois indices");
        }
        verificar(porNome.altura() == -1 && porTelefone.altura() == -1, "Dois indices vazios");
    }

    public static void main(String[] args) {
        testarRotacoes();
        testarOperacoes();
        testarNomesIguais();
        System.out.println("TESTES_DA_AVL_PASSARAM");
    }
}

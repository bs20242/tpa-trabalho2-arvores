package testes;

import app.CadastroContatos;
import colecao.ListaEncadeada;
import dominio.Contato;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Random;

public class TesteCadastro {
    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }

    public static void main(String[] args) throws Exception {
        for (boolean ordenada : new boolean[] {false, true}) {
            testarSequencias(ordenada);
            CadastroContatos cadastro = new CadastroContatos(ordenada);
            Contato ana1 = new Contato("Ana", "111");
            Contato ana2 = new Contato("Ana", "222");
            verificar(cadastro.adicionar(ana1) && cadastro.adicionar(ana2), "Aceitar homonimos");
            verificar(!cadastro.adicionar(new Contato("Outra", "111")), "Rejeitar telefone repetido");
            verificar(cadastro.remover(ana1), "Remover a primeira Ana");
            verificar(cadastro.porNome().pesquisar(new Contato("Ana", "")) == ana2, "Preservar outra Ana");
            verificar(cadastro.porTelefone().pesquisar(new Contato("", "111")) == null, "Excluir telefone");
            verificar(!cadastro.remover(ana1), "Remocao repetida nao afeta outra pessoa");
            verificar(!cadastro.removerPorNome(new Contato("Ana", "222")), "Exigir mesma instancia");
            verificar(cadastro.remover(ana2), "Remover ultimo contato");
            verificar(cadastro.porNome().quantidadeNos() == 0 && cadastro.porTelefone().quantidadeNos() == 0,
                    "Duas listas vazias");
            Contato recadastrada = new Contato("Recadastrada", "111");
            verificar(cadastro.adicionar(recadastrada), "Permitir recadastro de telefone removido");
            verificar(cadastro.remover(recadastrada), "Remover contato recadastrado");
            verificar(cadastro.porNome().quantidadeNos() == 0, "Lista vazia apos remover recadastrada");

            Path arquivo = Files.createTempFile("tpa-teste", ".txt");
            try {
                Files.writeString(arquivo, "Ana;111\nAna;222\nOutra;111\n;333\nBia;\ninvalida\n", StandardCharsets.UTF_8);
                verificar(cadastro.carregar(arquivo) == 2, "Carga deve validar dados e duplicatas");
                verificar(cadastro.carregar(arquivo) == 0, "Repetir carga nao duplica contatos");
                verificar(cadastro.porNome().quantidadeNos() == 2 && cadastro.porTelefone().quantidadeNos() == 2,
                        "Mesmo total nas duas listas");
            } finally {
                Files.delete(arquivo);
            }

            // O alvo artificial do gerador deve exigir percurso completo, inclusive por nome.
            Contador comparador = new Contador();
            ListaEncadeada<Integer> lista = new ListaEncadeada<>(comparador, ordenada);
            lista.adicionar(999);
            for (int i = 0; i < 100; i++) lista.adicionar(i);
            comparador.chamadas = 0;
            verificar(lista.pesquisar(999) == 999, "Encontrar alvo");
            verificar(comparador.chamadas == 101, "Busca deve comparar todos os nos");
            verificar(lista.obterUltimo() == 999, "Alvo deve ser a cauda");
        }
        System.out.println("TESTES_DO_CADASTRO_PASSARAM");
    }

    private static void testarSequencias(boolean ordenada) {
        ListaEncadeada<Integer> lista = new ListaEncadeada<>(Integer::compareTo, ordenada);
        ArrayList<Integer> referencia = new ArrayList<>();
        Random random = new Random(42);
        verificar(!lista.adicionar(null) && lista.pesquisar(null) == null
                && !lista.remover(null), "Valores nulos");
        for (int i = 0; i < 1000; i++) {
            int valor = random.nextInt(50);
            switch (random.nextInt(3)) {
                case 0:
                    lista.adicionar(valor);
                    referencia.add(0, valor);
                    if (ordenada) referencia.sort(Integer::compareTo);
                    break;
                case 1:
                    verificar(lista.remover(valor) == referencia.remove(Integer.valueOf(valor)),
                            "Remocao deve concordar com a referencia");
                    break;
                default:
                    Integer resultado = lista.pesquisar(valor);
                    verificar((resultado != null) == referencia.contains(valor), "Resultado da busca");
            }
            verificar(lista.quantidadeNos() == referencia.size(), "Quantidade apos operacao");
            verificar(lista.toString().equals(referencia.toString()), "Conteudo e ordem apos operacao");
        }
    }

    private static class Contador implements Comparator<Integer> {
        int chamadas;
        public int compare(Integer a, Integer b) {
            chamadas++;
            return a.compareTo(b);
        }
    }
}

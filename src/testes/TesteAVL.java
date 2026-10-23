package testes;

import arvorebinaria.ArvoreAVL;
import arvorebinaria.ArvoreBinaria;
import colecao.IColecao;
import dominio.Contato;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TesteAVL {
    // A inspecao dos nos fica nos testes, sem acrescentar metodos publicos a biblioteca.
    private static final Field RAIZ = campo(ArvoreBinaria.class, "raiz");
    private static final Field VALOR = campo(RAIZ.getType(), "valor");
    private static final Field ESQ = campo(RAIZ.getType(), "esq");
    private static final Field DIR = campo(RAIZ.getType(), "dir");

    private static Field campo(Class<?> classe, String nome) {
        try {
            Field campo = classe.getDeclaredField(nome);
            campo.setAccessible(true);
            return campo;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static Object ler(Field campo, Object objeto) {
        try {
            return campo.get(objeto);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    private static void verificar(boolean condicao, String caso) {
        if (!condicao) throw new AssertionError(caso);
    }

    @SuppressWarnings("unchecked")
    private static <T> int inspecionar(Object no, List<T> valores, Map<Object, Boolean> visitados) {
        if (no == null) return -1;
        verificar(visitados.put(no, true) == null, "Ciclo ou no compartilhado");
        int esquerda = inspecionar(ler(ESQ, no), valores, visitados);
        valores.add((T) ler(VALOR, no));
        int direita = inspecionar(ler(DIR, no), valores, visitados);
        int alturaReal = 1 + Math.max(esquerda, direita);
        verificar(Math.abs(direita - esquerda) <= 1, "No desbalanceado");
        verificar((int) ler(campo(no.getClass(), "altura"), no) == alturaReal, "Altura armazenada incorreta");
        return alturaReal;
    }

    private static <T> void auditar(ArvoreAVL<T> arvore, Comparator<T> comparador, List<T> modelo) {
        List<T> valores = new ArrayList<>();
        int alturaReal = inspecionar(ler(RAIZ, arvore), valores, new IdentityHashMap<>());
        List<T> esperado = new ArrayList<>(modelo);
        esperado.sort(comparador);
        verificar(valores.equals(esperado), "Conteudo ou ordem diferente do modelo");
        verificar(arvore.altura() == alturaReal, "Altura publica incorreta");
        verificar(arvore.quantidadeNos() == modelo.size(), "Contagem incorreta");
        verificar(arvore.caminharEmOrdem().equals(esperado.toString()), "Percurso em ordem incorreto");
        verificar(arvore.toString().equals(arvore.caminharEmOrdem()), "toString herdado");
    }

    private static ArvoreAVL<Integer> criar(int... valores) {
        ArvoreAVL<Integer> arvore = new ArvoreAVL<>(Integer::compare);
        for (int valor : valores) verificar(arvore.adicionar(valor), "Insercao");
        return arvore;
    }

    private static void testarContratosERotacoes() {
        ArvoreAVL<Integer> vazia = criar();
        IColecao<Integer> colecao = vazia;
        verificar(vazia instanceof ArvoreBinaria<?>, "Heranca exigida");
        verificar(!colecao.adicionar(null) && colecao.pesquisar(null) == null, "Nulos na inclusao e busca");
        verificar(!colecao.remover(null) && !vazia.removerReferencia(null), "Nulos na remocao");
        verificar(!colecao.remover(1) && vazia.altura() == -1, "Arvore vazia");
        verificar(vazia.caminharEmNivel().equals("[]"), "Nivel vazio");
        boolean rejeitouComparador = false;
        try { new ArvoreAVL<Integer>(null); } catch (NullPointerException e) { rejeitouComparador = true; }
        verificar(rejeitouComparador, "Comparador obrigatorio");

        int[][] insercoes = {{30, 20, 10}, {10, 20, 30}, {30, 10, 20}, {10, 30, 20}};
        for (int[] ordem : insercoes) {
            ArvoreAVL<Integer> arvore = criar(ordem);
            verificar(arvore.caminharEmNivel().equals("[20\n10, 30]"), "Rotacao na insercao");
            verificar(arvore.profundidade(10) == 1 && arvore.profundidade(99) == -1, "Profundidade herdada");
            verificar(arvore.folhaMaisDistante() == 30, "Folha mais distante herdada");
            auditar(arvore, Integer::compare, List.of(10, 20, 30));
        }
        int[][] remocoes = {{30, 20, 40, 10}, {20, 10, 30, 40}, {30, 10, 40, 20}, {20, 10, 40, 30}};
        int[] alvos = {40, 10, 40, 10};
        for (int i = 0; i < remocoes.length; i++) {
            ArvoreAVL<Integer> arvore = criar(remocoes[i]);
            List<Integer> modelo = new ArrayList<>();
            for (int valor : remocoes[i]) modelo.add(valor);
            modelo.remove(Integer.valueOf(alvos[i]));
            verificar(arvore.remover(alvos[i]), "Rotacao na remocao");
            auditar(arvore, Integer::compare, modelo);
        }
        ArvoreAVL<Integer> filhoZero = criar(4, 2, 5, 1, 3);
        verificar(filhoZero.remover(5), "Remocao com filho de fator zero");
        verificar(filhoZero.caminharEmNivel().equals("[2\n1, 4\n3]"), "Rotacao direita com fator zero");
        auditar(filhoZero, Integer::compare, List.of(1, 2, 3, 4));
        filhoZero = criar(2, 1, 4, 3, 5);
        verificar(filhoZero.remover(1), "Remocao com filho de fator zero");
        verificar(filhoZero.caminharEmNivel().equals("[4\n2, 5\n3]"), "Rotacao esquerda com fator zero");
        auditar(filhoZero, Integer::compare, List.of(2, 3, 4, 5));

        Comparator<String> reverso = Comparator.reverseOrder();
        ArvoreAVL<String> palavras = new ArvoreAVL<>(reverso);
        for (String s : List.of("a", "b", "c", "d", "e")) palavras.adicionar(s);
        verificar(palavras.remover("c") && palavras.pesquisar("d").equals("d"), "Comparador reverso");
        auditar(palavras, reverso, List.of("a", "b", "d", "e"));
        System.out.println("OK: contratos, quatro rotacoes na inclusao/remocao e filho com fator zero.");
    }

    private static void permutar(List<Integer> valores, int posicao, List<List<Integer>> ordens) {
        if (posicao == valores.size()) { ordens.add(new ArrayList<>(valores)); return; }
        for (int i = posicao; i < valores.size(); i++) {
            Collections.swap(valores, posicao, i);
            permutar(valores, posicao + 1, ordens);
            Collections.swap(valores, posicao, i);
        }
    }

    private static void testarPermutacoes() {
        List<List<Integer>> ordens = new ArrayList<>();
        permutar(new ArrayList<>(List.of(1, 2, 3, 4, 5)), 0, ordens);
        for (List<Integer> insercao : ordens) {
            for (List<Integer> remocao : ordens) {
                ArvoreAVL<Integer> arvore = criar();
                List<Integer> modelo = new ArrayList<>();
                for (int valor : insercao) { arvore.adicionar(valor); modelo.add(valor); }
                auditar(arvore, Integer::compare, modelo);
                for (int valor : remocao) {
                    verificar(arvore.remover(valor), "Remocao na permutacao");
                    modelo.remove(Integer.valueOf(valor));
                    auditar(arvore, Integer::compare, modelo);
                }
            }
        }
        System.out.println("OK: " + ordens.size() * ordens.size() + " pares de ordens de insercao/remocao com cinco chaves.");
    }

    private static void testarOperacoesAleatorias() {
        Random random = new Random(20261023);
        ArvoreAVL<Integer> arvore = criar();
        List<Integer> modelo = new ArrayList<>();
        for (int i = 0; i < 6000; i++) {
            int valor = random.nextInt(100);
            if (random.nextBoolean()) {
                verificar(arvore.adicionar(valor), "Insercao aleatoria"); modelo.add(valor);
            } else {
                verificar(arvore.remover(valor) == modelo.remove(Integer.valueOf(valor)), "Remocao aleatoria");
            }
            verificar((arvore.pesquisar(valor) != null) == modelo.contains(valor), "Pesquisa aleatoria");
            auditar(arvore, Integer::compare, modelo);
        }
        System.out.println("OK: 6000 operacoes aleatorias com repeticoes e invariantes apos cada operacao.");
    }

    private static void testarReferenciasEIndices() {
        Comparator<Contato> nome = new Contato.ComparadorPorNome();
        Comparator<Contato> telefone = new Contato.ComparadorPorTelefone();
        ArvoreAVL<Contato> porNome = new ArvoreAVL<>(nome);
        ArvoreAVL<Contato> porTelefone = new ArvoreAVL<>(telefone);
        List<Contato> modelo = new ArrayList<>();
        for (int i = 0; i < 255; i++) {
            Contato contato = new Contato("Ana", String.format("%05d", i));
            modelo.add(contato); porNome.adicionar(contato); porTelefone.adicionar(contato);
        }
        auditar(porNome, nome, modelo);
        verificar(!porNome.removerReferencia(new Contato("Ana", "00000")), "Instancia diferente nao remove");
        List<Contato> ordem = new ArrayList<>(modelo);
        Collections.shuffle(ordem, new Random(41));
        ArvoreBinaria<Contato> referenciaBase = porNome;
        for (Contato contato : ordem) {
            Contato encontrado = porTelefone.pesquisar(new Contato("", contato.getTelefone()));
            verificar(encontrado == contato, "Mesmo contato localizado por telefone");
            verificar(porTelefone.remover(new Contato("", contato.getTelefone())), "Excluir indice por telefone");
            verificar(referenciaBase.removerReferencia(encontrado), "Excluir instancia por nome pela classe base");
            modelo.remove(contato);
            auditar(porNome, nome, modelo); auditar(porTelefone, telefone, modelo);
            verificar(!porNome.removerReferencia(contato), "Nao excluir outro homonimo");
        }
        Contato repetido = new Contato("Bia", "1");
        for (int i = 0; i < 3; i++) porNome.adicionar(repetido);
        verificar(porNome.removerReferencia(repetido), "Mesma instancia repetida");
        auditar(porNome, nome, List.of(repetido, repetido));

        porNome = new ArvoreAVL<>(nome); modelo.clear();
        Random random = new Random(75);
        for (int i = 0; i < 2000; i++) {
            if (modelo.isEmpty() || random.nextBoolean()) {
                Contato contato = new Contato("Nome" + random.nextInt(8), Integer.toString(i));
                porNome.adicionar(contato); modelo.add(contato);
            } else {
                Contato contato = modelo.remove(random.nextInt(modelo.size()));
                verificar(porNome.removerReferencia(contato), "Referencia entre chaves diferentes e iguais");
            }
            auditar(porNome, nome, modelo);
        }
        System.out.println("OK: dois indices com 255 homonimos, referencias repetidas e 2000 operacoes por identidade.");
    }

    private static void testarCargaCrescente() {
        int[] comparacoes = {0};
        Comparator<Integer> contador = (a, b) -> { comparacoes[0]++; return Integer.compare(a, b); };
        ArvoreAVL<Integer> arvore = new ArvoreAVL<>(contador);
        List<Integer> modelo = new ArrayList<>();
        int n = 100000;
        for (int i = 0; i < n; i++) { arvore.adicionar(i); modelo.add(i); }
        verificar(arvore.altura() < 24, "Altura logaritmica na carga crescente");
        verificar(comparacoes[0] < n * 24, "Insercao nao faz varredura completa");
        auditar(arvore, Integer::compare, modelo);
        comparacoes[0] = 0;
        verificar(arvore.pesquisar(n - 1) == n - 1 && comparacoes[0] <= arvore.altura() + 1, "Busca usa um caminho");
        for (int i = n - 1; i >= 0; i--) {
            int alturaAnterior = arvore.altura();
            comparacoes[0] = 0;
            verificar(arvore.remover(i), "Remocao decrescente");
            verificar(comparacoes[0] <= alturaAnterior + 1, "Remocao por chave usa um caminho");
        }
        auditar(arvore, Integer::compare, List.of());
        System.out.println("OK: 100000 insercoes crescentes e 100000 remocoes decrescentes.");
    }

    public static void main(String[] args) {
        testarContratosERotacoes();
        testarPermutacoes();
        testarOperacoesAleatorias();
        testarReferenciasEIndices();
        testarCargaCrescente();
        System.out.println("TESTES_DA_AVL_PASSARAM");
    }
}

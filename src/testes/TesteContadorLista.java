package testes;
import colecao.ListaEncadeada;
import java.util.*;

public class TesteContadorLista {
    public static void main(String[] args) {
        int verificacoes=0;
        for(boolean ordenada:new boolean[]{false,true}) {
            ListaEncadeada<Integer> lista=new ListaEncadeada<>(Integer::compare,ordenada);
            List<Integer> modelo=new ArrayList<>();
            Random random=new Random(42);
            for(int i=0;i<10000;i++) {
                Integer valor=Integer.valueOf(1000+random.nextInt(60));
                int operacao=random.nextInt(4);
                if(operacao==0) { lista.adicionar(valor); modelo.add(valor); }
                else if(operacao==1) {
                    int indice=-1;
                    for(int j=modelo.size()-1;j>=0;j--)if(modelo.get(j).equals(valor)){indice=j;break;}
                    boolean esperado=indice>=0;
                    if(esperado)modelo.remove(indice);
                    if(lista.remover(valor)!=esperado)throw new AssertionError("Remocao por chave");
                } else if(operacao==2 && !modelo.isEmpty()) {
                    Integer instancia=modelo.get(random.nextInt(modelo.size()));
                    int posicao=-1;
                    for(int j=0;j<modelo.size();j++)if(modelo.get(j)==instancia){posicao=j;break;}
                    modelo.remove(posicao);
                    if(!lista.removerReferencia(instancia))throw new AssertionError("Remocao por referencia");
                } else {
                    if(lista.adicionar(null) || lista.remover(null) || lista.removerReferencia(null))throw new AssertionError("Nulo");
                }
                if(lista.quantidadeNos()!=modelo.size())throw new AssertionError("Contador divergente");
                List<Integer> ordem=new ArrayList<>(modelo);
                if(ordenada)ordem.sort(Integer::compare); else Collections.reverse(ordem);
                if(!lista.toString().equals(ordem.toString()))throw new AssertionError("Contador e percurso divergentes");
                verificacoes++;
            }
            for(Integer instancia:new ArrayList<>(modelo)) {
                if(!lista.removerReferencia(instancia))throw new AssertionError("Esvaziamento");
            }
            if(lista.quantidadeNos()!=0 || lista.remover(999))throw new AssertionError("Vazia");
        }
        System.out.println("OK: "+verificacoes+" verificacoes aleatorias do contador nos dois modos.");
    }
}

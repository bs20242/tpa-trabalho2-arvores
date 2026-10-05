package testes;
import app.CadastroContatos;
import arvorebinaria.ArvoreBinaria;
import dominio.Contato;
import java.nio.file.*;
import java.io.*;
import java.util.Locale;

public class BenchmarkArvores {
    private static volatile Object resultado;
    public static void main(String[] args)throws Exception {
        Locale.setDefault(Locale.ROOT);
        Path pasta=Path.of(args.length>0?args[0]:"dados");
        Path saida=Path.of(args.length>1?args[1]:"dados/resultados_arvores.csv");
        // Aquecimento de nome, telefone, leitura e remocao fora dos intervalos medidos.
        for(int repeticao=0;repeticao<10;repeticao++) {
            CadastroContatos c=new CadastroContatos(3);
            c.carregar(pasta.resolve("perfeita_2047.txt"));
            Contato alvo=((ArvoreBinaria<Contato>)c.porTelefone()).folhaMaisDistante();
            for(int i=0;i<100;i++) { resultado=c.porNome().pesquisar(alvo); resultado=c.porTelefone().pesquisar(alvo); }
            c.remover(alvo);
        }
        try(PrintWriter w=new PrintWriter(Files.newBufferedWriter(saida))) {
            w.println("formato,n,repeticao,alturaTelefone,alturaNome,profundidadeTelefone,profundidadeNome,telefoneAlvo,cargaMs,buscaTelefoneMs,buscaNomeMs,remocaoMs");
            for(int n:GeradorDadosArvores.TAMANHOS)for(String formato:new String[]{"perfeita","degenerada"})for(int rep=1;rep<=3;rep++) {
                CadastroContatos c=new CadastroContatos(3);
                long inicio=System.nanoTime();
                int lidos=c.carregar(pasta.resolve(formato+"_"+n+".txt"));
                long carga=System.nanoTime()-inicio;
                ArvoreBinaria<Contato> tel=(ArvoreBinaria<Contato>)c.porTelefone();
                ArvoreBinaria<Contato> nome=(ArvoreBinaria<Contato>)c.porNome();
                Contato alvo=tel.folhaMaisDistante();
                int ht=tel.altura(),hn=nome.altura(),dt=tel.profundidade(alvo),dn=nome.profundidade(alvo);
                int alturaEsperada=formato.equals("perfeita")?31-Integer.numberOfLeadingZeros(n):n-1;
                if(lidos!=n || tel.quantidadeNos()!=n || nome.quantidadeNos()!=n || ht!=alturaEsperada || hn!=ht || dt!=ht || dn!=ht)throw new AssertionError("Carga ou topologia incorreta");
                Contato chaveTel=new Contato("",alvo.getTelefone()),chaveNome=new Contato(alvo.getNome(),"");
                inicio=System.nanoTime(); Contato buscaTel=tel.pesquisar(chaveTel); long bt=System.nanoTime()-inicio;
                inicio=System.nanoTime(); Contato buscaNome=nome.pesquisar(chaveNome); long bn=System.nanoTime()-inicio;
                inicio=System.nanoTime(); boolean removido=tel.remover(chaveTel); long rm=System.nanoTime()-inicio;
                if(buscaTel!=alvo || buscaNome!=alvo || !removido || !c.removerPorNome(alvo) || tel.quantidadeNos()!=n-1 || nome.quantidadeNos()!=n-1 || tel.pesquisar(chaveTel)!=null)throw new AssertionError("Operacao incorreta");
                w.printf("%s,%d,%d,%d,%d,%d,%d,%s,%.6f,%.6f,%.6f,%.6f%n",formato,n,rep,ht,hn,dt,dn,alvo.getTelefone(),carga/1e6,bt/1e6,bn/1e6,rm/1e6);
                w.flush();
                System.out.printf("%s n=%d repeticao=%d: carga %.3f ms%n",formato,n,rep,carga/1e6);
            }
        }
        System.out.println("24 execucoes verificadas. Resultados em "+saida);
    }
}

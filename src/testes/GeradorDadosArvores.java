package testes;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class GeradorDadosArvores {
    public static final int[] TAMANHOS={2047,4095,8191,16383};
    private static void escrever(BufferedWriter w,int id)throws IOException {
        w.write(String.format(java.util.Locale.ROOT,"Contato %06d;%011d",id,id));
        w.newLine();
    }
    private static void medianas(BufferedWriter w,int inicio,int fim)throws IOException {
        if(inicio>fim)return;
        int meio=(inicio+fim)/2;
        escrever(w,meio);
        medianas(w,inicio,meio-1);
        medianas(w,meio+1,fim);
    }
    public static void main(String[] args)throws IOException {
        Path pasta=Path.of(args.length==0?"dados":args[0]);
        Files.createDirectories(pasta);
        for(int n:TAMANHOS) {
            for(String formato:new String[]{"perfeita","degenerada"}) {
                Path arquivo=pasta.resolve(formato+"_"+n+".txt");
                try(BufferedWriter w=Files.newBufferedWriter(arquivo,StandardCharsets.UTF_8)) {
                    if(formato.equals("perfeita"))medianas(w,1,n);
                    else for(int id=1;id<=n;id++)escrever(w,id);
                }
                System.out.println(arquivo+": "+n+" contatos");
            }
        }
    }
}

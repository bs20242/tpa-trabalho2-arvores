from pathlib import Path
import csv, json, statistics, math, hashlib, html
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from reportlab.platypus import SimpleDocTemplate, Paragraph, Table, TableStyle, Spacer, PageBreak, Preformatted, Image
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from pypdf import PdfReader

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'relatorio/Relatorio_Parcial_Trabalho2_Arvores.pdf'
SIZES=[2047,4095,8191,16383]
rows=list(csv.DictReader((ROOT/'dados/resultados_arvores.csv').open(encoding='utf-8-sig')))
assert len(rows)==24
med={}
for modo in ['perfeita','degenerada']:
    for n in SIZES:
        grupo=[r for r in rows if r['formato']==modo and int(r['n'])==n]
        assert len(grupo)==3 and sorted(int(r['repeticao']) for r in grupo)==[1,2,3]
        h=int(math.log2(n+1))-1 if modo=='perfeita' else n-1
        assert all(int(r[k])==h for r in grupo for k in ['alturaTelefone','alturaNome','profundidadeTelefone','profundidadeNome'])
        med[modo,n]={k:statistics.median(float(r[k]) for r in grupo) for k in ['cargaMs','buscaTelefoneMs','buscaNomeMs','remocaoMs']}
ambiente=json.loads((ROOT/'dados/ambiente.json').read_text(encoding='utf-8-sig'))
fontes=Path(matplotlib.get_data_path())/'fonts/ttf'
for nome,arquivo in [('Texto','DejaVuSans.ttf'),('Negrito','DejaVuSans-Bold.ttf'),('Mono','DejaVuSansMono.ttf')]:
    pdfmetrics.registerFont(TTFont(nome,str(fontes/arquivo)))
pdfmetrics.registerFontFamily('Texto',normal='Texto',bold='Negrito',italic='Texto',boldItalic='Negrito')
AZUL=colors.HexColor('#193A50'); VERDE=colors.HexColor('#147D83'); CLARO=colors.HexColor('#EDF3F6')
styles={
 'p':ParagraphStyle('p',fontName='Texto',fontSize=9.2,leading=13,spaceAfter=8),
 'h1':ParagraphStyle('h1',fontName='Negrito',fontSize=18,leading=24,textColor=AZUL,spaceAfter=15,keepWithNext=True),
 'h2':ParagraphStyle('h2',fontName='Negrito',fontSize=12,leading=17,textColor=VERDE,spaceBefore=8,spaceAfter=8,keepWithNext=True),
 'nota':ParagraphStyle('nota',fontName='Texto',fontSize=8,leading=11,textColor=colors.HexColor('#52626E'),spaceAfter=8),
 'cell':ParagraphStyle('cell',fontName='Texto',fontSize=8,leading=11),
 'cab':ParagraphStyle('cab',fontName='Negrito',fontSize=8,leading=11,textColor=colors.white),
 'capa':ParagraphStyle('capa',fontName='Negrito',fontSize=30,leading=38,textColor=AZUL,spaceAfter=18),
}
story=[]; md=[]
def texto(s,style='p'):
    story.append(Paragraph(s,styles[style])); md.append(s.replace('<br/>','\n'))
def titulo(s):
    story.append(Paragraph(s,styles['h2'])); md.append('\n## '+s+'\n')
def pagina(s):
    story.append(PageBreak()); story.append(Paragraph(s,styles['h1'])); md.append('\n# '+s+'\n')
def tabela(cabecalho,dados,larguras):
    cells=[[Paragraph(html.escape(str(v)),styles['cab']) for v in cabecalho]]
    cells += [[Paragraph(html.escape(str(v)),styles['cell']) for v in linha] for linha in dados]
    t=Table(cells,colWidths=larguras,repeatRows=1,hAlign='LEFT')
    t.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,0),AZUL),('VALIGN',(0,0),(-1,-1),'TOP'),('LEFTPADDING',(0,0),(-1,-1),7),('RIGHTPADDING',(0,0),(-1,-1),7),('TOPPADDING',(0,0),(-1,-1),4.5),('BOTTOMPADDING',(0,0),(-1,-1),4.5),('ROWBACKGROUNDS',(0,1),(-1,-1),[colors.white,CLARO])]))
    story.extend([t,Spacer(1,10)])
    md.append('| '+' | '.join(str(v) for v in cabecalho)+' |')
    md.append('| '+' | '.join('---' for v in cabecalho)+' |')
    md.extend('| '+' | '.join(str(v) for v in linha)+' |' for linha in dados); md.append('')
src=(ROOT/'src/arvorebinaria/ArvoreBinaria.java').read_text(encoding='utf-8').splitlines()
def codigo(inicio,fim,tamanho=7.4):
    linhas=[f'{i:3}  {src[i-1]}' for i in range(inicio,fim+1)]
    assert all(pdfmetrics.stringWidth(l,'Mono',tamanho)<480 for l in linhas)
    story.append(Preformatted('\n'.join(linhas),ParagraphStyle('codigo',fontName='Mono',fontSize=tamanho,leading=tamanho+3,spaceAfter=12)))
    md.append('```java\n'+'\n'.join(linhas)+'\n```\n')
def numero(n,d=4):return f'{n:,.{d}f}'.replace(',','X').replace('.',',').replace('X','.')
def inteiro(n):return f'{n:,}'.replace(',','.')

story.append(Spacer(1,34)); texto('INSTITUTO FEDERAL DO ESPÍRITO SANTO<br/>Campus Serra','nota')
story.append(Spacer(1,65)); texto('Trabalho 2<br/>Árvores binárias','capa')
texto('Relatório parcial - seções 1, 2 e 3','h2')
texto('Técnicas de Programação Avançada<br/>Professor: Victorio Albani de Carvalho')
story.append(Spacer(1,30))
tabela(['Integrantes'],[['Bernardo Simão Rosa'],['Matheus Abreu'],['Levi Monteiro']],[480])
story.append(Spacer(1,32)); titulo('Conteúdo desta entrega')
texto('1. Desenvolvimento<br/>2. Análise matemática de complexidade<br/>3. Análise empírica de complexidade')
story.append(Spacer(1,28)); texto('Serra - ES | Outubro de 2026','nota')

pagina('1. Desenvolvimento')
titulo('1.1 Biblioteca e aplicativo')
texto('A biblioteca de árvores binárias de busca foi implementada em Java. A classe ArvoreBinaria&lt;T&gt; herda de ArvoreBinariaBase&lt;T&gt;, fornecida pelo professor. Generics permite escolher o tipo dos objetos, e o Comparator recebido no construtor define a chave de comparação. O aplicativo usa duas árvores de contatos, uma indexada por nome e outra por telefone.')
texto('A inserção segue a regra ensinada nas aulas: valores menores vão para a esquerda; maiores ou iguais vão para a direita. A busca segue um único caminho. A remoção trata nós sem filhos, com um filho e com dois filhos. Neste último caso, usa o menor valor da subárvore direita, uma das opções dos slides. Não há rotações: a ordem da entrada determina o formato da árvore.')
texto('Foram implementados adicionar, pesquisar, remover, quantidadeNos, altura e os caminhamentos em ordem e em nível. O toString é herdado da classe base. A altura conta arestas: -1 na árvore vazia e 0 quando há apenas a raiz. Os percursos usam laços, pilha ou fila explícita para permitir cadeias profundas sem depender de recursão.')
texto('O aplicativo do trabalho de listas foi reaproveitado. Na abertura, permite escolher lista não ordenada (1), lista ordenada (2) ou árvore binária (3). Os índices continuam declarados como IColecao&lt;Contato&gt;. A remoção por referência no índice de nomes preserva o contato correto quando existem nomes iguais. Um HashSet valida telefones únicos e libera o número após a exclusão.')
texto('Na lista encadeada, um campo total passa a guardar a quantidade de elementos. Cada inserção bem-sucedida incrementa o contador, e cada remoção bem-sucedida o decrementa, inclusive por referência. Assim, quantidadeNos passa de Θ(n) para Θ(1), sem mudar a ordem dos elementos. A árvore mantém a contagem por percurso, com custo Θ(n).')
titulo('1.2 Participação e repositório')
tabela(['Integrante','Responsabilidade no grupo'],[['Bernardo Simão Rosa','Biblioteca de árvores binárias'],['Matheus Abreu','Adaptação do aplicativo de contatos'],['Levi Monteiro','Relatório e experimentos']],[200,280])
texto('Repositório do projeto: <link href="https://github.com/bs20242/tpa-trabalho2-arvores" color="#147D83">https://github.com/bs20242/tpa-trabalho2-arvores</link>. O README descreve a organização dos pacotes e a compilação com JDK 21. O programa de teste da biblioteca, os geradores e os benchmarks ficam no pacote testes.')
titulo('1.3 Uso de inteligência artificial')
texto('A Inteligência Artificial auxiliou na implementação e na revisão do código, na preparação dos testes e na elaboração das análises. Também ajudou na organização do relatório, das tabelas e dos gráficos. Os tempos apresentados foram medidos em execuções locais do benchmark.')

pagina('2. Análise matemática de complexidade')
titulo('2.1 Critérios da análise')
texto('As linhas numeradas a seguir correspondem ao arquivo ArvoreBinaria.java deste projeto. No Modelo Simplificado de Custos, atribuímos custo constante a comparação, acesso a referência e atribuição. Seja n o número de nós, h a altura em arestas e d o número de nós visitados em uma procura. Para strings de comprimento variável L, a comparação pode custar O(L); as análises abaixo consideram chaves de tamanho limitado.')
texto('Na árvore perfeita, n = 2^(h + 1) - 1, logo h = log₂(n + 1) - 1. Na árvore completamente degenerada, h = n - 1. Esses formatos podem aparecer na mesma biblioteca, porque ela não faz balanceamento automático.')
titulo('2.2 adicionar')
codigo(19,33)
tabela(['Linhas','Contagem e raciocínio'],[['19, 28, 31-33','Assinatura e delimitadores de blocos; não percorrem nós.'],['20','Uma validação de valor nulo: Θ(1).'],['21','Uma criação de nó: Θ(1).'],['22','Um teste da raiz. Se estiver vazia, liga o nó e retorna.'],['23','Uma atribuição para iniciar a procura na raiz.'],['24-25','d iterações e d comparações para escolher um lado.'],['26 e 29','Um teste de filho por iteração; na última, insere e retorna.'],['27 e 30','d - 1 avanços no total, descendo um nível de cada vez.']],[70,410])
texto('T(n, h) = a + b·d, com d ≤ h + 1 para árvore não vazia. O pior caso ocorre ao inserir abaixo de uma folha mais profunda. O custo é Θ(h + 1): Θ(log n) na perfeita e Θ(n) na degenerada. Inserir na árvore vazia custa Θ(1).')

pagina('2. Análise matemática | pesquisa')
titulo('2.3 pesquisar')
codigo(36,45,8)
tabela(['Linhas','Contagem e raciocínio'],[['36, 43 e 45','Assinatura e delimitadores.'],['37','Uma validação de valor nulo.'],['38','Uma atribuição: a procura começa na raiz.'],['39','Até d + 1 testes, incluindo o teste final de nó nulo.'],['40','d comparações com a chave de cada nó visitado.'],['41','d testes de igualdade; retorna se encontrar.'],['42','Até d avanços, escolhendo apenas um dos filhos.'],['44','Um retorno de null somente quando a busca não encontra a chave.']],[70,410])
texto('T(n, h) = a + b·d. Buscar uma folha mais profunda, ou uma chave ausente que percorra um caminho de altura máxima, produz Θ(h + 1). Assim, o pior caso é Θ(log n) na perfeita e Θ(n) na degenerada. Encontrar a raiz custa Θ(1).')
titulo('Relação entre altura e trabalho da busca')
tabela(['Formato','Altura','Nós visitados na folha mais profunda'],[['Perfeita','log₂(n + 1) - 1','log₂(n + 1)'],['Degenerada','n - 1','n']],[110,155,215])
texto('Com 16.383 nós, o pior caminho da árvore perfeita visita 14 nós. Na degenerada, visita 16.383. Na perfeita, cada escolha descarta uma subárvore grande. Na degenerada, quase todos os nós ficam no mesmo caminho, como em uma lista encadeada.')

pagina('2. Análise matemática | remoção')
titulo('2.4 remover e seu auxiliar')
codigo(48,50)
codigo(54,80,7.1)
tabela(['Linhas','Contagem e raciocínio'],[['48-50 e 54','remover chama o auxiliar uma vez; assinatura e delimitadores.'],['55-56','Uma validação e uma inicialização das referências.'],['57-61','d visitas: compara, verifica a chave e avança um nível.'],['62-64','Fecha o laço, testa se encontrou e escolhe o caso de remoção.'],['65','Inicia a procura do sucessor, apenas no caso de dois filhos.'],['66-68','s avanços no caminho esquerdo da subárvore direita.'],['69-72','Fecha o laço, copia o valor e religa o filho do sucessor: Θ(1).'],['73-77','No caso de zero ou um filho, religa o pai ou a raiz: Θ(1).'],['78-80','Fecha os blocos e retorna true.']],[70,410])
texto('T(n, h) = a + b·d + c·s. A procura e a descida até o sucessor compõem um caminho limitado pela altura. O pior caso é Θ(h + 1): Θ(log n) na perfeita e Θ(n) na degenerada. Remover uma folha mais profunda já atinge esse custo. A remoção por referência usa o mesmo auxiliar, distinguindo a instância do objeto quando há nomes iguais.')

pagina('2. Análise matemática | contagem')
titulo('2.5 quantidadeNos da árvore')
codigo(83,95,8)
tabela(['Linhas','Contagem e raciocínio'],[['83, 93 e 95','Assinatura e delimitadores.'],['84','Um teste de raiz vazia; retorna 0 nesse caso.'],['85-87','Uma criação de pilha, uma inclusão da raiz e uma inicialização.'],['88','n + 1 testes de término.'],['89-90','n retiradas da pilha e n incrementos do contador local.'],['91-92','n testes de cada filho e n - 1 inclusões na pilha ao todo.'],['94','Um retorno da contagem obtida.']],[70,410])
texto('T(n) = a + b·n + c·(n - 1), portanto Θ(n) na perfeita e na degenerada. Cada nó entra e sai da pilha uma vez. ArrayDeque tem custo constante amortizado para essas operações. A contagem da árvore não usa o contador permanente acrescentado à lista.')
titulo('2.6 Síntese dos piores casos')
tabela(['Método da árvore','Perfeita','Degenerada'],[['adicionar','Θ(log n)','Θ(n)'],['pesquisar','Θ(log n)','Θ(n)'],['remover','Θ(log n)','Θ(n)'],['quantidadeNos','Θ(n)','Θ(n)']],[210,135,135])
texto('Na carga crescente, a inserção do i-ésimo nó compara com i - 1 nós. A soma de 0 até n - 1 é n(n - 1)/2: montagem Θ(n²). Inserir pelas medianas produz altura logarítmica, com montagem Θ(n log n). A leitura e a validação de telefones com HashSet acrescentam custo esperado Θ(n), considerando registros de tamanho limitado. Montar dois índices mantém essas ordens.')
texto('Na lista não ordenada, inserir no início custa Θ(1), e a carga com HashSet tem custo esperado Θ(n). Na lista ordenada, a carga pode custar Θ(n²). Consultar a quantidade na lista agora custa Θ(1); isso não torna a busca ou a remoção constantes.','nota')

pagina('3. Análise empírica de complexidade')
titulo('3.1 Implementação dos experimentos')
texto('GeradorDadosArvores produz arquivos UTF-8 no formato nome;telefone. Foram usados quatro tamanhos: 2.047, 4.095, 8.191 e 16.383 contatos. Cada tamanho tem duas versões, totalizando oito arquivos. Os tamanhos são iguais a 2^k - 1 para permitir árvores perfeitas, com todas as folhas no mesmo nível, conforme a definição dos slides. A escolha mantém a carga degenerada repetida em tempo viável; o enunciado recomenda, mas não exige, os mesmos tamanhos do trabalho de listas.')
texto('Cada registro usa um identificador único: nome Contato XXXXXX e telefone com 11 dígitos. A ordem crescente por nome coincide com a ordem por telefone. Na entrada degenerada, os identificadores vão de 1 até n. Na entrada perfeita, são escritos primeiro a mediana do intervalo e depois, recursivamente, as medianas de seus intervalos esquerdo e direito. A árvore é formada pelas inserções normais, sem balanceamento automático.')
texto('Após a carga, folhaMaisDistante faz um percurso em nível e seleciona o último nó visitado. Ele é uma folha no nível mais profundo e define o alvo da busca por telefone. Em seguida, pesquisa-se o nome do mesmo contato e remove-se seu telefone. As alturas, profundidades e quantidades foram verificadas antes das medidas. Nesta entrada controlada, os dois índices têm o mesmo formato, e o alvo também é o pior caso por nome; essa coincidência não é garantida com nomes reais em outra ordem.')
tabela(['n','Altura perfeita','Altura degenerada','Visitas: perfeita / degenerada'],[[inteiro(n),str(int(math.log2(n+1))-1),inteiro(n-1),f'{int(math.log2(n+1))} / {inteiro(n)}'] for n in SIZES],[65,110,120,185])
titulo('Ambiente e intervalos de medição')
texto(f"Coleta realizada em 05/10/2026, em {ambiente['sistema']}, processador {ambiente['processador']}, {numero(ambiente['ramGB'],1)} GB de RAM e Java {ambiente['java']}. A JVM usou heap inicial de 256 MB e máximo de 1.024 MB. Houve aquecimento com dez cargas de 2.047 contatos e buscas, fora dos intervalos medidos.")
texto('Foram feitas três execuções por tamanho e formato, com cadastro novo a cada carga: 24 execuções. A carga mede abertura, leitura, criação dos contatos, validação e montagem dos dois índices. As buscas e a remoção medem uma chamada ao respectivo método com System.nanoTime(). Escolher o alvo, calcular a altura, preparar as chaves e sincronizar a remoção por nome ficam fora desses intervalos. As tabelas mostram as medianas, em milissegundos.')
texto('Após cada exclusão, os dois índices foram verificados com n - 1 nós e o telefone removido deixou de ser encontrado. Os testes da biblioteca passaram em 25.139 verificações; o contador da lista passou em outras 20.000 verificações aleatórias. Os testes anteriores e os roteiros do menu nos três modos também passaram.','nota')

pagina('3. Análise empírica | resultados')
titulo('3.2 Tempos coletados e interpretação')
texto('Tabela 1. Medianas da leitura e montagem dos dois índices.','nota')
tabela(['Contatos','Perfeita (ms)','Degenerada (ms)'],[[inteiro(n),numero(med['perfeita',n]['cargaMs'],3),numero(med['degenerada',n]['cargaMs'],3)] for n in SIZES],[100,190,190])
texto('Tabela 2. Medianas das operações no mesmo contato, na folha mais profunda.','nota')
tabela(['Formato','Contatos','Telefone (ms)','Nome (ms)','Remoção (ms)'],[[modo.capitalize(),inteiro(n),numero(med[modo,n]['buscaTelefoneMs']),numero(med[modo,n]['buscaNomeMs']),numero(med[modo,n]['remocaoMs'])] for modo in ['perfeita','degenerada'] for n in SIZES],[100,70,110,100,100])
ratios=[med['degenerada',SIZES[i]]['cargaMs']/med['degenerada',SIZES[i-1]]['cargaMs'] for i in range(1,4)]
titulo('Leitura dos resultados')
texto('Ao quase dobrar o número de contatos, a mediana da carga degenerada aumentou '+', '.join(numero(r,2)+'x' for r in ratios)+'. O crescimento fica próximo do previsto para a soma quadrática das comparações. A árvore perfeita teve carga menor nos quatro tamanhos. Seus caminhos curtos evitam a soma quadrática, mas a carga ainda inclui leitura, alocação, validação e dois índices.')
texto('As buscas e a remoção na perfeita visitam de 11 a 14 nós nos tamanhos usados; na degenerada, de 2.047 a 16.383. Os tempos curtos oscilam por compilação da JVM, cache, coleta de lixo e agendamento do sistema. Há também valores de carga que não crescem de forma monotônica. Três repetições e quatro tamanhos não bastam para ajustar uma lei de crescimento com precisão. A contagem dos caminhos e a análise matemática explicam as ordens; os tempos mostram o comportamento desta execução.')
texto('Os CSVs preservam cada medição, sem remover valores extremos. A mediana de três medidas é o valor central após ordenar o trio. Escolher a folha e contar os nós não entra no tempo das buscas nem no da remoção.','nota')

pagina('3. Análise empírica | gráficos')
titulo('Gráficos das medianas')
plt.rcParams.update({'font.family':'DejaVu Sans','font.size':8})
fig,axs=plt.subplots(2,2,figsize=(8.8,6.6))
keys=['cargaMs','buscaTelefoneMs','buscaNomeMs','remocaoMs']
names=['Leitura e montagem','Busca por telefone','Busca por nome','Remoção por telefone']
for ax,key,name in zip(axs.flat,keys,names):
    for modo,cor in [('perfeita','#147D83'),('degenerada','#C06536')]:
        ax.plot(SIZES,[med[modo,n][key] for n in SIZES],'o-',color=cor,label=modo.capitalize())
    ax.set_title(name); ax.set_xlabel('Quantidade de contatos'); ax.set_ylabel('Tempo (ms), escala log'); ax.set_yscale('log'); ax.grid(True,alpha=.25)
    ax.set_xticks(SIZES,[inteiro(n) for n in SIZES]); ax.tick_params(axis='x',labelsize=7)
axs[0,0].legend(fontsize=8); fig.tight_layout()
grafico=ROOT/'relatorio/graficos_medianos.png'; fig.savefig(grafico,dpi=190); plt.close(fig)
story.append(Image(str(grafico),width=480,height=360)); md.append('![Gráficos das medianas](../relatorio/graficos_medianos.png)\n')
texto('Figura 1. Os pontos foram calculados com as mesmas medianas das tabelas 1 e 2. A escala vertical logarítmica permite mostrar os dois formatos. As linhas apenas ligam pontos medidos; não representam outras execuções.','nota')
titulo('Efeito do formato da árvore')
texto('A entrada crescente gera uma cadeia mesmo usando a biblioteca correta. Nesse formato, uma busca ou remoção no fim do caminho exige Θ(n), como o percurso de uma lista. A entrada por medianas mantém a altura pequena e permite Θ(log n) para essas operações. Isso mostra que a eficiência depende da ordem de entrada enquanto não há balanceamento automático.')
texto('A contagem de elementos da lista agora é constante, mas não altera a topologia da árvore nem o número de comparações na busca. Também não muda os tempos antigos: a contagem estava fora dos intervalos cronometrados do benchmark de listas.','nota')

pagina('3. Análise empírica | comparação com listas')
titulo('Resultados do trabalho anterior')
old=list(csv.DictReader((ROOT/'dados/resultados_listas_trabalho1.csv').open(encoding='utf-8-sig')))
texto('Tabela 3. Tempos em milissegundos registrados no trabalho 1. São da coleta anterior, com outros tamanhos e entradas.','nota')
tabela(['Lista','Contatos','Carga','Telefone','Nome','Remoção'],[[('Ordenada' if r['modo']=='ordenada' else 'Não ordenada'),inteiro(int(r['n'])),numero(float(r['tempoMontagemMs']),2),numero(float(r['tempoBuscaTelefoneMs'])),numero(float(r['tempoBuscaNomeMs'])),numero(float(r['tempoRemocaoMs']))] for r in old],[90,65,85,80,80,80])
texto('A lista não ordenada insere no início em Θ(1) e, com HashSet para validar telefones, tem carga esperada Θ(n). A lista ordenada procura a posição de cada contato e pode atingir Θ(n²) na carga. Nas duas listas, buscar ou remover o elemento do fim custa Θ(n). A árvore degenerada tem a mesma ordem de percurso; a perfeita reduz o pior caminho a Θ(log n).')
texto('Nesta coleta, a carga da árvore perfeita com 16.383 contatos teve mediana de '+numero(med['perfeita',16383]['cargaMs'],3)+' ms. No trabalho anterior, a lista ordenada com 10.000 contatos levou 961,56 ms. Os resultados mostram diferenças entre as coletas, mas não permitem afirmar um fator de velocidade controlado: tamanhos, entradas e aquecimento são diferentes. A comparação de ordens é sustentada pela análise dos algoritmos.')
titulo('Materiais e arquivos usados')
texto('Foram usados o enunciado Trabalho 2 - Árvores Binárias.pdf, itens 1 a 5; os slides APNP - Semana 3 - Arvores.pdf, especialmente altura, busca, caminhamentos e os três casos de remoção; e o exemplo ArvBin.zip. O exemplo C usa recursão e o maior da subárvore esquerda; esta versão Java usa percursos iterativos e o menor da direita, outra opção dos slides. A árvore vazia retorna -1 conforme o contrato Java fornecido pelo professor.')
texto('A classe base e a interface são do <link href="https://github.com/victoriocarvalho/tpa_bsi/tree/d2ce6e256f7dd02bc487d4a782b80895f658ed55/colecoes/src" color="#147D83">repositório do professor</link>. O endereço tpa_2026_1 do enunciado redireciona para tpa_bsi. O CSV histórico foi obtido do <link href="https://github.com/bs20242/tpa-trabalho1-etapa1/blob/c2061d78694cb72b3caccb67165f2adb1cc9ffa9/dados/resultados.csv" color="#147D83">trabalho 1 entregue</link>. O CSV atual está em dados/resultados_arvores.csv; ambiente e verificação das entradas estão em ambiente.json e manifesto.json.','nota')

def decor(c,doc):
    w,h=A4
    if doc.page==1:
        c.setFillColor(VERDE); c.rect(45,h-30,w-90,5,fill=1,stroke=0)
    else:
        c.setFillColor(AZUL); c.setFont('Texto',8); c.drawString(55,h-30,'TPA | Trabalho 2 | Relatório parcial')
        c.setStrokeColor(colors.HexColor('#CBD9E0')); c.line(55,43,w-55,43)
        c.setFillColor(colors.HexColor('#52626E')); c.drawString(55,29,'Árvores binárias de busca'); c.drawRightString(w-55,29,str(doc.page))
doc=SimpleDocTemplate(str(OUT),pagesize=A4,leftMargin=58,rightMargin=57,topMargin=55,bottomMargin=57,title='Trabalho 2 - Relatório parcial de árvores binárias',author='Bernardo Simão Rosa; Matheus Abreu; Levi Monteiro')
doc.build(story,onFirstPage=decor,onLaterPages=decor)
(ROOT/'docs/Relatorio_Parcial.md').write_text('\n\n'.join(md),encoding='utf-8')
text='\n'.join(p.extract_text() for p in PdfReader(OUT).pages)
for modo in ['perfeita','degenerada']:
    for n in SIZES:
        for key in keys:
            assert numero(med[modo,n][key],3 if key=='cargaMs' else 4) in text
assert all(s in text for s in ['Campus Serra','Bernardo Simão Rosa','Matheus Abreu','Levi Monteiro','https://github.com/bs20242/tpa-trabalho2-arvores'])
print('PDF gerado:',OUT)
for i,p in enumerate(PdfReader(OUT).pages,1):print('Pagina',i,':',len(p.extract_text()),'caracteres')
print('32 medianas verificadas contra os dados do CSV.')

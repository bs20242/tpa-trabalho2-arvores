from pathlib import Path
import csv, json, statistics, math, html
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
OUT=ROOT/'relatorio/Relatorio_Trabalho2_Arvores.pdf'
SIZES=[2047,4095,8191,16383]
KEYS=['cargaMs','buscaTelefoneMs','buscaNomeMs','remocaoMs']
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
rows_avl=list(csv.DictReader((ROOT/'dados/resultados_avl.csv').open(encoding='utf-8-sig')))
assert len(rows_avl)==24
avl={}
for est in ['avl','binaria']:
    for n in SIZES:
        grupo=[r for r in rows_avl if r['estrutura']==est and int(r['n'])==n]
        assert len(grupo)==3 and sorted(int(r['repeticao']) for r in grupo)==[1,2,3]
        h=int(math.log2(n+1))-1 if est=='avl' else n-1
        assert all(int(r[k])==h for r in grupo for k in ['alturaTelefone','alturaNome','profundidadeTelefone','profundidadeNome'])
        avl[est,n]={k:statistics.median(float(r[k]) for r in grupo) for k in KEYS}
ambiente_avl=json.loads((ROOT/'dados/ambiente_avl.json').read_text(encoding='utf-8-sig'))
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
story=[]
def texto(s,style='p'):
    story.append(Paragraph(s,styles[style]))
def titulo(s):
    story.append(Paragraph(s,styles['h2']))
def pagina(s):
    story.append(PageBreak()); story.append(Paragraph(s,styles['h1']))
def tabela(cabecalho,dados,larguras):
    cells=[[Paragraph(html.escape(str(v)),styles['cab']) for v in cabecalho]]
    cells += [[Paragraph(html.escape(str(v)),styles['cell']) for v in linha] for linha in dados]
    t=Table(cells,colWidths=larguras,repeatRows=1,hAlign='LEFT')
    t.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,0),AZUL),('VALIGN',(0,0),(-1,-1),'TOP'),('LEFTPADDING',(0,0),(-1,-1),7),('RIGHTPADDING',(0,0),(-1,-1),7),('TOPPADDING',(0,0),(-1,-1),4.5),('BOTTOMPADDING',(0,0),(-1,-1),4.5),('ROWBACKGROUNDS',(0,1),(-1,-1),[colors.white,CLARO])]))
    story.extend([t,Spacer(1,10)])
src=(ROOT/'src/arvorebinaria/ArvoreBinaria.java').read_text(encoding='utf-8').splitlines()
src_avl=(ROOT/'src/arvorebinaria/ArvoreAVL.java').read_text(encoding='utf-8').splitlines()
def codigo(inicio,fim,tamanho=7.4,src=src):
    linhas=[f'{i:3}  {src[i-1]}' for i in range(inicio,fim+1)]
    assert all(pdfmetrics.stringWidth(l,'Mono',tamanho)<480 for l in linhas)
    story.append(Preformatted('\n'.join(linhas),ParagraphStyle('codigo',fontName='Mono',fontSize=tamanho,leading=tamanho+3,spaceAfter=12)))
def numero(n,d=4):return f'{n:,.{d}f}'.replace(',','X').replace('.',',').replace('X','.')
def inteiro(n):return f'{n:,}'.replace(',','.')

story.append(Spacer(1,34)); texto('INSTITUTO FEDERAL DO ESPÍRITO SANTO<br/>Campus Serra','nota')
story.append(Spacer(1,65)); texto('Trabalho 2<br/>Árvores binárias','capa')
texto('Relatório final - seções 1 a 6','h2')
texto('Técnicas de Programação Avançada<br/>Professor: Victorio Albani de Carvalho')
story.append(Spacer(1,30))
tabela(['Integrantes'],[['Bernardo Simão Rosa'],['Matheus Abreu'],['Levi Monteiro']],[480])
story.append(Spacer(1,32)); titulo('Conteúdo desta entrega')
texto('1. Desenvolvimento<br/>2. Análise matemática de complexidade<br/>3. Análise empírica de complexidade<br/>4. Árvore AVL: análise matemática<br/>5. Árvore AVL: análise empírica<br/>6. Árvores binárias na biblioteca padrão de Java')
story.append(Spacer(1,20)); texto('Serra - ES | Outubro de 2026','nota')

pagina('1. Desenvolvimento')
titulo('1.1 Biblioteca e aplicativo')
texto('A biblioteca de árvores binárias de busca foi implementada em Java. A classe ArvoreBinaria&lt;T&gt; herda de ArvoreBinariaBase&lt;T&gt;, fornecida pelo professor. Generics permite escolher o tipo dos objetos, e o Comparator recebido no construtor define a chave de comparação. O aplicativo usa duas árvores de contatos, uma indexada por nome e outra por telefone.')
texto('A inserção segue a regra ensinada nas aulas: valores menores vão para a esquerda; maiores ou iguais vão para a direita. A busca segue um único caminho. A remoção trata nós sem filhos, com um filho e com dois filhos. Neste último caso, usa o menor valor da subárvore direita, uma das opções dos slides. Não há rotações: a ordem da entrada determina o formato da árvore.')
texto('Foram implementados adicionar, pesquisar, remover, quantidadeNos, altura e os caminhamentos em ordem e em nível. O toString é herdado da classe base. A altura conta arestas: -1 na árvore vazia e 0 quando há apenas a raiz. Os percursos usam laços, pilha ou fila explícita para permitir cadeias profundas sem depender de recursão.')
texto('O aplicativo do trabalho de listas foi reaproveitado. Na abertura, permite escolher lista não ordenada (1), lista ordenada (2), árvore binária (3) ou árvore AVL (4). Os índices continuam declarados como IColecao&lt;Contato&gt;. A remoção por referência no índice de nomes preserva o contato correto quando existem nomes iguais. Um HashSet valida telefones únicos e libera o número após a exclusão.')
texto('A classe ArvoreAVL&lt;T&gt; herda de ArvoreBinaria&lt;T&gt; e sobrescreve adicionar, remover e removerReferencia, aplicando rotações simples e duplas no retorno da recursão. Para permitir a herança, o nó e a raiz de ArvoreBinaria passaram de private para protected; a AVL usa uma subclasse do nó, NoAVL, que guarda a altura. Pesquisa, contagem e caminhamentos são herdados sem alteração. No aplicativo, foram ajustadas a escolha da estrutura e a remoção por referência para aceitar a AVL.')
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
texto('As buscas e a remoção na perfeita visitam de 11 a 14 nós nos tamanhos usados; na degenerada, de 2.047 a 16.383. Os tempos curtos podem variar com a compilação da JVM, cache, coleta de lixo e agendamento do sistema. Há também valores de carga que não crescem de forma monotônica. Três repetições e quatro tamanhos não bastam para ajustar uma lei de crescimento com precisão. A contagem dos caminhos e a análise matemática explicam as ordens; os tempos mostram o comportamento desta execução.')
texto('Os CSVs preservam cada medição, sem remover valores extremos. A mediana de três medidas é o valor central após ordenar o trio. Escolher a folha e contar os nós não entra no tempo das buscas nem no da remoção.','nota')

pagina('3. Análise empírica | gráficos')
titulo('Gráficos das medianas')
plt.rcParams.update({'font.family':'DejaVu Sans','font.size':8})
fig,axs=plt.subplots(2,2,figsize=(8.8,6.6))
keys=KEYS
names=['Leitura e montagem','Busca por telefone','Busca por nome','Remoção por telefone']
for ax,key,name in zip(axs.flat,keys,names):
    for modo,cor in [('perfeita','#147D83'),('degenerada','#C06536')]:
        ax.plot(SIZES,[med[modo,n][key] for n in SIZES],'o-',color=cor,label=modo.capitalize())
    ax.set_title(name); ax.set_xlabel('Quantidade de contatos'); ax.set_ylabel('Tempo (ms), escala log'); ax.set_yscale('log'); ax.grid(True,alpha=.25)
    ax.set_xticks(SIZES,[inteiro(n) for n in SIZES]); ax.tick_params(axis='x',labelsize=7)
axs[0,0].legend(fontsize=8); fig.tight_layout()
grafico=ROOT/'relatorio/graficos_medianos.png'; fig.savefig(grafico,dpi=190); plt.close(fig)
story.append(Image(str(grafico),width=480,height=360))
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

# Confere que as linhas citadas nas tabelas continuam apontando para o codigo certo.
for linha,trecho in [(24,'public boolean adicionar'),(30,'private No<T> adicionar'),(41,'public boolean remover'),(47,'public boolean removerReferencia'),(51,'private boolean remover'),(58,'private No<T> remover'),(65,'porReferencia && no.valor != valor'),(80,'resultado.realizada ? balancear'),(83,'private No<T> removerMenor'),(90,'public int altura'),(94,'private int altura'),(98,'private void atualizarAltura'),(103,'private int fatorBalanceamento'),(107,'private No<T> balancear'),(122,'private No<T> rotacaoEsquerda'),(131,'private No<T> rotacaoDireita'),(140,'private No<T> rotacaoEsquerdaDireita'),(145,'private No<T> rotacaoDireitaEsquerda')]:
    assert trecho in src_avl[linha-1],(linha,trecho)

pagina('4. Árvore AVL: análise matemática')
titulo('4.1 Implementação e balanceamento')
texto('ArvoreAVL&lt;T&gt; herda de ArvoreBinaria&lt;T&gt;. Seus nós são da subclasse NoAVL, que acrescenta a altura ao nó da classe pai. Seguindo os slides, o fator de balanceamento é altura(direita) - altura(esquerda), e a árvore é AVL quando o fator de todos os nós está entre -1 e 1. A inserção e a remoção são recursivas: descem pelo mesmo caminho da árvore binária comum e, na volta de cada chamada, balancear atualiza a altura do nó e corrige o desbalanceamento com rotações. As linhas abaixo são de ArvoreAVL.java.')
codigo(94,120,7.4,src_avl)
tabela(['Linhas','Contagem e raciocínio'],[['94-96','altura de um nó: um teste e um acesso; nó nulo vale -1. Θ(1).'],['98-100','atualizarAltura usa as alturas já guardadas nos filhos: um max e uma soma. Θ(1), sem percorrer a subárvore.'],['102-105','fatorBalanceamento: duas chamadas Θ(1) e uma subtração.'],['107-109','balancear atualiza a altura e calcula o fator: Θ(1).'],['111-114','Fator 2 (pesado à direita): se o filho direito pende para a esquerda, rotação direita-esquerda; senão, rotação simples à esquerda.'],['115-118','Fator -2 (pesado à esquerda): caso simétrico, esquerda-direita ou rotação simples à direita.'],['110 e 119','Na correção de um nó desbalanceado após remoção, o filho pode ter fator zero; usa-se rotação simples. Sem desbalanceamento, retorna o próprio nó.']],[70,410])

pagina('4. Árvore AVL | rotações')
codigo(122,148,7.6,src_avl)
tabela(['Linhas','Contagem e raciocínio'],[['122-129','rotacaoEsquerda: o filho direito sobe, três referências mudam e duas alturas são atualizadas, primeiro a do nó que desce e depois a da nova raiz. Θ(1).'],['131-138','rotacaoDireita: simétrica. Θ(1).'],['140-148','Rotações duplas: uma rotação no filho e outra no nó. Duas operações Θ(1).']],[70,410])
texto('Assim, balancear custa Θ(1) em todos os casos: no máximo duas rotações, cada uma com quantidade fixa de operações, independente de n. As rotações preservam o percurso em ordem e, portanto, a propriedade de árvore de busca.')
texto('altura() é sobrescrito (linhas 89-92) e devolve a altura guardada na raiz, em Θ(1). Na árvore binária comum, o mesmo método percorre a árvore em nível, em Θ(n).')

pagina('4. Árvore AVL | adicionar')
titulo('4.2 adicionar')
codigo(23,38,7.8,src_avl)
tabela(['Linhas','Contagem e raciocínio'],[['25','Validação de valor nulo: Θ(1).'],['26','Uma chamada ao auxiliar recursivo e a atribuição da nova raiz, que pode mudar após uma rotação.'],['31','Caso base: cria o NoAVL na posição de folha, com altura 0. Ocorre uma vez.'],['32-36','Uma comparação e uma chamada recursiva para apenas um dos filhos; iguais vão para a direita. As chamadas seguem um único caminho: d ≤ h + 1 nós.'],['37','balancear no retorno de cada chamada: Θ(1), conforme 4.1.']],[70,410])
texto('Recorrência: T(d) = T(d - 1) + c, com T(0) = c₀. Logo T = Θ(d) = Θ(h + 1). Na AVL, a altura é limitada para qualquer ordem de entrada. Seja N(h) o menor número de nós de uma AVL de altura h: N(0) = 1, N(1) = 2 e N(h) = N(h - 1) + N(h - 2) + 1, pois a raiz tem uma subárvore de altura h - 1 e, no mínimo, outra de altura h - 2. Essa recorrência é a de Fibonacci deslocada, N(h) = F(h + 3) - 1, que cresce como φ^h, com φ ≈ 1,618. Portanto h &lt; 1,4405·log₂(n + 2) - 0,3277, ou seja, h = Θ(log n).')
texto('Pior caso de adicionar: inserir uma chave que desça até o nível mais profundo, visitando h + 1 nós, e provocar uma rotação dupla. O custo é Θ(log n). Não existe caso degenerado: ao contrário da árvore binária comum, uma entrada crescente não aumenta a altura além do limite. Em uma inserção, uma única rotação (simples ou dupla) devolve à subárvore a altura que tinha antes; acima dela não há mais rotações, mas o código ainda chama balancear em cada nível, com custo Θ(1) por nível.')
tabela(['Formato','Altura','adicionar no pior caso'],[['AVL perfeitamente balanceada','log₂(n + 1) - 1','Θ(log n)'],['AVL mais desbalanceada possível (árvore de Fibonacci)','< 1,4405·log₂(n + 2)','Θ(log n)'],['Degenerada','impossível na AVL','-']],[220,140,120])

pagina('4. Árvore AVL | remover')
titulo('4.3 remover e removerReferencia')
codigo(40,87,6.6,src_avl)
pagina('4. Árvore AVL | análise da remoção')
tabela(['Linhas','Contagem e raciocínio'],[['40-56','Validação, criação de Remocao e atribuição da raiz: trabalho local Θ(1), além do custo da chamada ao auxiliar recursivo.'],['59-60','Por chamada: um teste de nulo e uma comparação.'],['61-64','Chave menor ou maior: desce para apenas um dos filhos. Ao todo d ≤ h + 1 chamadas.'],['72-74','Nó encontrado com zero ou um filho: marca a remoção e retorna o filho, Θ(1).'],['75-78','Dois filhos: o laço desce s nós até o sucessor (menor da direita), copia o valor e chama removerMenor, que refaz os mesmos s passos.'],['80','Só rebalanceia se houve remoção: Θ(1) por nível, podendo haver rotação em vários níveis.'],['83-87','removerMenor: s chamadas, cada uma com balancear Θ(1).'],['65-70','Só em removerReferencia, quando a chave é igual mas a instância é outra (nomes repetidos). Ver abaixo.']],[70,410])
texto('T = a + b·d + c·s, com d + s ≤ h + 1, pois o sucessor está no prolongamento do mesmo caminho. No pior caso, remover custa Θ(h + 1) = Θ(log n). Isso ocorre, por exemplo, ao remover uma folha no nível mais profundo. Em certas árvores AVL, a remoção também exige rotações em vários níveis até a raiz. São até O(log n) rotações, cada uma Θ(1), mantendo o total em Θ(log n).')
texto('Chaves iguais: na árvore binária comum, iguais sempre vão para a direita. Na AVL, uma rotação pode levar uma chave igual para a esquerda. Por isso, ao remover por referência um contato cujo nome se repete, as linhas 65-70 procuram a instância nos dois lados da região de nomes iguais. Com r contatos de mesmo nome, o custo é O(log n + r). No índice por telefone, sem repetições, esse ramo nunca é executado. O pior caso teórico seria todos os n contatos com o mesmo nome, Θ(n).')
titulo('4.4 Síntese')
tabela(['Método','Binária perfeita','Binária degenerada','AVL (qualquer entrada)'],[['adicionar','Θ(log n)','Θ(n)','Θ(log n)'],['pesquisar (herdado)','Θ(log n)','Θ(n)','Θ(log n)'],['remover','Θ(log n)','Θ(n)','Θ(log n)'],['altura','Θ(n)','Θ(n)','Θ(1), lê a altura da raiz'],['quantidadeNos (herdado)','Θ(n)','Θ(n)','Θ(n)'],['Montar com n inserções','Θ(n log n)','Θ(n²)','Θ(n log n)']],[130,110,110,130])
texto('A AVL troca um custo constante extra por inserção e remoção (alturas e rotações) pela garantia de altura logarítmica. A pesquisa não foi sobrescrita: é o mesmo laço da classe pai, que agora percorre um caminho curto.','nota')

pagina('5. Árvore AVL: análise empírica')
titulo('5.1 Metodologia')
texto(f"BenchmarkAVL usa os mesmos arquivos degenerados da seção 3, com telefones em ordem crescente, e faz as mesmas observações: carga dos dois índices, busca por telefone da folha mais distante da raiz, busca por nome do mesmo contato e remoção por telefone. Para a comparação ser justa, a árvore binária comum foi medida de novo, na mesma execução e na mesma máquina, alternando a ordem das estruturas a cada repetição. Coleta em 26/10/2026: {ambiente_avl['processador']}, {numero(ambiente_avl['ramGB'],1)} GB de RAM, Java {ambiente_avl['java']}, heap {ambiente_avl['heap']}, três repetições e aquecimento das duas estruturas. As tabelas mostram medianas em milissegundos.")
texto('Antes de medir, o programa confirma n nós em cada índice, a altura esperada e a profundidade do alvo. Com entrada crescente e n = 2^k - 1, a AVL termina perfeitamente balanceada: altura k - 1, a mesma da entrada por medianas. O alvo escolhido por folhaMaisDistante está, portanto, a log₂(n + 1) - 1 arestas da raiz, contra n - 1 na binária comum.')
tabela(['n','Altura AVL','Altura binária','Nós visitados: AVL / binária'],[[inteiro(n),str(int(math.log2(n+1))-1),inteiro(n-1),f'{int(math.log2(n+1))} / {inteiro(n)}'] for n in SIZES],[65,100,110,205])
titulo('5.2 Tempos coletados')
texto('Tabela 4. Leitura e montagem dos dois índices com telefones crescentes.','nota')
tabela(['Contatos','AVL (ms)','Binária (ms)','Binária / AVL'],[[inteiro(n),numero(avl['avl',n]['cargaMs'],3),numero(avl['binaria',n]['cargaMs'],3),numero(avl['binaria',n]['cargaMs']/avl['avl',n]['cargaMs'],1)+'x'] for n in SIZES],[90,120,130,140])
texto('Tabela 5. Operações no mesmo contato, na folha mais distante da raiz.','nota')
tabela(['Estrutura','Contatos','Telefone (ms)','Nome (ms)','Remoção (ms)'],[[('AVL' if est=='avl' else 'Binária'),inteiro(n),numero(avl[est,n]['buscaTelefoneMs']),numero(avl[est,n]['buscaNomeMs']),numero(avl[est,n]['remocaoMs'])] for est in ['avl','binaria'] for n in SIZES],[90,70,110,100,110])

pagina('5. Árvore AVL | interpretação')
titulo('5.3 Interpretação e comparação')
r_bin=[avl['binaria',SIZES[i]]['cargaMs']/avl['binaria',SIZES[i-1]]['cargaMs'] for i in range(1,4)]
r_avl=[avl['avl',SIZES[i]]['cargaMs']/avl['avl',SIZES[i-1]]['cargaMs'] for i in range(1,4)]
texto('Carga. Ao quase dobrar n, a carga da binária comum cresceu '+', '.join(numero(r,2)+'x' for r in r_bin)+', perto do fator 4 esperado para Θ(n²). Na AVL, os fatores foram '+', '.join(numero(r,2)+'x' for r in r_avl)+'. O primeiro tamanho teve carga maior que o segundo. Essas oscilações podem envolver aquecimento da JVM e atividades do sistema; a coleta não isolou suas causas. As medidas mostram vantagem da AVL, mas não bastam para confirmar sozinhas a ordem Θ(n log n). Com 16.383 contatos, a binária levou '+numero(avl['binaria',16383]['cargaMs'],1)+' ms e a AVL '+numero(avl['avl',16383]['cargaMs'],1)+' ms: a razão entre as medianas foi de '+numero(avl['binaria',16383]['cargaMs']/avl['avl',16383]['cargaMs'],0)+' para 1.')
buscas_avl=[avl['avl',n][k] for n in SIZES for k in ['buscaTelefoneMs','buscaNomeMs']]
remocoes_avl=[avl['avl',n]['remocaoMs'] for n in SIZES]
texto('Buscas e remoção. As medianas das buscas na AVL ficaram entre '+numero(min(buscas_avl))+' e '+numero(max(buscas_avl))+' ms; as remoções, entre '+numero(min(remocoes_avl))+' e '+numero(max(remocoes_avl))+' ms. São tempos muito curtos, sujeitos a variações entre execuções. Na busca por telefone da binária comum, a mediana passou de '+numero(avl['binaria',2047]['buscaTelefoneMs'])+' para '+numero(avl['binaria',16383]['buscaTelefoneMs'])+' ms. A AVL visita de 11 a 14 nós, enquanto a binária visita de 2.047 a 16.383. Ao remover a folha dessas árvores AVL perfeitas, as alturas são atualizadas, mas não são necessárias rotações. Portanto, este teste não mede o caso de remoção com várias rotações.')
texto('Comparação com a seção 3. Os tempos da AVL nos arquivos crescentes ficaram na mesma faixa da árvore binária perfeita da seção 3 (entrada por medianas), porque as duas têm a mesma altura. A diferença é que a binária só é perfeita quando a ordem de entrada é favorável; a AVL garante a altura logarítmica para qualquer ordem. As medidas da seção 3 foram feitas em outra máquina, por isso a comparação entre seções considera as ordens de grandeza, e não os valores exatos. Em relação às listas do trabalho 1, a busca e a remoção na AVL deixam de depender linearmente de n.')
titulo('Gráficos')
fig,axs=plt.subplots(2,2,figsize=(8.8,6.2))
for ax,key,name in zip(axs.flat,KEYS,names):
    for est,cor,rotulo in [('avl','#147D83','AVL'),('binaria','#C06536','Binária comum')]:
        ax.plot(SIZES,[avl[est,n][key] for n in SIZES],'o-',color=cor,label=rotulo)
    ax.set_title(name+' (telefones crescentes)',fontsize=8); ax.set_xlabel('Quantidade de contatos'); ax.set_ylabel('Tempo (ms), escala log'); ax.set_yscale('log'); ax.grid(True,alpha=.25)
    ax.set_xticks(SIZES,[inteiro(n) for n in SIZES]); ax.tick_params(axis='x',labelsize=7)
axs[0,0].legend(fontsize=8); fig.tight_layout()
grafico_avl=ROOT/'relatorio/graficos_avl.png'; fig.savefig(grafico_avl,dpi=190); plt.close(fig)
story.append(Image(str(grafico_avl),width=450,height=317))
texto('Figura 2. Medianas das tabelas 4 e 5, em escala logarítmica.','nota')

pagina('6. Árvores binárias na biblioteca padrão de Java')
titulo('6.1 TreeMap e TreeSet')
texto('A biblioteca padrão oferece java.util.TreeMap&lt;K,V&gt;, um mapa ordenado por chave, e java.util.TreeSet&lt;E&gt;, um conjunto ordenado baseado em TreeMap. TreeSet guarda os elementos como chaves do mapa interno, com um objeto fixo como valor. TreeMap oferece firstKey, lastKey, floorKey, ceilingKey, headMap, tailMap e subMap. Em TreeSet, as operações equivalentes são first, last, floor, ceiling, headSet, tailSet e subSet.')
titulo('6.2 Estrutura: árvore rubro-negra')
texto('Segundo a documentação, TreeMap é uma implementação baseada em árvore rubro-negra, com algoritmos adaptados do livro Introduction to Algorithms (Cormen, Leiserson e Rivest). Cada Entry guarda chave, valor, referências para esquerda, direita e pai, e um booleano de cor. A árvore mantém as propriedades: (1) todo nó é vermelho ou preto; (2) a raiz é preta; (3) as folhas nulas são pretas; (4) um nó vermelho não tem filho vermelho; (5) todo caminho de um nó até as folhas nulas abaixo dele passa pelo mesmo número de nós pretos. Delas decorre que o caminho mais longo tem no máximo o dobro do mais curto, e a altura é no máximo 2·log₂(n + 1).')
texto('Depois de put, o método fixAfterInsertion recolore nós e aplica no máximo duas rotações (rotateLeft/rotateRight). Depois de remove (deleteEntry), fixAfterDeletion aplica no máximo três rotações. A recoloração pode subir até a raiz, em O(log n). Os métodos de busca, como getEntry, são iterativos, como o pesquisar da nossa biblioteca.')
tabela(['Aspecto','Nossa ArvoreAVL','TreeMap (rubro-negra)'],[['Critério de balanceamento','|fator| ≤ 1 em todo nó','Regras de cor e altura preta'],['Altura máxima','≈ 1,44·log₂(n + 2)','2·log₂(n + 1)'],['Rotações por inserção','até 2 (uma simples ou uma dupla)','até 2'],['Rotações por remoção','até O(log n)','até 3'],['Informação extra por nó','altura (int)','cor (boolean) e referência ao pai'],['Chaves repetidas','permitidas','não: put substitui o valor'],['Tamanho','quantidadeNos percorre, Θ(n)','size() lê um contador, Θ(1)']],[140,170,170])
texto('A AVL impõe um limite menor à altura. A rubro-negra limita a quantidade de rotações por atualização. As duas têm as mesmas ordens de complexidade; o desempenho prático também depende das entradas e das operações usadas.')

pagina('6. Biblioteca padrão | comparação e complexidade')
titulo('6.3 Estratégia de comparação')
texto('TreeMap usa a ordem natural das chaves, quando é criado sem argumentos, ou um Comparator passado ao construtor, como na nossa biblioteca. Na ordem natural, as chaves precisam implementar Comparable, e o método compareTo é chamado; se não implementarem, ocorre ClassCastException ao inserir. Chave nula não é aceita na ordem natural (NullPointerException); com Comparator, depende de ele tratar null. Igualdade é decidida pelo comparador, e não por equals: duas chaves com compare igual a 0 são a mesma chave. Por isso a documentação recomenda um comparador consistente com equals.')
texto('Exemplo no nosso domínio: new TreeMap&lt;&gt;(new Contato.ComparadorPorTelefone()) funcionaria como índice por telefone. Já para o índice por nome, contatos com o mesmo nome se substituiriam; seria preciso usar TreeMap&lt;String, List&lt;Contato&gt;&gt; ou incluir o telefone no critério de desempate. Nossa biblioteca aceita repetições diretamente.')
titulo('6.4 Ordem de complexidade')
tabela(['Operação','TreeMap / TreeSet','Observação'],[['put / add (inserção)','O(log n)','descida + recoloração e até 2 rotações'],['get, containsKey, contains (busca)','O(log n)','laço iterativo da raiz até a chave'],['remove','O(log n)','sucessor + até 3 rotações'],['firstKey, lastKey, floorKey, ceilingKey','O(log n)','seguem um caminho da árvore'],['size','O(1)','contador mantido nas atualizações'],['percorrer todos os elementos','O(n)','iterador em ordem, usando a referência ao pai']],[170,110,200])
titulo('6.5 Outras estruturas relacionadas')
texto('HashMap e HashSet usam tabelas de dispersão, mas podem transformar baldes com muitas colisões em árvores rubro-negras. Isso melhora várias situações, sem garantir busca O(log k) para qualquer chave: com hashes iguais e sem uma comparação que defina um lado, a busca pode visitar os dois lados da árvore. PriorityQueue usa um heap binário guardado em vetor, com offer e poll em O(log n) e peek em O(1); contains custa O(n). ConcurrentSkipListMap oferece operações básicas em O(log n) esperado, mas usa skip list, não árvore.')
texto('Fontes: documentação do Java SE 21 - <link href="https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/TreeMap.html" color="#147D83">TreeMap</link>, <link href="https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/TreeSet.html" color="#147D83">TreeSet</link>, <link href="https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/PriorityQueue.html" color="#147D83">PriorityQueue</link> e <link href="https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ConcurrentSkipListMap.html" color="#147D83">ConcurrentSkipListMap</link>; código do OpenJDK 21 - <link href="https://github.com/openjdk/jdk21u/blob/master/src/java.base/share/classes/java/util/TreeMap.java" color="#147D83">TreeMap.java</link> e <link href="https://github.com/openjdk/jdk21u/blob/master/src/java.base/share/classes/java/util/HashMap.java" color="#147D83">HashMap.java</link>.','nota')

def decor(c,doc):
    w,h=A4
    if doc.page==1:
        c.setFillColor(VERDE); c.rect(45,h-30,w-90,5,fill=1,stroke=0)
    else:
        c.setFillColor(AZUL); c.setFont('Texto',8); c.drawString(55,h-30,'TPA | Trabalho 2 | Relatório final')
        c.setStrokeColor(colors.HexColor('#CBD9E0')); c.line(55,43,w-55,43)
        c.setFillColor(colors.HexColor('#52626E')); c.drawString(55,29,'Árvores binárias de busca e AVL'); c.drawRightString(w-55,29,str(doc.page))
doc=SimpleDocTemplate(str(OUT),pagesize=A4,leftMargin=58,rightMargin=57,topMargin=55,bottomMargin=57,title='Trabalho 2 - Relatório de árvores binárias e AVL',author='Bernardo Simão Rosa; Matheus Abreu; Levi Monteiro')
doc.build(story,onFirstPage=decor,onLaterPages=decor)
text='\n'.join(p.extract_text() for p in PdfReader(OUT).pages)
for modo in ['perfeita','degenerada']:
    for n in SIZES:
        for key in keys:
            assert numero(med[modo,n][key],3 if key=='cargaMs' else 4) in text
assert all(s in text for s in ['Campus Serra','Bernardo Simão Rosa','Matheus Abreu','Levi Monteiro','https://github.com/bs20242/tpa-trabalho2-arvores'])
print('PDF gerado:',OUT)
for i,p in enumerate(PdfReader(OUT).pages,1):print('Pagina',i,':',len(p.extract_text()),'caracteres')
for est in ['avl','binaria']:
    for n in SIZES:
        for key in KEYS:
            assert numero(avl[est,n][key],3 if key=='cargaMs' else 4) in text,(est,n,key)
print('64 medianas verificadas contra os dados dos CSVs.')

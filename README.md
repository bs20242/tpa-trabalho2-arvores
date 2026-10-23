# Trabalho 2 - Árvores binárias

Técnicas de Programação Avançada - IFES, Campus Serra.

O projeto contém as bibliotecas de árvore binária e AVL, o aplicativo de contatos com três opções e o relatório parcial de árvores sem balanceamento automático. A biblioteca AVL está pronta para a integração da quarta opção ao aplicativo, descrita abaixo.

Grupo: Bernardo Simão Rosa, Matheus Abreu e Levi Monteiro.

## Aplicativo de contatos

Os arquivos de listas e contatos foram trazidos do [repositório entregue no trabalho 1](https://github.com/bs20242/tpa-trabalho1-etapa1/tree/c2061d78694cb72b3caccb67165f2adb1cc9ffa9/src), no commit `c2061d7`. Estão neste mesmo projeto para que o aplicativo possa usar listas ou árvores.

- `src/colecao/ListaEncadeada.java` e `No.java`: listas ordenadas e não ordenadas.
- `src/dominio/Contato.java`: contato e comparadores por nome e telefone.
- `src/app/CadastroContatos.java`: cadastro, carga, validação e sincronização dos índices.
- `src/app/ProgramaContatos.java`: menu e operações do aplicativo de contatos.
- `src/testes/TesteLista.java`, `TesteCadastro.java`, `GeradorDadosContatos.java` e `BenchmarkListas.java`: testes e ferramentas do trabalho anterior.

O aplicativo permite escolher entre lista não ordenada (1), lista ordenada (2) e árvore binária (3). `CadastroContatos` cria os dois índices na estrutura escolhida e remove por referência no índice de nomes, preservando outros contatos com nomes iguais. O construtor com `boolean` continua disponível para os testes e o benchmark de listas do trabalho anterior.

Depois de compilar, o aplicativo pode ser executado com:

```powershell
java -cp bin app.ProgramaContatos
```

## Árvore binária sem balanceamento

`ArvoreBinaria<T>` herda da classe `ArvoreBinariaBase<T>` fornecida pelo professor. O tipo armazenado é definido por Generics; o critério de comparação é recebido no construtor por um `Comparator<T>`. Assim, o mesmo tipo de objeto pode ser armazenado em árvores diferentes, por exemplo, uma por nome e outra por telefone.

Métodos exigidos pelo enunciado:

| Método | Comportamento |
| --- | --- |
| `adicionar` | Insere o objeto conforme o comparador. Valores iguais vão para a direita. |
| `pesquisar` | Retorna um objeto cuja chave seja igual pelo comparador, ou `null` se não encontrar. |
| `remover` | Remove uma ocorrência da chave e informa se encontrou. Trata os casos de zero, um e dois filhos. |
| `quantidadeNos` | Percorre os nós e retorna a contagem atual. |
| `altura` | Conta arestas: árvore vazia retorna `-1`; raiz sozinha retorna `0`. |
| `caminharEmOrdem` | Retorna os valores na ordem definida pelo comparador, entre colchetes. |
| `caminharEmNivel` | Retorna os valores por nível, com um nível por linha, entre colchetes. |
| `toString` | Usa o caminhamento em ordem herdado da classe base. |

A inserção e a busca seguem a regra apresentada nas aulas: menor à esquerda, maior ou igual à direita. Ao remover um nó com dois filhos, seu valor é substituído pelo menor da subárvore direita. O sucessor é então desligado, preservando seu possível filho direito. Essa é uma das opções apresentadas nos slides.

Na lista encadeada, `quantidadeNos()` retorna um campo atualizado pelas inserções e remoções, com custo O(1). Na árvore, esse método mantém o percurso O(n) descrito na tabela e no relatório.

Os percursos usam laços, pilha ou fila explícita para funcionar também em árvores degeneradas profundas. `ArvoreBinaria` não realiza rotações; a ordem da entrada determina o formato da árvore. Valores nulos não são inseridos. Chaves iguais são permitidas, e a busca ou remoção por chave atua sobre uma ocorrência.

## Biblioteca AVL — item 6 do enunciado

`ArvoreAVL<T>` estende `ArvoreBinaria<T>` e sobrescreve `adicionar`, `remover`, `removerReferencia` e `altura`. A busca, a contagem e os percursos são herdados. Na classe `ArvoreBinaria`, apenas o nó interno e a raiz passaram de `private` para `protected`, permitindo o acesso pela subclasse.

O fator de balanceamento segue os slides: **altura da direita menos altura da esquerda**. Cada nó da AVL guarda sua altura. A inserção e a remoção são recursivas; na volta, atualizam as alturas e aplicam rotação à esquerda, à direita, esquerda-direita ou direita-esquerda quando necessário. Árvore vazia tem altura `-1` e uma folha tem altura `0`. Na remoção, um filho com fator zero também permite uma rotação simples.

Chaves iguais são permitidas. A inserção começa procurando à direita em caso de empate, mas as rotações podem deixar iguais dos dois lados. Por isso, `removerReferencia` procura a mesma instância nos dois lados quando encontra outra com chave igual. Isso evita remover a pessoa errada quando há contatos com o mesmo nome.

| Operação na AVL | Custo no pior caso |
| --- | --- |
| `adicionar`, `pesquisar`, `remover` por chave | O(log n) |
| `altura` | O(1), pela altura armazenada na raiz |
| `quantidadeNos`, percursos e `folhaMaisDistante` | O(n) |
| `removerReferencia` | O(n) quando há muitas chaves iguais; O(log n) com chaves únicas |

Esses custos consideram que cada comparação e a representação de cada valor têm custo constante. São a análise dos algoritmos; os experimentos da AVL ainda precisam ser feitos após sua integração ao aplicativo.

## Organização

```text
src/
  colecao/
    IColecao.java
    ListaEncadeada.java
    No.java
  arvorebinaria/
    ArvoreBinariaBase.java
    ArvoreBinaria.java
    ArvoreAVL.java
    NoArvore.java
  dominio/
    Contato.java
  app/
    CadastroContatos.java
    ProgramaContatos.java
  testes/
    TesteArvore.java
    TesteAVL.java
    TesteLista.java
    TesteCadastro.java
    TesteContadorLista.java
    GeradorDadosArvores.java
    BenchmarkArvores.java
    GeradorDadosContatos.java
    BenchmarkListas.java
compilar.ps1
README.md
```

`IColecao.java` e `ArvoreBinariaBase.java` são arquivos do professor, preservados como fornecidos no [repositório da disciplina](https://github.com/victoriocarvalho/tpa_bsi/tree/d2ce6e256f7dd02bc487d4a782b80895f658ed55/colecoes/src). As implementações ficam em `ArvoreBinaria.java` e `ArvoreAVL.java`.

## Compilar e executar os testes

É necessário um JDK 21. Na raiz do repositório, em PowerShell:

```powershell
.\compilar.ps1
java -cp bin testes.TesteArvore
java -ea -cp bin testes.TesteAVL
java -ea -cp bin testes.TesteLista
java -ea -cp bin testes.TesteCadastro
java -ea -cp bin testes.TesteContadorLista
```

O script usa `JAVA_HOME`, quando definido, ou o `javac` disponível no PATH. A execução também deve usar o Java 21: confira `java -version`. Se o PATH ainda apontar para um Java antigo, use `& "$env:JAVA_HOME\bin\java.exe" -ea -cp bin testes.TesteAVL` com `JAVA_HOME` definido para o JDK 21. Para compilar manualmente:

```powershell
$fontes = Get-ChildItem src -Recurse -Filter '*.java' | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -Xlint:all -d bin $fontes
java -cp bin testes.TesteArvore
```

Os testes verificam árvores vazias, percursos, remoção de folhas e raízes, remoção com um ou dois filhos, sucessor com filho, nomes iguais, comparadores diferentes e cadeia com 10.000 nós. Também comparam 10.000 operações aleatórias com um modelo de referência, incluindo chaves repetidas. Ao terminar, imprimem `OK` com a quantidade de verificações.

`TesteAVL` verifica as quatro rotações na inserção e na remoção, incluindo o caso de filho com fator zero. Confere os 14.400 pares possíveis de ordens de inserção e remoção de cinco chaves, 6.000 operações aleatórias com repetições, dois índices com 255 contatos de mesmo nome e 2.000 operações por referência. Também executa 100.000 inserções crescentes e 100.000 remoções decrescentes. A inspeção dos nós recalcula as alturas para conferir o balanceamento e os valores armazenados, sem depender apenas de `altura()`.

## Usar a biblioteca no aplicativo

As variáveis do aplicativo podem continuar usando `IColecao`. A escolha da estrutura muda a instanciação:

```java
IColecao<Contato> porNome =
    new ArvoreBinaria<>(new Contato.ComparadorPorNome());
IColecao<Contato> porTelefone =
    new ArvoreBinaria<>(new Contato.ComparadorPorTelefone());
```

Esse trecho usa a classe `Contato` e os comparadores do trabalho de listas, já disponíveis em `src/dominio`. Essa instanciação já é usada na opção 3 do aplicativo.

Para contatos com nomes iguais, a biblioteca também oferece `removerReferencia(contato)`. Esse método remove a mesma instância localizada pelo índice de telefone, evitando excluir outra pessoa com o mesmo nome. Ao sincronizar as duas árvores, o aplicativo também precisa liberar o telefone no conjunto usado para validar duplicatas.

Para os experimentos, `folhaMaisDistante()` retorna uma folha no nível mais profundo; em empate, retorna a última no percurso em nível. `profundidade(valor)` retorna o nível da primeira ocorrência da chave, ou `-1` quando ela não existe. A escolha do alvo e a medição da altura devem ficar fora do intervalo cronometrado de busca ou remoção.

### Integração da opção 4 — parte do Matheus

A biblioteca pode ser instanciada da mesma forma:

```java
import arvorebinaria.ArvoreAVL;

// No case 4 de CadastroContatos:
porNome = new ArvoreAVL<>(new Contato.ComparadorPorNome());
porTelefone = new ArvoreAVL<>(new Contato.ComparadorPorTelefone());
```

Para integrar ao aplicativo:

1. Em `ProgramaContatos`, apresentar a opção `4 — Árvore AVL` e aceitar valores de 1 a 4 na escolha da estrutura.
2. Em `CadastroContatos`, aceitar o tipo 4 e criar os dois índices com `ArvoreAVL` no novo `case 4`. Manter as opções anteriores e o construtor com `boolean`.
3. Em `removerPorNome`, atender `tipoEstrutura == 3 || tipoEstrutura == 4` no ramo das árvores. O cast para `ArvoreBinaria<Contato>` pode continuar: a AVL é sua subclasse e executa sua própria remoção por referência. Manter a liberação do telefone no `HashSet` após a remoção.
4. Ampliar os testes do cadastro para a opção 4: inclusão, busca, edição de nome, telefone duplicado, remoção entre nomes iguais e novo cadastro com o telefone removido. Conferir que as opções 1, 2 e 3 continuam funcionando.

Os métodos `folhaMaisDistante` e `profundidade` também estão disponíveis na AVL. O benchmark deve escolher a folha mais distante no índice de telefone, buscar o mesmo contato por nome e removê-lo por telefone, usando os arquivos crescentes do experimento anterior, conforme o item 7.

## Relatório e experimentos

O relatório parcial está em `relatorio/Relatorio_Parcial_Trabalho2_Arvores.pdf` e cobre as três primeiras seções, sobre árvores sem balanceamento automático. `GeradorDadosArvores` gera os oito arquivos usados para os quatro tamanhos e os dois formatos; `BenchmarkArvores` coleta três repetições por cenário com medições documentadas na pasta `dados`. O PDF e essas medições ainda não incluem a AVL.

# Trabalho 2 - Árvores binárias

Técnicas de Programação Avançada - IFES, Campus Serra.

Esta primeira contribuição contém o **item 1: desenvolvimento da biblioteca de árvores binárias**. Ela serve de base para a entrega parcial com as três primeiras seções do relatório sobre árvores sem balanceamento automático.

Grupo: Bernardo Simão Rosa, Matheus Abreu e Levi Monteiro.

## Base do aplicativo para a parte 2

Os arquivos de listas e contatos foram trazidos do [repositório entregue no trabalho 1](https://github.com/bs20242/tpa-trabalho1-etapa1/tree/c2061d78694cb72b3caccb67165f2adb1cc9ffa9/src), no commit `c2061d7`. Estão neste mesmo projeto para que o aplicativo possa usar listas ou árvores após a adaptação da parte 2.

- `src/colecao/ListaEncadeada.java` e `No.java`: listas ordenadas e não ordenadas.
- `src/dominio/Contato.java`: contato e comparadores por nome e telefone.
- `src/app/CadastroContatos.java`: cadastro, carga, validação e sincronização dos índices.
- `src/app/ProgramaContatos.java`: aplicativo original de contatos.
- `src/testes/TesteLista.java`, `TesteCadastro.java`, `GeradorDadosContatos.java` e `BenchmarkListas.java`: testes e ferramentas do trabalho anterior.

O aplicativo trazido do trabalho 1 ainda escolhe somente entre listas ordenadas e não ordenadas. A parte 2 deve acrescentar a escolha por árvore e adaptar a criação das duas coleções. Os métodos auxiliares de `CadastroContatos` que fazem conversão para `ListaEncadeada` também precisam aceitar árvores, especialmente a remoção por referência quando existem nomes iguais.

Depois de compilar, o aplicativo original pode ser executado com:

```powershell
java -cp bin app.ProgramaContatos
```

## O que está implementado

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

Os percursos usam laços, pilha ou fila explícita para funcionar também em árvores degeneradas profundas. A biblioteca não realiza rotações; a ordem da entrada determina o formato da árvore. Valores nulos não são inseridos. Chaves iguais são permitidas, e a busca ou remoção por chave atua sobre uma ocorrência.

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
    NoArvore.java
  dominio/
    Contato.java
  app/
    CadastroContatos.java
    ProgramaContatos.java
  testes/
    TesteArvore.java
    TesteLista.java
    TesteCadastro.java
    GeradorDadosContatos.java
    BenchmarkListas.java
compilar.ps1
README.md
```

`IColecao.java` e `ArvoreBinariaBase.java` são arquivos do professor, preservados como fornecidos no [repositório da disciplina](https://github.com/victoriocarvalho/tpa_bsi/tree/d2ce6e256f7dd02bc487d4a782b80895f658ed55/colecoes/src). O endereço citado no enunciado, `tpa_2026_1`, redireciona para esse repositório. A implementação específica fica em `ArvoreBinaria.java`.

## Compilar e executar os testes

É necessário um JDK 21. Na raiz do repositório, em PowerShell:

```powershell
.\compilar.ps1
java -cp bin testes.TesteArvore
java -ea -cp bin testes.TesteLista
java -ea -cp bin testes.TesteCadastro
```

O script usa `JAVA_HOME`, quando definido, ou o `javac` disponível no PATH. Para compilar manualmente:

```powershell
$fontes = Get-ChildItem src -Recurse -Filter '*.java' | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -Xlint:all -d bin $fontes
java -cp bin testes.TesteArvore
```

Os testes verificam árvores vazias, percursos, remoção de folhas e raízes, remoção com um ou dois filhos, sucessor com filho, nomes iguais, comparadores diferentes e cadeia com 10.000 nós. Também comparam 10.000 operações aleatórias com um modelo de referência, incluindo chaves repetidas. Ao terminar, imprimem `OK` com a quantidade de verificações.

## Usar a biblioteca no aplicativo

As variáveis do aplicativo podem continuar usando `IColecao`. A escolha da estrutura muda a instanciação:

```java
IColecao<Contato> porNome =
    new ArvoreBinaria<>(new Contato.ComparadorPorNome());
IColecao<Contato> porTelefone =
    new ArvoreBinaria<>(new Contato.ComparadorPorTelefone());
```

Esse trecho usa a classe `Contato` e os comparadores do trabalho de listas, já disponíveis em `src/dominio`. Ele mostra a instanciação que deve ser integrada à escolha de estrutura no aplicativo.

Para contatos com nomes iguais, a biblioteca também oferece `removerReferencia(contato)`. Esse método remove a mesma instância localizada pelo índice de telefone, evitando excluir outra pessoa com o mesmo nome. Ao sincronizar as duas árvores, o aplicativo também precisa liberar o telefone no conjunto usado para validar duplicatas.

Para os experimentos, `folhaMaisDistante()` retorna uma folha no nível mais profundo; em empate, retorna a última no percurso em nível. `profundidade(valor)` retorna o nível da primeira ocorrência da chave, ou `-1` quando ela não existe. A escolha do alvo e a medição da altura devem ficar fora do intervalo cronometrado de busca ou remoção.

## Acrescentar as próximas partes

Os próximos commits podem adaptar o aplicativo, integrar os programas de medição das árvores e acrescentar o relatório parcial. Usem os pacotes existentes para manter a compatibilidade com `IColecao` e `ArvoreBinariaBase`. Para contribuir, clonem o repositório, façam suas alterações e enviem uma branch com pull request; isso permite revisar cada contribuição antes de juntá-la à principal.

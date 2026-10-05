$ErrorActionPreference = 'Stop'

$compilador = if ($env:JAVA_HOME) {
    Join-Path $env:JAVA_HOME 'bin\javac.exe'
} else {
    (Get-Command javac -ErrorAction Stop).Source
}

$saida = Join-Path $PSScriptRoot 'bin'
$fontes = Get-ChildItem (Join-Path $PSScriptRoot 'src') -Recurse -Filter '*.java' |
    ForEach-Object { $_.FullName }

New-Item -ItemType Directory -Force -Path $saida | Out-Null
& $compilador -encoding UTF-8 -Xlint:all -d $saida $fontes
if ($LASTEXITCODE -ne 0) {
    throw 'A compilacao falhou. Confira as mensagens do javac.'
}
Write-Host 'Compilacao concluida. Execute: java -cp bin testes.TesteArvore'

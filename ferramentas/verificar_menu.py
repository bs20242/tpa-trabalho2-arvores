from pathlib import Path
import subprocess, os, json, hashlib, csv
root=Path(__file__).resolve().parents[1]
java=str(Path(os.environ['JAVA_HOME'])/'bin/java.exe') if os.environ.get('JAVA_HOME') else 'java'
roteiro='2\nAna\n111\n2\nAna\n222\n2\nOutra\n111\n5\n222\n3\nAna\n2\nBia\n222\n6\nAna\nClara\n333\n4\n111\n4\n333\n3\nBia\n7\n'
logs=root/'dados/logs'; logs.mkdir(exist_ok=True)
for tipo in [1,2,3]:
    p=subprocess.run([java,'-cp','bin','app.ProgramaContatos'],input=str(tipo)+'\n'+roteiro,text=True,capture_output=True,cwd=root)
    assert p.returncode==0,p.stderr
    for trecho in ['Contato encontrado: Ana - 111','Contato encontrado: Clara - 333','Contato encontrado: Bia - 222','Quantidade total atual de contatos: 2','Ja existe um contato cadastrado','Contato nao existe']:
        assert trecho in p.stdout,(tipo,trecho)
    (logs/f'menu_{tipo}.txt').write_text(p.stdout,encoding='utf-8')
    carga=f'{tipo}\n1\ndados/perfeita_2047.txt\n4\n00000002047\n5\n00000002047\n7\n'
    p=subprocess.run([java,'-cp','bin','app.ProgramaContatos'],input=carga,text=True,capture_output=True,cwd=root)
    assert p.returncode==0,p.stderr
    assert '2047 contatos carregados com sucesso' in p.stdout
    assert 'Quantidade total atual de contatos: 2046' in p.stdout
    (logs/f'carga_{tipo}.txt').write_text(p.stdout,encoding='utf-8')
    print(f'OK: menu e carga no modo {tipo}')
manifesto={}
for n in [2047,4095,8191,16383]:
    for formato in ['perfeita','degenerada']:
        p=root/f'dados/{formato}_{n}.txt'
        linhas=p.read_text(encoding='utf-8').splitlines()
        chaves=[linha.split(';')[1] for linha in linhas]
        assert len(chaves)==n and len(set(chaves))==n
        if formato=='degenerada': assert chaves==sorted(chaves)
        manifesto[p.name]={'registros':n,'sha256':hashlib.sha256(p.read_bytes()).hexdigest()}
rows=list(csv.DictReader((root/'dados/resultados_arvores.csv').open(encoding='utf-8-sig')))
assert len(rows)==24
for row in rows:
    n=int(row['n']); h=(n+1).bit_length()-2 if row['formato']=='perfeita' else n-1
    assert all(int(row[key])==h for key in ['alturaTelefone','alturaNome','profundidadeTelefone','profundidadeNome'])
(root/'dados/manifesto.json').write_text(json.dumps(manifesto,indent=2),encoding='utf-8')
print('OK: oito entradas e topologia das 24 execucoes verificadas.')

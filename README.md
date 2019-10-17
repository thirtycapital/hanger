# Hanger [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
### O Hanger é uma ferramenta gráfica de orquestração de processos e qualidade de dados, responsável pela execução dos processos de ETL, controle de dependências e validação dos dados.

## Instalação

##### REQUISITOS

- Tomcat 8 +
- MySQL 5.7 +
- Java 8 +
- Jenkins ( [https://jenkins.io/](https://jenkins.io/) )
- Jenkins Notification Plugin ( [https://wiki.jenkins.io/display/JENKINS/Notification+Plugin](https://wiki.jenkins.io/display/JENKINS/Notification+Plugin) )

Caso haja problemas de permissão entre Hanger e o Jenkins (Erro 403), verifique se a opção Prevenir site contra invasões está marcada e a desmarque:

Acesse o Jenkins e vá no menu Gerenciar Jenkins
Clique no menu em Configurar segurança global
Na seção CSRF Protection, desmarque a opção Prevenir site contra invasões.

##### CONSTRUÇÃO
Utilizando o Maven:

- Acesse o diretório no qual os fontes do Hanger se localizam.
- Digite o comando mvn package.
- O arquivo hanger.war será gerado no subdiretório target. 

##### CONFIGURAÇÃO

- Crie o arquivo ~/.hanger/hanger.properties, com o seguinte conteúdo:
 
```
# Hanger MySQL
spring.datasource.url=jdbc:mysql://<host>:<port>/<db>
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.username=<user>
spring.datasource.password=<password>
spring.datasource.validationQuery=SELECT 1
 
# Hanger Secret Key
hanger.encrypt.key=<Any key>
 
# Hanger Anonymous Access
hanger.anonymous.access=true
 
# Slack bot token
slackBotToken=<Token of your bot>
webHookUrl=<WebHook URL>
 
# Log
logging.level.root=INFO
logging.level.org.springframework.web=WARN
logging.level.org.hibernate=INFO
logging.level.org.hibernate.SQL=WARN
 
# Timezone
spring.jackson.time-zone=America/Sao_Paulo
```

Para habilitar a integração com slack, siga os documentos:

- Habilitar Slack bot e pegar token.
- Habilitar Slack WebHook e pegar URL.

##### DEPLOY
Utilizando o Apache Tomcat:

- Copie o arquivo hanger.war para o diretório webapps do Apache Tomcat.

## Monitor
O Monitor é onde a carga de trabalho do Hanger é monitorada. O dashboard é organizado da seguinte forma:

- Na parte superior são exibidos todos os subjects disponíveis e uma opção de filtro:
	- Na aba de assuntos:
		- Clique sob o nome do subject desejado e será exibida a lista de jobs do subject contendo as seguintes informações:
			- Status: Status do job.
			- Server: Instância do Jenkins no qual o job é executado.
			- Job: Nome do job. 
			- Warning: Número de warning na cadeia do job. 
			- Link: Link para redirecionamento para o job no Jenkins. 
			- Check-Up: Link para redirecionamento para lista de execuções do checkup.
			- Scope: Escopo de execução do job.
			- Update: Data de execução do job. 
		- Clique no nome do job para ser direcionado para a cadeia de dependências do job e será exibida a cadeia de dependências do job, denominada flow. 
		- Clique no número exibido na coluna warning e será exibida a lista com todos os jobs com problema e seus respectivos status. 
		- Clique na opção Delete para remover um job do subject.
		- Clique na opção Build para executar um job. 
		- Clique no botão Approval para aprovar ou recusar um job com status BLOCKED ou UNHEALTHY.
			- Analise o log de validação.
			- Informe a justificativa para sua decisão.
			- Clique no botão Approve ou Disapprove de acordo com a decisão tomada. 
		- Clique no botão Add Job para adicionar um job em um subject. 
- Na guia HOME é exibido o progresso de cada subject e a contagem de jobs por status:
	- No gráfico: 
		- Cinza: Representa o percentual de jobs ainda não executados. 
		- Verde: Representa o percentual de jobs executados com sucesso. 
		- Vermelho: Representa o percentual de jobs com com falha ou com problema de validação dos dados.
		- Laranja: Representa o percentual de jobs com alertas na cadeia de dependência. 
	- Na tabela:
		- Jobs: Total de jobs em um subjects
		- Success: Total de jobs executados com sucesso.
		- Waiting: Total de jobs ainda não executados. 
		- Building: Total de jobs prontos para serem executado mas ainda aguardando na fila. 
		- Running: Total de jobs sendo executados.
		- Warning: Total de jobs com alertas na cadeia de dependência. 
		- Failure: Total de jobs com falha ou com problema de validação dos dados. 

## Search

O search é o caminho mais rápido para encontrar e ter acesso as informações de status de um job no Hanger. 

- Clique na opção Search.
- Será exibida a tela de pesquisa, contendo um campo de texto e um botão com o desenho de uma lupa. 
- Digite o conteúdo a ser pesquisado e clique no botão.

O conteúdo informado no campo de busca pode ser uma parte do nome, nome completo ou o alias do job.

## Flow

O flow é a representação gráfica da cadeia de dependências de um job. 

Cada job representado no flow apresenta uma imagem representando o status e as seguintes informações:

- Nome do job.
- Link para o job no Jenkins.
- Link para o console do Jenkins.
- Link para a página de resultados da validação do job. 
	- Clique no link CHECKUP e será exibida a página com o resultado dos últimos dez checkups do job.
	- Clique no botão View para visualizar a configuração do job.
	- Clique no botão Flow para ser redirecionado novamente para a cadeia de dependência do job.
- Instância do Jenkins na qual o job é executado.  
- Data de execução do job
- Escopo do job
Clique com o botão direito sobre o status do job e será exibida uma lista suspensa com as seguintes opções:
- Flow: Atalho para o flow do job selecionado. 
- Propagation: Atalho para o propagation do job selecionado. 
- Build: Permite executar o job selecionado. 
- Build Mesh: Permite executar toda a cadeia do job selecionado. 
- Build history: Exibe uma lista com o histórico completo de execuções do job selecionado, a lista contem os campos:
	- Status: Status da execução.
	- Start: Data e hora inicial da execução.
	- Finish: Data e hora final da execução.
	- Duration in minutes: Duração da execução em minutos.
	- Efficiency: Exibe a porcentagem que a execução ficou em fila e o tempo real.

##### ZOOM OUT
Permite reduzir o zoom do flow. 

##### ZOOM IN
Permite aumentar o zoom do flow.

##### EXPAND ALL
Permite expandir toda a cadeia de dependência de um job.

##### COLLPASE ALL
Permite fechar toda a cadeia de dependência de um job.

##### APPROVAL
Redireciona para a página de aprovação do job.

Esta opção só estará disponível caso o status do job seja BLOCKED ou UNHEALTHY.

##### GLOSSARY
Exibe o glossário de todos os possíveis status de um job, contendo:

- Icon: Ícone de status. 
- Name: Nome do status. 
- Description: Descrição do status.

## Servers
Servers são as instâncias de Jenkins que serão gerenciadas pelo Hanger. 

##### CONNECT
Permite testar conexão uma instância do Jenkins cadastrada.

- No menu lateral, acesse a opção Server.
- Selecione o servidor desejado e clique no botão Connect.
- Uma frase aparecerá no topo da tela informando se o servidor está ou não conectado.

##### IMPORT
Permite importar todos os jobs cadastrados no Jenkins.

- No menu lateral, acesse a opção Server.
- Selecione o servidor desejado e clique no botão Import, será exibido o modal Import jobs no qual será apresentado uma mensagem de confirmação.
- Caso deseje realmente importar todos os jobs do Jenkins, clique em Yes.
- Após a importação de todos os jobs, uma frase aparecerá no topo da tela informando que os jobs foram sincronizados.

##### ADD SERVER
Permite adicionar uma nova instância do Jenkins.

No menu lateral, acesse a opção Server.
- Clique no botão Add Server, representado pelo ícone +.
- Defina o nome da conexão no campo Name.
- Informe a URL completa do servidor, com porta e host no campo URL.
- Informe o usuário administrador do servidor no campo Username.
- Informe a senha ou token do usuário no campo Token.
- Clique no botão Save.

##### EDIT
Permite alterar um servidor.

##### DELETE
Permite exluir um servidor.

## Connections
Connections são as conexões com os bancos de dados que serão utilizados no processo de validação de dados.

##### ADD CONNECTION
Permite adicionar uma nova conexão.

- No menu lateral, acesse a opção Connection.
- Clique no botão Add Connection, representado pelo ícone +.
- Defina o nome da conexão no campo Name.
- Selecione qual o banco de dados desejado no campo Database.
- Informe a URL para conexão com o JDBC, deve conter host e banco de dados.
- Informe o usuário do banco de dados no campo Username.
- Informe a senha do usuário no campo Password.
- Clique no botão Save.


Os bancos de dados suportados e as respectivas JDBC Urls são as seguintes:

MYSQL: jdbc:mysql://<url>:<porta>/<database>
POSTGRES: jdbc:postgresql://<url>:<porta>/<database>
ATHENA: jdbc:awsathena://AwsRegion=<region>;S3OutputLocation=<bucket>


##### TEST CONNECTIONS
Permite validar o estado de todas as conexões cadastradas.

>Dependendo do número de conexões este processo pode ser demorado. 

##### CONNECT
Permite testar o estado de uma conexão específica. 

##### EDIT 
Permite alterar uma conexão.

##### DELETE
Permite excluir uma conexão.

##### TABLE
Permite explorar as tabelas de uma conexão.


## Subjects
Subjects são agrupadores utilizados para a organização e o acompanhamento sumarizado da execução dos jobs. 

##### ADD SUBJECT
Permite adicionar um novo assunto.

- No menu lateral, acesse a opção Subjects.
- Clique no botão Add Subject, representado pelo ícone +.
- Defina o nome do assunto no campo Subject.
- Defina a descrição do assunto no campo Description.
- Caso deseje receber notificações dos jobs pertencentes deste subject no slack, marque a opção Slack notification.
- Para definir o canal no qual as notificações serão enviadas, clique no botão Add channel. Será exibido o modal Slack channels no qual será possível selecionar um ou mais canais do Slack nos quais as notificações devem ser enviadas.

Caso nenhum canal seja selecionado no modal Slack channels, o canal configurado na guia Configuration será usado para notificação deste job.

- Para definir se o assunto deve ser visto por todos os usuários no monitor, marque a opção Mandatory. 
- Clique no botão Save.

##### SUBSCRIPTION
Permite selecionar qual subject será exibido no monitor quando o usuário estiver logado.

Alguns subjects são obrigatórios e não podem ser removidos do monitor, estes subjects são exibidos com o checkbox de subscrição desabilitado.


##### EDIT 
Permite editar um assunto. 

##### DELETE
Permite excluir um assunto.

## Jobs
Jobs são referencias para jobs do Jenkins.

##### ADD JOB
Permite adicionar um novo job. 
- No menu lateral, acesse a opção Job.
- Clique no botão Add Job, representado pelo ícone +.
- Selecione a instância desejada do Jenkins no combo Server.

	Para adicionar um novo job é necessário ter ao menos um servidor cadastrado.

- Clique no botão Job List para que todos os jobs da instância selecionada do Jenkins sejam listados. 
- Selecione o job desejado no combo Name. 
- Caso deseje definir um nome sugestivo para o job, informe o no campo Alias. O alias, com o sufixo [alias], substituirá o nome do job nas principais funcionalidades do Hanger.
- Defina a descrição do job no campo Description.
- Caso o job possa ser executado mais de uma vez ao dia, marque o checkbox Rebuild along the day. Por padrão, um job pode ser executado apenas uma vez ao dia. Quando esta opção estiver selecionada, o job será executado a primeira vez quando todas as dependências forem atendidas e voltará a ser executado sempre que qualquer uma das dependências for executada com sucesso no decorrer do dia. Caso necessite que o job somente seja reexecutado quando todas as dependências forem atendidas novamente, selecione quais dependências serão blockers na lista de parents.
- Caso deseje definir um intervalo mínimo para que o job seja reexecutado, digite a quantidade de minutos no campo Rebuild interval in minutes.
- Defina o conteúdo do campo Eagerness do job entre 0 e 12 horas. Para que uma dependência de um job seja considerada atendida, ela precisa ser executada com sucesso ao menos uma vez no dia. Caso alguma dependência precise ser executada antes da meia noite, o campo Eagerness deve ser preenchido com o número de horas, antes da meia noite, em que caso o job seja executado com sucesso, a dependência seja considerada como atendida. 
- Caso deseje receber notificações de execuções do job, marque a opção Slack notification.
- Para definir o canal no qual as notificações serão enviadas, clique no botão Add channel. Será exibido o modal Slack channels no qual será possível selecionar um ou mais canais do Slack nos quais as notificações devem ser enviadas.

	Caso nenhum canal seja selecionado no modal Slack channels, o canal configurado na guia Configuration será usado para notificação deste job.

- Caso deseje classificar o job dentro de um ou mais assuntos, clique no botão Add subject. Será exibido o modal Subject no qual será possível selecionar um ou mais subjects para o job. 
	- Clique no botão Add.
	- Será criada uma nova seção denominada Subjects, onde será possível gerenciar os subjects do job.
	- Para remover um subject, clique no botão Remove. 
Caso o job tenha uma ou mais dependências, clique no botão Add Parent, para adicionar jobs da mesma instância do Jenkins como dependência, ou clique na seta ao lado do botão para selecionar uma instância específica do Jenkins. Será exibido o modal Jenkins Server, no qual será possível selecionar todos os jobs que serão dependência do job que está sendo criado. 

- Caso os jobs do Jenkins possuam upstream jobs, marque a opção Import Jenkins upstream project? para que a relação de dependências representadas pelo upstream jobs do Jenkins seja replicada para o mecanismo de dependências do Hanger. 
	- Clique no botão Add.
	- Será criada uma nova seção denominada Parent, onde será possível gerenciar os parents do job.
	- Para remover uma dependência, clique no botão Remove. 
	- Para definir escopo de uma dependência, selecione entre uma das opções disponíveis no combo apresentado na coluna Scope. 


Uma dependência pode ter os seguintes escopos:

FULL : Identifica que todas as dependências com escopo FULL ou PARTIAL precisam ser atendidas para que o job seja executado. 

PARTIAL: Identifica que todas as dependências com escopo PARTIAL precisam ser atendidas para que o job seja executado, o job será executado com escopo parcial pois nem todas suas dependências foram atendidas. 

OPTIONAL: Identifica que a dependência é opcional, ou seja, sendo atendida ou não o job será executado normalmente. Caso todas as dependências do job sejam OPTIONAL, o job será executado assim que a primeira dependência for atendida. 


- Caso seja necessário realizar a validação dos dados resultantes da execução de um job, especificamente em um processo de ETL, clique no botão Add checkup. Será criada uma nova seção denominada Checkups, na qual será possível definir uma instrução SQL para validação. Para criar um checkup:
	- Informe a descrição do checkup no campo Description.
	- Selecione em qual conexão a validação será executada no combo Connection.
	- Defina em qual escopo a validação será realizada, selecionando uma das opções do combo Scope. 
	- Defina a instrução SQL de validação no campo SQL Select Statement, o resultset deve retornar apenas um valor inteiro. 
	- Defina o teste que será feito para comparação entre o resultset e o threshold, selecionando uma das opções disponíveis no combo Test.
	- Defina o resultado esperado no campo Threshold.

	> É possível referenciar o resultado de outro ckeckup no campo threshold, utilizando a seguinte sintaxe: ${<checkup ID>}

	- Defina a ação que será executada caso a validação falhe, selecionando uma das opções disponíveis no combo On fail.

	Em caso de falha de um checkup, as seguintes ações podem ser executadas:

	NOTHING: Apenas bloqueia a execução da cadeia. 

	REBUILD: Caso seja uma pós validação, reexecuta o job. Caso contrário, tem o mesmo efeito de NOTHING. 

	REBUILD_MESH: Caso seja uma pós validação, reexecuta todas as dependências do job e as dependências de cada dependência. Caso contrário, tem o mesmo efeito de NOTHING. 

	REBUILD_TRIGGER:  Caso seja uma pós validação, reexecuta dependências específicas selecionadas clicando no botão Add Trigger. Caso contrário, tem o mesmo efeito de NOTHING. 

	LOG_AND_CONTINUE: Grava o resultado da execução do checkup e notifica os canais do Slack selecionados no job, mas não bloqueia a execução da cadeia. 

	- Caso a ação escolhida seja REBUILD_TRIGGER, clique no botão Add Trigger. Será exibido o modal Trigger no qual será possível selecionar quais dependências devem ser reexecutadas.
	- Caso seja necessário executar algum comando quando um checkup falhar, clique no botão Add Command. Será criada um nova seção,Script on failure:
		- Selecione o tipo de ação que será executada entre SHELL ou SQL.
		- Informe no campo Script a ação a ser executada.
		- Para remover uma ação, clique no botão Remove. 
	- Um checkup pode ser executado como uma pré-validação ou uma pós validação. Pré-validações ocorrem quando todas as dependências foram atendidas e imediatamente antes da execução do job, enquanto pós-validações ocorrem após a execução do job. Para definir o tipo de um checkup, clique no toggle e escolha entre Pre-validation e Post-validation. 
- Defina o número de tentativa de execução do job no campo Retries. 
- Caso necessário, selecione um aprovador para o checkup no combo Approver.
- Clique no botão Save.

##### BUILD 
Permite executar um job.  

##### SEE
Permite visualizar as configurações do job. Na página de visualização as seguintes ações estão disponíveis:

- Clique no botão Add Job, representado pelo ícone +, para adicionar um novo job. 
- Clique no botão Edit para editar o job. 
- Clique no botão Delete para deletar o job.
- Clique no botão Propagation para analisar quais jobs são impactados pelo job na cadeia de dependências. 
- Clique no botão Flow para analisar quais jobs fazem parte da cadeia de dependência do job. 
- Clique no botão Build para executar o job.
- Clique no botão Build mesh para executar toda a cadeia de dependência do job. 

##### EDIT 
Permite editar as configurações de um job.

##### DELETE
Permite excluir um job.

##### HEATMAP
Permite a distribuição do trabalho em um período de tempo específico.
- Selecione a data que será analisada no campo Date.
- Selecione o intervalo que será analisado no campo Interval.
- Clique no botão Filter, será exibido o modal Job Filter no qual será possível escolher quais jobs serão analisados. 

##### GANTT
Permite analisar a carga de trabalho durante um intervalo de tempo.
- Selecione a data de início da análise no campo From.
- Selecione a data de fim da análise no campo To.
- Selecione a fase que será analisada no campo Phase.
- Clique no botão Filter. 

##### REFRESH
Permite atualizar a lista de jobs do Hanger. Por questões de performance o Hanger mantém o máximo possível de informação em memória, sincronizando os dados com o banco de dados somente quando necessário; por meio da opção Refresh é possível forçar a sincronização imediata destes dados.

## Log
Logs são registros das atividades dos usuários no sistema e expões as seguintes informações:
- Date: Data da ação.
- Entity: Tipo de entidade na qual a ação foi executada.
- Name: Nome da entidade na qual a ação foi executada.
- User: Usuário que executou a ação.
- Event: Ação executada pelo usuário.

## User
Users são os usuários do Hanger. 

##### ADD USER
Adiciona um novo usuário.
- No menu lateral, acesse a opção User.
- Clique no botão Add User, representado pelo ícone +.
- Defina o e-mail do usuário no campo E-mail.
- Defina o nome de usuário do sistema no campo Username.
- Defina o primeiro nome do usuário no campo First Name.
- Defina o sobrenome do usuário no campo Last Name.
- Selecione o papel do usuário no campo Role.

	HERO: É o administrador integral do sistema, pode efetuar todas as operações disponíveis.

	ADMIN: Este usuário pode efetuar todas as operações dentro do sistema exceto criar, editar ou excluir usuários.

	USER: Este usuário tem permissão de efetuar build em um job e aprovar jobs que estão em seu nome na seção Approval.

- É possível definir se este usuário estará ou não ativo no sistema através do campo Enabled.
- Clique no botão Save.

##### EDIT
Permite alterar um usuário e, para HERO, redefinir a senha de outros usuários: 
- Clique no botão Edit.
- Clique no botão Reset password.
- Um e-mail será enviado ao usuário com a nova senha.

##### DELETE
Permite excluir um usuário.

> Caso o usuário a ser excluído seja aprovador de algum job, será necessário trocar o usuário aprovador no momento da exclusão deste usuário.

##### CHANGE PASSWORD
Permite ao usuário alterar a própria senha.

## Configuration
Configuration contém as configurações globais do Hanger. 

- No menu lateral, acesse a opção Configuration
- Digite o servidor utilizado para mandar e-mails no campo Host.
- Digite a porta do servidor de e-mail no campo Port.
- Digite o endereço de e-mail no campo Address.
- Digite a senha no campo Password.
- No campo Log Retention é possível definir em dias a limpeza do log de validação de dados e de aprovações. 
- Defina qual é o canal padrão utilizado pelo Slack no campo Default channel.
- Clique no botão Upload Logo para alterar o logo da ferramenta por qualquer arquivo do tipo imagem.
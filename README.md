# Hanger [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
### Hanger is a graphical tool for process orchestration and data quality, responsible for the execution of[ETL](https://pt.wikipedia.org/wiki/Extract,_transform,_load) processes, dependency control and data validation.

## Instalação

##### REQUIREMENTS

- Tomcat 8 +
- MySQL 5.7 +
- Java 8 +
- Jenkins ( [https://jenkins.io/](https://jenkins.io/) )
- Jenkins Notification Plugin ( [https://wiki.jenkins.io/display/JENKINS/Notification+Plugin](https://wiki.jenkins.io/display/JENKINS/Notification+Plugin) )

##### CONSTRUCTION
Utilizando o [Maven](https://maven.apache.org/):

- Acesse o diretório no qual os fontes do Hanger se localizam.
- Digite o comando **mvn package**.
- O arquivo hanger.war será gerado no subdiretório *target*. 

##### CONFIGURATION

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

> Para habilitar a integração com slack, siga os documentos:
> - Habilitar Slack bot e pegar token.
> - Habilitar Slack WebHook e pegar URL.

##### DEPLOY
Utilizando o [Apache Tomcat](http://tomcat.apache.org/):

- Copie o arquivo hanger.war para o diretório webapps do Apache Tomcat.

##### TROUBLESHOOTING

> Caso haja problema para executar um job pelo Hanger, verifique se o campo *URL do Jenkins* está definida corretamente no Jenkins:
> - Acesse o Jenkins e vá no menu Gerenciar Jenkins
> - Clique no menu em Configurar o sistema
> - Localize o campo *URL do Jenkins* na seção *Jenkins Location* e defina o valor corretamente. 

> Caso haja problemas de permissão entre Hanger e o Jenkins (Erro 403), verifique se a opção *Prevenir site contra invasões* está marcada e a desmarque:
> - Acesse o Jenkins e vá no menu Gerenciar Jenkins
> - Clique no menu em Configurar segurança global
> - Na seção CSRF Protection, desmarque a opção *Prevenir site contra invasões*.

## Monitor
*Monitor* is where the freight of Hanger is observed. The *dashboard* is organized as follows:

- On top are shown all the available *subjects* and a filter option:
	- At the left side of the *subject* name is displayed an icon that indcates that the logged user is subscribed. However, the icon only appears if the *subject* is not mandatory. A *subject* is defined as mandatory if the user wants that the *subject* is visible to all other Hanger users. 
	- To the right of the subject's name, the total number of *jobs* contained in it is informed.
	- Na aba de assuntos:
		- Clique sob o nome do *subject* desejado e será exibida a lista de *jobs* do subject contendo as seguintes informações:
			- **Status:** Status do *job*.
			- **Server:** Instância do Jenkins no qual o *job* é executado.
			- **Job:** Nome do *job*. 
			- **Warning:** Número de warning na cadeia do *job*. 
			- **Link:** Link para redirecionamento para o *job* no Jenkins. 
			- **Check-Up:** Link para redirecionamento para lista de execuções do checkup.
			- **Scope:** Escopo de execução do *job*.
			- **Update:** Data de execução do *job*. 
		- Clique no nome do *job* para ser direcionado para a cadeia de dependências do *job* e será exibida a cadeia de dependências do *job*, denominada **flow**. 
		- Clique no número exibido na coluna warning e será exibida a lista com todos os *jobs* com problema e seus respectivos status. 
		- Clique na opção ***Delete*** para remover um *job* do *subject*.
		- Clique na opção ***Build*** para executar um *job*. 
		- Clique no botão ***Approval*** para aprovar ou recusar um *job* com status ***BLOCKED*** ou ***UNHEALTHY***.
			- Analise o log de validação.
			- Informe a justificativa para sua decisão.
			- Clique no botão *Approve* ou *Disapprove* de acordo com a decisão tomada. 
		- Clique no botão ***Add Job*** para adicionar um *job* em um *subject*. 
- Na guia **HOME** é exibido o progresso de cada *subject* e a contagem de *jobs* por status:
	- No gráfico: 
		- **Cinza:** Representa o percentual de *jobs* ainda não executados. 
		- **Verde:** Representa o percentual de *jobs* executados com sucesso. 
		- **Vermelho:** Representa o percentual de *jobs* com falha ou com problema de validação dos dados.
		- **Laranja:** Representa o percentual de *jobs* com alertas na cadeia de dependência. 
	- Na tabela:
		- **Success:** Total de *jobs* executados com sucesso.
		- **Waiting:** Total de *jobs* ainda não executados. 
		- **Building:** Total de *jobs* prontos para serem executados, mas ainda aguardando na fila. 
		- **Running:** Total de *jobs* sendo executados.
		- **Warning:** Total de *jobs* com alertas na cadeia de dependência. 
		- **Failure:** Total de *jobs* com falhas ou com problema de validação dos dados. 
		- **Checkup:** validação de health check de todos os checkups do *job*.
- Na guia **ALL** são exibidos todos os jobs cadastrados no Hanger e seus respectivos status.

## Search

*Search* is the faster way to find and to access *job* status informations on Hanger. To use it, read below:

- On the side menu, click on Search.
- A search screen and a button Search with a magnifying glass symbol will be shown.
- Write the content to be searched and click on the button, it can be part of the name, full name or the *job* alias.

## Flow

*Flow* is a graphical representation of a job's dependency chain.

Each *job* represented on flow has a  status and the follow information:

- *Job* name.
- Link to *job* on Jenkins.
- Link to Jenkins console.
- Link to the *job* validation results page. 
	- Click on ***CHECKUP*** link and the page with the result of the last ten job checkups will be displayed.
	- Click on ***View*** button to see the job configuration.
	- Click on ***Flow*** button to be redirected to job dependency chain.
- Jenkins instance in which the *job* is executed.
- *Job* execution date.
- *Job* scope.
- Click on *job* status with the right button and a list will be displayed with the following options:
	- **Flow:** Shortcut to the selected *job flow*.
	- **Propagation:** Shortcut to the selected *job propagation*. 	
	- **Build history:**  Shows a list with the complete execution history of the selected job. The list contains these fields:
		- **Start:** Start date and time of execution.
		- **Finish:** Final date and time of execution.
		- **Duration in minutes:** Execution duration in minutes.
		- **Efficiency:**  Shows the percentage that the execution was queued.
	- **Actions:**
		- **Build:** Allows to execute the selected job. 
		- **Build Mesh:** Allows to execute all he selected job chain.
		- **Parent:** Shows a list with all the Jenkins available servers, where it is possible to add one or more parents to the current job:	
			- Click on the chosen server.
			- A screen with all the available jobs in that server will be displayed.
			- It is possible to select the jobs on the list or simply type the name of the jobs on Jobs field (comma-separated).
			- Click on Add to conclude the action.
		- **Children:**  Shows a list with all the Jenkins available servers, where it is possible to add one or more children to the current job:
			- Click on the chosen server.
			- A screen with all the available jobs in that server will be displayed.
			- It is possible to select the jobs on the list or simply type the name of the jobs on Jobs field (comma-separated).
			- Click on Add to conclude the action.

		- **Disable:** Allows to disable/enable a job.

During the Flow view, the available options are:

##### ZOOM OUT
Zoom out the flow.

##### ZOOM IN
Zoom in the flow.

##### EXPAND ALL
Expand all the job dependency chain.

##### COLLPASE ALL
Close all the dependency chain of *job*.

##### APPROVAL
Redirect to job approbation page. However, this option only will be available if the *job* status is BLOCKED or UNHEALTHY.

##### GLOSSARY
Shows the glossary of all the possible status of a *job* with the following information:

- ***Name*:**  Status name. 
- ***Description*:** Status description.

## Server
***Servers*** are Jenkins instances that will be managed by Hanger. 

- To access the ***Server*** main page, on side menu, click on the ***Server*** option.
- All the servers will be displayed.
- If you want to edit a specific server, click on ***Edit*** button.

##### ADD SERVER
Allows to add a new Jenkins instance

- On ***Server*** side menu option, click on the ***down arrow*** and choose the ***Add server*** option.
- On ***Name*** field, define the server name.
- Inform the full server URL, with port and host on ***URL*** field.
- On ***Username*** field, inform the server administrator user.
- On ***Token*** field, inform either the password or the user [token](https://stackoverflow.com/questions/45466090/how-to-get-the-api-token-for-jenkins).
- Click on ***Save*** button to save.

##### IMPORT
This option allows to import all the jobs registered on Jenkins.

- On side menu, click on ***Server*** option.
- Select the chosen server and click on ***Import*** button.
- A confirmation message will be displayed. Click on ***Yes*** to confirm or click on ***No*** to cancel the operation.
- After the importation of all jobs, a message will be displayed on top informing that the jobs have been synced.

##### CONNECT
This option allows to test the connection of a registered Jenkins instance.

- On side menu, click on ***Server*** option.
- Select the server that you want to test and click on ***Connect*** button.
- A message will be displayed at the top, informing whether the server is connected or not.

##### EDIT
Allows to edit a *server*. This process is pretty similar to Add server.

- On side menu, click on ***Server***.
- Select the chosen server and click on ***Edit*** button.
- Fill in the information accordingly and, at the end, click on ***Save*** to save.

##### DELETE
Allows to remove a server.

- On ***Server*** main page, select a server that you want to remove and click on ***Delete*** button.

## Connection
*Connections* are the databases connections that can be used on data validation process and to do queries on Workbench.

- To access the ***Connection*** main page, on side menu, click on the ***Connection*** option.
- All the connections will be displayed.
- If you want to edit a specific connection, click on ***Edit*** button.

##### ADD CONNECTION
Allows to add a new connection.

- On ***Connection*** side menu option, click on the ***down arrow*** and choose the ***Add connection*** option.	
- On ***Name*** field, inform the connection name.
- Select the chosen database on ***Database*** field.
- On ***Class name*** field, inform the used class to execute the connection with a *Generic* database.
- On ***JDBC Url*** field, inform the URL to connect with JDBC. Must contain host and database.
- On ***Username*** field, inform the database user.
- On ***Password*** field, inform the user password.
- Click on ***Save*** button to save.

***Note:*** The supported databases and its JDBC Urls are:

    ***MYSQL:*** jdbc:mysql://<url>:<porta>/<database>
	***POSTGRES:*** jdbc:postgresql://<url>:<porta>/<database>
	***ATHENA:*** jdbc:awsathena://AwsRegion=<region>;S3OutputLocation=<bucket>
	***MSSQL:*** jdbc:sqlserver://<url>:<porta>;DataBaseName=<database>
	***HANA:*** jdbc:sap://<url>:<porta>/<database>
	***GENERIC:*** To use a generic connection, the URL must be put on ${CATALINA_HOME}/lib server directory and you have to inform the driver class name.

##### CHECK ALL CONNECTIONS
This option allows to validate the estate of all the registered connections. However, depending on the number of connections, this process can be time-consuming.

- On side menu, click on ***Connection*** option.
- At bottom, click on ***Check all connections***. 
- If all the connections are okay, a success message will be displayed.

##### CHECK
Allows to check a specific connection.

- On side menu, click on ***Connection*** option.
- Choose a connection to check and click on ***Check*** button.
- If the connection is okay, a success message will be displayed.

##### SCHEMA
This option allows to explore the catalogs and schemas of a connection.

- On side menu, click on ***Connection*** option.
- Select the chosen connection and click on ***Schema*** button.
- A table will be displayed with all the catalogs and schemas of that connection.
- Click on ***Table*** button to explore the chosen schema or catalog tables.
- Click on ***Column*** button to access the table metadata. Will be shown the primary key, index, fields and its types.

##### EDIT 
Allows to edit a *connection*. This process is pretty similar to Add connection.

- On side menu, click on ***Server***.
- Select the chosen connection and click on ***Edit*** button.
- Fill in the information accordingly and, at the end, click on ***Save*** to save.

##### DELETE
Allows to remove a connection.

- On ***Connection*** main page, select a connection that you want to remove and click on ***Delete*** button.

## Workbench
*Workbench* is a tool that allows to execute queries with all the available connections.

- On side menu, click on ***Workbench***.
- The ***Workbench*** page will be shown.
- Select the chosen ***connection*** on connection field.
- On ***Search*** field, search the database to be consulted or chose on the displayed list.
- Click on the chosen database name and, after that, click on the table name or schema to be consulted.
- After clicking on the name of the table or schema, a predefined query will be displayed in the text area on the side.
- It is possible to make changes to the query and then click on ***Play*** to execute it.
- The result will be displayed.

###### SAVE A QUERY
*Workbench* allows to save the query. To do that:

- Click on the menu button, located next to the ***Play*** button, in the lower right corner of the screen.
- Click ***Save a query*** button.
- In the ***Name*** field, enter the query name.
- The ***Connection*** field shows the connection chosen previously
- The ***Query*** field shows the query text to be saved and allows changes
- Click on ***Shared***, if you want to share the query with other Hanger users. 
- At the end, click on ***Save*** button to save the query.
- Click on ***Close*** to cancel the operation.

###### SAVED QUERIES
Allows to view all the saved queries.

- On ***Workbench*** side menu option, click on the ***down arrow*** and choose the ***Saved queries*** option.
- All the saved queries will be displayed.
- You can also view the saved queries through the ***Workbench*** page by clicking on the menu button, in the lower right corner of the screen and then choosing the ***Saved queries*** option.

***Note:*** queries saved by other users can only be viewed if they have been previously shared.

###### OPEN
This button allows to open a query and play it.

##### SEE
This button allows to view the query deitails.

***Note:*** On this page, you can click on ***Open in workbench*** button to execute the query o ***Workbench*** main page.
 
###### EDIT
This button allows to edit the information about a query.

- Inform the new query name on ***Name***  filed.
- The ***Connection*** field informs the current connection, but you can change it.
- In the text area ***Query***, the query to be edited is informed.
- Click on ***Shared***, if you want to share the query with the other Hanger users.
- At the end, click on ***Save***.
- Click on ***Close***, if you want to cancel the operation.

###### DELETE
This button allows to delete a query.

###### EXPORT AS CSV
To export the query result to a CSV file, you must click on ***Export as csv*** button on ***Workbench*** page.

###### SEND E-MAIL
On *Workbench* it is possible to send the resultset by e-mail to other users.

- On ***Workbench*** page, click on ***Save an e-mail*** button, available on side menu next to ***Play*** button.
- Inform the recipients in the ***Recipient*** field.
- Inform recipients external to Hanger in the ***External Recipient*** field.
- Inform the e-mail subject on ***Subject*** field.
- If it is necessary, write some message on ***Body*** field.
- Click on ***Send*** button to send.
- Click on ***Save*** to save the e-mail and send it later.
- Click on ***Close*** to cancel the operation.

**Note:** after clicking ***Save***, the page will be redirected to ***Saved e-mails***, where all saved e-mails are located.

###### SAVE AN E-MAIL
On *Workbench* is it possible to save an e-mail to send it later.

- In ***Workbench***, click on the ***Save an email*** button available in the menu next to the ***Play*** button.
- Fill in the fields presented previously.
- Click on ***Save*** para salvar o e-mail.
- After clicking on ***Save***, the page will be redirected to the ***Saved e-mails*** page.

###### SAVED E-MAILS
On *Saved e-mails* page you can view a list with all the saved e-mails.

- On ***Workbench*** side menu option, click on the ***down arrow*** and choose the ***Saved e-mails*** option.
- All the saved e-mails will be displayed.
- Click on e-mail Subject to see more details.
- After clicking on e-mail subject, the *jobs* related to this e-mail will be shown.
- In this page, you can send an saved e-mail by clicking on ***Send*** button.
- A success message will be displayed as soon as the email is sent.

**Note:** You can also view the saved e-mails through the ***Workbench*** page by clicking on the menu button, in the lower right corner of the screen and then choosing the ***Saved e-mails*** option. 

###### ADD EMAIL
On *Saved e-mails* page you can also add a new e-mail to the list.

- Click on ***+ Add Email*** and fill in the informations.
- Click on ***Save*** to save, then click on ***Go back*** to return to the saved e-mails page.
- You can send this new e-mail anytime by clicking on ***Send*** button.

###### OPEN WORKBENCH
If you are either in ***Saved queries*** page or in ***Saved e-mails*** page, you can return to the ***Workbench*** main page by clicking on ***Open workbench*** option.

## Subject
*Subjects* are groupers used for organizing and follow the *jobs* execution process.

- To access the ***Subject*** main page, on side menu, click on the ***Subject*** option.
- All the subjects will be displayed.
- If you want to edit a specific subject, click on ***Edit*** button.

##### EDIT
Allows to edit a subject. This process is pretty similar to Add Subject.

- On side menu, click on ***Subject***.
- Select the chosen subject and click on ***Edit*** button.
- Fill in the information accordingly and, at the end, click on ***Save*** to save.

##### ADD SUBJECT
Permite adicionar um novo assunto.

- On ***Subject*** side menu option, click on the ***down arrow*** and choose the ***Add subject*** option.
- On ***Subject*** field, inform the subject name..
- Define the subject description on ***Description*** field, if it is necessary. You can use *Markdown* language to write it.
- On ***Subscription*** section, check the option ***Mandatory***, if you want that the subject can be seen on monitor by all the users. Some subjects are required and can not be removed from monitor, so, these subjects are shown with the subscription checkbox disabled.
- Check the option ***Slack notification***, if you want to receive notifications of jobs belonging to this subject on slack.
- Click on ***Slack Channel*** button to define the channel that the notifications will be sent. A modal will be displayed and you just only have to select the chosen channels and click on ***Add***.
- Click on ***Swinlanes*** button to group the jobs that is in the same subject, by using *regexp*. This is a very useful resource when to subdivide the jobs.
- At the end, click on ***Save*** .

***Note:*** if no channel is selected in the ***Slack channels*** modal, the channel configured on the ***Configuration*** tab will be used for notification of this *job*.

##### DELETE
Allows to remove a subject.

- On ***Subject*** main page, select a subject that you want to remove and click on ***Delete*** button.

## Jobs
On Hanger, *Jobs* are references to [Jenkins](https://jenkins.io/) jobs.

##### ADD JOB
Permite adicionar um novo *job*. 
- No menu lateral, acesse a opção ***Job***.
- Clique no botão Add Job, representado pelo ícone **+**.
- Selecione a instância desejada do Jenkins no combo ***Server*** .
	> Para adicionar um novo *job* é necessário ter ao menos um servidor cadastrado.
- Clique no botão ***Job List*** para que todos os *jobs* da instância selecionada do Jenkins sejam listados. 
- Selecione o *job* desejado no combo ***Name***. 
- Caso deseje definir um nome sugestivo para o *job*, informe o no campo ***Alias***. O alias, com o sufixo [alias], substituirá o nome do *job* nas principais funcionalidades do Hanger.
- Defina a descrição do *job* no campo ***Description***.
	> Neste campo é possível utilizar a linguagem ***Markdown*** para formatação do texto.
- Caso o *job* possa ser executado mais de uma vez ao dia, marque o checkbox ***Rebuild along the day***. Por padrão, um *job* pode ser executado apenas uma vez ao dia. Quando esta opção estiver selecionada, o *job* será executado a primeira vez quando todas as dependências forem atendidas e voltará a ser executado sempre que qualquer uma das dependências for executada com sucesso no decorrer do dia. Caso necessite que o *job* somente seja reexecutado quando todas as dependências forem atendidas novamente, selecione quais dependências serão ***blockers*** na lista de ***parents***.
- Caso deseje definir um intervalo mínimo para que o *job* seja reexecutado, digite a quantidade de minutos no campo ***Rebuild interval in minutes***.
- Defina o conteúdo do campo ***Eagerness*** do job entre 0 e 12 horas. Para que uma dependência de um *job* seja considerada atendida, ela precisa ser executada com sucesso ao menos uma vez no dia. Caso alguma dependência precise ser executada antes da meia noite, o campo ***Eagerness*** deve ser preenchido com o número de horas, antes da meia noite, em que caso o job seja executado com sucesso, a dependência seja considerada como atendida. 
- Defina, em formato cron (http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html), o período permitido para a execução do job no campo ***Time restriction***.
- Defina o conteúdo do campo ***Eagerness*** do job entre 0 e 12 horas. Para que uma dependência de um *job* seja considerada atendida, ela precisa ser executada com sucesso ao menos uma vez no dia. Caso alguma dependência precise ser executada antes da meia noite, o campo ***Eagerness*** deve ser preenchido com o número de horas, antes da meia noite, em que caso o job seja executado com sucesso, a dependência seja considerada como atendida.
- Caso deseje definir um intervalo mínimo para que o *job* seja reexecutado, digite a quantidade de minutos no campo ***Rebuild interval***.
- Marque o checkbox ***Enabled*** para habilitar o job ou desmarque para desabilitá-lo. 
- Caso o *job* possa ser executado mais de uma vez ao dia, marque o checkbox ***Rebuildable***. Por padrão, um *job* pode ser executado apenas uma vez ao dia. Quando esta opção estiver selecionada, o *job* será executado pela primeira vez quando todas as dependências forem atendidas e voltará a ser executado sempre que qualquer uma das dependências for executada com sucesso no decorrer do dia. Caso necessite que o *job* somente seja reexecutado quando todas as dependências forem atendidas novamente, selecione quais dependências serão ***blockers*** na lista de ***parents***.
- Caso deseje vincular e-mails ao *job*, clique no botão ***E-mail***.
- Será exibido o modal ***E-mail list***. Selecione os e-mails que deseja vincular ao *job* e clique no botão ***Add***.
- Os e-mails selecionados serão enviados aos destinatários assim que o *job* for executado com sucesso, ou seja, assim que o *build* for realizado.
	> Caso queira ver mais detalhes, clique no assunto do e-mail desejado e a página será redirecionada para as informações do e-mail escolhido.
- Caso deseje receber notificações de execuções do job, marque a opção ***Slack notification***.
- Para definir o canal no qual as notificações serão enviadas, clique no botão ***Slack channel***. Será exibido o modal ***Slack channels*** no qual será possível selecionar um ou mais canais do Slack nos quais as notificações devem ser enviadas.
	> Caso nenhum canal seja selecionado no modal Slack channels, o canal configurado na guia Configuration será usado para notificação deste *job*.
- Caso deseje classificar o *job* dentro de um ou mais assuntos, clique no botão ***Add subject***. Será exibido o modal ***Subject*** no qual será possível selecionar um ou mais subjects para o *job*. 
	- Clique no botão ***Add***.
	- Será criada uma nova seção denominada ***Subjects***, onde será possível gerenciar os subjects do job.
	- Para remover um subject, clique no botão ***Remove***. 
Caso o *job* tenha uma ou mais dependências, clique no botão ***Add Parent***, para adicionar *jobs* da mesma instância do Jenkins como dependência, ou clique na **seta** ao lado do botão para selecionar uma instância específica do Jenkins. Será exibido o modal ***Jenkins Server***, no qual será possível selecionar todos os *jobs* que serão dependência do *job* que está sendo criado. 
- Caso os *jobs* do Jenkins possuam *upstream jobs*, marque a opção ***Import upstream project*** para que a relação de dependências representadas pelo upstream jobs do Jenkins seja replicada para o mecanismo de dependências do Hanger. 
	- Clique no botão ***Add***.
	- Será criada uma nova seção denominada ***Parent***, onde será possível gerenciar os parents do job.
	- Para remover uma dependência, clique no botão ***Remove***. 
	- Para definir escopo de uma dependência, selecione entre uma das opções disponíveis no combo apresentado na coluna ***Scope***. 


Uma dependência pode ter os seguintes escopos:

**FULL:** Identifica que todas as dependências com escopo FULL ou PARTIAL precisam ser atendidas para que o *job* seja executado. 

**PARTIAL:** Identifica que todas as dependências com escopo PARTIAL precisam ser atendidas para que o *job* seja executado, o *job* será executado com escopo parcial pois nem todas suas dependências foram atendidas. 

**OPTIONAL:** Identifica que a dependência é opcional, ou seja, sendo atendida ou não o *job* será executado normalmente. Caso todas as dependências do *job* sejam OPTIONAL, o *job* será executado assim que a primeira dependência for atendida. 


- Caso seja necessário realizar a validação dos dados resultantes da execução de um job, especificamente em um processo de [https://pt.wikipedia.org/wiki/Extract,_transform,_load](ETL), clique no botão ***Add checkup***. Será criada uma nova seção denominada ***Checkup***, na qual será possível definir uma instrução SQL para validação. Para criar um *checkup*:
	- Informe a descrição do *checkup* no campo ***Description***.
	- Selecione em qual conexão a validação será executada no combo ***Connection***.
	- Defina em qual escopo a validação será realizada, selecionando uma das opções do combo ***Scope***. 
		> **FULL:** o checkup do ***job*** só será executado quando todas as dependências com escopo **FULL** e **PARTIAL** foram atendidas.
		> **PARTIAL:** o checkup do ***job*** só será executado quando as dependências com o escopo **PARTIAL** foram atendidas.
		> **ONYONE:** o checkup do ***job*** será executado sempre que as depêndencias com escopo **PARTIAL** ou **FULL** foram atendidas.
	- Defina a instrução SQL de validação no campo ***SQL Select Statement***, o *resultset* deve retornar apenas um valor inteiro. 
	- Defina o teste que será feito para comparação entre o resultset e o threshold, selecionando uma das opções disponíveis no combo ***Test***.
	- Defina o resultado esperado no campo ***Threshold***.
		> É possível referenciar o resultado de outro ckeckup no campo threshold, utilizando a seguinte sintaxe: `${<checkup ID>}`
	- Defina a ação que será executada caso a validação falhe, selecionando uma das opções disponíveis no combo **On fail**.
		> Em caso de falha de um checkup, as seguintes ações podem ser executadas:
		> 
		> **NOTHING:** Apenas bloqueia a execução da cadeia. 
		> 
		> **REBUILD:** Caso seja uma pós validação, reexecuta o job. Caso contrário, tem o mesmo efeito de NOTHING. 
		> 
		> **REBUILD_MESH:** Caso seja uma pós validação, reexecuta todas as dependências do job e as dependências de cada dependência. Caso contrário, tem o mesmo efeito de NOTHING. 
		> 
		> **REBUILD_TRIGGER:**  Caso seja uma pós validação, reexecuta dependências específicas selecionadas clicando no botão ***Add Trigger***. Caso contrário, tem o mesmo efeito de NOTHING. 
		> 
		> **LOG_AND_CONTINUE:** Grava o resultado da execução do *checkup* e notifica os canais do Slack selecionados no job, mas não bloqueia a execução da cadeia. 
	- Caso a ação escolhida seja **REBUILD_TRIGGER**, clique no botão ***Add Trigger***. Será exibido o modal ***Trigger*** no qual será possível selecionar quais dependências devem ser reexecutadas.
	- Caso seja necessário executar algum comando quando um checkup falhar, clique no botão ***Add Command***. Será criada um nova seção, ***Script on failure***:
		- Selecione o tipo de ação que será executada entre ***SHELL*** ou ***SQL***.
		- Informe no campo ***Script*** a ação a ser executada.
		- Para remover uma ação, clique no botão ***Remove***. 
	- Um checkup pode ser executado como uma pré-validação ou uma pós validação. Pré-validações ocorrem quando todas as dependências foram atendidas e imediatamente antes da execução do *job*, enquanto pós-validações ocorrem após a execução do *job*. Para definir o tipo de um checkup, clique no toggle e escolha entre ***Pre-validation*** e ***Post-validation***. 
- Defina o número de tentativas de execução do *job* no campo ***Retries***.
> Para ações NOTHING e LOG_AND_CONTINUE não há número de tentativas.
- Caso necessário, selecione um aprovador para o checkup no combo ***Approver***.
- Clique no botão ***Save***.

##### BUILD 
Permite executar um *job*.  

##### SEE
Permite visualizar as configurações do *job*. Na página de visualização as seguintes ações estão disponíveis:

- Clique no botão ***Add Job***, representado pelo ícone **+**, para adicionar um novo *job*. 
- Clique no botão ***Edit*** para editar o *job*. 
- Clique no botão ***Delete*** para deletar o *job*.
- Clique no botão ***Propagation*** para analisar quais *jobs* são impactados pelo *job* na cadeia de dependências. 
- Clique no botão ***Flow*** para analisar quais *jobs* fazem parte da cadeia de dependência do *job*. 
- Clique no botão ***Build*** para executar o *job*.
- Clique no botão ***Build mesh*** para executar toda a cadeia de dependência do *job*. 

##### EDIT 
Permite editar as configurações de um *job*.

##### DELETE
Permite excluir um *job*.

##### HEATMAP
Permite a distribuição do trabalho em um período de tempo específico.
- Selecione a data que será analisada no campo ***Period***.
- Selecione o intervalo que será analisado no campo ***Interval***.
- Clique no botão ***Filter***, será exibido o modal ***Job Filter*** no qual será possível escolher quais *jobs* serão analisados. 

##### GANTT
Permite analisar a carga de trabalho durante um intervalo de tempo.
- Selecione a data de início da análise no campo ***From***.
- Selecione a data de fim da análise no campo ***To***.
- Selecione a fase que será analisada no campo ***Phase***.
- Clique no botão ***Filter***. 

##### REFRESH
Permite atualizar a lista de *jobs* do Hanger. Por questões de performance o Hanger mantém o máximo possível de informação em memória, sincronizando os dados com o banco de dados somente quando necessário; por meio da opção ***Refresh*** é possível forçar a sincronização imediata destes dados.

## Log
*Logs* are records of the executed activities by all the Hanger system users. To use the *Log* option, follow these steps.

- On the side menu, click in ***Log***.
- On top, select the chosen period to see the logs.
- Click the ***Filter*** button and all records will be displayed in a table.

Each logs table field has a meaning:

- ***Date:*** Action date.
- ***Type:*** Action type.
- ***User:*** User who did the action.
- ***Data:*** Changes that were made.

## User
*Users* are the users of Hanger.

##### ADD USER
Adiciona um novo usuário.

- On ***User*** side menu option, click on the ***down arrow*** and choose the ***Add user*** option.
- On ***E-mail*** field, inform the user e-mail address.
- On ***Username*** field, define the username.
- On ***First Name*** field, inform the user first name.
- On ***Last Name*** field, inform the user last name.
- On ***Role*** field, define the user level.
- Check the ***Enabled*** option to enable the user on system. If the option is unchecked, the user becomes inactive.
- Click on ***Privileges*** button to set the user's privileges.
- Click on ***Save*** button to save.

***Note:*** the user levels, that is, the ***Roles*** present in the Hangar are:

**HERO:** is the integral system administrator. A Hero user can execute all the available operations. 

**ADMIN:** this user can execute all the operations in Hanger, except create, edit or delete other users.

**USER:** this user can build a job and approve jobs that are in their name on ***Approval*** section.

##### EDIT
Allows to edit an user. This process is pretty similar to Add user.

- On side menu, click on ***User***.
- Select the chosen user and click on ***Edit*** button.
- Fill in the information accordingly and, at the end, click on ***Save*** to save.

##### DELETE
Allows to remove an user.

- On ***User*** main page, select an user that you want to remove and click on ***Delete*** button.

##### CHANGE PASSWORD
This option allows the user to change their own password. For this:



##### API TOKEN
Permite ao usuário obter a sua chave de acesso à API do glove. O token gerado não tem validade determinada e pode ser usado até que o próprio usuário opte por renová-lo.
Para renovar o Token e invalidar o token gerado anteriormente:
- Clique no botão ***Refresh Token***.
- Um novo token será gerado, para visualizá-lo acesse novamente a opção de menu ***API Token***

## Configuration
In *Configuration* are all the global Hanger configurations. To set according to your need, follow the guidelines below:

- On side menu, click on ***Configuration***.
- On ***Host*** field, inform the server used to send e-mails.
- Write the e-mail server port on the field ***Port***.
- On ***Address*** field, inform the e-mail address.
- Write the password on the ***Password*** field.
- On Log Retention field it is possible to define, in days, the period for cleaning the approvals and data validation log.
- On ***Default Channel*** field, inform the default channel that will be used by Slack.
- On ***Schema and table searcheble*** field, define the maximum number of entities allowed on Workbench.
- Set the allowed domains to send e-mails on the ***E-mail filter (RegExp)*** field. If it is empty, any domain is allowed.  
- Define the maximum number of rows per query on the ***Max rows per query field***.
- Click on ***Update Logo*** button to change the tool logo to any image file.
- Click on ***Update cache*** button if you want to update the cache.
- Click on ***Update plugin*** button to update the notification plugin.

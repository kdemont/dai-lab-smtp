DAI lab: SMTP
=============

Description du projet
----------
Ce projet consiste à la création d’une application client TCP en Java.  Cette application client utilise l’API Socket pour communiquer avec un serveur SMTP.

Nous avons ensuite mis cela en pratique en développant une application client qui envoie automatiquement un e-mail à une liste de victimes. Pour tester et avoir une représentation graphique de notre programme, nous avons utilisé l’outil MailDev afin de simuler une boîte de réception d’e-mail.

Instruction pour mettre en place un serveur SMTP simulé
----------
Pour mettre en place un serveur SMTP simulé (mock server), une manière simple est d’utiliser MailDev. Cela permet d’avoir un retour visuel et une structure simulant l’interface de la gestion d’une boîte de réception d’e-mail.

Pour utiliser MailDev avec Docker, il suffit de lancer la commande suivante :

    docker run -d -p 1080:1080 -p 1025:1025 maildev/maildev

Pour ensuite voir cette interface graphique, il suffit de taper dans le navigateur web :
[localhost:1080](http://localhost:1080)


Instruction pour configurer une campagne de canular d’e-mail
----------
Pour pouvoir lancer une campagne de canular, nous avons mis à disposition un générateur de JSON qui permet de créer une liste de victimes ainsi qu’une liste de messages incluant un sujet.

Ensuite, il suffit de lancer le programme dans le fichier Client qui lui permet de lire ces fichiers JSON (attention à adapter l’emplacement de ces fichiers), puis qui va former des groupes aléatoirement parmi la liste de victimes formés d’un envoyeur et de receveur(s). Le programme va ensuite envoyer des e-mails contenant des messages parmi la liste de messages (composé d’un contenu « body » et d’un sujet « subject »). Exemple de résultat sur MailDev :

![MailDev_result](/figures/maildev.PNG)

Description de l’implémentation
----------
L’implémentation est composée de plusieurs parties :

Le **générateur de fichier JSON** qui permet de créer des fichiers JSON. Nous l’avons implémenté de sorte à générer des messages très basiques (dont nous avons gardé une structure simple dont le contenu est « message i » et « sujet i », i étant le numéro du message). Pareil pour les e-mails qui prennent la forme : « victim<NuméroDeLaVictime>@exemple.com ».

La **classe Message** qui définit la structure d’un message. Celui-ci est composé d’un contenu et d’un sujet.

La **classe Client** qui contient entre autres le main où l’on effectue l’envoi des e-mail, ainsi que d’autres fonctions utilisées pour y parvenir tel que :

* readFromJasonFile() et readMessagesFromJasonFile() pour lire les victimes et les messages depuis les fichier JSON et qui renvoient une liste de String et de Message respectivement

* checkValidAdress() pour savoir si une adresse e-mail est valide

* divideIntoGroups() pour former des groupes

* sendEmail() pour pouvoir envoyer des e-mails à un groupe de receveurs, cette fonction va établir une connexion SMTP avec un serveur et envoyer des commandes SMTP pour spécifier l’expéditeur, les destinataires, et le contenu d’un e-mail. Voici un schéma da la communication entre le client et le serveur :

![SMTP_schema](/figures/smtp_resquests.png)

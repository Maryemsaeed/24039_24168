# TP1 – SOAP et WSDL

## Étape 1

1. L'annotation `@WebService` sur la classe `EmpruntService` en fait un service web.
   L'annotation `@WebFault` sur `LivreIndisponibleException` en fait une erreur déclarée
   dans le contrat (elle apparaît dans le WSDL comme `wsdl:fault`).

2. JAXB crée les objets par réflexion : il instancie d'abord la classe avec un constructeur
   sans argument, puis remplit les champs à partir du XML reçu. Sans constructeur vide,
   il ne peut pas construire l'objet.

## Étape 2

1. L'URL du service est http://localhost:8080/bib/EmpruntService (élément soap:address
   du port EmpruntPort). Le service expose deux opérations :
   - emprunterLivre : message d'entrée emprunterLivre, message de sortie
     emprunterLivreResponse, et une fault (message LivreIndisponibleException) ;
   - rendreLivre : message d'entrée rendreLivre, message de sortie rendreLivreResponse,
     sans fault déclarée.

2. Le binding EmpruntPortBinding déclare style="document" et use="literal". Il s'agit de
   SOAP 1.1, car l'espace de noms est http://schemas.xmlsoap.org/wsdl/soap/ et le
   transport http://schemas.xmlsoap.org/soap/http.

3. Dans le schéma XSD (?xsd=1), isbn et dateRetour ont tous deux le type xs:string. La
   règle de format de l'ISBN n'apparaît pas : l'expression régulière n'existe que dans
   le code Java, le contrat ne l'exprime pas. Une date est aussi transportée comme une
   simple chaîne.

4. Non, idAdherent et isbn ne portent pas de préfixe d'espace de noms. Le schéma ne
   déclare pas elementFormDefault="qualified" : les éléments locaux (les paramètres) ne
   sont donc pas qualifiés, contrairement à l'élément de l'opération (bib:emprunterLivre).
   Les messages envoyés avec curl sur ce modèle ont été acceptés par le service.

5. Dans l'onglet Raw, HTTP correspond à la ligne POST et aux en-têtes (Content-Type,
   SOAPAction) : il transporte le message jusqu'au serveur. XML est le format du
   document envoyé. SOAP est la structure de l'enveloppe (Envelope, Header, Body) qui
   donne un sens à ce XML. Le WSDL est le contrat : il décrit les opérations, les
   messages, le format (binding) et l'adresse du service, et permet à SoapUI de
   construire la requête sans lire le code Java.
\# TP1 – SOAP et WSDL



Matricules : 24039 et 24168



Travail rendu partiellement : étapes 1 et 2 de la partie A terminées.

Les étapes 3 à 6 et la conclusion ne sont pas faites.



\## Étape 1



1\. L'annotation `@WebService` sur la classe `EmpruntService` en fait un service web.

&#x20;  L'annotation `@WebFault` sur `LivreIndisponibleException` en fait une erreur déclarée

&#x20;  dans le contrat (elle apparaît dans le WSDL comme `fault`).



2\. JAXB crée les objets par réflexion : il instancie d'abord la classe avec un constructeur

&#x20;  sans argument, puis remplit les champs à partir du XML reçu. Sans constructeur vide,

&#x20;  il ne peut pas construire l'objet.



\## Étape 2



1\. URL du service : http://localhost:8080/bib/EmpruntService (soap:address dans

&#x20;  le service EmpruntService, port EmpruntPort). Deux opérations :

&#x20;  - emprunterLivre : entrée = message emprunterLivre, sortie = message

&#x20;    emprunterLivreResponse, fault = message LivreIndisponibleException

&#x20;  - rendreLivre : entrée = message rendreLivre, sortie = message rendreLivreResponse



2\. Le binding EmpruntPortBinding déclare style="document" et use="literal".

&#x20;  C'est du SOAP 1.1 : l'espace de noms de soap:binding est

&#x20;  http://schemas.xmlsoap.org/wsdl/soap/.



3\. Dans le schéma XSD (?xsd=1), isbn et dateRetour ont le type xs:string. La règle de

&#x20;  format de l'ISBN n'apparaît pas : elle n'existe que dans le code Java (Pattern).



4\. Non, idAdherent et isbn n'ont pas de préfixe d'espace de noms (le schéma n'a pas

&#x20;  elementFormDefault="qualified"). Seul bib:emprunterLivre porte l'espace de noms.



5\. Dans l'onglet Raw : HTTP = la ligne POST, les en-têtes et le code de réponse 200.

&#x20;  XML = le format du texte. SOAP = l'enveloppe Envelope / Header / Body.

&#x20;  HTTP transporte le message, XML est le format de données, SOAP définit la structure

&#x20;  de l'enveloppe et des Faults. Le WSDL est le contrat (adresse, binding, opérations, types).



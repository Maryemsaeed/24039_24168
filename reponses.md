# TP1 – SOAP et WSDL

Matricules : 24039 et 24168

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

5. Dans l'onglet Raw, HTTP correspond à la ligne POST, aux en-têtes et au code de réponse
   200 : il transporte le message jusqu'au serveur. XML est le format du texte échangé.
   SOAP est la structure de l'enveloppe (Envelope, Header, Body) et des Faults. Le WSDL
   est le contrat : il décrit les opérations, les messages, le binding, l'adresse du
   service et les types.

## Étape 3

1. POST est utilisé parce que SOAP transporte une enveloppe XML dans le corps de la
   requête HTTP. Un GET n'a pas de corps : il ne peut pas porter l'enveloppe, même
   pour une simple lecture. Le nom de l'opération et ses paramètres sont dans le Body.

2. Non, le bloc jeton n'a pas empêché le traitement : le service a répondu 200 avec
   un numéro d'emprunt. Le bloc n'a pas l'attribut mustUnderstand="1", donc le serveur
   n'est pas obligé de le comprendre : il est ignoré. Aucune opération du service ne
   déclare ce jeton dans le WSDL.

3. Le service ne traite pas le message SOAP 1.2 : curl reçoit « Empty reply from server »,
   sans numéro d'emprunt. Le binding du WSDL est déclaré en SOAP 1.1
   (http://schemas.xmlsoap.org/wsdl/soap/), le service attend donc l'enveloppe
   http://schemas.xmlsoap.org/soap/envelope/. L'enveloppe SOAP 1.2
   (http://www.w3.org/2003/05/soap-envelope) appartient à une autre version du protocole
   et n'est pas reconnue par ce service. Le contrat WSDL fixe donc la version SOAP que
   le client doit utiliser.

4. Les cinq erreurs et leur correction :
   1. `<soap:envelope>` en minuscules : XML est sensible à la casse, la balise ouvrante ne
      correspond pas à `</soap:Envelope>`. Correction : `<soap:Envelope>`.
   2. `</rendreLivre>` sans préfixe : ne correspond pas à `<bib:rendreLivre>`.
      Correction : `</bib:rendreLivre>`.
   3. Mauvais espace de noms `http://supnum.mr/bibliotheque`. Correction :
      `http://supnum.mr/bib`.
   4. Paramètre `<bib:numero>` : le nom attendu est `numeroEmprunt`, sans préfixe (comme
      `idAdherent` et `isbn`). Correction : `<numeroEmprunt>`.
   5. Le Header est placé après le Body. Correction : Header avant Body.

   Résultats observés :
   - Erreurs 1 et 2 : XML mal formé, curl reçoit « Empty reply from server ».
   - Erreur 3 : HTTP 500, le service ne reconnaît pas l'opération dans cet espace de noms.
   - Erreurs 4 et 5 : aucune erreur côté serveur (HTTP 200, pénalité 0). Pour l'erreur 4,
     le paramètre inconnu est ignoré et rendreLivre ne contrôle pas son argument. Pour
     l'erreur 5, Metro tolère l'ordre Header/Body, bien que la spécification SOAP
     impose le Header avant le Body.

## Étape 4

1. Cas 1 (ISBN `978-2-10`) : HTTP 500, faultcode `S:Client`, faultstring
   « ISBN mal formé : 978-2-10 ». Cas 2 (ISBN `978-2-10-074444-4`, livre déjà emprunté) :
   HTTP 500, faultcode `S:Server`, faultstring « Livre indisponible ».

2. Dans le cas 2, `<detail>` contient l'élément `LivreIndisponible` avec `<isbn>` et
   `<retourPrevu>` (2026-12-02). Sa structure est décrite dans le WSDL : l'opération
   `emprunterLivre` déclare une `fault` (message `LivreIndisponibleException`) dont la
   partie pointe vers l'élément `tns:LivreIndisponible`, défini dans le schéma XSD
   (`?xsd=1`) par le type `livreIndisponibleInfo`.

3. JAX-WS a choisi `Server` pour le livre indisponible. Ce n'est pas vraiment une panne
   du serveur : la demande du client ne peut pas aboutir parce que le livre est déjà
   sorti. J'aurais plutôt choisi `Client`, la demande ne pouvant pas être satisfaite
   telle quelle. (Un argument pour `Server` : l'état du serveur, et non le format du
   message, est la cause du refus.)

4. Avec `mustUnderstand="1"` sur le bloc `jeton`, le serveur refuse le message : HTTP 500,
   faultcode `S:MustUnderstand`, car il ne comprend pas l'en-tête
   `{http://supnum.mr/bib}jeton` et l'opération n'est pas exécutée. Avec
   `mustUnderstand="0"`, le bloc est facultatif : le serveur l'ignore et répond
   normalement (HTTP 200, numéro d'emprunt).

5. La Fault 1.2 (`xml/03-fault-sender-12.xml`) diffère de la Fault 1.1 par :
   l'espace de noms de l'enveloppe (`http://www.w3.org/2003/05/soap-envelope`),
   `faultcode` remplacé par `Code/Value` avec `Sender` (au lieu de `Client`),
   `faultstring` remplacé par `Reason/Text` avec l'attribut obligatoire `xml:lang`, et
   `detail` remplacé par `Detail`, dans l'espace de noms SOAP.

Test supplémentaire : dans `rendreLivre`, un numéro ne commençant pas par `E-2026-`
(par exemple `X-999`) renvoie HTTP 500, faultcode `S:Client`, faultstring
« Numéro d'emprunt inconnu : X-999 ».

Note : `xmllint` n'étant pas disponible sous Windows, la validité de
`03-fault-sender-12.xml` a été vérifiée avec le parseur XML de PowerShell.

Note technique : les Faults n'étaient pas produites avec `mvn exec:java` (corps vide, erreurs
SAAJ et JAXB introuvables). Les dépendances `jaxb-runtime` et `saaj-impl` ont été ajoutées
au pom.xml, et le serveur a été lancé directement avec `java -cp`.


## Étape 5

1. L'opération est `emprunterLivre` (portType `EmpruntPortType`). Son message de fault est `bib:LivreIndisponibleFault`, dont la partie `fault` référence l'élément `bib:LivreIndisponible`. L'action SOAP à envoyer est `http://supnum.mr/bib/emprunterLivre` (`soap:operation soapAction` dans le binding).

2. La signature est `public EmpruntType emprunterLivre(String idAdherent, String isbn) throws LivreIndisponibleFault`, annotée `@WebMethod(action = "http://supnum.mr/bib/emprunterLivre")` et `@WebResult(name = "emprunt")`. `isbn` est un `String` : `IsbnType` dérive de `xs:string`, la restriction du motif n'existe donc plus côté Java.

3. `dateRetour` devient un `XMLGregorianCalendar` avec `@XmlSchemaType(name = "date")`. En partie A, c'était un `String` et le WSDL généré déclarait `xs:string`. En contract-first, le type `xs:date` du contrat est conservé.

4. La classe d'exception générée s'appelle `LivreIndisponibleFault`. Son nom vient du `wsdl:message name="LivreIndisponibleFault"` utilisé par le `wsdl:fault`. La classe `LivreIndisponible` (`isbn`, `retourPrevu`) vient de l'élément XSD du même nom et sert de `faultInfo`.

## Étape 6

1. Le WSDL publié est bien celui fourni : il fait 65 lignes, garde les noms du contrat (`EmpruntSoapBinding`, `emprunterLivreRequest`, `LivreIndisponible`) et seul un commentaire « Published by XML-WS Runtime » est ajouté, avec une indentation différente. Il contient le motif de l'ISBN (`xs:pattern value="97[89]-\d-\d{2,5}-\d{2,6}-\d"` dans `IsbnType`) et le type `xs:date` pour `dateRetour` et `retourPrevu`. Le schéma est intégré dans `wsdl:types`.

2. Oui, il a fallu modifier `01-emprunter.xml` : envoyé tel quel, il provoque HTTP 500 (`S:Server`, « Cannot invoke "String.equals(Object)" because "isbn" is null »). Le schéma déclare `elementFormDefault="qualified"` : `idAdherent` et `isbn` doivent être dans l'espace de noms `http://supnum.mr/bib`. Sans préfixe, le serveur ne les reconnaît pas et les paramètres arrivent à `null`. La version corrigée (`xml/10-emprunter-contract.xml`, avec `<bib:idAdherent>` et `<bib:isbn>`) donne HTTP 200 et `E-2026-0812`. Avec l'ISBN `978-2-10-074444-4` (`xml/11-livre-emprunte-contract.xml`), le service renvoie HTTP 500, `S:Server`, « Livre indisponible », et un `detail` contenant `LivreIndisponible` avec `isbn` et `retourPrevu` (2026-12-02).

3. Sans `@SchemaValidation`, l'ISBN `978-2-10` n'est pas refusé : HTTP 200 avec un numéro d'emprunt, car rien ne vérifie le motif et le code Java ne contrôle pas le format. Avec `@com.sun.xml.ws.developer.SchemaValidation`, il est refusé par une Fault HTTP 500, faultcode `S:Client`, faultstring `org.xml.sax.SAXParseException; cvc-pattern-valid : La valeur '978-2-10' n'est pas un facet valide par rapport au modèle '97[89]-\d-\d{2,5}-\d{2,6}-\d' pour le type 'IsbnType'.` Il n'y a pas de `detail`.

4. Sans `wsdlLocation`, JAX-WS ne publie plus le WSDL fourni mais en génère un (commentaire « Generated by XML-WS Runtime ») à partir des annotations. Il fait 43 lignes au lieu de 65. Le schéma est séparé (`xsd:import ... schemaLocation=".../EmpruntService?xsd=1"`). Le binding devient `EmpruntPortBinding` (au lieu de `EmpruntSoapBinding`), le message `emprunterLivre` (au lieu de `emprunterLivreRequest`), la fault `LivreIndisponibleFault` (au lieu de `LivreIndisponible`), et des `wsam:Action` sont ajoutés. Surtout, `isbn` devient un simple `xs:string` dans `?xsd=1` : le motif a disparu. Avec `@SchemaValidation` toujours actif, l'ISBN `978-2-10` est alors accepté (HTTP 200), car la validation se fait contre le schéma généré. `soapAction`, service, port, `soap:address`, `document` et `literal` sont inchangés. Le contrat réellement publié n'est donc plus celui qu'on a écrit.
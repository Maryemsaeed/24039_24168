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

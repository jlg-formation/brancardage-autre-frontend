# User Stories et User Flows

## 1. User Stories (US)

### Épique 1 : Identification du Patient

- **US-001 : Recherche manuelle de patient**
  - _En tant que_ personnel soignant,
  - _Je veux_ pouvoir rechercher un patient en saisissant son nom, prénom, IPP ou numéro de sécurité sociale,
  - _Afin de_ l'identifier formellement avant de demander un brancardage.

- **US-002 : Recherche par scan de bracelet**
  - _En tant que_ personnel soignant,
  - _Je veux_ pouvoir scanner le bracelet (QR Code ou Code-barres) du patient avec l'appareil photo de mon téléphone,
  - _Afin de_ récupérer automatiquement son dossier sans saisie manuelle.

### Épique 2 : Gestion des Médias et Documents

- **US-003 : Prise de photo directe**
  - _En tant que_ personnel soignant,
  - _Je veux_ pouvoir prendre une photo de l'état du patient ou de l'équipement requis directement depuis l'application,
  - *Afin d'*enrichir la demande de brancardage avec des informations visuelles.

- **US-004 : Import de photo depuis la galerie**
  - _En tant que_ personnel soignant,
  - _Je veux_ pouvoir sélectionner une photo existante dans la galerie de mon téléphone,
  - _Afin de_ l'ajouter à la demande de brancardage.

- **US-005 : Scan de document médical**
  - _En tant que_ personnel soignant,
  - _Je veux_ pouvoir prendre en photo un document médical (ex: fiche de transfert, ordonnance) avec une interface optimisée pour la lisibilité,
  - _Afin de_ transmettre des informations importantes au brancardier.

### Épique 3 : Commande d'un Brancardier

- **US-006 : Localisation automatique du départ**
  - _En tant que_ personnel soignant,
  - _Je veux_ que l'application utilise le GPS de mon téléphone pour déterminer automatiquement le lieu de départ,
  - _Afin de_ gagner du temps lors de la création de la demande.

- **US-007 : Sélection de la destination**
  - _En tant que_ personnel soignant,
  - _Je veux_ pouvoir choisir le service ou la chambre de destination via une liste déroulante ou une recherche,
  - *Afin d'*indiquer clairement où le patient doit être transporté.

- **US-008 : Validation et envoi de la demande**
  - _En tant que_ personnel soignant,
  - _Je veux_ pouvoir consulter un récapitulatif de ma demande (Patient, Médias, Départ, Arrivée) avant de la valider,
  - _Afin de_ m'assurer de l'exactitude des informations envoyées au backend.

## 2. User Flows

### Flow Principal : Commande de Brancardage

1. **Écran d'Accueil :** Choix entre "Recherche Manuelle" et "Scan Bracelet".
2. **Écran de Recherche / Scan :** Saisie des critères ou scan du bracelet.
3. **Écran Dossier Patient :** Affichage des informations du patient. Bouton "Créer une demande".
4. **Écran Médias (Optionnel) :** Ajout de photos ou scan de documents.
5. **Écran Localisation :** Affichage de la position GPS actuelle (modifiable).
6. **Écran Destination :** Sélection du service/chambre d'arrivée.
7. **Écran Récapitulatif :** Vérification des données et bouton "Valider".
8. **Écran Confirmation :** Message de succès et retour à l'accueil.

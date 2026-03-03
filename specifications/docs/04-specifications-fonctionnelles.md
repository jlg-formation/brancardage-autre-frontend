# Spécifications Fonctionnelles

## 1. Identification et Recherche de Patient

L'application doit permettre au personnel soignant d'identifier de manière univoque le patient à transporter. Deux méthodes sont proposées :

### 1.1. Recherche Manuelle

- **Interface :** Un formulaire de recherche multicritères.
- **Champs de recherche :**
  - Nom et Prénom (recherche textuelle).
  - Numéro de patient (IPP - Identifiant Permanent du Patient).
  - Numéro de sécurité sociale (format standard français).
- **Comportement :** L'utilisateur saisit au moins un critère et valide. L'application interroge l'API backend et affiche une liste de résultats ou directement le dossier si le résultat est unique.

### 1.2. Recherche par Scan (Bracelet Patient)

- **Interface :** Ouverture de l'appareil photo avec une zone de ciblage.
- **Fonctionnement :** L'application utilise la caméra pour détecter et lire un code-barres ou un QR Code présent sur le bracelet du patient.
- **Comportement :** Dès qu'un code valide est détecté, l'application extrait l'identifiant (ex: IPP), interroge automatiquement l'API backend et affiche le dossier du patient correspondant.

## 2. Gestion des Médias et Documents

Pour enrichir la demande de brancardage et fournir un contexte visuel au brancardier, l'utilisateur peut joindre des médias.

### 2.1. Prise de Photo Directe

- **Fonctionnalité :** L'utilisateur peut déclencher l'appareil photo depuis l'application pour capturer une image (ex: état du patient, équipement spécifique requis comme une potence à sérum).
- **Contrainte :** La photo doit être compressée avant l'envoi pour limiter la consommation de bande passante.

### 2.2. Import depuis la Galerie

- **Fonctionnalité :** L'utilisateur peut parcourir la galerie de son appareil et sélectionner une ou plusieurs photos existantes à joindre à la demande.

### 2.3. Scan de Documents Médicaux

- **Fonctionnalité :** Un mode spécifique de l'appareil photo optimisé pour la capture de documents (ex: fiche de transfert, ordonnance).
- **Traitement :** L'interface doit guider l'utilisateur pour cadrer le document et appliquer un traitement d'image (recadrage automatique, amélioration du contraste) pour garantir la lisibilité.

## 3. Commande d'un Brancardier (Workflow Principal)

Une fois le patient identifié et les éventuels médias ajoutés, l'utilisateur finalise la demande de transport.

### 3.1. Localisation de Départ (GPS)

- **Fonctionnalité :** L'application sollicite les services de localisation de l'appareil (GPS/Réseau) pour déterminer les coordonnées exactes de l'utilisateur.
- **Interface :** Affichage de la position détectée (ex: "Bâtiment A, Étage 2"). L'utilisateur doit pouvoir affiner ou modifier manuellement cette localisation si la précision GPS est insuffisante en intérieur.

### 3.2. Sélection de la Destination

- **Interface :** Un composant de sélection (liste déroulante avec autocomplétion ou champ de recherche) permettant de choisir le service, l'unité ou la chambre de destination au sein de l'hôpital.
- **Données :** La liste des destinations possibles est fournie par l'API backend.

### 3.3. Validation et Récapitulatif

- **Interface :** Un écran de synthèse affichant toutes les informations collectées :
  - Identité du patient.
  - Médias joints (miniatures).
  - Lieu de départ (coordonnées/texte).
  - Destination choisie.
- **Action :** Un bouton "Valider la demande". Au clic, les données sont formatées et envoyées au backend via un appel réseau. Un retour visuel (succès ou erreur) est présenté à l'utilisateur.

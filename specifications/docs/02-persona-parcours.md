# Personas et Parcours Utilisateurs

## 1. Personas

### 1.1. Le Personnel Soignant (Utilisateur Principal)

- **Rôle :** Infirmier(ère), aide-soignant(e) ou médecin au sein du centre hospitalier.
- **Objectif :** Demander le transport d'un patient d'un point A à un point B de manière rapide et sécurisée.
- **Besoins :** Une interface simple, rapide à utiliser, permettant d'identifier le patient sans erreur (scan de bracelet) et de fournir toutes les informations nécessaires au brancardier (photos, documents, localisation précise).
- **Contraintes :** Temps limité, environnement parfois stressant, besoin de fiabilité.

### 1.2. Le Brancardier (Utilisateur Indirect)

- **Rôle :** Agent chargé du transport des patients au sein de l'hôpital.
- **Objectif :** Recevoir des demandes de transport claires, complètes et localisées.
- **Besoins :** Connaître l'identité du patient, le lieu de prise en charge exact, la destination, et les éventuelles consignes particulières (matériel spécifique, état du patient via photos).

## 2. Parcours Utilisateur Principal (Personnel Soignant)

Le parcours principal de l'application se décompose en trois grandes étapes :

### Étape 1 : Identification du Patient

1. L'utilisateur ouvre l'application.
2. Il choisit le mode d'identification :
   - **Scan :** Il utilise l'appareil photo pour scanner le bracelet du patient (QR Code ou Code-barres).
   - **Manuel :** Il saisit les informations du patient (Nom/Prénom, IPP, ou Numéro de sécurité sociale) dans un formulaire de recherche.
3. L'application interroge le backend et affiche le dossier du patient.

### Étape 2 : Enrichissement de la Demande (Médias)

1. L'utilisateur peut ajouter des informations visuelles à la demande :
   - Prise de photo directe (ex: état du patient, équipement requis).
   - Import d'une photo depuis la galerie du téléphone.
   - Scan d'un document médical (ex: fiche de transfert) avec optimisation de la lisibilité.

### Étape 3 : Commande du Brancardier

1. L'application récupère automatiquement la position GPS de l'utilisateur pour définir le lieu de départ (modifiable manuellement si besoin).
2. L'utilisateur sélectionne le service ou la chambre de destination via une liste ou une recherche.
3. Un récapitulatif de la demande s'affiche (Patient, Médias, Départ, Arrivée).
4. L'utilisateur valide la demande, qui est envoyée au backend pour traitement.

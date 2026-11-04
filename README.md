# NOVA PAY Android

Application Android Kotlin + Jetpack Compose. Identité visuelle : noir profond, bleu électrique et argent. Le logo institutionnel or/argent/bleu est conservé pour les supports de présentation ; l'icône Android utilise la signature bleu/argent.

## État actuel

La version 1.0 est un prototype visuel avec tableau de bord, solde fictif, carte virtuelle de démonstration, verrouillage visuel local, activité, profil et navigation.

## Feuille de route

1. **Comptes utilisateurs et authentification** — commencer par un parcours de démonstration local, puis concevoir une authentification serveur adaptée.
2. **Portefeuille avec données persistantes** — enregistrer les préférences et données de démonstration localement, avec une séparation claire entre données fictives et données financières.
3. **Transactions de démonstration** — historique, statuts et reçus simulés, sans transfert réel d'argent.
4. **Architecture sécurisée pour les futurs paiements** — définir les frontières client/serveur, la gestion des secrets, les journaux et les contrôles de sécurité avant toute intégration financière.
5. **Partenariats avec des prestataires de paiement** — étudier les prestataires autorisés, leur couverture géographique, leurs exigences contractuelles et réglementaires.

## Limites et sécurité

Cette version ne crée pas de compte financier réel, ne conserve pas de fonds, n'émet pas de carte Visa/Mastercard et n'effectue aucun paiement. Les opérations financières réelles nécessiteront des prestataires agréés, les autorisations applicables et une infrastructure serveur auditée. Ne pas saisir de vraies coordonnées bancaires ni de secrets dans le prototype.

## Compilation

Ouvrir le projet dans Android Studio avec JDK 17 et Android SDK 35, puis lancer `assembleDebug`. Le workflow GitHub Actions compile automatiquement l'APK de débogage.

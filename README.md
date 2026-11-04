# NOVA PAY Android

Application Android Kotlin + Jetpack Compose. Identité visuelle : noir profond, bleu électrique et argent.

## État actuel

Prototype Android de démonstration : inscription/connexion locales, profil modifiable, solde fictif, carte de test non valide, opérations simulées et historique local.

## Fondation backend Supabase

Le dossier `supabase/` contient le premier schéma SQL pour préparer une application connectée :

- `profiles` : profil lié à l'identifiant Auth Supabase.
- `demo_transactions` : historique de transactions strictement simulées.
- Row Level Security (RLS) activée sur les deux tables.
- Chaque utilisateur ne peut lire/modifier que son propre profil et consulter/créer ses propres opérations de démonstration.
- Les opérations de démonstration sont immuables depuis le client (aucune règle UPDATE/DELETE).

### Mise en place

1. Créer un projet Supabase.
2. Ouvrir **SQL Editor** dans le tableau de bord.
3. Copier puis exécuter le contenu de `supabase/schema.sql`.
4. Récupérer l'URL du projet et la clé **publishable** depuis le panneau Connect. Ne jamais utiliser ni intégrer une clé `service_role` dans l'application Android.
5. Communiquer uniquement l'URL du projet et la clé publishable si nécessaire pour configurer l'application. Ne jamais partager de mot de passe, code de connexion ou secret d'administration.

Le schéma SQL est une fondation ; l'application Android utilise encore son authentification et son stockage locaux. La connexion réelle à Supabase sera faite dans une étape distincte, puis testée avec deux comptes et deux appareils.

## Feuille de route

1. **Backend** — schéma PostgreSQL et isolation des données avec RLS (fondation ajoutée).
2. **Authentification distante** — inscription, connexion, déconnexion et session Supabase.
3. **Profil synchronisé** — mêmes informations sur plusieurs appareils.
4. **Historique synchronisé** — transactions fictives avec statuts et horodatages.
5. **Tests de sécurité** — vérifier qu'un compte ne peut jamais lire les données d'un autre.

## Limites et sécurité

NOVA PAY est un prototype éducatif. Il ne détient pas de fonds, n'émet pas de carte Visa/Mastercard et n'effectue aucun paiement réel. Les soldes et transactions sont fictifs. Une intégration financière réelle exigerait notamment des prestataires autorisés, les validations réglementaires applicables, une architecture serveur dédiée et des audits de sécurité. Ne pas saisir de vraies coordonnées bancaires ni de secrets dans le prototype.

## Compilation

Projet Android Kotlin avec JDK 17 et Android SDK 35. Le workflow GitHub Actions compile l'APK de débogage.

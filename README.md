# Infinity Android (Kotlin + XML)

Projet Android natif modulaire pour Infinity. L'interface est construite avec des vues Android XML, la logique en Kotlin, et les icônes principales sont des Vector Drawables XML (pas des PNG agrandis).

## Modules
- `MainActivity.kt` : coordination de l'écran et des actions.
- `network/InfinityApi.kt` : communication avec le backend Infinity (chat, historique et feedback).
- `data/ChatMessage.kt` : modèle de message.
- `data/LocalChatStore.kt` : historique local.
- `ui/MessageAdapter.kt` : affichage des messages et boutons 👍/👎.
- `res/drawable/*.xml` : icônes vectorielles et formes UI.
- `.github/workflows/android.yml` : compilation automatique de l'APK debug.

## Compiler sur GitHub Actions
1. Crée un dépôt GitHub et téléverse le contenu de ce dossier à la racine du dépôt.
2. Ouvre l'onglet **Actions** et active les workflows si GitHub le demande.
3. Lance **Build Infinity Android APK** avec **Run workflow**, ou pousse un commit sur `main`/`master`.
4. Ouvre l'exécution terminée, puis télécharge l'artifact `Infinity-debug-apk`.
5. L'APK se trouve dans cet artifact sous `app-debug.apk`.

Le workflow compile avec JDK 17 et Gradle, puis publie l'APK debug. Ce n'est pas une APK de production signée.

## Backend
Le client appelle les endpoints existants `/api/chat`, `/api/history` et `/api/feedback`. L'URL du backend est centralisée dans `InfinityApi.kt` afin de pouvoir la remplacer si tu déploies un nouveau domaine.

**Important concernant Google :** la connexion actuelle ouvre le parcours d'authentification web existant dans le navigateur. Pour une connexion Google véritablement native avec une session partagée dans l'application, le backend doit prendre en charge un callback Android (App Link / deep link) et remettre un jeton/session à l'application. Le projet ne prétend donc pas avoir finalisé ce pont OAuth natif. Les appels chat/feedback peuvent être testés en mode invité si le backend les autorise.

## Notes
- Les évaluations et les signalements utilisent `/api/feedback`.
- Les icônes sont des vecteurs XML, donc elles restent nettes à toutes les tailles.
- Les données de conversation de ce squelette sont enregistrées localement sur l'appareil.
- Vérifie les noms des champs SSE renvoyés par `/api/chat` dans `InfinityApi.kt` si le backend change son format.


## Logo, icône et écran de démarrage
- L’icône Android utilise `icon-192.png` et `icon-512.png` du ZIP web original, copiées dans les dossiers mipmap. `icon-static.png` n’est pas utilisé.
- `res/drawable/ic_infinity.xml` est un wrapper XML qui affiche l’image originale sans la redessiner; le contenu du logo reste une image bitmap, pas un tracé vectoriel XML.
- L’écran de démarrage affiche le logo au centre, un cercle animé et « Chargement ».
- Pendant l’attente de la réponse, un point blanc pulse au lieu d’une icône.

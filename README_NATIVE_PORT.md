# Port natif Infinity — première tranche Chat/UI

Cette archive est reconstruite à partir de deux sources fournies : le ZIP web `Infinity_ui-main.zip` et le projet Android existant. Le contenu web original est conservé dans `web-original/` comme référence de comportement et de contrat backend ; il n'est pas embarqué dans une WebView.

## Périmètre de cette tranche

- UI de chat Android native en XML/Views (pas de WebView), identité noire et or, logo original et ressources de lancement séparées.
- Messages natifs via `MessageAdapter.kt` : cette classe couvre la liste et les bulles de messages; `activity_main.xml` et `MainActivity.kt` couvrent l'écran et la saisie.
- `ChatViewModel.kt`, `ChatUiState.kt`, `ChatRepository.kt`, `ConversationStore.kt` pour l'état et l'orchestration. L'ID actif est gardé en mémoire; aucun historique ou message n'est persisté localement.
- `AIService.kt` et `ConversationService.kt` pour le backend partagé avec le Web.
- `TypingIndicator.kt` : point blanc animé, texte fixe « Infinity réfléchit… » et balayage lumineux sur le texte.
- `InfinityNotificationManager.kt`, `NotificationChannels.kt`, `NetworkMonitor.kt` pour notifications et connectivité.
- Icônes vectorielles/réutilisation des ressources existantes, et PNG original conservé.

## Contrat backend vérifié dans `web-original/script.js`

- Base URL : `https://aur-x-backend.vercel.app`
- Nouvelle conversation : `POST /api/conversations` avec credentials/cookies de session; l'identifiant provient de `convId`, `id` ou `conversation.id` dans la réponse.
- Message : `POST /api/chat`, JSON `{ "message": ..., "convId": ... }`, credentials de session; réponse lue comme flux SSE avec lignes `data:`.
- Les événements de recherche reconnus sont `search_started`, `search_completed` et `search_unavailable`.
- Les fragments de texte acceptés sont `content`, `delta.content` et `choices[0].delta.content`; `[DONE]` marque la fin du flux.
- Historique serveur : `GET /api/history`; feedback : `POST /api/feedback`.

L'ID de conversation est créé par le serveur, jamais synthétisé par Android. Les cookies de session et le `convId` actif restent en mémoire uniquement. La copie du site Web conserve son propre code original inchangé, y compris les fonctions web hors du périmètre natif de cette première tranche.

## Configuration / compilation

Les fichiers Gradle existants ont été conservés. La compilation doit être lancée par le workflow GitHub Actions du dépôt ou depuis un environnement Android/Gradle configuré. Cette archive n'inclut pas de wrapper Gradle qui n'était pas présent dans les ZIP sources fournis.

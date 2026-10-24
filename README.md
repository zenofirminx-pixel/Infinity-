# Infinity Android Native

Projet Android natif Kotlin/XML pour Infinity.

## Fonctions incluses dans cette version
- Requête `POST` JSON vers `/api/chat`.
- Lecture SSE (`text/event-stream`) dans le fichier dédié `network/StreamingResponseReader.kt`, avec affichage progressif des fragments de réponse.
- Réponses JSON non-streaming toujours prises en charge.
- Affichage de messages dans une liste RecyclerView (sans ScrollView imbriqué).
- Barre latérale avec historique local des conversations et bouton nouvelle conversation.
- Demande de permission de notifications sur Android 13 et versions ultérieures.
- Workflow GitHub Actions qui compile et publie l'APK debug en artefact.

## Configuration du serveur
L'URL actuelle est configurée dans `app/src/main/java/com/infinity/app/network/InfinityApi.kt`, dans `BASE_URL`. Vérifie qu'elle correspond au domaine actuellement déployé de ton backend avant de compiler. L'appel chat utilise `POST` avec un corps JSON `{ "message": "...", "convId": "..." }` quand un identifiant de conversation est fourni.

## Compilation
GitHub Actions lance `gradle --no-daemon assembleDebug`. L'APK debug est publiée dans l'artefact `Infinity-debug-apk`.

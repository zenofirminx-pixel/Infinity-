# Infinity — port Android natif (état de livraison)

Ce ZIP contient le projet Android Kotlin/XML de départ et les améliorations natives apportées à partir des deux archives fournies. Aucun fichier Gradle, `settings.gradle.kts`, `gradle.properties` ou workflow de compilation n’a été modifié.

## Structure actuelle
- `app/src/main/java/com/infinity/app/MainActivity.kt` : cycle de vie de l’écran de chat et orchestration.
- `data/ChatMessage.kt`, `data/LocalChatStore.kt` : modèle et persistance locale.
- `network/InfinityApi.kt` : API chat (y compris SSE), historique et feedback.
- `ui/MessageAdapter.kt` : rendu des bulles et votes.
- `ui/TypingIndicatorView.kt` : point de réflexion natif animé, isolé dans son propre fichier.
- `SplashActivity.kt` et `res/layout/activity_splash.xml` : écran de démarrage avec le symbole seul.

## Limites importantes à connaître
Ce livrable est un port natif initial fonctionnel, pas encore une parité exhaustive avec toutes les fonctions du site web. Le ZIP web a été inspecté et contient aussi des parcours de conditions d’utilisation, installation PWA, conversations serveur, paramètres (modèle/ton), profil/Google, bibliothèque/projets, signalement et rendu Markdown/KaTeX/Highlight.js. Plusieurs de ces parcours ne sont pas encore reproduits dans l’application Android actuelle. Le login Google ouvre encore le parcours web et n’établit pas une authentification OAuth Android native.

Le backend configuré est `https://aur-x-backend.vercel.app`; vérifie qu’il s’agit bien du backend voulu et que ses contrats JSON/SSE correspondent à `InfinityApi.kt`. Les conversations Android présentes sont enregistrées localement; la synchronisation complète des conversations du site doit encore être raccordée.

## Vérification
Aucun wrapper Gradle n’est inclus dans le projet source. Le ZIP est fourni comme source à examiner/intégrer; ne le considère pas comme un APK compilé ni comme une parité fonctionnelle complète.

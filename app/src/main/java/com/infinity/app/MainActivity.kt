package com.infinity.app

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.infinity.app.data.ChatMessage
import com.infinity.app.data.LocalChatStore
import com.infinity.app.network.InfinityApi
import com.infinity.app.ui.MessageAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private val api = InfinityApi()
    private lateinit var store: LocalChatStore
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: MessageAdapter
    private lateinit var chat: RecyclerView
    private lateinit var welcome: View
    private lateinit var input: EditText
    private lateinit var send: MaterialButton
    private lateinit var status: TextView
    private lateinit var searchStatus: TextView
    private var busy = false
    private var lastQuestion: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        store = LocalChatStore(this)
        chat = findViewById(R.id.chatContainer)
        welcome = findViewById(R.id.welcomePanel)
        input = findViewById(R.id.messageInput)
        send = findViewById(R.id.sendButton)
        status = findViewById(R.id.statusText)
        searchStatus = findViewById(R.id.searchStatus)
        messages.addAll(store.load())
        adapter = MessageAdapter(messages, ::rateAnswer)
        chat.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = false }
        chat.adapter = adapter
        updateWelcome()
        send.setOnClickListener { sendMessage() }
        input.setOnEditorActionListener { _, _, _ -> sendMessage(); true }
        findViewById<View>(R.id.historyButton).setOnClickListener { showHistory() }
        findViewById<View>(R.id.menuButton).setOnClickListener { showMenu() }
    }

    private fun sendMessage() {
        val question = input.text.toString().trim()
        if (question.isEmpty() || busy) return
        input.setText("")
        lastQuestion = question
        addMessage(ChatMessage(question, true))
        busy = true
        send.isEnabled = false
        status.text = "Infinity réfléchit…"
        val waiting = ChatMessage("●", false)
        addMessage(waiting)
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    api.sendMessage(question, onSearch = { searching ->
                        runOnUiThread {
                            searchStatus.visibility = if (searching) View.VISIBLE else View.GONE
                            if (searching) searchStatus.text = "Recherche web en cours…"
                        }
                    })
                }
                val index = messages.indexOf(waiting)
                if (index >= 0) {
                    messages[index] = ChatMessage(result.answer, false, question)
                    adapter.notifyItemChanged(index)
                } else addMessage(ChatMessage(result.answer, false, question))
                persist()
                status.text = "Votre assistant IA"
            } catch (e: Exception) {
                val index = messages.indexOf(waiting)
                val error = "Je n'ai pas pu joindre le serveur. Vérifie ta connexion puis réessaie.\n\nDétail : ${e.message ?: "erreur réseau"}"
                if (index >= 0) {
                    messages[index] = ChatMessage(error, false, question)
                    adapter.notifyItemChanged(index)
                } else addMessage(ChatMessage(error, false, question))
                persist()
                status.text = "Connexion indisponible"
            } finally {
                searchStatus.visibility = View.GONE
                busy = false
                send.isEnabled = true
                hideKeyboard()
            }
        }
    }

    private fun addMessage(message: ChatMessage) {
        messages.add(message)
        adapter.notifyItemInserted(messages.lastIndex)
        chat.scrollToPosition(messages.lastIndex)
        updateWelcome()
        persist()
    }

    private fun updateWelcome() {
        welcome.visibility = if (messages.isEmpty()) View.VISIBLE else View.GONE
        chat.visibility = if (messages.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun persist() = store.save(messages)

    private fun rateAnswer(position: Int, vote: String) {
        if (position !in messages.indices) return
        val item = messages[position]
        if (item.rated) return
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    api.sendFeedback("rating", rating = vote, question = item.questionForRating, answer = item.text)
                }
                item.rated = true
                adapter.notifyItemChanged(position)
                Toast.makeText(this@MainActivity, "Merci pour ton retour !", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Évaluation non envoyée. Réessaie plus tard.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showMenu() {
        val options = arrayOf("Se connecter avec Google", "Signaler un bug", "Nouvelle conversation", "Effacer l'historique local", "À propos d'Infinity")
        AlertDialog.Builder(this).setTitle("Infinity").setItems(options) { _, which ->
            when (which) {
                0 -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("${InfinityApi.BASE_URL}/api/auth/google")))
                1 -> showBugDialog()
                2 -> { messages.clear(); adapter.notifyDataSetChanged(); persist(); updateWelcome() }
                3 -> AlertDialog.Builder(this).setTitle("Effacer l'historique ?")
                    .setMessage("Cette action supprimera les conversations enregistrées sur cet appareil.")
                    .setNegativeButton("Annuler", null).setPositiveButton("Effacer") { _, _ ->
                        messages.clear(); store.clear(); adapter.notifyDataSetChanged(); updateWelcome()
                    }.show()
                4 -> AlertDialog.Builder(this).setTitle("À propos d'Infinity")
                    .setMessage("Infinity est ton assistant IA. Vérifie les réponses, car l'IA peut faire des erreurs.")
                    .setPositiveButton("Fermer", null).show()
            }
        }.show()
    }

    private fun showBugDialog() {
        val field = EditText(this).apply { hint = "Décris le problème rencontré"; minLines = 3; maxLines = 7 }
        AlertDialog.Builder(this).setTitle("Signaler un bug")
            .setMessage("Décris ce qui ne fonctionne pas. Le signalement sera envoyé à l'équipe Infinity.")
            .setView(field)
            .setNegativeButton("Annuler", null)
            .setPositiveButton("Envoyer") { _, _ ->
                val description = field.text.toString().trim()
                if (description.isBlank()) {
                    Toast.makeText(this, "Ajoute une description du problème.", Toast.LENGTH_SHORT).show()
                } else {
                    lifecycleScope.launch {
                        try {
                            withContext(Dispatchers.IO) { api.sendFeedback("bug", description = description) }
                            Toast.makeText(this@MainActivity, "Signalement envoyé. Merci !", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, "Envoi impossible. Réessaie plus tard.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }.show()
    }

    private fun showHistory() {
        val local = if (messages.isEmpty()) "Aucun message enregistré sur cet appareil." else
            messages.takeLast(20).joinToString("\n\n") { (if (it.fromUser) "Vous : " else "Infinity : ") + it.text.take(180) }
        AlertDialog.Builder(this).setTitle("Historique récent").setMessage(local)
            .setPositiveButton("Fermer", null).show()
    }

    private fun hideKeyboard() {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.hideSoftInputFromWindow(input.windowToken, 0)
        input.clearFocus()
    }
}

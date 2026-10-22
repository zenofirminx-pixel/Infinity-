package com.infinity.app.ui

import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.infinity.app.R
import com.infinity.app.data.ChatMessage

class MessageAdapter(
    private val items: MutableList<ChatMessage>,
    private val onRate: (Int, String) -> Unit
) : RecyclerView.Adapter<MessageAdapter.MessageHolder>() {
    class MessageHolder(val root: LinearLayout) : RecyclerView.ViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        val root = LinearLayout(parent.context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(4)
            layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        return MessageHolder(root)
    }

    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        val item = items[position]
        val ctx = holder.root.context
        holder.root.removeAllViews()
        holder.root.gravity = if (item.fromUser) Gravity.END else Gravity.START
        val bubble = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16)
            background = ctx.getDrawable(if (item.fromUser) R.drawable.bg_message_user else R.drawable.bg_message_bot)
            layoutParams = LinearLayout.LayoutParams(
                (ctx.resources.displayMetrics.widthPixels * if (item.fromUser) .84f else .96f).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 8 }
        }
        val isTypingDot = !item.fromUser && item.text == "●" && item.questionForRating == null
        if (isTypingDot) {
            bubble.addView(TypingIndicatorView(ctx), LinearLayout.LayoutParams(
                (28 * ctx.resources.displayMetrics.density).toInt(),
                (28 * ctx.resources.displayMetrics.density).toInt()
            ))
        } else {
            val text = TextView(ctx).apply {
                this.text = item.text
                textSize = 16f
                setTextColor(ctx.getColor(R.color.infinity_text))
                setLineSpacing(3f, 1.12f)
                setTextIsSelectable(true)
            }
            bubble.addView(text)
        }
        if (!item.fromUser && item.questionForRating != null) {
            val row = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.END }
            listOf("👍" to "up", "👎" to "down").forEach { (label, value) ->
                val button = Button(ctx).apply {
                    this.text = label
                    textSize = 16f
                    isAllCaps = false
                    isEnabled = !item.rated
                    minWidth = 48
                    setPadding(4)
                    setOnClickListener { onRate(position, value) }
                }
                row.addView(button)
            }
            bubble.addView(row)
        }
        holder.root.addView(bubble)
    }

    override fun getItemCount() = items.size
}

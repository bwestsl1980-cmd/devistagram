package com.bethwestsl.devistagram.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bethwestsl.devistagram.R
import com.bethwestsl.devistagram.model.Note
import java.text.SimpleDateFormat
import java.util.*

class NotesAdapter(
    private val onNoteClick: (Note) -> Unit,
    private val onNoteLongClick: (Note) -> Boolean,
    private val isSelectionMode: () -> Boolean,
    private val isNoteSelected: (Note) -> Boolean,
    private val onSelectionToggle: (Note) -> Unit
) : ListAdapter<Note, NotesAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkBox: CheckBox = itemView.findViewById(R.id.noteCheckBox)
        private val senderTextView: TextView = itemView.findViewById(R.id.noteSenderTextView)
        private val subjectTextView: TextView = itemView.findViewById(R.id.noteSubjectTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.noteTimestampTextView)
        private val unreadIndicator: View = itemView.findViewById(R.id.unreadIndicator)

        fun bind(note: Note) {
            // Show checkbox only in selection mode
            checkBox.visibility = if (isSelectionMode()) View.VISIBLE else View.GONE
            checkBox.isChecked = isNoteSelected(note)

            // Sender
            senderTextView.text = note.originator?.username ?: "Unknown"

            // Subject
            subjectTextView.text = note.subject ?: "(No Subject)"

            // Timestamp
            timestampTextView.text = formatTimestamp(note.timestamp)

            // Unread indicator
            unreadIndicator.visibility = if (note.isRead == false) View.VISIBLE else View.GONE

            // Click handlers
            itemView.setOnClickListener {
                if (isSelectionMode()) {
                    onSelectionToggle(note)
                    checkBox.isChecked = isNoteSelected(note)
                } else {
                    onNoteClick(note)
                }
            }

            itemView.setOnLongClickListener {
                onNoteLongClick(note)
            }

            checkBox.setOnClickListener {
                onSelectionToggle(note)
            }
        }

        private fun formatTimestamp(timestamp: String?): String {
            if (timestamp == null) return ""

            return try {
                // Parse ISO 8601 timestamp
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val date = sdf.parse(timestamp)

                if (date != null) {
                    DateUtils.getRelativeTimeSpanString(
                        date.time,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    ).toString()
                } else {
                    timestamp
                }
            } catch (e: Exception) {
                timestamp
            }
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.noteId == newItem.noteId
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}


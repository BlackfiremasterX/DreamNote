package com.omgodse.notally.recyclerview.viewholders

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.omgodse.notally.R
import com.omgodse.notally.databinding.ListItemPreviewBinding
import com.omgodse.notally.databinding.RecyclerBaseNoteBinding
import com.omgodse.notally.helpers.SettingsHelper
import com.omgodse.notally.miscellaneous.applySpans
import com.omgodse.notally.miscellaneous.bindLabels
import com.omgodse.notally.recyclerview.ItemListener
import com.omgodse.notally.room.BaseNote
import com.omgodse.notally.room.Type
import org.ocpsoft.prettytime.PrettyTime
import java.text.SimpleDateFormat
import java.util.*

class BaseNoteVH(
    private val binding: RecyclerBaseNoteBinding,
    private val settingsHelper: SettingsHelper,
    private val itemListener: ItemListener,
    private val prettyTime: PrettyTime,
    private val formatter: SimpleDateFormat,
    private val inflater: LayoutInflater,
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.Note.maxLines = settingsHelper.getMaxLines()

        binding.CardView.setOnClickListener {
            itemListener.onClick(adapterPosition)
        }

        binding.CardView.setOnLongClickListener {
            itemListener.onLongClick(adapterPosition)
            return@setOnLongClickListener true
        }
    }

    fun bind(baseNote: BaseNote) {
        when (baseNote.type) {
            Type.NOTE -> bindNote(baseNote)
            Type.LIST -> bindList(baseNote)
        }

        binding.LabelGroup.bindLabels(baseNote.labels)

        val date = Date(baseNote.timestamp)
        when (settingsHelper.getDateFormat()) {
            binding.root.context.getString(R.string.relativeKey) -> {
                binding.Date.visibility = View.VISIBLE
                binding.Date.text = prettyTime.format(date)
            }
            binding.root.context.getString(R.string.absoluteKey) -> {
                binding.Date.visibility = View.VISIBLE
                binding.Date.text = formatter.format(date)
            }
            else -> binding.Date.visibility = View.GONE
        }

        if (isEmpty(baseNote)) {
            binding.Note.setText(getEmptyMessage(baseNote))
            binding.Note.isVisible = true
        }
    }

    private fun bindNote(note: BaseNote) {
        binding.LinearLayout.isVisible = false

        binding.Title.text = note.title
        binding.Note.text = note.body.applySpans(note.spans)

        binding.Title.isVisible = note.title.isNotEmpty()
        binding.Note.isVisible = note.body.isNotEmpty()
    }

    private fun bindList(list: BaseNote) {
        binding.Note.isVisible = false
        binding.LinearLayout.isVisible = true

        binding.Title.text = list.title
        binding.Title.isVisible = list.title.isNotEmpty()

        val maxItems = settingsHelper.getMaxItems()
        val filteredList = list.items.take(maxItems)

        binding.LinearLayout.removeAllViews()

        for (item in filteredList) {
            val view = ListItemPreviewBinding.inflate(inflater).root
            view.text = item.body
            handleChecked(view, item.checked)
            binding.LinearLayout.addView(view)
        }

        if (list.items.size > maxItems) {
            val view = ListItemPreviewBinding.inflate(inflater).root
            val itemsRemaining = list.items.size - maxItems
            view.text = if (itemsRemaining == 1) {
                binding.root.context.getString(R.string.one_more_item)
            } else binding.root.context.getString(R.string.more_items, itemsRemaining)
            binding.LinearLayout.addView(view)
        }

        binding.LinearLayout.isVisible = list.items.isNotEmpty()
    }


    private fun isEmpty(baseNote: BaseNote): Boolean {
        return when (baseNote.type) {
            Type.NOTE -> baseNote.title.isBlank() && baseNote.body.isBlank()
            Type.LIST -> baseNote.title.isBlank() && baseNote.items.isEmpty()
        }
    }

    private fun getEmptyMessage(baseNote: BaseNote): Int {
        return when (baseNote.type) {
            Type.NOTE -> R.string.empty_note
            Type.LIST -> R.string.empty_list
        }
    }

    private fun handleChecked(textView: TextView, checked: Boolean) {
        if (checked) {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.checkbox_16, 0, 0, 0)
        } else textView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.checkbox_outline_16, 0, 0, 0)
        textView.paint.isStrikeThruText = checked
    }
}
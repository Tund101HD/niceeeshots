package com.lucaperri.niceeshotss.utils.tasks

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lucaperri.niceeshotss.R

class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val titleTextView: TextView = itemView.findViewById(R.id.ui_task_tasktitle)
    val descriptionTextView: TextView = itemView.findViewById(R.id.ui_task_taskdescription)
    val expTextView : TextView = itemView.findViewById(R.id.ui_task_taskexp)
}
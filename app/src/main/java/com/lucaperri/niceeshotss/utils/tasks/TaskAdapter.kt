package com.lucaperri.niceeshotss.utils.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lucaperri.niceeshotss.R

class TaskAdapter(private val itemList: List<Task>) : RecyclerView.Adapter<TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.task_card,
        parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder,
                                  position: Int) {
        val item = itemList[position]
        holder.titleTextView.text = item.title
        holder.descriptionTextView.text = item.description
    }

    override fun getItemCount():
    Int = itemList.size
}
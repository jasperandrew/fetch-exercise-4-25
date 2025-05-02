package com.example.fetchexercise

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class ItemListAdapter(ctx: Context, itemList: List<Item>)
    : ArrayAdapter<Item>(ctx, 0, itemList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val holder: ItemViewHolder
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
            holder = ItemViewHolder(view)
            view.tag = holder
        } else {
            holder = view.tag as ItemViewHolder
        }

        val item = getItem(position) ?: return view!!

        holder.idView.text = item.id.toString()
        holder.listIdView.text = item.listId.toString()
        holder.nameView.text = item.name

        return view!!
    }

    inner class ItemViewHolder(view: View) {
        val idView: TextView = view.findViewById(R.id.item_id)
        val listIdView: TextView = view.findViewById(R.id.item_list_id)
        val nameView: TextView = view.findViewById(R.id.item_name)
    }
}
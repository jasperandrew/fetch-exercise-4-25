package com.example.fetchexercise

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fetchexercise.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var itemList: MutableList<Item> = mutableListOf()
    private lateinit var itemListAdapter: ItemListAdapter

    companion object {
        const val DATA_URL = "https://hiring.fetch.com/hiring.json"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        itemListAdapter = ItemListAdapter(this, itemList)
        binding.list.adapter = itemListAdapter

        refreshItemlist()
    }

    private fun refreshItemlist() {
        lifecycleScope.launch(Dispatchers.IO) {
            val jsonString: String
            try {
                jsonString = retrieveJSON()
            } catch (e: Exception) {
                toast(R.string.network_error)
                return@launch
            }

            val jsonArray: JSONArray
            try {
                jsonArray = JSONArray(jsonString)
            } catch (e: Exception) {
                toast(R.string.json_error)
                return@launch
            }

            val items = mutableListOf<Item>()
            for(i in 0..jsonArray.length()) {
                jsonArray.optJSONObject(i)?.run {
                    val id = optInt("id", -1)
                    val listId = optInt("listId", -1)
                    if (id != -1 && listId != -1)
                        items.add(Item(id, listId, optString("name")))
                }
            }

            itemList = items.filter { it.name != "null" && it.name != "" }.toMutableList() // "Filter out any items where "name" is blank or null"
            updateListView()
        }
    }

    private fun toast(msgResId: Int) = runOnUiThread {
        Toast.makeText(this@MainActivity, getString(msgResId), Toast.LENGTH_LONG).show()
    }

    private fun retrieveJSON(): String {
        val reader = BufferedReader(InputStreamReader(URL(DATA_URL).openConnection().getInputStream()))
        var line: String?
        val jsonData = StringBuilder()
        while (reader.readLine().also { line = it } != null) {
            jsonData.append(line)
        }
        reader.close()
        return jsonData.toString()
    }

    private fun updateListView() = runOnUiThread {
        if (groupByListID) {
            // Does "Display all the items grouped by "listId"" mean something different
            //   than "Sort the results first by "listId" ..."?
            // I thought about doing a "sticky" group header system, but it would involve creating a
            //   separate list adapter with a bunch of custom logic (or using a 3rd-party library),
            //   and I decided it was outside the scope of this exercise
            itemList.sortWith(
                compareBy<Item> { it.listId }
                .thenBy {
                    if (sortBy == SortBy.NAME) it.name
                    else it.id
                })
        } else {
            itemList.sortWith(compareBy {
                when (sortBy) {
                    SortBy.NAME -> it.name
                    SortBy.ID -> it.id
                    SortBy.LISTID -> it.listId
                }
            })
        }

        if (sortDescending)
            itemList.reverse()

        itemListAdapter.updateList(itemList)
    }

    private lateinit var listSortItem: MenuItem
    private lateinit var nameSortItem: MenuItem

    enum class SortBy { ID, LISTID, NAME }

    private var groupByListID = true
    private var sortDescending = false
    private var sortBy = SortBy.NAME

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        listSortItem = menu.findItem(R.id.action_sort_listid)
        nameSortItem = menu.findItem(R.id.action_sort_name)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.isCheckable)
            item.setChecked(!item.isChecked)

        var update = false

        when (item.itemId) {
            R.id.action_group_listid -> {
                groupByListID = item.isChecked
                listSortItem.apply {
                    isVisible = !item.isChecked
                    if (!isVisible && isChecked) {
                        isChecked = false
                        nameSortItem.setChecked(true)
                        sortBy = SortBy.NAME
                    }
                }
                update = true
            }
            R.id.action_sort_ascending -> {
                sortDescending = false
                update = true
            }
            R.id.action_sort_descending -> {
                sortDescending = true
                update = true
            }
            R.id.action_sort_id -> {
                sortBy = SortBy.ID
                update = true
            }
            R.id.action_sort_listid -> {
                sortBy = SortBy.LISTID
                update = true
            }
            R.id.action_sort_name -> {
                sortBy = SortBy.NAME
                update = true
            }
        }

        if (update) {
            updateListView()
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}
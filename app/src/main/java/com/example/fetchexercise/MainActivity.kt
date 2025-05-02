package com.example.fetchexercise

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        lifecycleScope.launch(Dispatchers.IO) {
            val jsonString = dataFromUrl("https://hiring.fetch.com/hiring.json") // todo: catch network errors
            val jsonArray = JSONArray(jsonString) // todo: catch json errors

            val itemList = mutableListOf<Item>()
            for(i in 0..jsonArray.length()) {
                jsonArray.optJSONObject(i)?.run {
                    val id = optInt("id", -1)
                    val listId = optInt("listId", -1)
                    val name = optString("name")
                    if (id == -1 || listId == -1 || name == "null" || name == "") return@run // "Filter out any items where "name" is blank or null"
                    itemList.add(Item(id, listId, name))
                }
            }

            // Does "Display all the items grouped by "listId"" mean something different
            //   than "Sort the results first by "listId" ..."?
            itemList.sortWith(compareBy<Item> { it.listId }.thenBy { it.name })

            runOnUiThread {
                binding.list.adapter = ItemListAdapter(this@MainActivity, itemList)
            }
        }
    }

    private fun dataFromUrl(url: String): String {
        val reader = BufferedReader(InputStreamReader(URL(url).openConnection().getInputStream()))
        var line: String?
        val jsonData = StringBuilder()
        while (reader.readLine().also { line = it } != null) {
            jsonData.append(line)
        }
        reader.close()
        return jsonData.toString()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
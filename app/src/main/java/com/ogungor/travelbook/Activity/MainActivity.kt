package com.ogungor.travelbook.Activity

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.graphics.createBitmap
import com.ogungor.travelbook.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var database: SQLiteDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var locationList = ArrayList<String>()
        var locationIdList = ArrayList<Int>()

        val listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, locationList)
        listview.adapter = listAdapter
        database = this.openOrCreateDatabase("TravelBook", Context.MODE_PRIVATE, null)
        database.execSQL("CREATE TABLE IF NOT EXISTS travelBook (id INTEGER PRIMARY KEY,locationname VARCHAR,locationlat VARCHAR,locationlon VARCHAR)")

        var cursor = database.rawQuery("SELECT * FROM travelbook", null)
        var locationNameIx = cursor.getColumnIndex("locationname")
        var locationIdIx = cursor.getColumnIndex("id")


        while (cursor.moveToNext()) {
            locationList.add(cursor.getString(locationNameIx))
            locationIdList.add(cursor.getInt(locationIdIx))
            println("list: "+ locationList[locationIdIx])
        }

        cursor.close()
        listAdapter.notifyDataSetChanged()

        listview.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

            println("id: " + locationIdList[i] + "name : " + locationList[i])
            var listItemIntent = Intent(this, MapsActivity::class.java)
            listItemIntent.putExtra("locationId", locationIdList[i])
            listItemIntent.putExtra("info", "old")
            startActivity(listItemIntent)


        }


    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var menuInflater = menuInflater
        menuInflater.inflate(R.menu.add_location, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_location_item) {
            val addIntent = Intent(this, MapsActivity::class.java)
            addIntent.putExtra("info", "new")
            startActivity(addIntent)
        }
        return super.onOptionsItemSelected(item)
    }


}
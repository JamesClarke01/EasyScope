/*Adapted from https://youtu.be/4tUBF8ZjAUo?si=HnG7HR7fDdvCE7K8*/

package com.james.telescopeapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import org.json.JSONObject

class StarDBHelper(private val context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    companion object {
        private const val DATABASE_NAME = "stars.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "stars"
        private const val COL_ID = "id"
        private const val COL_NAME = "name"
        private const val COL_RA = "ra"
        private const val COL_DEC = "dec"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = "CREATE TABLE $TABLE_NAME (   $COL_ID INTEGER PRIMARY KEY, " +
                                                            "$COL_NAME TEXT, " +
                                                            "$COL_RA DOUBLE," +
                                                            "$COL_DEC DOUBLE)"
        db?.execSQL(createTableQuery)
        insertInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME"
        db?.execSQL(dropTableQuery)
        onCreate(db)
    }

    private fun insertInitialData(db: SQLiteDatabase?) {
        val values = ContentValues()

        val jsonFileName = "StarData.json"
        val inputStream = context.assets.open(jsonFileName)
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        val jsonString = String(buffer, Charsets.UTF_8)
        val jsonObject = JSONObject(jsonString)

        val starArray:JSONArray? = jsonObject.optJSONArray("Stars")

        if (starArray != null) {
            for (i in 0 until starArray.length()) {
                val star = starArray.getJSONObject(i)
                values.put(COL_NAME, star.getString("Name"))
                values.put(COL_RA, star.getDouble("RA"))
                values.put(COL_DEC, star.getDouble("DEC"))
                db?.insert(TABLE_NAME, null, values)
            }
        }


        /*
        values.put(COL_NAME, "Polaris")
        values.put(COL_RA, 3.06)
        values.put(COL_DEC, 89.36572)
        db?.insert(TABLE_NAME, null, values)

        values.put(COL_NAME, "Alderamin")
        values.put(COL_RA, 18.616)
        values.put(COL_DEC, 62.6903)
        db?.insert(TABLE_NAME, null, values)

        values.put(COL_NAME, "Capella")
        values.put(COL_RA, 5.3)
        values.put(COL_DEC, 46.0217)
        db?.insert(TABLE_NAME, null, values)
         */
    }

    fun getAllStars(): List<Star> {
        val starList = mutableListOf<Star>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME))
            val ra = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_RA))
            val dec = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_DEC))

            val star = Star(id, name, ra, dec)
            starList.add(star)
        }
        cursor.close()
        db.close()
        return starList
    }

    /*
    fun insertStar(star: Star) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, star.name)
            put(COLUMN_RA, star.ra)
            put(COLUMN_DEC, star.dec)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }
     */
}
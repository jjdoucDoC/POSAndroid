package com.example.posapp.repository

import android.content.ContentValues
import android.content.Context
import com.example.posapp.Databases
import com.example.posapp.Databases.Companion.CATEGORIES_TABLE
import com.example.posapp.Databases.Companion.CATEGORY_COLUMN_ID
import com.example.posapp.Databases.Companion.CATEGORY_COLUMN_NAME
import com.example.posapp.Databases.Companion.PRODUCTS_TABLE
import com.example.posapp.Databases.Companion.PRODUCT_COLUMN_CATEGORY
import com.example.posapp.models.Categories

class CategoryRepository(private val db: Databases) {
    companion object {
        var instance: CategoryRepository? = null
        @Synchronized
        fun getInstance(context: Context): CategoryRepository {
            return if (instance == null) {
                CategoryRepository(Databases.getInstance(context)).also {
                    instance = it
                }
            } else {
                instance!!
            }
        }
    }

    // Add Category
    fun insertCategory(category: Categories): Boolean {
        val db = db.writableDatabase
        val values = ContentValues().apply {
            put(CATEGORY_COLUMN_NAME, category.name)
        }
        val result = db.insert(CATEGORIES_TABLE, null, values)
        db.close()

        return result != -1L
    }

    // Get All Category
    fun getCategory(): List<Categories> {
        val categoryList = mutableListOf<Categories>()
        val db = db.readableDatabase
        val query = "SELECT * FROM $CATEGORIES_TABLE"
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(CATEGORY_COLUMN_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY_COLUMN_NAME))

            val cat = Categories(id, name)
            categoryList.add(cat)
        }

        cursor.close()
        db.close()

        return categoryList
    }

    // Get Category ID by Name
    fun getCategoryIdByName(categoryName: String): Int? {
        val db = db.readableDatabase
        val query =
            "SELECT $CATEGORY_COLUMN_ID FROM $CATEGORIES_TABLE WHERE $CATEGORY_COLUMN_NAME = ?"
        val cursor = db.rawQuery(query, arrayOf(categoryName))

        var categoryId: Int? = null
        if (cursor.moveToFirst()) {
            categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(CATEGORY_COLUMN_ID))
        }

        cursor.close()
        db.close()
        return categoryId
    }

    // Get Details Category by ID
    fun getCategoryByID(categoryId: Int): Categories {
        val db = db.readableDatabase
        val query = "SELECT * FROM $CATEGORIES_TABLE WHERE $CATEGORY_COLUMN_ID = $categoryId"
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()

        val id = cursor.getInt(cursor.getColumnIndexOrThrow(CATEGORY_COLUMN_ID))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY_COLUMN_NAME))

        cursor.close()
        db.close()
        return Categories(id, name)
    }

    // Update Category
    fun updateCategory(category: Categories): Boolean {
        val db = db.writableDatabase
        val values = ContentValues().apply {
            put(CATEGORY_COLUMN_NAME, category.name)
        }
        val whereClause = "$CATEGORY_COLUMN_ID = ?"
        val whereArgs = arrayOf(category.id.toString())
        val result = db.update(CATEGORIES_TABLE, values, whereClause, whereArgs)
        db.close()

        return result > 0
    }

    // Delete Category
    fun deleteCategory(categoryId: Int): Boolean {
        val db = db.writableDatabase

        val deleteProductQuery = "DELETE FROM $PRODUCTS_TABLE WHERE $PRODUCT_COLUMN_CATEGORY = ?"
        db.execSQL(deleteProductQuery, arrayOf(categoryId.toString()))

        val whereClause = "$CATEGORY_COLUMN_ID = ?"
        val whereArgs = arrayOf(categoryId.toString())
        val result = db.delete(CATEGORIES_TABLE, whereClause, whereArgs)
        db.close()

        return result > 0
    }
}
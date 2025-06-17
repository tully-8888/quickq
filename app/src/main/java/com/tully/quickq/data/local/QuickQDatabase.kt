package com.tully.quickq.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tully.quickq.data.local.dao.JobDao
import com.tully.quickq.data.local.entity.JobEntity

@Database(entities = [JobEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class QuickQDatabase : RoomDatabase() {
    abstract fun jobDao(): JobDao
}

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
} 
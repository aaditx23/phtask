package com.aaditx23.phtask.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.aaditx23.phtask.data.local.Course.CourseDao
import com.aaditx23.phtask.data.local.Course.Entity.CourseEntity

@Database(
    entities = [
        CourseEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun courseDao(): CourseDao
}
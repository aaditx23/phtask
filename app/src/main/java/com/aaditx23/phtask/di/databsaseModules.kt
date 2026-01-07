package com.aaditx23.phtask.di

import androidx.room.Room
import com.aaditx23.phtask.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "phtask_database"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    single {
        get<AppDatabase>().courseDao()
    }
}
package com.aaditx23.phtask.di

import com.aaditx23.phtask.presentation.screens.CourseList.CourseListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::CourseListViewModel)
}
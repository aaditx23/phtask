package com.aaditx23.phtask.di

import com.aaditx23.phtask.domain.usecase.EnrollCourseUseCase
import com.aaditx23.phtask.domain.usecase.GetCourseByIdUseCase
import com.aaditx23.phtask.domain.usecase.GetCoursesUseCase
import com.aaditx23.phtask.domain.usecase.SearchCoursesUseCase
import com.aaditx23.phtask.domain.usecase.UnenrollCourseUseCase
import com.aaditx23.phtask.presentation.screens.CourseDetails.CourseDetailViewModel
import com.aaditx23.phtask.presentation.screens.CourseList.CourseListViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // Use Cases
    factoryOf(::GetCoursesUseCase)
    factoryOf(::SearchCoursesUseCase)
    factoryOf(::GetCourseByIdUseCase)
    factoryOf(::EnrollCourseUseCase)
    factoryOf(::UnenrollCourseUseCase)

    // ViewModels
    viewModelOf(::CourseListViewModel)
    viewModelOf(::CourseDetailViewModel)

}
package com.aaditx23.phtask.presentation.screens.CourseList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaditx23.phtask.data.remote.repository.CourseRepository
import kotlinx.coroutines.launch

class CourseListViewModel(
    private val repository: CourseRepository
): ViewModel() {

    fun getList(){
        viewModelScope.launch {
            repository.getCourses()
        }
    }
}
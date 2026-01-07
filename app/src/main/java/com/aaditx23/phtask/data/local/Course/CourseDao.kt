package com.aaditx23.phtask.data.local.Course

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aaditx23.phtask.data.local.Course.Entity.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    @Query("SELECT * FROM courses ORDER BY title ASC")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE courseId = :courseId")
    fun getCourseById(courseId: String): Flow<CourseEntity?>

    @Query("""
        SELECT * FROM courses 
        WHERE title LIKE '%' || :query || '%' 
           OR tags LIKE '%' || :query || '%'
        ORDER BY title ASC
    """)
    fun searchCourses(query: String): Flow<List<CourseEntity>>

    @Query("UPDATE courses SET is_enrolled = :isEnrolled WHERE course_id = :courseId")
    suspend fun updateEnrollmentStatus(courseId: String, isEnrolled: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Query("DELETE FROM courses")
    suspend fun deleteAll()
}
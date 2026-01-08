package com.aaditx23.phtask.presentation.screens.CourseDetails.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aaditx23.phtask.domain.model.Course
import com.aaditx23.phtask.presentation.components.DurationDisplay
import com.aaditx23.phtask.presentation.components.RatingDisplay

@Composable
fun CourseDetailsContent(
    course: Course,
    isEnrolling: Boolean,
    onEnrollClick: () -> Unit,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = course.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Rating, Duration, and Enrollment Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RatingDisplay(rating = course.rating)
            DurationDisplay(durationWeeks = course.durationWeeks)
            EnrollmentBadge(isEnrolled = course.isEnrolled)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Instructor Card
        InstructorCard(instructor = course.instructor)

        Spacer(modifier = Modifier.height(24.dp))

        // Description Card
        DescriptionCard(description = course.descriptionShort)

        Spacer(modifier = Modifier.height(24.dp))

        // Tags Section
        Text(
            text = "Tags",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        TagsSection(tags = course.tags)

        Spacer(modifier = Modifier.height(32.dp))

        // Price Display
        PriceDisplay(
            priceUsd = course.priceUsd,
            isPremium = course.isPremium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Enrollment Button
        EnrollmentButton(
            isEnrolled = course.isEnrolled,
            isLoading = isEnrolling,
            onEnrollClick = onEnrollClick
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.PlaceholderAiRepository
import com.example.data.local.SessionPreferences
import com.example.data.local.TargetSsbDatabase
import com.example.data.repository.TargetSsbRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TargetSsbRepositoryTest {

    private lateinit var db: TargetSsbDatabase
    private lateinit var repository: TargetSsbRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TargetSsbDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = TargetSsbRepository(db.dao(), SessionPreferences(context))
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun seedData_createsAnmGnmAndRrbNtpcCourses() = runBlocking {
        repository.ensureSeeded()
        val courses = repository.courses.first()
        assertEquals(2, courses.size)
        assertTrue(courses.any { it.category == "ANM & GNM" && it.priceInr == 999 })
        assertTrue(courses.any { it.category == "RRB NTPC" && it.priceInr == 599 })
    }

    @Test
    fun paymentVerification_unlocksCourseOnlyOnSuccess() = runBlocking {
        repository.ensureSeeded()
        val student = repository.registerStudent(
            fullName = "Test Student",
            mobile = "9900112233",
            email = "teststudent@targetssb.in",
            password = "password123"
        ).getOrThrow()

        val rrbCourse = repository.courses.first().first { it.category == "RRB NTPC" }

        // Failed payment must NOT unlock course
        val failedResult = repository.processCoursePayment(student, rrbCourse, "UPI", simulateFailure = true)
        assertTrue(failedResult.isFailure)
        assertTrue(repository.observeUserEnrollments(student.id).first().isEmpty())

        // Verified payment MUST unlock course
        val successResult = repository.processCoursePayment(student, rrbCourse, "UPI", simulateFailure = false)
        assertTrue(successResult.isSuccess)
        val enrollments = repository.observeUserEnrollments(student.id).first()
        assertEquals(1, enrollments.size)
        assertEquals(rrbCourse.id, enrollments.first().courseId)
    }

    @Test
    fun placeholderAi_returnsSamplePreviewResponse() = runBlocking {
        val aiRepo = PlaceholderAiRepository()
        val res = aiRepo.askStudyDoubt("ANM & GNM Life Science", "Explain heart circulation")
        assertTrue(res.isPreviewPlaceholder)
        assertTrue(res.answerText.contains("Sample"))
    }
}

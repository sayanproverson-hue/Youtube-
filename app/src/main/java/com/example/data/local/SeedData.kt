package com.example.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SeedData {

    // Public Google/Commondatastorage sample educational MP4 & HLS streams for real Media3 playback
    private const val SAMPLE_VIDEO_1 =
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
    private const val SAMPLE_VIDEO_2 =
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
    private const val SAMPLE_VIDEO_3 =
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"
    private const val SAMPLE_HLS_LIVE =
        "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8"

    suspend fun ensureSeeded(dao: TargetSsbDao) = withContext(Dispatchers.IO) {
        if (dao.countCourses() > 0) return@withContext

        val now = System.currentTimeMillis()
        val hour = 3_600_000L
        val day = 86_400_000L

        // 1. Settings
        dao.upsertAppSettings(
            AppSettingsEntity(
                id = 1,
                supportPhone = "+91 98765 43210",
                whatsappNumber = "+91 98765 43210",
                supportEmail = "support@targetssb.in",
                tickerHeadline = "🎯 TARGET SSB – ANM & GNM | RRB NTPC | Live Classes | Mock Tests | Online Learning",
                certificatesEnabled = true
            )
        )

        // 2. Users (Demo Student + Admin)
        val studentId = dao.insertUser(
            UserEntity(
                fullName = "Arjun Chatterjee",
                mobile = "9876500001",
                email = "student@targetssb.in",
                password = "123456",
                role = "STUDENT",
                studentCode = "TSSB-2026-1042",
                createdAt = now - 15 * day
            )
        )
        dao.insertUser(
            UserEntity(
                fullName = "Soumyajit Roy",
                mobile = "9876500002",
                email = "soumya@targetssb.in",
                password = "123456",
                role = "STUDENT",
                studentCode = "TSSB-2026-1043",
                createdAt = now - 8 * day
            )
        )
        dao.insertUser(
            UserEntity(
                fullName = "Target SSB Admin",
                mobile = "9876543210",
                email = "admin@targetssb.in",
                password = "admin123",
                role = "ADMIN",
                studentCode = "ADMIN-001",
                createdAt = now - 60 * day
            )
        )

        // 3. Courses
        val anmCourseId = dao.insertCourse(
            CourseEntity(
                title = "ANM & GNM Complete Batch 2026",
                subtitle = "WBJEEB ANM(R) & GNM Full Syllabus (Bengali + English)",
                category = "ANM & GNM",
                priceInr = 999,
                originalPriceInr = 2499,
                description = "Complete target batch for West Bengal ANM & GNM nursing entrance exam. Covers Life Science, Physical Science, Basic English, Mathematics, General Knowledge & Logical Reasoning with live doubt clearing.",
                featuresCsv = "Live Classes,Recorded Classes,PDF Notes,Mock Tests,Previous Year Question Papers",
                bannerKey = "anm_gnm",
                instructor = "Dr. Ananya Sen & Target SSB Faculty",
                validityDays = 365
            )
        )

        val rrbCourseId = dao.insertCourse(
            CourseEntity(
                title = "RRB NTPC CBT-1 & CBT-2 Target Batch",
                subtitle = "Railway NTPC Complete Preparation (Bengali + English)",
                category = "RRB NTPC",
                priceInr = 599,
                originalPriceInr = 1799,
                description = "Structured preparation for Railway RRB NTPC Graduate & Undergraduate posts. Includes Mathematics shortcuts, General Intelligence & Reasoning, General Awareness & Current Affairs.",
                featuresCsv = "Live Classes,Recorded Classes,PDF Notes,Mock Tests,Previous Year Question Papers",
                bannerKey = "rrb_ntpc",
                instructor = "Subhankar Das & Target SSB Faculty",
                validityDays = 365
            )
        )

        // Pre-enroll demo student in ANM & GNM so Continue Watching / Enrolled experience is immediately testable,
        // while RRB NTPC remains available to purchase via Checkout!
        val txnDemo = "TXN_TSSB_902184"
        dao.insertEnrollment(
            EnrollmentEntity(
                userId = studentId,
                courseId = anmCourseId,
                purchasedAt = now - 10 * day,
                expiryAt = now + 355 * day,
                progressPercent = 42,
                transactionId = txnDemo,
                amountPaid = 999
            )
        )
        dao.insertPayment(
            PaymentEntity(
                userId = studentId,
                userName = "Arjun Chatterjee",
                courseId = anmCourseId,
                courseTitle = "ANM & GNM Complete Batch 2026",
                amountInr = 999,
                method = "UPI",
                status = "SUCCESS",
                transactionId = txnDemo,
                verifiedByAdmin = true,
                createdAt = now - 10 * day
            )
        )

        // 4. Live Classes
        dao.insertLiveClass(
            LiveClassEntity(
                courseId = anmCourseId,
                courseTitle = "ANM & GNM Complete Batch 2026",
                title = "Human Circulatory System & Blood Groups (হৃৎপিণ্ড ও রক্ত সংবহন)",
                subject = "Life Science",
                teacherName = "Dr. Ananya Sen",
                scheduledAtMillis = now,
                durationMinutes = 60,
                status = "LIVE",
                streamUrl = SAMPLE_HLS_LIVE,
                reminderEnabled = true
            )
        )
        dao.insertLiveClass(
            LiveClassEntity(
                courseId = rrbCourseId,
                courseTitle = "RRB NTPC CBT-1 & CBT-2 Target Batch",
                title = "Time, Speed & Distance – Railway Shortcut Tricks",
                subject = "Mathematics",
                teacherName = "Subhankar Das",
                scheduledAtMillis = now + 3 * hour,
                durationMinutes = 75,
                status = "UPCOMING",
                streamUrl = SAMPLE_VIDEO_1,
                reminderEnabled = false
            )
        )
        dao.insertLiveClass(
            LiveClassEntity(
                courseId = anmCourseId,
                courseTitle = "ANM & GNM Complete Batch 2026",
                title = "Cell Division: Mitosis & Meiosis Revision",
                subject = "Life Science",
                teacherName = "Dr. Ananya Sen",
                scheduledAtMillis = now + 26 * hour,
                durationMinutes = 60,
                status = "UPCOMING",
                streamUrl = SAMPLE_VIDEO_2,
                reminderEnabled = false
            )
        )
        dao.insertLiveClass(
            LiveClassEntity(
                courseId = rrbCourseId,
                courseTitle = "RRB NTPC CBT-1 & CBT-2 Target Batch",
                title = "Indian Polity & Constitutional Amendments Marathon",
                subject = "General Awareness",
                teacherName = "Ritam Mukherjee",
                scheduledAtMillis = now - 24 * hour,
                durationMinutes = 90,
                status = "COMPLETED",
                streamUrl = SAMPLE_VIDEO_3,
                reminderEnabled = false
            )
        )

        // 5. Recorded Classes
        dao.insertRecordedClass(
            RecordedClassEntity(
                courseId = anmCourseId,
                courseTitle = "ANM & GNM Complete Batch 2026",
                chapterName = "Chapter 1: Cell Biology & Tissues",
                title = "Prokaryotic vs Eukaryotic Cell Structure (কোষ ও কলা)",
                teacherName = "Dr. Ananya Sen",
                durationSeconds = 180,
                watchedSeconds = 95,
                videoUrl = SAMPLE_VIDEO_1,
                isCompleted = false
            )
        )
        dao.insertRecordedClass(
            RecordedClassEntity(
                courseId = anmCourseId,
                courseTitle = "ANM & GNM Complete Batch 2026",
                chapterName = "Chapter 2: Nervous System & Hormones",
                title = "Endocrine Glands & Plant Hormones (হরমোন ও স্নায়ুতন্ত্র)",
                teacherName = "Dr. Ananya Sen",
                durationSeconds = 210,
                watchedSeconds = 210,
                videoUrl = SAMPLE_VIDEO_2,
                isCompleted = true
            )
        )
        dao.insertRecordedClass(
            RecordedClassEntity(
                courseId = anmCourseId,
                courseTitle = "ANM & GNM Complete Batch 2026",
                chapterName = "Chapter 3: Physical Science",
                title = "Acids, Bases, Salts & pH Scale Numericals",
                teacherName = "Prof. Tanmoy Roy",
                durationSeconds = 195,
                watchedSeconds = 0,
                videoUrl = SAMPLE_VIDEO_3,
                isCompleted = false
            )
        )
        dao.insertRecordedClass(
            RecordedClassEntity(
                courseId = rrbCourseId,
                courseTitle = "RRB NTPC CBT-1 & CBT-2 Target Batch",
                chapterName = "Chapter 1: Arithmetic Mastery",
                title = "Percentage, Profit & Loss Fast Ratio Method",
                teacherName = "Subhankar Das",
                durationSeconds = 240,
                watchedSeconds = 0,
                videoUrl = SAMPLE_VIDEO_1,
                isCompleted = false
            )
        )
        dao.insertRecordedClass(
            RecordedClassEntity(
                courseId = rrbCourseId,
                courseTitle = "RRB NTPC CBT-1 & CBT-2 Target Batch",
                chapterName = "Chapter 2: General Intelligence",
                title = "Syllogism, Coding-Decoding & Number Series",
                teacherName = "Subhankar Das",
                durationSeconds = 200,
                watchedSeconds = 0,
                videoUrl = SAMPLE_VIDEO_2,
                isCompleted = false
            )
        )

        // 6. PDF Notes
        dao.insertPdfNote(
            PdfNoteEntity(
                courseId = anmCourseId,
                courseTitle = "ANM & GNM Complete Batch 2026",
                subject = "Life Science",
                chapter = "Chapter 1: Control & Coordination",
                title = "Human Brain, Reflex Action & Hormones Short Notes",
                pageCount = 4,
                fileSizeLabel = "1.4 MB",
                contentBody = """
TARGET SSB – ANM & GNM LIFE SCIENCE STUDY MODULE
Chapter 1: Control & Coordination (নিয়ন্ত্রণ ও সমন্বয়)

1. Neuron (স্নায়ুকোষ): Structural and functional unit of the nervous system. Parts include Dendron, Cell Body (Cyton), and Axon.
2. Human Brain Divisions:
   - Forebrain (অগ্রমস্তিষ্ক): Cerebrum (intelligence, memory), Thalamus, Hypothalamus (body temperature, hunger).
   - Midbrain (মধ্যমস্তিষ্ক): Visual & auditory reflexes.
   - Hindbrain (পশ্চাৎ মস্তিষ্ক): Cerebellum (balance & posture), Pons, Medulla Oblongata (breathing & heartbeat).
3. Major Endocrine Glands & Hormones:
   - Pituitary (Master Gland): Growth Hormone (STH), TSH, ACTH, ADH.
   - Thyroid Gland: Thyroxine (regulates BMR; deficiency causes Goitre).
   - Pancreas (Islets of Langerhans): Insulin (beta cells, lowers blood sugar), Glucagon (alpha cells).
   - Adrenal Gland: Adrenaline (emergency / fight-or-flight hormone).
4. Plant Hormones (Phytohormones):
   - Auxin (IAA): Apical dominance, phototropism.
   - Gibberellin: Stem elongation, breaks seed dormancy.
   - Cytokinin: Cell division, delays leaf senescence.
                """.trimIndent()
            )
        )
        dao.insertPdfNote(
            PdfNoteEntity(
                courseId = anmCourseId,
                courseTitle = "ANM & GNM Complete Batch 2026",
                subject = "Physical Science",
                chapter = "Chapter 2: Light & Optics",
                title = "Spherical Mirrors, Lenses & Refractive Index Formula Sheet",
                pageCount = 3,
                fileSizeLabel = "980 KB",
                contentBody = """
TARGET SSB – ANM & GNM PHYSICAL SCIENCE NOTES
Chapter 2: Light & Optics (আলো)

1. Mirror Formula: 1/v + 1/u = 1/f; Magnification m = -v/u.
2. Lens Formula: 1/v - 1/u = 1/f; Power of Lens P = 1/f (in meters), unit = Dioptre (D).
3. Snell's Law: sin(i) / sin(r) = n2 / n1 = constant.
4. Eye Defects:
   - Myopia (Near-sightedness): Corrected using Concave lens.
   - Hypermetropia (Far-sightedness): Corrected using Convex lens.
   - Presbyopia: Bifocal lens.
5. Dispersion: Violet bends the most (shortest wavelength); Red bends the least (longest wavelength).
                """.trimIndent()
            )
        )
        dao.insertPdfNote(
            PdfNoteEntity(
                courseId = rrbCourseId,
                courseTitle = "RRB NTPC CBT-1 & CBT-2 Target Batch",
                subject = "Mathematics",
                chapter = "Chapter 1: Quantitative Aptitude",
                title = "Trigonometry, Mensuration & Algebra 100 Important Formulas",
                pageCount = 5,
                fileSizeLabel = "1.8 MB",
                contentBody = """
TARGET SSB – RRB NTPC MATHEMATICS CAPSULE
Chapter 1: Quick Revision Formula Compendium

1. Algebraic Identities:
   - (a + b)^2 = a^2 + 2ab + b^2
   - a^3 + b^3 = (a + b)(a^2 - ab + b^2)
   - If x + 1/x = k, then x^2 + 1/x^2 = k^2 - 2, and x^3 + 1/x^3 = k^3 - 3k.
2. Time, Speed & Distance:
   - Average Speed for equal distances = 2xy / (x + y).
   - Convert km/h to m/s: multiply by 5/18.
   - Convert m/s to km/h: multiply by 18/5.
3. Mensuration (2D & 3D):
   - Cylinder Volume = pi * r^2 * h; Curved Surface Area = 2 * pi * r * h.
   - Cone Volume = (1/3) * pi * r^2 * h; Slant height l = sqrt(r^2 + h^2).
   - Sphere Volume = (4/3) * pi * r^3; Surface Area = 4 * pi * r^2.
                """.trimIndent()
            )
        )
        dao.insertPdfNote(
            PdfNoteEntity(
                courseId = rrbCourseId,
                courseTitle = "RRB NTPC CBT-1 & CBT-2 Target Batch",
                subject = "General Awareness",
                chapter = "Chapter 3: Indian History & Railways",
                title = "Indian National Movement & Railway GK One-Liners",
                pageCount = 6,
                fileSizeLabel = "2.1 MB",
                contentBody = """
TARGET SSB – RRB NTPC GENERAL AWARENESS DIGEST
Important Events & Railway Facts:

1. First Passenger Train in India: 16 April 1853, Bombay (Bori Bunder) to Thane (34 km).
2. 1857 Revolt Leaders: Delhi (Bahadur Shah II), Kanpur (Nana Saheb), Jhansi (Rani Lakshmibai), Lucknow (Begum Hazrat Mahal).
3. Partition of Bengal: 1905 (Lord Curzon); Annulled in 1911.
4. Non-Cooperation Movement: 1920; Civil Disobedience (Dandi March): 1930; Quit India Movement: 8 August 1942.
5. Longest Railway Platform in India: Hubballi Junction (Siddharoodha Swamiji), Karnataka.
                """.trimIndent()
            )
        )

        // 7. Mock Tests & Questions
        val anmTestId = dao.insertMockTest(
            MockTestEntity(
                courseId = anmCourseId,
                courseTitle = "ANM & GNM Complete Batch 2026",
                title = "ANM & GNM Full Practice Mock Test #01",
                subject = "Life Science & Physical Science",
                durationMinutes = 15,
                totalMarks = 10,
                negativeMarksPerWrong = 0.25,
                isPublished = true
            )
        )
        val anmQuestions = listOf(
            MockQuestionEntity(
                testId = anmTestId,
                questionText = "Which gland is known as the 'Master Gland' of the human endocrine system? (কোন গ্রন্থিকে 'প্রভু গ্রন্থি' বলা হয়?)",
                optionA = "Thyroid Gland",
                optionB = "Pituitary Gland",
                optionC = "Adrenal Gland",
                optionD = "Pancreas",
                correctOptionIndex = 1,
                explanation = "The Pituitary gland secretes tropic hormones that regulate most other endocrine glands."
            ),
            MockQuestionEntity(
                testId = anmTestId,
                questionText = "Which blood group is called the 'Universal Donor'?",
                optionA = "AB+",
                optionB = "A+",
                optionC = "O-",
                optionD = "B-",
                correctOptionIndex = 2,
                explanation = "O negative (O-) red blood cells lack A, B, and Rh antigens on their surface."
            ),
            MockQuestionEntity(
                testId = anmTestId,
                questionText = "Deficiency of Vitamin C causes which disease?",
                optionA = "Rickets",
                optionB = "Scurvy",
                optionC = "Beriberi",
                optionD = "Night Blindness",
                correctOptionIndex = 1,
                explanation = "Vitamin C (Ascorbic Acid) deficiency leads to Scurvy, affecting gums and connective tissue."
            ),
            MockQuestionEntity(
                testId = anmTestId,
                questionText = "What is the SI unit of Power of a Lens?",
                optionA = "Watt",
                optionB = "Joule",
                optionC = "Dioptre",
                optionD = "Newton",
                correctOptionIndex = 2,
                explanation = "Power P = 1/f (in metres) is measured in Dioptre (D)."
            ),
            MockQuestionEntity(
                testId = anmTestId,
                questionText = "Which organelle is called the 'Powerhouse of the Cell'?",
                optionA = "Ribosome",
                optionB = "Golgi Body",
                optionC = "Mitochondria",
                optionD = "Lysosome",
                correctOptionIndex = 2,
                explanation = "Mitochondria generate cellular ATP through aerobic respiration."
            ),
            MockQuestionEntity(
                testId = anmTestId,
                questionText = "The pH value of pure neutral water at 25°C is:",
                optionA = "0",
                optionB = "5.5",
                optionC = "7",
                optionD = "14",
                correctOptionIndex = 2,
                explanation = "Pure water at 25°C has equal H+ and OH- concentrations, giving a neutral pH of 7."
            ),
            MockQuestionEntity(
                testId = anmTestId,
                questionText = "Which part of the brain controls balance and body posture?",
                optionA = "Cerebellum",
                optionB = "Medulla",
                optionC = "Thalamus",
                optionD = "Spinal Cord",
                correctOptionIndex = 0,
                explanation = "The Cerebellum coordinates voluntary movements, posture, and equilibrium."
            ),
            MockQuestionEntity(
                testId = anmTestId,
                questionText = "Normal systolic/diastolic blood pressure in a healthy adult is:",
                optionA = "100/60 mmHg",
                optionB = "120/80 mmHg",
                optionC = "140/95 mmHg",
                optionD = "90/50 mmHg",
                correctOptionIndex = 1,
                explanation = "Standard healthy adult blood pressure is approximately 120/80 mmHg."
            ),
            MockQuestionEntity(
                testId = anmTestId,
                questionText = "Which plant hormone helps in cell division and delays leaf ageing?",
                optionA = "Auxin",
                optionB = "Ethylene",
                optionC = "Cytokinin",
                optionD = "Abscisic Acid",
                correctOptionIndex = 2,
                explanation = "Cytokinin promotes cytokinesis (cell division) and delays senescence."
            ),
            MockQuestionEntity(
                testId = anmTestId,
                questionText = "Which gas is mainly responsible for acid rain along with SO2?",
                optionA = "Nitrogen Dioxide (NO2)",
                optionB = "Methane (CH4)",
                optionC = "Argon (Ar)",
                optionD = "Helium (He)",
                correctOptionIndex = 0,
                explanation = "SO2 and NO2 react with atmospheric moisture to form H2SO4 and HNO3."
            )
        )
        anmQuestions.forEach { dao.insertMockQuestion(it) }

        val rrbTestId = dao.insertMockTest(
            MockTestEntity(
                courseId = rrbCourseId,
                courseTitle = "RRB NTPC CBT-1 & CBT-2 Target Batch",
                title = "RRB NTPC CBT-1 Speed Booster Test #01",
                subject = "Maths, Reasoning & GK",
                durationMinutes = 12,
                totalMarks = 8,
                negativeMarksPerWrong = 0.33,
                isPublished = true
            )
        )
        val rrbQuestions = listOf(
            MockQuestionEntity(
                testId = rrbTestId,
                questionText = "A train 150 m long passes a pole in 15 seconds. What is its speed in km/h?",
                optionA = "32 km/h",
                optionB = "36 km/h",
                optionC = "40 km/h",
                optionD = "45 km/h",
                correctOptionIndex = 1,
                explanation = "Speed = 150/15 = 10 m/s = 10 * 18/5 = 36 km/h."
            ),
            MockQuestionEntity(
                testId = rrbTestId,
                questionText = "If x + 1/x = 5, find the value of x² + 1/x².",
                optionA = "23",
                optionB = "25",
                optionC = "27",
                optionD = "21",
                correctOptionIndex = 0,
                explanation = "x² + 1/x² = 5² - 2 = 25 - 2 = 23."
            ),
            MockQuestionEntity(
                testId = rrbTestId,
                questionText = "Find the missing term in the series: 3, 9, 27, 81, ?",
                optionA = "162",
                optionB = "216",
                optionC = "243",
                optionD = "324",
                correctOptionIndex = 2,
                explanation = "Each term is multiplied by 3: 81 × 3 = 243."
            ),
            MockQuestionEntity(
                testId = rrbTestId,
                questionText = "Who was the Chairman of the Drafting Committee of the Indian Constitution?",
                optionA = "Dr. Rajendra Prasad",
                optionB = "Dr. B. R. Ambedkar",
                optionC = "Jawaharlal Nehru",
                optionD = "Sardar Patel",
                correctOptionIndex = 1,
                explanation = "Dr. B. R. Ambedkar served as Chairman of the Drafting Committee."
            ),
            MockQuestionEntity(
                testId = rrbTestId,
                questionText = "In which year did the first passenger train run in India?",
                optionA = "1848",
                optionB = "1853",
                optionC = "1857",
                optionD = "1862",
                correctOptionIndex = 1,
                explanation = "India's first passenger train ran between Bori Bunder and Thane on 16 April 1853."
            ),
            MockQuestionEntity(
                testId = rrbTestId,
                questionText = "What is 25% of 40% of 800?",
                optionA = "60",
                optionB = "80",
                optionC = "100",
                optionD = "120",
                correctOptionIndex = 1,
                explanation = "0.25 × 0.40 × 800 = 0.10 × 800 = 80."
            ),
            MockQuestionEntity(
                testId = rrbTestId,
                questionText = "Which article of the Indian Constitution abolishes Untouchability?",
                optionA = "Article 14",
                optionB = "Article 17",
                optionC = "Article 19",
                optionD = "Article 21",
                correctOptionIndex = 1,
                explanation = "Article 17 abolishes untouchability and forbids its practice in any form."
            ),
            MockQuestionEntity(
                testId = rrbTestId,
                questionText = "If BOOK is coded as CPPL, how is PEN coded?",
                optionA = "QFO",
                optionB = "QFP",
                optionC = "RFN",
                optionD = "QEO",
                correctOptionIndex = 0,
                explanation = "Each letter shifts forward by +1: P->Q, E->F, N->O."
            )
        )
        rrbQuestions.forEach { dao.insertMockQuestion(it) }

        // Seed 1 historical attempt for demo student
        dao.insertMockAttempt(
            MockAttemptEntity(
                userId = studentId,
                testId = anmTestId,
                testTitle = "ANM & GNM Full Practice Mock Test #01",
                totalQuestions = 10,
                correctCount = 8,
                wrongCount = 1,
                unansweredCount = 1,
                scoreMarks = 7.75,
                maxMarks = 10,
                timeTakenSeconds = 490,
                attemptedAt = now - 3 * day
            )
        )

        // 8. Mock Test Papers
        dao.insertMockPaper(
            MockPaperEntity(
                courseId = anmCourseId,
                courseTitle = "ANM & GNM Complete Batch 2026",
                title = "WBJEEB ANM & GNM 2024 Official Question Paper (Shift 1)",
                paperType = "Previous Year",
                yearLabel = "2024",
                questionsSummary = "100 Questions | 115 Marks | Life Science (50), Physical Science (25), English (15), Maths (10), GK & Reasoning (15)",
                answerKeyText = """
ANSWER KEY – WBJEEB ANM & GNM 2024 (SHIFT 1)
Q1: B (Pituitary) | Q2: C (O-) | Q3: B (Scurvy) | Q4: C (Dioptre) | Q5: C (Mitochondria)
Q6: C (7) | Q7: A (Cerebellum) | Q8: B (120/80) | Q9: C (Cytokinin) | Q10: A (NO2)
Q11: D (Insulin) | Q12: A (Auxin) | Q13: B (Nephrons) | Q14: C (Haemoglobin) | Q15: A (Chlorophyll)
                """.trimIndent()
            )
        )
        dao.insertMockPaper(
            MockPaperEntity(
                courseId = anmCourseId,
                courseTitle = "ANM & GNM Complete Batch 2026",
                title = "Target SSB ANM & GNM Model Question Set – 2026",
                paperType = "Model Paper",
                yearLabel = "2026",
                questionsSummary = "Full-length bilingual practice booklet with Nursing Aptitude & Life Science special questions.",
                answerKeyText = """
ANSWER KEY – TARGET SSB MODEL SET 2026
1-B, 2-A, 3-D, 4-C, 5-B, 6-A, 7-C, 8-D, 9-B, 10-A
Detailed solutions included inside your enrolled PDF library.
                """.trimIndent()
            )
        )
        dao.insertMockPaper(
            MockPaperEntity(
                courseId = rrbCourseId,
                courseTitle = "RRB NTPC CBT-1 & CBT-2 Target Batch",
                title = "RRB NTPC CBT-1 Previous Year Solved Paper",
                paperType = "Previous Year",
                yearLabel = "2023",
                questionsSummary = "100 Questions | 90 Minutes | Mathematics (30), General Intelligence (30), General Awareness (40)",
                answerKeyText = """
ANSWER KEY – RRB NTPC CBT-1 SOLVED PAPER
1-B (36 km/h), 2-A (23), 3-C (243), 4-B (Dr. Ambedkar), 5-B (1853), 6-B (80), 7-B (Art 17), 8-A (QFO)
                """.trimIndent()
            )
        )
        dao.insertMockPaper(
            MockPaperEntity(
                courseId = rrbCourseId,
                courseTitle = "RRB NTPC CBT-1 & CBT-2 Target Batch",
                title = "RRB NTPC CBT-2 High-Scoring Practice Set #04",
                paperType = "Practice Set",
                yearLabel = "2026",
                questionsSummary = "120 Questions | High-yield Arithmetic, Reasoning Puzzles & Static GK.",
                answerKeyText = """
ANSWER KEY – RRB NTPC PRACTICE SET #04
Section A (Maths): 1-C, 2-D, 3-A, 4-B, 5-C
Section B (Reasoning): 6-A, 7-B, 8-D, 9-C, 10-B
                """.trimIndent()
            )
        )

        // 9. Announcements
        dao.insertAnnouncement(
            AnnouncementEntity(
                title = "WBJEEB ANM & GNM 2026 New Live Batch Started!",
                message = "নতুন ব্যাচের লাইভ ক্লাস ও PDF নোটস এখনই আপনার ড্যাশবোর্ডে উপলব্ধ। প্রতিদিন সন্ধ্যা ৭টায় লাইভ ক্লাসে যোগ দিন।",
                isTicker = true,
                createdAt = now - 2 * hour
            )
        )
        dao.insertAnnouncement(
            AnnouncementEntity(
                title = "RRB NTPC ₹599/- Special Offer Extended",
                message = "Enroll in RRB NTPC CBT-1 & CBT-2 Target Batch at just ₹599/- with full mock test series & previous year papers.",
                isTicker = false,
                createdAt = now - 1 * day
            )
        )

        // 10. Notifications
        dao.insertNotification(
            NotificationEntity(
                userId = 0L,
                type = "LIVE_CLASS",
                title = "Live Now: Human Circulatory System",
                body = "Dr. Ananya Sen is live right now in ANM & GNM Batch. Join now!",
                isRead = false,
                createdAt = now - 15 * 60_000L
            )
        )
        dao.insertNotification(
            NotificationEntity(
                userId = 0L,
                type = "PDF",
                title = "New PDF Notes Uploaded",
                body = "Human Brain & Hormones Short Notes PDF has been added to Life Science.",
                isRead = false,
                createdAt = now - 4 * hour
            )
        )
        dao.insertNotification(
            NotificationEntity(
                userId = studentId,
                type = "PAYMENT",
                title = "Payment Verified: ₹999/-",
                body = "Transaction $txnDemo succeeded. ANM & GNM Complete Batch 2026 unlocked.",
                isRead = true,
                createdAt = now - 10 * day
            )
        )
    }
}

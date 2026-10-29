package com.example.pr7

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pr7.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: StudentAdapter

    private val studentsList = mutableListOf<Student>()
    private var selectedStudentId: Long = -1L

    private val courses = arrayOf("Select Course", "BCA", "MCA")

    private var selectedYear = -1
    private var selectedMonth = -1
    private var selectedDay = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DatabaseHelper(this)

        setupSpinners()
        setupListView()
        setupListeners()
        loadStudentsList()
    }

    private fun setupSpinners() {
        val courseAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses)
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCourse.adapter = courseAdapter

        setupSemesterSpinner("Select Course")

        binding.spinnerCourse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCourse = courses[position]
                setupSemesterSpinner(selectedCourse)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSemesterSpinner(course: String, selectedSem: String? = null) {
        val semesters = when (course) {
            "BCA" -> arrayOf(
                "Select Semester",
                "Semester 1",
                "Semester 2",
                "Semester 3",
                "Semester 4",
                "Semester 5",
                "Semester 6"
            )
            "MCA" -> arrayOf(
                "Select Semester",
                "Semester 1",
                "Semester 2",
                "Semester 3",
                "Semester 4"
            )
            else -> arrayOf("Select Course First")
        }

        val semAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, semesters)
        semAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSemester.adapter = semAdapter

        if (selectedSem != null) {
            val index = semesters.indexOf(selectedSem)
            if (index >= 0) {
                binding.spinnerSemester.setSelection(index)
            }
        }
    }

    private fun setupListView() {
        adapter = StudentAdapter(this, studentsList)
        binding.listViewStudents.adapter = adapter

        binding.listViewStudents.setOnItemClickListener { _, _, position, _ ->
            val student = studentsList[position]
            selectedStudentId = student.id

            binding.etEnrolment.setText(student.enrolmentNo)
            binding.etName.setText(student.name)
            binding.etDob.setText(student.dob)
            binding.etContact.setText(student.contactNo)

            binding.tilEnrolment.error = null
            binding.tilName.error = null
            binding.tilDob.error = null
            binding.tilContact.error = null

            val courseIndex = courses.indexOf(student.course)
            if (courseIndex >= 0) {
                binding.spinnerCourse.setSelection(courseIndex)
                setupSemesterSpinner(student.course, student.semester)
            }

            Toast.makeText(this, "Selected record for edit/delete: ${student.name}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        binding.etDob.setOnClickListener {
            showDatePickerDialog()
        }

        binding.btnInsert.setOnClickListener {
            insertStudent()
        }

        binding.btnUpdate.setOnClickListener {
            updateStudent()
        }

        binding.btnDelete.setOnClickListener {
            deleteStudent()
        }

        binding.btnClear.setOnClickListener {
            clearForm()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = if (selectedYear != -1) selectedYear else calendar.get(Calendar.YEAR) - 18
        val month = if (selectedMonth != -1) selectedMonth else calendar.get(Calendar.MONTH)
        val day = if (selectedDay != -1) selectedDay else calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, y, m, d ->
                selectedYear = y
                selectedMonth = m
                selectedDay = d
                val formattedDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                binding.etDob.setText(formattedDate)
                binding.tilDob.error = null
            },
            year,
            month,
            day
        )

        val maxCalendar = Calendar.getInstance()
        maxCalendar.add(Calendar.DAY_OF_YEAR, -1)
        datePickerDialog.datePicker.maxDate = maxCalendar.timeInMillis

        datePickerDialog.show()
    }

    private fun validateInputs(): Student? {
        val enrolment = binding.etEnrolment.text.toString().trim()
        val name = binding.etName.text.toString().trim()
        val dob = binding.etDob.text.toString().trim()
        val contact = binding.etContact.text.toString().trim()

        val selectedCoursePos = binding.spinnerCourse.selectedItemPosition
        val selectedCourse = if (selectedCoursePos > 0) binding.spinnerCourse.selectedItem.toString() else ""

        val selectedSemPos = binding.spinnerSemester.selectedItemPosition
        val selectedSem = if (selectedCourse.isNotEmpty() && selectedSemPos > 0) {
            binding.spinnerSemester.selectedItem.toString()
        } else ""

        var isValid = true

        binding.tilEnrolment.error = null
        binding.tilName.error = null
        binding.tilDob.error = null
        binding.tilContact.error = null

        // Enrolment No validation: exactly 15 digits
        if (enrolment.isEmpty()) {
            binding.tilEnrolment.error = "Enrolment No. is required"
            isValid = false
        } else if (!enrolment.matches(Regex("^\\d{15}$"))) {
            binding.tilEnrolment.error = "Enrolment No. must be exactly 15 digits"
            isValid = false
        }

        // Name validation: alphabets and spaces only
        if (name.isEmpty()) {
            binding.tilName.error = "Name is required"
            isValid = false
        } else if (!name.matches(Regex("^[a-zA-Z\\s]+$"))) {
            binding.tilName.error = "Name must contain only alphabets"
            isValid = false
        }

        // Date of Birth validation: past date
        if (dob.isEmpty()) {
            binding.tilDob.error = "Date of Birth is required"
            isValid = false
        } else {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                sdf.isLenient = false
                val dobDate = sdf.parse(dob)
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                if (dobDate == null || !dobDate.before(today)) {
                    binding.tilDob.error = "Date of Birth must be a past date"
                    isValid = false
                }
            } catch (e: Exception) {
                binding.tilDob.error = "Invalid date format (YYYY-MM-DD)"
                isValid = false
            }
        }

        // Contact No validation: exactly 10 digits
        if (contact.isEmpty()) {
            binding.tilContact.error = "Contact No. is required"
            isValid = false
        } else if (!contact.matches(Regex("^\\d{10}$"))) {
            binding.tilContact.error = "Contact No. must be exactly 10 digits"
            isValid = false
        }

        // Course validation
        if (selectedCourse.isEmpty()) {
            Toast.makeText(this, "Please select a Course (BCA or MCA)", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        // Semester validation
        if (selectedCourse.isNotEmpty() && selectedSem.isEmpty()) {
            Toast.makeText(this, "Please select a Semester", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (!isValid) return null

        return Student(
            id = selectedStudentId,
            enrolmentNo = enrolment,
            name = name,
            dob = dob,
            contactNo = contact,
            course = selectedCourse,
            semester = selectedSem
        )
    }

    private fun insertStudent() {
        val student = validateInputs() ?: return

        if (dbHelper.isEnrolmentExists(student.enrolmentNo)) {
            binding.tilEnrolment.error = "Enrolment No. already exists"
            Toast.makeText(this, "Student with this Enrolment No. already exists!", Toast.LENGTH_SHORT).show()
            return
        }

        val result = dbHelper.insertStudent(student)
        if (result != -1L) {
            Toast.makeText(this, "Student record inserted successfully!", Toast.LENGTH_SHORT).show()
            clearForm()
            loadStudentsList()
        } else {
            Toast.makeText(this, "Failed to insert student record.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateStudent() {
        val student = validateInputs() ?: return

        val studentToUpdateId = if (selectedStudentId != -1L) {
            selectedStudentId
        } else {
            val existing = dbHelper.getStudentByEnrolment(student.enrolmentNo)
            existing?.id ?: -1L
        }

        if (studentToUpdateId == -1L) {
            Toast.makeText(this, "Please select a record from list or enter existing Enrolment No.", Toast.LENGTH_LONG).show()
            return
        }

        if (dbHelper.isEnrolmentExists(student.enrolmentNo, studentToUpdateId)) {
            binding.tilEnrolment.error = "Enrolment No. belongs to another student"
            Toast.makeText(this, "Enrolment No. already exists for another record!", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedStudent = student.copy(id = studentToUpdateId)
        val rows = dbHelper.updateStudent(updatedStudent)
        if (rows > 0) {
            Toast.makeText(this, "Student record updated successfully!", Toast.LENGTH_SHORT).show()
            clearForm()
            loadStudentsList()
        } else {
            Toast.makeText(this, "Failed to update record.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteStudent() {
        val enrolment = binding.etEnrolment.text.toString().trim()

        val studentToDeleteId = if (selectedStudentId != -1L) {
            selectedStudentId
        } else if (enrolment.isNotEmpty()) {
            val existing = dbHelper.getStudentByEnrolment(enrolment)
            existing?.id ?: -1L
        } else {
            -1L
        }

        if (studentToDeleteId == -1L) {
            Toast.makeText(this, "Please select a student from list or enter Enrolment No. to delete", Toast.LENGTH_LONG).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Delete Student Record")
            .setMessage("Are you sure you want to delete this student record?")
            .setPositiveButton("Yes") { _, _ ->
                val rows = dbHelper.deleteStudent(studentToDeleteId)
                if (rows > 0) {
                    Toast.makeText(this, "Student record deleted successfully!", Toast.LENGTH_SHORT).show()
                    clearForm()
                    loadStudentsList()
                } else {
                    Toast.makeText(this, "Failed to delete record.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun clearForm() {
        selectedStudentId = -1L
        selectedYear = -1
        selectedMonth = -1
        selectedDay = -1

        binding.etEnrolment.setText("")
        binding.etName.setText("")
        binding.etDob.setText("")
        binding.etContact.setText("")

        binding.tilEnrolment.error = null
        binding.tilName.error = null
        binding.tilDob.error = null
        binding.tilContact.error = null

        binding.spinnerCourse.setSelection(0)
        setupSemesterSpinner("Select Course")

        binding.tvListHeader.text = "Registered Students (${studentsList.size})"
    }

    private fun loadStudentsList() {
        studentsList.clear()
        studentsList.addAll(dbHelper.getAllStudents())
        adapter.notifyDataSetChanged()

        binding.tvListHeader.text = "Registered Students (${studentsList.size})"
        if (studentsList.isEmpty()) {
            binding.tvEmptyList.visibility = View.VISIBLE
            binding.listViewStudents.visibility = View.GONE
        } else {
            binding.tvEmptyList.visibility = View.GONE
            binding.listViewStudents.visibility = View.VISIBLE
        }
    }
}
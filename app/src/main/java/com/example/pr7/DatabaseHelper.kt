package com.example.pr7

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "StudentInfo.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_STUDENT = "students"
        const val COLUMN_ID = "_id"
        const val COLUMN_ENROLMENT = "enrolment_no"
        const val COLUMN_NAME = "name"
        const val COLUMN_DOB = "dob"
        const val COLUMN_CONTACT = "contact_no"
        const val COLUMN_COURSE = "course"
        const val COLUMN_SEMESTER = "semester"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_STUDENT (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ENROLMENT TEXT UNIQUE,
                $COLUMN_NAME TEXT,
                $COLUMN_DOB TEXT,
                $COLUMN_CONTACT TEXT,
                $COLUMN_COURSE TEXT,
                $COLUMN_SEMESTER TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STUDENT")
        onCreate(db)
    }

    fun insertStudent(student: Student): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ENROLMENT, student.enrolmentNo)
            put(COLUMN_NAME, student.name)
            put(COLUMN_DOB, student.dob)
            put(COLUMN_CONTACT, student.contactNo)
            put(COLUMN_COURSE, student.course)
            put(COLUMN_SEMESTER, student.semester)
        }
        val result = db.insert(TABLE_STUDENT, null, values)
        db.close()
        return result
    }

    fun getAllStudents(): List<Student> {
        val studentList = mutableListOf<Student>()
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_STUDENT ORDER BY $COLUMN_ID DESC"
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val enrolment = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENROLMENT))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val dob = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOB))
                val contact = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT))
                val course = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURSE))
                val semester = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SEMESTER))

                studentList.add(Student(id, enrolment, name, dob, contact, course, semester))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return studentList
    }

    fun updateStudent(student: Student): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ENROLMENT, student.enrolmentNo)
            put(COLUMN_NAME, student.name)
            put(COLUMN_DOB, student.dob)
            put(COLUMN_CONTACT, student.contactNo)
            put(COLUMN_COURSE, student.course)
            put(COLUMN_SEMESTER, student.semester)
        }
        val rowsAffected = db.update(
            TABLE_STUDENT,
            values,
            "$COLUMN_ID = ?",
            arrayOf(student.id.toString())
        )
        db.close()
        return rowsAffected
    }

    fun deleteStudent(id: Long): Int {
        val db = this.writableDatabase
        val rowsDeleted = db.delete(
            TABLE_STUDENT,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
        db.close()
        return rowsDeleted
    }

    fun isEnrolmentExists(enrolmentNo: String, excludeId: Long = -1): Boolean {
        val db = this.readableDatabase
        val query = if (excludeId != -1L) {
            "SELECT 1 FROM $TABLE_STUDENT WHERE $COLUMN_ENROLMENT = ? AND $COLUMN_ID != ?"
        } else {
            "SELECT 1 FROM $TABLE_STUDENT WHERE $COLUMN_ENROLMENT = ?"
        }
        val selectionArgs = if (excludeId != -1L) {
            arrayOf(enrolmentNo, excludeId.toString())
        } else {
            arrayOf(enrolmentNo)
        }
        val cursor = db.rawQuery(query, selectionArgs)
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun getStudentByEnrolment(enrolmentNo: String): Student? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_STUDENT,
            null,
            "$COLUMN_ENROLMENT = ?",
            arrayOf(enrolmentNo),
            null, null, null
        )
        var student: Student? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val enrolment = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENROLMENT))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            val dob = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOB))
            val contact = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT))
            val course = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURSE))
            val semester = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SEMESTER))
            student = Student(id, enrolment, name, dob, contact, course, semester)
        }
        cursor.close()
        db.close()
        return student
    }
}
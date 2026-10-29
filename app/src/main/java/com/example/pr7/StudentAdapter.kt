package com.example.pr7

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.pr7.databinding.ItemStudentBinding

class StudentAdapter(
    context: Context,
    private val studentList: List<Student>
) : ArrayAdapter<Student>(context, 0, studentList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: ItemStudentBinding
        val view: View

        if (convertView == null) {
            binding = ItemStudentBinding.inflate(LayoutInflater.from(context), parent, false)
            view = binding.root
            view.tag = binding
        } else {
            view = convertView
            binding = view.tag as ItemStudentBinding
        }

        val student = studentList[position]
        binding.tvStudentName.text = student.name
        binding.tvEnrolment.text = "Enrolment: ${student.enrolmentNo}"
        binding.tvCourseSem.text = "${student.course} - ${student.semester}"
        binding.tvDob.text = "DOB: ${student.dob}"
        binding.tvContact.text = "Contact: ${student.contactNo}"

        return view
    }
}
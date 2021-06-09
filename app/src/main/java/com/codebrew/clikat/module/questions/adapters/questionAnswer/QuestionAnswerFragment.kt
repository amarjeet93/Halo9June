package com.codebrew.clikat.module.questions.adapters.questionAnswer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.codebrew.clikat.R
import com.codebrew.clikat.data.model.api.AnswersData
import com.codebrew.clikat.data.model.api.QuestionList
import com.codebrew.clikat.module.questions.adapters.AnswersAdapter
import kotlinx.android.synthetic.main.fragment_question_answer_layout.*

class QuestionAnswerFragment : Fragment() {

    private var data: QuestionList?=null
    var adapter:AnswersAdapter?=null

    var listData = arrayListOf<AnswersData>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_question_answer_layout, container, false);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        rcycle_answers.layoutManager = LinearLayoutManager(activity)
        tv_question.text = data?.question

        var isMultipleSelection = false
        if (data?.questionTypeSelection == 2) isMultipleSelection = true

        adapter= AnswersAdapter(requireActivity(),listData,isMultipleSelection)
        rcycle_answers.adapter=adapter

    }

    fun setQuestion(questionData: QuestionList) {
        listData.clear()
        data=questionData
        listData.addAll(questionData.optionsList)
        adapter?.notifyDataSetChanged()


    }


}
package com.sjn.stamp.ui.custom

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.sjn.stamp.R
import com.sjn.stamp.utils.TimeHelper
import org.joda.time.LocalDate
import java.util.*

class PeriodSelectLayout(context: Context, var period: Period) : LinearLayout(context) {

    private var yearLayout: ViewGroup
    private var monthLayout: ViewGroup
    private var dayLayout: ViewGroup
    private var customLayout: ViewGroup
    private var datePickerFrom: EditText
    private var datePickerTo: EditText
    private var periodTypeSpinner: Spinner
    private var spinnerDay: Spinner
    private var spinnerMonth: Spinner
    private var spinnerYear: Spinner
    private var periodChangeListener: PeriodChangeListener? = null

    interface PeriodChangeListener {
        fun onChange(period: Period)
    }

    class Period internal constructor(var periodType: PeriodType, internal var from: LocalDate?, internal var to: LocalDate?) {

        fun toString(resources: Resources): String = periodType.toString(resources, from, to)
        fun from(): Date? = if (periodType == PeriodType.TOTAL) null else from?.toDateTimeAtStartOfDay()?.toDate()
        fun to(): Date? = if (periodType == PeriodType.TOTAL) null else to?.toDateTimeAtStartOfDay()?.plusDays(1)?.toDate()

        companion object {
            fun latestWeek(): Period = Period(PeriodType.CUSTOM, TimeHelper.japanToday.minusWeeks(1), TimeHelper.japanToday)
        }
    }

    enum class PeriodType(val value: Int, val textId: Int, val yearVisibility: Int, val monthVisibility: Int, val dayVisibility: Int, val customVisibility: Int) {
        TOTAL(0, R.string.period_total, View.GONE, View.GONE, View.GONE, View.GONE) {
            override fun toString(resources: Resources, from: LocalDate?, to: LocalDate?): String =
                    resources.getString(R.string.period_label_total)
        },
        YEARLY(1, R.string.period_yearly, View.VISIBLE, View.GONE, View.GONE, View.GONE) {
            override fun toString(resources: Resources, from: LocalDate?, to: LocalDate?): String =
                    resources.getString(R.string.period_label_year, to?.year.toString())
        },
        MONTHLY(2, R.string.period_monthly, View.VISIBLE, View.VISIBLE, View.GONE, View.GONE) {
            override fun toString(resources: Resources, from: LocalDate?, to: LocalDate?): String =
                    resources.getString(R.string.period_label_month, to?.year.toString(), to?.monthOfYear.toString())
        },
        DAIRY(3, R.string.period_dairy, View.VISIBLE, View.VISIBLE, View.VISIBLE, View.GONE) {
            override fun toString(resources: Resources, from: LocalDate?, to: LocalDate?): String =
                    resources.getString(R.string.period_label_day, to?.year.toString(), to?.monthOfYear.toString(), to?.dayOfMonth.toString())
        },
        CUSTOM(4, R.string.period_custom, View.GONE, View.GONE, View.GONE, View.VISIBLE) {
            override fun toString(resources: Resources, from: LocalDate?, to: LocalDate?): String =
                    from?.toString() + "~" + to?.toString()
        };

        companion object {
            fun of(value: Int): PeriodType? = PeriodType.values().firstOrNull { it.value == value }
            fun strings(resources: Resources): Array<String> = PeriodType.values().map { resources.getString(it.textId) }.toTypedArray()
        }

        abstract fun toString(resources: Resources, from: LocalDate?, to: LocalDate?): String
    }

    init {
        val layout = LayoutInflater.from(context).inflate(R.layout.layout_period_select, this)
        yearLayout = layout.findViewById(R.id.period_year)
        monthLayout = layout.findViewById(R.id.period_month)
        dayLayout = layout.findViewById(R.id.period_day)
        customLayout = layout.findViewById(R.id.period_custom)
        periodTypeSpinner = layout.findViewById(R.id.period_kind_spinner)
        spinnerDay = layout.findViewById(R.id.period_day_spinner)
        spinnerMonth = layout.findViewById(R.id.period_month_spinner)
        spinnerYear = layout.findViewById(R.id.period_year_spinner)
        datePickerFrom = layout.findViewById(R.id.date_picker_from)
        datePickerTo = layout.findViewById(R.id.date_picker_to)
        initPeriodTypeSpinner()
        initDaySpinner()
        initMonthSpinner()
        initYearSpinner()
        period.from?.let { from ->
            datePickerFrom.setText(from.toString())
            datePickerFrom.setOnClickListener {
                DatePickerDialog(
                        getContext(),
                        DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                            period.from = LocalDate(year, monthOfYear + 1, dayOfMonth)
                            onPeriodChange()
                        },
                        from.year, from.monthOfYear - 1, from.dayOfMonth).show()
            }
        }
        period.to?.let { to ->
            datePickerTo.setText(to.toString())
            datePickerTo.setOnClickListener {
                DatePickerDialog(
                        getContext(),
                        DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                            period.to = LocalDate(year, monthOfYear + 1, dayOfMonth)
                            onPeriodChange()
                        },
                        to.year, to.monthOfYear - 1, to.dayOfMonth).show()
            }
        }
        reloadVisibilities()
    }

    constructor(context: Context) : this(context, Period.latestWeek())

    private fun reloadVisibilities() {
        yearLayout.visibility = period.periodType.yearVisibility
        monthLayout.visibility = period.periodType.monthVisibility
        dayLayout.visibility = period.periodType.dayVisibility
        customLayout.visibility = period.periodType.customVisibility
    }

    private fun initPeriodTypeSpinner() {
        periodTypeSpinner.adapter = createSpinnerAdapter(PeriodType.strings(resources))
        periodTypeSpinner.setSelection(period.periodType.value)
        periodTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                if (periodTypeSpinner.visibility != View.VISIBLE) {
                    return
                }
                val periodType = PeriodType.of(i) ?: return
                period.periodType = periodType
                reloadVisibilities()
                updatePeriodBySpinner()
                onPeriodChange()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }
        }

    }

    private fun initDaySpinner() {
        val days = arrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31)
        spinnerDay.adapter = createSpinnerAdapter(days)
        spinnerDay.setSelection(findOr0(days, TimeHelper.japanNow.dayOfMonth))
        spinnerDay.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                updatePeriodBySpinner()
                onPeriodChange()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }
        }
    }

    private fun initMonthSpinner() {
        val months = arrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
        spinnerMonth.adapter = createSpinnerAdapter(months)
        spinnerMonth.setSelection(findOr0(months, TimeHelper.japanNow.monthOfYear))
        spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                val maxDay = period.to?.withMonthOfYear(Integer.valueOf(spinnerMonth.selectedItem.toString()))?.dayOfMonth()?.withMaximumValue()?.dayOfMonth
                        ?: return
                val days = (1..maxDay).toList()
                spinnerDay.adapter = createSpinnerAdapter(days.toTypedArray())
                spinnerDay.setSelection(findOr0(days.toTypedArray(), TimeHelper.japanNow.dayOfMonth))
                updatePeriodBySpinner()
                onPeriodChange()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }
        }
    }

    private fun initYearSpinner() {
        var year = 2017
        val years = mutableListOf<Int>()
        while (year <= TimeHelper.japanNow.year) {
            years.add(year)
            year++
        }
        spinnerYear.adapter = createSpinnerAdapter(years.toTypedArray())
        spinnerYear.setSelection(findOr0(years.toTypedArray(), TimeHelper.japanNow.year))
        spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                updatePeriodBySpinner()
                onPeriodChange()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }
        }
    }

    private fun onPeriodChange() {
        datePickerFrom.setText(period.from?.toString())
        datePickerTo.setText(period.to?.toString())
        periodChangeListener?.onChange(period)
    }

    private fun updatePeriodBySpinner() {
        if (yearLayout.visibility == View.VISIBLE) {
            period.from = period.from?.withYear(Integer.valueOf(spinnerYear.selectedItem.toString()))?.dayOfYear()?.withMinimumValue()
            period.to = period.to?.withYear(Integer.valueOf(spinnerYear.selectedItem.toString()))?.dayOfYear()?.withMaximumValue()
        }
        if (monthLayout.visibility == View.VISIBLE) {
            period.from = period.from?.withMonthOfYear(Integer.valueOf(spinnerMonth.selectedItem.toString()))?.dayOfMonth()?.withMinimumValue()
            period.to = period.to?.withMonthOfYear(Integer.valueOf(spinnerMonth.selectedItem.toString()))?.dayOfMonth()?.withMaximumValue()
        }
        if (dayLayout.visibility == View.VISIBLE) {
            period.from = period.from?.withDayOfMonth(Integer.valueOf(spinnerDay.selectedItem.toString()))
            period.to = period.to?.withDayOfMonth(Integer.valueOf(spinnerDay.selectedItem.toString()))
        }
    }

    private fun findOr0(array: Array<Int>, value: Int): Int {
        return if (Arrays.asList(*array).contains(value)) Arrays.asList(*array).indexOf(value) else 0
    }

    private fun createSpinnerAdapter(strings: Array<Int>): ArrayAdapter<*> =
            ArrayAdapter(context, android.R.layout.simple_spinner_item, strings).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

    private fun createSpinnerAdapter(strings: Array<String>): ArrayAdapter<*> =
            ArrayAdapter(context, android.R.layout.simple_spinner_item, strings).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
}
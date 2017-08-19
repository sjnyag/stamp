package com.sjn.stamp.ui.custom;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.sjn.stamp.R;
import com.sjn.stamp.utils.TimeHelper;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PeriodSelectLayout extends LinearLayout {

    public static class Period {
        PeriodType mPeriodType;
        LocalDate mFrom;
        LocalDate mTo;

        public static Period latestWeek() {
            return new Period(PeriodType.CUSTOM, TimeHelper.getJapanToday().minusWeeks(1), TimeHelper.getJapanToday());
        }

        Period(PeriodType periodType, LocalDate from, LocalDate to) {
            mPeriodType = periodType;
            mFrom = from;
            mTo = to;
        }

        public String toString(Resources resources) {
            switch (mPeriodType) {
                case TOTAL:
                    return resources.getString(R.string.period_label_total);
                case YEARLY:
                    return resources.getString(R.string.period_label_year, String.valueOf(mTo.getYear()));
                case MONTHLY:
                    return resources.getString(R.string.period_label_month, String.valueOf(mTo.getYear()), String.valueOf(mTo.getMonthOfYear()));
                case DAIRY:
                    return resources.getString(R.string.period_label_day, String.valueOf(mTo.getYear()), String.valueOf(mTo.getMonthOfYear()), String.valueOf(mTo.getDayOfMonth()));
                case CUSTOM:
                    return mFrom.toString() + "~" + mTo.toString();
                default:
                    return "";
            }
        }

        public PeriodType getPeriodType() {
            return mPeriodType;
        }

        public Date from() {
            if (getPeriodType() == PeriodType.TOTAL) {
                return null;
            }
            if (mFrom == null) {
                return null;
            }
            return mFrom.toDateTimeAtStartOfDay().toDate();
        }


        public Date to() {
            if (getPeriodType() == PeriodType.TOTAL) {
                return null;
            }
            if (mTo == null) {
                return null;
            }
            return mTo.toDateTimeAtStartOfDay().plusDays(1).toDate();
        }
    }

    interface PeriodChangeListener {
        void onChange(Period period);
    }

    public enum PeriodType {
        TOTAL(0, R.string.period_total, GONE, GONE, GONE, GONE),
        YEARLY(1, R.string.period_yearly, VISIBLE, GONE, GONE, GONE),
        MONTHLY(2, R.string.period_monthly, VISIBLE, VISIBLE, GONE, GONE),
        DAIRY(3, R.string.period_dairy, VISIBLE, VISIBLE, VISIBLE, GONE),
        CUSTOM(4, R.string.period_custom, GONE, GONE, GONE, VISIBLE);

        final public int mValue;
        final public int mTextId;
        final public int mYearVisibility;
        final public int mMonthVisibility;
        final public int mDayVisibility;
        final public int mCustomVisibility;

        PeriodType(int value, int textId, int yearVisibility, int monthVisibility, int dayVisibility, int customVisibility) {
            mValue = value;
            mTextId = textId;
            mYearVisibility = yearVisibility;
            mMonthVisibility = monthVisibility;
            mDayVisibility = dayVisibility;
            mCustomVisibility = customVisibility;
        }

        public static PeriodType of(int value) {
            for (PeriodType periodType : PeriodType.values()) {
                if (periodType.mValue == value) return periodType;
            }
            return null;
        }

        public static String[] strings(Resources resources) {
            List<String> strings = new ArrayList<>();
            for (PeriodType periodType : PeriodType.values()) {
                strings.add(resources.getString(periodType.mTextId));
            }
            return strings.toArray(new String[0]);
        }

        public int getValue() {
            return mValue;
        }
    }

    Period mPeriod;
    View mLayout;
    ViewGroup mYearLayout;
    ViewGroup mMonthLayout;
    ViewGroup mDayLayout;
    ViewGroup mCustomLayout;
    EditText mDatePickerFrom;
    EditText mDatePickerTo;
    Spinner mPeriodTypeSpinner;
    Spinner mSpinnerDay;
    Spinner mSpinnerMonth;
    Spinner mSpinnerYear;
    PeriodChangeListener mPeriodChangeListener;

    public PeriodSelectLayout(Context context) {
        super(context);
    }

    public PeriodSelectLayout(Context context, AttributeSet attrs) {
        this(context, attrs, Period.latestWeek());
    }

    public PeriodSelectLayout(Context context, AttributeSet attr, Period defaultPeriod) {
        super(context, attr);
        mPeriod = defaultPeriod;
        mLayout = LayoutInflater.from(context).inflate(getResourceId(), this);
        mYearLayout = mLayout.findViewById(R.id.period_year);
        mMonthLayout = mLayout.findViewById(R.id.period_month);
        mDayLayout = mLayout.findViewById(R.id.period_day);
        mCustomLayout = mLayout.findViewById(R.id.period_custom);
        mPeriodTypeSpinner = mLayout.findViewById(R.id.period_kind_spinner);
        mSpinnerDay = mLayout.findViewById(R.id.period_day_spinner);
        mSpinnerMonth = mLayout.findViewById(R.id.period_month_spinner);
        mSpinnerYear = mLayout.findViewById(R.id.period_year_spinner);
        mDatePickerFrom = mLayout.findViewById(R.id.date_picker_from);
        mDatePickerFrom.setText(mPeriod.mFrom.toString());
        mDatePickerFrom.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                mPeriod.mFrom = new LocalDate(year, monthOfYear + 1, dayOfMonth);
                                onPeriodChange();
                            }
                        },
                        mPeriod.mFrom.getYear(), mPeriod.mFrom.getMonthOfYear() - 1, mPeriod.mFrom.getDayOfMonth());
                datePickerDialog.show();
            }
        });
        mDatePickerTo = mLayout.findViewById(R.id.date_picker_to);
        mDatePickerTo.setText(mPeriod.mTo.toString());
        mDatePickerTo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                mPeriod.mTo = new LocalDate(year, monthOfYear + 1, dayOfMonth);
                                onPeriodChange();
                            }
                        },
                        mPeriod.mTo.getYear(), mPeriod.mTo.getMonthOfYear() - 1, mPeriod.mTo.getDayOfMonth());
                datePickerDialog.show();
            }
        });
        //noinspection ResourceType
        mYearLayout.setVisibility(mPeriod.mPeriodType.mYearVisibility);
        //noinspection ResourceType
        mMonthLayout.setVisibility(mPeriod.mPeriodType.mMonthVisibility);
        //noinspection ResourceType
        mDayLayout.setVisibility(mPeriod.mPeriodType.mDayVisibility);
        //noinspection ResourceType
        mCustomLayout.setVisibility(mPeriod.mPeriodType.mCustomVisibility);
        initPeriodTypeSpinner();
        initDaySpinner();
        initMonthSpinner();
        initYearSpinner();
    }

    int getResourceId() {
        return R.layout.layout_period_select;
    }

    public Period getPeriod() {
        return mPeriod;
    }

    void initPeriodTypeSpinner() {
        mPeriodTypeSpinner.setAdapter(createSpinnerAdapter(PeriodType.strings(getResources())));
        mPeriodTypeSpinner.setSelection(mPeriod.mPeriodType.getValue());
        mPeriodTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (mPeriodTypeSpinner.getVisibility() != VISIBLE) {
                    return;
                }
                if (PeriodType.of(i) == null) {
                    return;
                }
                mPeriod.mPeriodType = PeriodType.of(i);
                //noinspection ResourceType
                mYearLayout.setVisibility(mPeriod.mPeriodType.mYearVisibility);
                //noinspection ResourceType
                mMonthLayout.setVisibility(mPeriod.mPeriodType.mMonthVisibility);
                //noinspection ResourceType
                mDayLayout.setVisibility(mPeriod.mPeriodType.mDayVisibility);
                //noinspection ResourceType
                mCustomLayout.setVisibility(mPeriod.mPeriodType.mCustomVisibility);
                updatePeriodBySpinner();
                onPeriodChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    void initDaySpinner() {
        Integer[] days = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31};
        mSpinnerDay.setAdapter(createSpinnerAdapter(days));
        mSpinnerDay.setSelection(findOr0(days, TimeHelper.getJapanNow().getDayOfMonth()));
        mSpinnerDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updatePeriodBySpinner();
                onPeriodChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    void initMonthSpinner() {
        Integer[] months = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        mSpinnerMonth.setAdapter(createSpinnerAdapter(months));
        mSpinnerMonth.setSelection(findOr0(months, TimeHelper.getJapanNow().getMonthOfYear()));
        mSpinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                List<Integer> days = new ArrayList<>();
                int maxDay = mPeriod.mTo.withMonthOfYear(Integer.valueOf(mSpinnerMonth.getSelectedItem().toString())).dayOfMonth().withMaximumValue().getDayOfMonth();
                for (int day = 1; day <= maxDay; day++) {
                    days.add(day);
                }
                mSpinnerDay.setAdapter(createSpinnerAdapter(days.toArray()));
                mSpinnerDay.setSelection(findOr0(days.toArray(new Integer[days.size()]), TimeHelper.getJapanNow().getDayOfMonth()));
                updatePeriodBySpinner();
                onPeriodChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    void initYearSpinner() {
        Integer[] years = {2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024, 2025, 2026, 2027, 2028, 2029, 2030};
        mSpinnerYear.setAdapter(createSpinnerAdapter(years));
        mSpinnerYear.setSelection(findOr0(years, TimeHelper.getJapanNow().getYear()));
        mSpinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updatePeriodBySpinner();
                onPeriodChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    void onPeriodChange() {
        mDatePickerFrom.setText(mPeriod.mFrom.toString());
        mDatePickerTo.setText(mPeriod.mTo.toString());
        if (mPeriodChangeListener != null) {
            mPeriodChangeListener.onChange(mPeriod);
        }
    }

    void updatePeriodBySpinner() {
        if (mYearLayout.getVisibility() == VISIBLE) {
            mPeriod.mFrom = mPeriod.mFrom.withYear(Integer.valueOf(mSpinnerYear.getSelectedItem().toString())).dayOfYear().withMinimumValue();
            mPeriod.mTo = mPeriod.mTo.withYear(Integer.valueOf(mSpinnerYear.getSelectedItem().toString())).dayOfYear().withMaximumValue();
        }
        if (mMonthLayout.getVisibility() == VISIBLE) {
            mPeriod.mFrom = mPeriod.mFrom.withMonthOfYear(Integer.valueOf(mSpinnerMonth.getSelectedItem().toString())).dayOfMonth().withMinimumValue();
            mPeriod.mTo = mPeriod.mTo.withMonthOfYear(Integer.valueOf(mSpinnerMonth.getSelectedItem().toString())).dayOfMonth().withMaximumValue();
        }
        if (mDayLayout.getVisibility() == VISIBLE) {
            mPeriod.mFrom = mPeriod.mFrom.withDayOfMonth(Integer.valueOf(mSpinnerDay.getSelectedItem().toString()));
            mPeriod.mTo = mPeriod.mTo.withDayOfMonth(Integer.valueOf(mSpinnerDay.getSelectedItem().toString()));
        }
    }

    int findOr0(Integer[] array, int value) {
        return Arrays.asList(array).contains(value) ? Arrays.asList(array).indexOf(value) : 0;
    }

    ArrayAdapter createSpinnerAdapter(Object[] strings) {
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, strings);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return spinnerArrayAdapter;
    }

}
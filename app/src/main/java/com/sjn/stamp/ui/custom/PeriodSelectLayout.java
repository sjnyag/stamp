package com.sjn.stamp.ui.custom;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.sjn.stamp.R;
import com.sjn.stamp.utils.TimeHelper;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PeriodSelectLayout extends LinearLayout {

    public static class Period {
        PeriodKind mPeriodKind = PeriodKind.TOTAL;
        int mYear = TimeHelper.getJapanYear();
        int mMonth = TimeHelper.getJapanMonth();
        int mDay = TimeHelper.getJapanDay();

        public Period(PeriodKind periodKind, int year, int month, int day) {
            mPeriodKind = periodKind;
            mYear = year;
            mMonth = month;
            mDay = day;
        }

        public Period() {
        }

        public LocalDate from() {
            return mPeriodKind.from(mYear, mMonth, mDay);
        }

        public LocalDate to() {
            return mPeriodKind.to(mYear, mMonth, mDay);
        }

        public String toString(Resources resources) {
            switch (mPeriodKind) {
                case TOTAL:
                    return resources.getString(R.string.period_label_total);
                case YEARLY:
                    return resources.getString(R.string.period_label_year, String.valueOf(mYear));
                case MONTHLY:
                    return resources.getString(R.string.period_label_month, String.valueOf(mYear), String.valueOf(mMonth));
                case DAIRY:
                    return resources.getString(R.string.period_label_day, String.valueOf(mYear), String.valueOf(mMonth), String.valueOf(mDay));
                default:
                    return "";
            }
        }

        public PeriodKind getPeriodKind() {
            return mPeriodKind;
        }

        public void setPeriodKind(PeriodKind periodKind) {
            mPeriodKind = periodKind;
        }

        public int getYear() {
            return mYear;
        }

        public void setYear(int year) {
            mYear = year;
        }

        public int getMonth() {
            return mMonth;
        }

        public void setMonth(int month) {
            mMonth = month;
        }

        public int getDay() {
            return mDay;
        }

        public void setDay(int day) {
            mDay = day;
        }
    }

    interface PeriodChangeListener {
        void onChange(LocalDate from, LocalDate to);
    }

    private enum PeriodKind {
        TOTAL(0, R.string.period_total, GONE, GONE, GONE),
        YEARLY(1, R.string.period_yearly, VISIBLE, GONE, GONE),
        MONTHLY(2, R.string.period_monthly, VISIBLE, VISIBLE, GONE),
        DAIRY(3, R.string.period_dairy, VISIBLE, VISIBLE, VISIBLE);

        final public int mValue;
        final public int mTextId;
        final public int mYearVisibility;
        final public int mMonthVisibility;
        final public int mDayVisibility;

        PeriodKind(int value, int textId, int yearVisibility, int monthVisibility, int dayVisibility) {
            mValue = value;
            mTextId = textId;
            mYearVisibility = yearVisibility;
            mMonthVisibility = monthVisibility;
            mDayVisibility = dayVisibility;
        }

        public static PeriodKind of(int value) {
            for (PeriodKind periodKind : PeriodKind.values()) {
                if (periodKind.mValue == value) return periodKind;
            }
            return null;
        }

        public String toString(Resources resources) {
            return resources.getString(mTextId);
        }

        public static String[] strings(Resources resources) {
            List<String> strings = new ArrayList<>();
            for (PeriodKind periodKind : PeriodKind.values()) {
                strings.add(resources.getString(periodKind.mTextId));
            }
            return strings.toArray(new String[0]);
        }

        public LocalDate from(int year, int month, int day) {
            if (mYearVisibility == GONE) {
                return null;
            }
            day = TimeHelper.yearMonthEnd(year, month).getDayOfMonth() < day ? TimeHelper.yearMonthEnd(year, month).getDayOfMonth() : day;
            return new LocalDate(
                    year,
                    mMonthVisibility == GONE ? 1 : month,
                    mDayVisibility == GONE ? 1 : day
            );
        }

        public LocalDate to(int year, int month, int day) {
            if (mYearVisibility == GONE) {
                return null;
            }
            int endDay = TimeHelper.yearMonthEnd(year, month).getDayOfMonth();
            day = endDay < day ? endDay : day;
            return new LocalDate(
                    year,
                    mMonthVisibility == GONE ? 12 : month,
                    mDayVisibility == GONE ? endDay : day
            );
        }

        public int getValue() {
            return mValue;
        }

        public int getTextId() {
            return mTextId;
        }

        public int getYearVisibility() {
            return mYearVisibility;
        }

        public int getMonthVisibility() {
            return mMonthVisibility;
        }

        public int getDayVisibility() {
            return mDayVisibility;
        }
    }

    public PeriodChangeListener getPeriodChangeListener() {
        return mPeriodChangeListener;
    }

    public Period getPeriod() {
        return mPeriod;
    }

    private PeriodChangeListener mPeriodChangeListener;

    private Period mPeriod;
    private LinearLayout mPeriodYear;
    private LinearLayout mPeriodMonth;
    private LinearLayout mPeriodDay;
    private LocalDate mCachedFromDate;
    private LocalDate mCachedToDate;
    protected View mLayout;

    public PeriodSelectLayout(Context context, AttributeSet attr, Period period) {
        super(context, attr);
        mLayout = inflateRootLayout(context);
        final Spinner periodSpinner = (Spinner) mLayout.findViewById(R.id.period_kind);
        Spinner spinnerYear = (Spinner) mLayout.findViewById(R.id.period_year_spinner);
        Spinner spinnerMonth = (Spinner) mLayout.findViewById(R.id.period_month_spinner);
        Spinner spinnerDay = (Spinner) mLayout.findViewById(R.id.period_day_spinner);
        mPeriodYear = (LinearLayout) mLayout.findViewById(R.id.period_year);
        mPeriodMonth = (LinearLayout) mLayout.findViewById(R.id.period_month);
        mPeriodDay = (LinearLayout) mLayout.findViewById(R.id.period_day);
        mPeriod = period;
        periodSpinner.setAdapter(createSpinnerAdapter(PeriodKind.strings(getResources())));
        periodSpinner.setSelection(mPeriod.getPeriodKind().getValue());
        periodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mPeriod.setPeriodKind(PeriodKind.of(i));
                if (mPeriod.getPeriodKind() == null) {
                    return;
                }
                //noinspection ResourceType
                mPeriodYear.setVisibility(mPeriod.getPeriodKind().getYearVisibility());
                //noinspection ResourceType
                mPeriodMonth.setVisibility(mPeriod.getPeriodKind().getMonthVisibility());
                //noinspection ResourceType
                mPeriodDay.setVisibility(mPeriod.getPeriodKind().getDayVisibility());
                onPeriodChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        Integer[] days = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31};
        spinnerDay.setAdapter(createSpinnerAdapter(days));
        spinnerDay.setSelection(findOr0(days, mPeriod.getDay()));
        spinnerDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Spinner spinner = (Spinner) adapterView;
                mPeriod.setDay(Integer.valueOf(spinner.getSelectedItem().toString()));
                onPeriodChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        Integer[] months = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        spinnerMonth.setAdapter(createSpinnerAdapter(months));
        spinnerMonth.setSelection(findOr0(months, mPeriod.getMonth()));
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Spinner spinner = (Spinner) adapterView;
                mPeriod.setMonth(Integer.valueOf(spinner.getSelectedItem().toString()));
                onPeriodChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        Integer[] years = {2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023};
        spinnerYear.setAdapter(createSpinnerAdapter(years));
        spinnerYear.setSelection(findOr0(years, mPeriod.getYear()));
        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Spinner spinner = (Spinner) adapterView;
                mPeriod.setYear(Integer.valueOf(spinner.getSelectedItem().toString()));
                onPeriodChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mCachedFromDate = mPeriod.from();
        mCachedToDate = mPeriod.to();
    }

    protected View inflateRootLayout(Context context) {
        return LayoutInflater.from(context).inflate(R.layout.layout_period_select, this);
    }

    private void onPeriodChange() {
        if (mCachedFromDate != null && mCachedToDate != null && mCachedFromDate.equals(mPeriod.from()) && mCachedToDate.equals(mPeriod.to())) {
            return;
        }
        mCachedFromDate = mPeriod.from();
        mCachedToDate = mPeriod.to();
        if (mPeriodChangeListener != null && mCachedFromDate != null && mCachedToDate != null) {
            mPeriodChangeListener.onChange(mCachedFromDate, mCachedToDate);
        }
    }

    protected int findOr0(Integer[] array, int value) {
        return Arrays.asList(array).contains(value) ? Arrays.asList(array).indexOf(value) : 0;
    }

    protected ArrayAdapter createSpinnerAdapter(Object[] strings) {
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, strings);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return spinnerArrayAdapter;
    }

}
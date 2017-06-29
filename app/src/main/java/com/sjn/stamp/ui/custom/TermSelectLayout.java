package com.sjn.stamp.ui.custom;

import android.content.Context;
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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

public class TermSelectLayout extends LinearLayout {

    @Accessors(prefix = "m")
    @Getter
    @Setter
    @AllArgsConstructor(suppressConstructorProperties = true)
    @NoArgsConstructor
    public static class Term {
        TermKind mTermKind = TermKind.TOTAL;
        int mYear = TimeHelper.getJapanYear();
        int mMonth = TimeHelper.getJapanMonth();
        int mDay = TimeHelper.getJapanDay();

        public LocalDate from() {
            return mTermKind.from(mYear, mMonth, mDay);
        }

        public LocalDate to() {
            return mTermKind.to(mYear, mMonth, mDay);
        }

        @Override
        public String toString() {
            switch (mTermKind) {
                case TOTAL:
                    return "総合";
                case YEARLY:
                    return mYear + "年";
                case MONTHLY:
                    return mYear + "年" + mMonth + "月";
                case DAIRY:
                    return mYear + "年" + mMonth + "月" + mDay + "日";
                default:
                    return "";
            }
        }
    }

    interface TermChangeListener {
        void onChange(LocalDate from, LocalDate to);
    }

    @Accessors(prefix = "m")
    @Getter
    @AllArgsConstructor
    private enum TermKind {
        TOTAL(0, "総合", GONE, GONE, GONE),
        YEARLY(1, "年間", VISIBLE, GONE, GONE),
        MONTHLY(2, "月刊", VISIBLE, VISIBLE, GONE),
        DAIRY(3, "デイリー", VISIBLE, VISIBLE, VISIBLE);

        final public int mValue;
        final public String mText;
        final public int mYearVisibility;
        final public int mMonthVisibility;
        final public int mDayVisibility;

        public static TermKind of(int value) {
            for (TermKind termKind : TermKind.values()) {
                if (termKind.mValue == value) return termKind;
            }
            return null;
        }

        public String toString() {
            return this.getText();
        }

        public static String[] strings() {
            List<String> strings = new ArrayList<>();
            for (TermKind termKind : TermKind.values()) {
                strings.add(termKind.mText);
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
    }

    @Accessors(prefix = "m")
    @Setter
    private TermChangeListener mTermChangeListener;

    @Accessors(prefix = "m")
    @Getter
    private Term mTerm;
    private LinearLayout mTermYear;
    private LinearLayout mTermMonth;
    private LinearLayout mTermDay;
    private LocalDate mCachedFromDate;
    private LocalDate mCachedToDate;
    protected View mLayout;

    public TermSelectLayout(Context context, AttributeSet attr, Term term) {
        super(context, attr);
        mLayout = inflateRootLayout(context);
        final Spinner termSpinner = (Spinner) mLayout.findViewById(R.id.term_kind);
        Spinner spinnerYear = (Spinner) mLayout.findViewById(R.id.term_year_spinner);
        Spinner spinnerMonth = (Spinner) mLayout.findViewById(R.id.term_month_spinner);
        Spinner spinnerTermDay = (Spinner) mLayout.findViewById(R.id.term_day_spinner);
        mTermYear = (LinearLayout) mLayout.findViewById(R.id.term_year);
        mTermMonth = (LinearLayout) mLayout.findViewById(R.id.term_month);
        mTermDay = (LinearLayout) mLayout.findViewById(R.id.term_day);
        mTerm = term;
        termSpinner.setAdapter(createSpinnerAdapter(TermKind.strings()));
        termSpinner.setSelection(mTerm.getTermKind().getValue());
        termSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mTerm.setTermKind(TermKind.of(i));
                if (mTerm.getTermKind() == null) {
                    return;
                }
                //noinspection ResourceType
                mTermYear.setVisibility(mTerm.getTermKind().getYearVisibility());
                //noinspection ResourceType
                mTermMonth.setVisibility(mTerm.getTermKind().getMonthVisibility());
                //noinspection ResourceType
                mTermDay.setVisibility(mTerm.getTermKind().getDayVisibility());
                onTermChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        Integer[] days = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31};
        spinnerTermDay.setAdapter(createSpinnerAdapter(days));
        spinnerTermDay.setSelection(findOr0(days, mTerm.getDay()));
        spinnerTermDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Spinner spinner = (Spinner) adapterView;
                mTerm.setDay(Integer.valueOf(spinner.getSelectedItem().toString()));
                onTermChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        Integer[] months = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        spinnerMonth.setAdapter(createSpinnerAdapter(months));
        spinnerMonth.setSelection(findOr0(months, mTerm.getMonth()));
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Spinner spinner = (Spinner) adapterView;
                mTerm.setMonth(Integer.valueOf(spinner.getSelectedItem().toString()));
                onTermChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        Integer[] years = {2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023};
        spinnerYear.setAdapter(createSpinnerAdapter(years));
        spinnerYear.setSelection(findOr0(years, mTerm.getYear()));
        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Spinner spinner = (Spinner) adapterView;
                mTerm.setYear(Integer.valueOf(spinner.getSelectedItem().toString()));
                onTermChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mCachedFromDate = mTerm.from();
        mCachedToDate = mTerm.to();
    }

    protected View inflateRootLayout(Context context) {
        return LayoutInflater.from(context).inflate(R.layout.layout_term_select, this);
    }

    private void onTermChange() {
        if (mCachedFromDate != null && mCachedToDate != null && mCachedFromDate.equals(mTerm.from()) && mCachedToDate.equals(mTerm.to())) {
            return;
        }
        mCachedFromDate = mTerm.from();
        mCachedToDate = mTerm.to();
        if (mTermChangeListener != null && mCachedFromDate != null && mCachedToDate != null) {
            mTermChangeListener.onChange(mCachedFromDate, mCachedToDate);
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
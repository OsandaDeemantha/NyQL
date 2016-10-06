package com.virtusa.gto.insight.nyql.traits

import com.virtusa.gto.insight.nyql.Column
import com.virtusa.gto.insight.nyql.FunctionColumn
import com.virtusa.gto.insight.nyql.utils.QUtils

/**
 * @author IWEERARATHNA
 */
trait FunctionTraits {

    def ADD(Object... cols)     { return vColumn("op_add", cols) }
    def MINUS(Object op1, Object op2)     { return vColumn("op_minus", op1, op2) }
    def MULTIPLY(Object op1, Object op2)     { return vColumn("op_multiply", op1, op2) }
    def DIVIDE(Object op1, Object op2)     { return vColumn("op_divide", op1, op2) }
    def MODULUS(Object op1, Object op2)     { return vColumn("op_modulus", op1, op2) }
    def INVERSE(Object op1)     { return DIVIDE(1, op1) }


    def ASC(Column column)      { return fColumn(column, "asc") }
    def DESC(Column column)     { return fColumn(column, "desc") }
    def COUNT(Object column)    { return fColumn(column, "count") }
    def DISTINCT(Column column) { return fColumn(column, "distinct") }
    def MAX(Object column)      { return fColumn(column, "max") }
    def MIN(Object column)      { return fColumn(column, "min") }
    def AVG(Object column)      { return fColumn(column, "avg") }
    def SUM(Object column)      { return fColumn(column, "sum") }

    def LCASE(Column column)    { return fColumn(column, "lcase") }
    def UCASE(Column column)    { return fColumn(column, "ucase") }
    def TRIM(Column column)     { return fColumn(column, "trim") }
    def LEN(Column column)      { return fColumn(column, "len") }

    def ROUND(Column column, Object decimalPlaces)    { return vColumn("round", column, decimalPlaces) }
    def FLOOR(Column column) { return fColumn(column, "floor") }
    def CEIL(Column column) { return fColumn(column, "ceil") }
    def ABS(Column column) { return fColumn(column, "abs") }

    def CONCAT(Object... columns) { return vColumn("concat", columns) }

    /// Date time functions
    def NOW() { return fColumn(null, "current_timestamp") }
    def CURDATE() { return fColumn(null, "current_date") }
    def CURTIME() { return fColumn(null, "current_time") }
    def CUREPOCH() { return fColumn(null, "current_epoch") }
    def DATE_TRUNC(Column column) { return fColumn(column, "date_trunc") }

    def EPOCH_TO_DATE(Column column)     { return fColumn(column, "epoch_to_date") }
    def EPOCH_TO_DATETIME(Column column)     { return fColumn(column, "epoch_to_datetime") }

    def DATE_DIFF_YEARS(Object sDate, Object eDate) { return vColumn("date_diff_years", sDate, eDate) }
    def DATE_DIFF_MONTHS(Object sDate, Object eDate) { return vColumn("date_diff_months", sDate, eDate) }
    def DATE_DIFF_DAYS(Object sDate, Object eDate) { return vColumn("date_diff_days", sDate, eDate) }
    def DATE_DIFF_WEEKS(Object sDate, Object eDate) { return vColumn("date_diff_weeks", sDate, eDate) }
    def DATE_DIFF_HOURS(Object sDate, Object eDate) { return vColumn("date_diff_hours", sDate, eDate) }
    def DATE_DIFF_MINUTES(Object sDate, Object eDate) { return vColumn("date_diff_minutes", sDate, eDate) }
    def DATE_DIFF_SECONDS(Object sDate, Object eDate) { return vColumn("date_diff_seconds", sDate, eDate) }
    def DATE_ADD_DAYS(Object cDate, Object by) { return vColumn("date_add_days", cDate, by) }
    def DATE_ADD_MONTHS(Object cDate, Object by) { return vColumn("date_add_months", cDate, by) }
    def DATE_ADD_YEARS(Object cDate, Object by) { return vColumn("date_add_years", cDate, by) }
    def DATE_ADD_WEEKS(Object cDate, Object by) { return vColumn("date_add_weeks", cDate, by) }
    def DATE_ADD_HOURS(Object cDate, Object by) { return vColumn("date_add_hours", cDate, by) }
    def DATE_ADD_MINUTES(Object cDate, Object by) { return vColumn("date_add_minutes", cDate, by) }
    def DATE_ADD_SECONDS(Object cDate, Object by) { return vColumn("date_add_seconds", cDate, by) }
    def DATE_SUB_DAYS(Object cDate, Object by) { return vColumn("date_sub_days", cDate, by) }
    def DATE_SUB_MONTHS(Object cDate, Object by) { return vColumn("date_sub_months", cDate, by) }
    def DATE_SUB_YEARS(Object cDate, Object by) { return vColumn("date_sub_years", cDate, by) }
    def DATE_SUB_WEEKS(Object cDate, Object by) { return vColumn("date_sub_weeks", cDate, by) }
    def DATE_SUB_HOURS(Object cDate, Object by) { return vColumn("date_sub_hours", cDate, by) }
    def DATE_SUB_MINUTES(Object cDate, Object by) { return vColumn("date_sub_minutes", cDate, by) }
    def DATE_SUB_SECONDS(Object cDate, Object by) { return vColumn("date_sub_seconds", cDate, by) }

    private FunctionColumn vColumn(String fName, Object... columns) {
        List<Object> vals = new ArrayList<>()
        vals.addAll(columns)
        return new FunctionColumn(_columns: vals, _func: fName, _setOfCols: true, _ctx: _ctx)
    }

    private FunctionColumn fColumn(Object column, String fName) {
        return new FunctionColumn(_wrapper: column, _func: fName, _ctx: _ctx)
    }
}
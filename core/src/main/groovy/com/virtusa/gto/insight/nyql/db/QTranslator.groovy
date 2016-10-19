package com.virtusa.gto.insight.nyql.db

import com.virtusa.gto.insight.nyql.*
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.blocks.AParam
import com.virtusa.gto.insight.nyql.model.blocks.ParamList
import com.virtusa.gto.insight.nyql.model.blocks.QNumber
import com.virtusa.gto.insight.nyql.model.blocks.QString
import com.virtusa.gto.insight.nyql.utils.QOperator
import com.virtusa.gto.insight.nyql.utils.QUtils
import com.virtusa.gto.insight.nyql.utils.QueryCombineType

import java.util.stream.Collectors

/**
 * @author Isuru Weerarathna
 */
trait QTranslator extends QJoins {

    String NULL() { 'NULL' }

    String ___resolve(Object obj, QContextType contextType, List<AParam> paramOrder=null) {
        if (obj == null) {
            return NULL()
        }

        if (obj instanceof QString) {
            return ___quoteString(obj.text) + (obj.__aliasDefined() && contextType == QContextType.SELECT ? ' AS ' + obj.__alias : '')
        } else if (obj instanceof QNumber) {
            return ___convertNumeric(obj.number) + (obj.__aliasDefined() && contextType == QContextType.SELECT ? ' AS ' + obj.__alias : '')
        } else if (obj instanceof Join) {
            return ___tableJoinName(obj, contextType, paramOrder)
        } else if (obj instanceof Table) {
            return ___tableName(obj, contextType)
        } else if (obj instanceof Column) {
            return ___columnName(obj, contextType)
        } else if (obj instanceof Boolean) {
            return ___convertBool(obj)
        } else if (obj instanceof String || obj instanceof GString) {
            return String.valueOf(obj)
        } else if (obj instanceof Number) {
            return ___convertNumeric(obj)
        } else if (obj instanceof AParam) {
            if (obj instanceof ParamList) {
                return QUtils.padParamList(obj.__name)
            }
            return '?' + (obj.__aliasDefined() && contextType == QContextType.SELECT ? ' AS ' + obj.__alias : '')
        } else if (obj instanceof QResultProxy) {
            return (obj.query ?: '').trim()
        } else if (obj instanceof List) {
            return obj.stream().map { ___resolve(it, contextType, paramOrder) }.collect(Collectors.joining(', ', '(', ')'))
        } else {
            throw new NyException('Unsupported data object to convert! [' + obj + ', type: ' + obj.class + ']')
        }
    }

    abstract def ___ifColumn(Case aCaseCol, List<AParam> paramOrder)

    /**
     * Transform the given text suitable inside a query. You may add proper
     * quoting here.
     *
     * @param text text to transform.
     * @return transformed text.
     */
    abstract String ___quoteString(String text)

    /**
     * Converts a boolean value to database specific representation in the query.
     *
     * @param value boolean value.
     * @return string representation of boolean value inside a query.
     */
    abstract String ___convertBool(Boolean value)

    /**
     * Converts a table name to string according to the given context.
     *
     * @param table table instance.
     * @param contextType context type.
     * @return string representation of the table.
     */
    abstract String ___tableName(Table table, QContextType contextType)

    /**
     * Converts a join chain to a string according to the given context.
     *
     * @param join join instance having a left and right table.
     * @param contextType context type.
     * @param paramOrder list of parameters.
     * @return string representation of the join.
     */
    abstract String ___tableJoinName(Join join, QContextType contextType, List<AParam> paramOrder)

    /**
     * Converts a column to string according to the given context.
     *
     * @param column column instance.
     * @param contextType context type.
     * @return string representation of the column.
     */
    abstract String ___columnName(Column column, QContextType contextType)

    /**
     * Generate a query part which could be reusable among other queries.
     *
     * @param q input query part model.
     * @return generated query.
     */
    abstract QResultProxy ___partQuery(QueryPart q)

    /**
     * Generate a delete query using given query model.
     *
     * @param q delete query model.
     * @return generated query.
     */
    abstract QResultProxy ___deleteQuery(QueryDelete q)

    /**
     * Generate q query composed of several queries with combinators like
     * UNION, EXCEPT, etc.
     *
     * @param q query model.
     * @return generated query.
     */
    abstract QResultProxy ___combinationQuery(QueryCombineType combineType, List<Object> queries)

    /**
     * Generates a select query using given query block instance.
     *
     * @param q input model of query.
     * @return generated select query.
     */
    abstract QResultProxy ___selectQuery(QuerySelect q)

    /**
     * Generate an insert query using given query model.
     *
     * @param q insert query model.
     * @return generated query.
     */
    abstract QResultProxy ___insertQuery(QueryInsert q)

    /**
     * Generate a query which can be executed as a stored function in database.
     *
     * @param q stored function model.
     * @return generated query.
     */
    abstract QResultProxy ___storedFunction(StoredFunction sp)

    /**
     * Generate an update query using given query model.
     *
     * @param q update query model.
     * @return generated query.
     */
    abstract QResultProxy ___updateQuery(QueryUpdate q)

    /**
     * Converts a given operator to appropriate db specific string.
     *
     * @param op operator.
     * @return string representation of operator.
     */
    String ___convertOperator(QOperator op) {
        return op.getOp()
    }

    /**
     * Converts a numeric value to database specific representation in the query.
     *
     * @param value numeric value.
     * @return string representation of numeric value inside a query.
     */
    def ___convertNumeric(Number number) {
        return String.valueOf(number)
    }

    /**
     * Returns the DDL translator for this database.
     *
     * @return the DDL translator.
     */
    abstract QDdl ___ddls()
}
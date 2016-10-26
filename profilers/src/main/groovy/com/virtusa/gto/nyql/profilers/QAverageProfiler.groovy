package com.virtusa.gto.nyql.profilers

import com.virtusa.gto.nyql.model.QProfiling
import com.virtusa.gto.nyql.model.QScript
import com.virtusa.gto.nyql.model.QSession
import groovy.json.JsonOutput
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Calculates average time for each query dynamically and emit them
 * to a file periodically.
 *
 * @author IWEERARATHNA
 */
class QAverageProfiler implements QProfiling {

    private static final Logger LOGGER = LoggerFactory.getLogger(QAverageProfiler)

    /**
     * map storing execution stats of scripts.
     */
    private final Map<String, AQueryStat> statMap = [:] as ConcurrentHashMap

    /**
     * map storing parsing stats of scripts.
     */
    private final Map<String, AQueryStat> parseStatMap = [:] as ConcurrentHashMap

    private static final DecimalFormat FORMATTER = new DecimalFormat('#0.0')

    /**
     * default interval to save stats to a file.
     */
    private static final int DUMP_INTERVAL = 10000

    /**
     * output file for the stats.
     */
    private static final String OUTPUT_FILE = './nyprofile-output.json'

    /**
     * thread pool used to save to the file.
     */
    private final ExecutorService POOL = Executors.newSingleThreadExecutor()

    /**
     * last dump time.
     */
    private long lastDumpTime = 0L
    private int writeInterval = DUMP_INTERVAL
    private boolean autoWriteOnClose = true
    private String outputFilePath = OUTPUT_FILE
    private final Object timeLock = new Object()

    @Override
    void start(Map options) {
        writeInterval = options['writeIntervalMS'] ?: DUMP_INTERVAL
        autoWriteOnClose = options['autoWriteOnClose'] ?: true
        outputFilePath = options['outputFilePath'] ?: OUTPUT_FILE
        synchronized (timeLock) {
            lastDumpTime = System.currentTimeMillis()
        }
    }

    @Override
    void doneParsing(final String scriptId, final long elapsed, final QSession session) {
        AQueryStat queryStat = parseStatMap.computeIfAbsent(scriptId, { new AQueryStat() })
        queryStat.update(elapsed)

        // try to dump to the file...
        dumpToFile()
    }

    @Override
    void doneExecuting(final QScript script, final long elapsed) {
        AQueryStat queryStat = statMap.computeIfAbsent(script.id, { new AQueryStat() })
        queryStat.update(elapsed)

        // try to dump to the file...
        dumpToFile()
    }

    /**
     * Check whether interval has been exceeded, if so, save to the file.
     *
     * @param force flag indicating forcefully save to file or not.
     */
    private void dumpToFile(boolean force = false) {
        synchronized (timeLock) {
            long current = System.currentTimeMillis()
            boolean needWrite = force || writeInterval < (current - lastDumpTime)
            lastDumpTime = current

            if (needWrite) {
                POOL.execute({ writeToFile() })
            }
        }
    }

    /**
     * Write stats to the file.
     */
    private void writeToFile() {
        Map<String, Object> map = new LinkedHashMap<>()
        map.put('writtenTime', System.currentTimeMillis())
        map.put('executions', statMap)
        map.put('parsing', parseStatMap)

        try {
            new File(outputFilePath).withWriter(StandardCharsets.UTF_8.name()) {
                it.write(JsonOutput.toJson(map))
                it.flush()
            }
        } catch (IOException ex) {
            LOGGER.error('Stat writing error!', ex)
            LOGGER.error('Error occurred while writing query stats to the file {}!', outputFilePath)
        }
    }

    @Override
    void close() throws IOException {
        dumpToLog()
        if (autoWriteOnClose) {
            writeToFile()
        }

        POOL.shutdownNow()
        statMap.clear()
        parseStatMap.clear()
    }

    /**
     * Dump all stats to the log.
     */
    private void dumpToLog() {
        LOGGER.debug("-"*75)
        LOGGER.debug("Query Stats:")
        LOGGER.debug("-"*75)

        parseStatMap.sort().each {
            AQueryStat execStat = statMap[it.key] ?: new AQueryStat()

            LOGGER.debug("Query: " + it.key)
            LOGGER.debug("  > Parsing  : {#: {}, Avg: {}, Max: {}, Min: {}}",
                            it.value.getInvocationCount(),
                            FORMATTER.format(it.value.getAvgTime()),
                            it.value.getMaxTime(),
                            it.value.getMinTime())

            LOGGER.debug("  > Execution: {#: {}, Avg: {} ms, Max: {} ms, Min: {} ms}",
                    execStat.getInvocationCount(),
                    FORMATTER.format(execStat.getAvgTime()),
                    execStat.getMaxTime(),
                    execStat.getMinTime())
        }

        LOGGER.debug("-"*75)
        statMap.sort().each {
            if (!parseStatMap.containsKey(it.key)) {
                LOGGER.debug("Query: " + it.key)
                LOGGER.debug("  > Execution  : {#: {}, Avg: {}, Max: {}, Min: {}}",
                        it.value.getInvocationCount(),
                        FORMATTER.format(it.value.getAvgTime()),
                        it.value.getMaxTime(),
                        it.value.getMinTime())
            }
        }
    }

    /**
     * A class storing stats for a single script.
     */
    private static class AQueryStat {
        private long invocationCount = 0L
        private long totalTime = 0L
        private int maxTime = Integer.MIN_VALUE
        private int minTime = Integer.MAX_VALUE

        private final Object calcLock = new Object()

        private void update(long elapsed) {
            synchronized (calcLock) {
                invocationCount++
                totalTime += elapsed
                maxTime = Math.max(maxTime, elapsed)
                minTime = Math.min(minTime, elapsed)
            }
        }

        /**
         * Returns number of invoked times of this script.
         *
         * @return invoked number.
         */
        long getInvocationCount() {
            synchronized (calcLock) {
                return invocationCount
            }
        }

        /**
         * Returns average time for query.
         *
         * @return average time for query.
         */
        double getAvgTime() {
            synchronized (calcLock) {
                if (invocationCount == 0) {
                    return 0.0
                } else {
                    totalTime / invocationCount
                }
            }
        }

        /**
         * Returns maximum time took for query.
         *
         * @return maximum time took for query.
         */
        int getMaxTime() {
            synchronized (calcLock) {
                return maxTime
            }
        }

        /**
         * Returns minimum time for query.
         *
         * @return minimum time for query.
         */
        int getMinTime() {
            synchronized (calcLock) {
                return minTime
            }
        }
    }
}
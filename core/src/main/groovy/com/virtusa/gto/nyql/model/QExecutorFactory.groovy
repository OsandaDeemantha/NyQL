package com.virtusa.gto.nyql.model

import com.virtusa.gto.nyql.exceptions.NyConfigurationException

/**
 * @author IWEERARATHNA
 */
interface QExecutorFactory {

    DbInfo init(Map options) throws NyConfigurationException

    QExecutor create()

    QExecutor createReusable()

    void shutdown()

}

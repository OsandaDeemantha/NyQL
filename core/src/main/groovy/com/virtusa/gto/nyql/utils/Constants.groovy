package com.virtusa.gto.nyql.utils

import groovy.transform.CompileStatic
import groovy.transform.Immutable

/**
 * @author IWEERARATHNA
 */
@CompileStatic
@Immutable
final class Constants {

    static final String MBEAN_OBJECT_NAME = "com.virtusa.gto.nyql"

    static final String DSL_ENTRY_WORD = '$DSL'

    static final String DSL_SESSION_WORD = '$SESSION'

    static final String DSL_CACHE_VARIABLE_NAME = 'do_cache'

    static final String DEFAULT_REPOSITORY_NAME = 'default'
    static final String DEFAULT_REPOSITORY_IMPL = 'default'

    static final String DEFAULT_EXECUTOR_NAME = 'jdbc'

    static final String PROFILING_REPO_NAME = 'profiling'
}

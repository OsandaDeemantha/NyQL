package com.virtusa.gto.insight.nyql.engine.repo

import com.virtusa.gto.insight.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.insight.nyql.exceptions.NyException
import com.virtusa.gto.insight.nyql.model.QScriptMapper
import com.virtusa.gto.insight.nyql.model.QSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.charset.StandardCharsets

/**
 * Contains script mapping from a single folder in the system.
 *
 * @author IWEERARATHNA
 */
class QScriptsFolder implements QScriptMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(QScriptsFolder.class)

    private static final boolean DEF_CACHING = false

    final File baseDir
    private int maxLen = 1

    private final Map<String, QSource> fileMap = [:]

    QScriptsFolder(File theBaseDir) {
        baseDir = theBaseDir

        // scans the given directory
        LOGGER.debug("Loading script files from directory '{}'", baseDir.canonicalPath)
        scanDir(baseDir)
        prettyPrintFiles()
    }

    private void scanDir(File dir) {
        if (dir.isDirectory()) {
            def files = dir.listFiles(new FilenameFilter() {
                @Override
                boolean accept(File d, String name) {
                    return d.isDirectory() || name.endsWith(".groovy")
                }
            })

            files.each {
                if (it.isDirectory()) {
                    scanDir(it)
                } else {
                    String relPath = captureFileName(baseDir.toPath().relativize(it.toPath()).toString()).replace('\\', '/')

                    String content = readAll(it)
                    GroovyCodeSource groovyCodeSource = new GroovyCodeSource(content, relPath, GroovyShell.DEFAULT_CODE_BASE)
                    groovyCodeSource.setCachable(true)

                    def qSrc = new QSource(id: relPath, file: it, doCache: DEF_CACHING, codeSource: groovyCodeSource)
                    fileMap[relPath] = qSrc
                    maxLen = Math.max(relPath.length(), maxLen)
                }
            }
        }
    }

    private static String readAll(File file) {
        return file.getText(StandardCharsets.UTF_8.name());
    }

    private static String captureFileName(String path) {
        int lp = path.lastIndexOf('.')
        if (lp > 0) {
            return path.substring(0, lp)
        }
        return path
    }

    static QScriptsFolder createNew(Map args) throws NyException {
        if (args == null || args.size() == 0 || !args.baseDir) {
            throw new NyConfigurationException('To create a new QScriptsFolder requires at least one parameter with specifying base directory!')
        }

        String path = args.baseDir;
        File dir = new File(path);
        if (!dir.exists()) {
            String configFilePath = args._location;
            if (configFilePath != null) {
                File activeDir = new File(configFilePath).getCanonicalFile().getParentFile()
                if (activeDir.exists() && !dir.isAbsolute()) {
                    return new QScriptsFolder(activeDir.toPath().resolve(path).toFile())
                }
            }
            throw new NyConfigurationException('Given script folder does not exist! [' + args[0] + ']')
        }
        return new QScriptsFolder(dir);
    }

    @Override
    QSource map(String id) {
        fileMap[id]
    }

    @Override
    Collection<QSource> allSources() {
        fileMap.values()
    }

    @Override
    boolean canCacheAtStartup() {
        true
    }

    private void prettyPrintFiles() {
        fileMap.keySet().sort().each {
            String kb = toKB(fileMap[it].file.length())
            LOGGER.debug(' > ' + it.padRight(maxLen + 5) + "${kb.padLeft(6)}")
        }
        LOGGER.debug("Found ${fileMap.size()} script(s).")
    }

    private static String toKB(long length) {
        return ((int)Math.ceil(length / 1024.0)) + ' KB'
    }
}

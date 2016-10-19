package nyql.scripting;

import com.virtusa.gto.insight.nyql.configs.ConfigBuilder;
import com.virtusa.gto.insight.nyql.engine.NyQL;
import com.virtusa.gto.insight.nyql.exceptions.NyException;
import groovy.json.JsonSlurper;
import nyql.utils.TUtils;
import org.testng.annotations.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author IWEERARATHNA
 */
@Test(groups = "scripts")
public class ScriptTest {

    @SuppressWarnings("unchecked")
    @BeforeClass
    public void startup() {
        System.setProperty("com.virtusa.gto.insight.nyql.autoBootstrap", "false");
        Map<String, Object> configs = (Map<String, Object>) new JsonSlurper().parse(new File("./configs/nyql-test.json"), StandardCharsets.UTF_8.name());
        ConfigBuilder configBuilder = ConfigBuilder.instance().setupFrom(configs);

        configBuilder.addExecutor(TUtils.executorWithMax(1, 1));
        configBuilder.build();
    }

    @AfterClass
    public void teardown() {
        NyQL.shutdown();
    }

    public void testVariables() throws NyException {
        NyQL.execute("scripts/variable_test");
    }


}
package demo.maven.conflict.liba;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibA {

    private static final Logger log = LoggerFactory.getLogger(LibA.class);

    public String versionHint() {
        log.info("LibA loaded");
        return "LibA depends on slf4j-api:1.7.25";
    }
}

package demo.maven.conflict.libb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibB {

    private static final Logger log = LoggerFactory.getLogger(LibB.class);

    public String versionHint() {
        log.info("LibB loaded");
        return "LibB depends on slf4j-api:1.7.36";
    }
}

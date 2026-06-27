package demo.maven.conflict.appshortpath;

import demo.maven.conflict.liba.LibA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppShortPath {

    private static final Logger log = LoggerFactory.getLogger(AppShortPath.class);

    public static void main(String[] args) {
        LibA libA = new LibA();
        log.info(libA.versionHint());
        Package pkg = LoggerFactory.class.getPackage();
        System.out.println("Resolved slf4j-api version: " + pkg.getImplementationVersion());
    }
}

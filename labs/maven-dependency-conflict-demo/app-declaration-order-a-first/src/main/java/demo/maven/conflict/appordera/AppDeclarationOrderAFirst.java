package demo.maven.conflict.appordera;

import demo.maven.conflict.liba.LibA;
import demo.maven.conflict.libb.LibB;
import org.slf4j.LoggerFactory;

public class AppDeclarationOrderAFirst {

    public static void main(String[] args) {
        System.out.println(new LibA().versionHint());
        System.out.println(new LibB().versionHint());
        Package pkg = LoggerFactory.class.getPackage();
        System.out.println("Resolved slf4j-api version: " + pkg.getImplementationVersion());
    }
}

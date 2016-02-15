package com.commercetools.build.taglets;

import java.io.File;

public final class InternalTagletUtils {
    public static File allProjectsBaseFile() {
        final File parentFile = new File(new File(".").getAbsoluteFile().getAbsolutePath().replace("/target/site/apidocs", "").replace("/target/apidocs", ""))
                .getParentFile();
        final boolean isAlreadyAllRoot = new File(parentFile, ".gitignore").exists();
        return isAlreadyAllRoot ?  parentFile : parentFile.getParentFile().getAbsoluteFile();
    }
}

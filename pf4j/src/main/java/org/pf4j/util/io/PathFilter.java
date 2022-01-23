package org.pf4j.util.io;

import java.nio.file.Path;

/**
 * @author Decebal Suiu
 */
public interface PathFilter {

    boolean accept(Path path);

}

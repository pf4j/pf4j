package org.pf4j.util.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Decebal Suiu
 */
public abstract class CompoundPathFilter implements PathFilter {

    /** The list of path filters. */
    protected List<PathFilter> pathFilters;

    public CompoundPathFilter() {
        this(new ArrayList<>());
    }

    public CompoundPathFilter(PathFilter... pathFilters) {
        this(Arrays.asList(pathFilters));
    }

    public CompoundPathFilter(List<PathFilter> pathFilters) {
        this.pathFilters = new ArrayList<>(pathFilters);
    }

    public CompoundPathFilter addPathFilter(PathFilter fileFilter) {
        pathFilters.add(fileFilter);

        return this;
    }

    public List<PathFilter> getPathFilters() {
        return Collections.unmodifiableList(pathFilters);
    }

    public boolean removePathFilter(PathFilter pathFilter) {
        return pathFilters.remove(pathFilter);
    }

    public void setPathFilters(List<PathFilter> pathFilters) {
        this.pathFilters = new ArrayList<>(pathFilters);
    }

}

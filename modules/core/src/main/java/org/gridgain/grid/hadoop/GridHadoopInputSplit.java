/* @java.file.header */

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.hadoop;

import java.io.*;

import org.jetbrains.annotations.*;

/**
 * Abstract fragment of an input data source.
 */
public interface GridHadoopInputSplit extends Externalizable {
    /**
     * @return Hosts.
     */
    public String[] hosts();

    /**
     * @return Either {@code null} or native input split if it's not FileSplit.
     */
    @Nullable public <T> T innerSplit();
}
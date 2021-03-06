/* 
 Copyright (C) GridGain Systems. All Rights Reserved.
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.kernal.processors.dataload;

import org.gridgain.grid.*;
import org.gridgain.grid.cache.*;
import org.gridgain.grid.dataload.*;
import org.gridgain.grid.marshaller.*;
import org.gridgain.grid.marshaller.optimized.*;
import org.gridgain.grid.spi.discovery.tcp.*;
import org.gridgain.grid.spi.discovery.tcp.ipfinder.*;
import org.gridgain.grid.spi.discovery.tcp.ipfinder.vm.*;
import org.gridgain.grid.util.typedef.*;
import org.gridgain.grid.util.typedef.internal.*;
import org.gridgain.testframework.junits.common.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import static org.gridgain.grid.cache.GridCacheMode.*;
import static org.gridgain.grid.cache.GridCacheWriteSynchronizationMode.*;

/**
 * Tests for {@code GridDataLoaderImpl}.
 */
public class GridDataLoaderImplSelfTest extends GridCommonAbstractTest {
    /** IP finder. */
    private static final GridTcpDiscoveryIpFinder IP_FINDER = new GridTcpDiscoveryVmIpFinder(true);

    /** Number of keys to load via data loader. */
    private static final int KEYS_COUNT = 1000;

    /** Started grid counter. */
    private static int cnt;

    /** {@inheritDoc} */
    @Override protected GridConfiguration getConfiguration(String gridName) throws Exception {
        GridConfiguration cfg = super.getConfiguration(gridName);

        GridTcpDiscoverySpi discoSpi = new GridTcpDiscoverySpi();
        discoSpi.setIpFinder(IP_FINDER);

        cfg.setDiscoverySpi(discoSpi);

        // Forth node goes without cache.
        if (cnt < 4)
            cfg.setCacheConfiguration(cacheConfiguration());

        cnt++;

        return cfg;
    }

    /**
     * @throws Exception If failed.
     */
    public void testNullPointerExceptionUponDataLoaderClosing() throws Exception {
        try {
            startGrids(5);

            final CyclicBarrier barrier = new CyclicBarrier(2);

            multithreadedAsync(new Callable<Object>() {
                @Override public Object call() throws Exception {
                    U.awaitQuiet(barrier);

                    G.stopAll(true);

                    return null;
                }
            }, 1);

            Grid g4 = grid(4);

            GridDataLoader<Object, Object> dataLdr = g4.dataLoader(null);

            dataLdr.perNodeBufferSize(32);

            for (int i = 0; i < 100000; i += 2) {
                dataLdr.addData(i, i);
                dataLdr.removeData(i + 1);
            }

            U.awaitQuiet(barrier);

            info("Closing data loader.");

            try {
                dataLdr.close(true);
            }
            catch (IllegalStateException ignore) {
                // This is ok to ignore this exception as test is racy by it's nature -
                // grid is stopping in different thread.
            }
        }
        finally {
            G.stopAll(true);
        }
    }

    /**
     * Data loader should correctly load entries from HashMap in case of grids with more than one node
     *  and with GridOptimizedMarshaller that requires serializable.
     *
     * @throws Exception If failed.
     */
    public void testAddDataFromMap() throws Exception {
        try {
            cnt = 0;

            startGrids(2);

            Grid g0 = grid(0);

            GridMarshaller marsh = g0.configuration().getMarshaller();

            if (marsh instanceof GridOptimizedMarshaller)
                assertTrue(((GridOptimizedMarshaller)marsh).isRequireSerializable());
            else
                fail("Expected GridOptimizedMarshaller, but found: " + marsh.getClass().getName());

            GridDataLoader<Integer, String> dataLdr = g0.dataLoader(null);

            Map<Integer, String> map = U.newHashMap(KEYS_COUNT);

            for (int i = 0; i < KEYS_COUNT; i ++)
                map.put(i, String.valueOf(i));

            dataLdr.addData(map);

            dataLdr.close();

            Random rnd = new Random();

            GridCache<Integer, String> c = g0.cache(null);

            for (int i = 0; i < KEYS_COUNT; i ++) {
                Integer k = rnd.nextInt(KEYS_COUNT);

                String v = c.get(k);

                assertEquals(k.toString(), v);
            }
        }
        finally {
            G.stopAll(true);
        }
    }

    /**
     * Gets cache configuration.
     *
     * @return Cache configuration.
     */
    private GridCacheConfiguration cacheConfiguration() {
        GridCacheConfiguration cacheCfg = defaultCacheConfiguration();

        cacheCfg.setCacheMode(PARTITIONED);
        cacheCfg.setBackups(1);
        cacheCfg.setWriteSynchronizationMode(FULL_SYNC);

        return cacheCfg;
    }

    /**
     *
     */
    private static class TestObject implements Serializable {
        /** */
        private int val;

        /**
         */
        private TestObject() {
            // No-op.
        }

        /**
         * @param val Value.
         */
        private TestObject(int val) {
            this.val = val;
        }

        public Integer val() {
            return val;
        }

        /** {@inheritDoc} */
        @Override public int hashCode() {
            return val;
        }

        /** {@inheritDoc} */
        @Override public boolean equals(Object obj) {
            return obj instanceof TestObject && ((TestObject)obj).val == val;
        }
    }
}

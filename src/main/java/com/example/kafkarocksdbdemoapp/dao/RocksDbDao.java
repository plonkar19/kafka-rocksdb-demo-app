package com.example.kafkarocksdbdemoapp.dao;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.BloomFilter;
import org.rocksdb.Cache;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.DBOptions;
import org.rocksdb.Filter;
import org.rocksdb.HistogramType;
import org.rocksdb.InfoLogLevel;
import org.rocksdb.LRUCache;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.Statistics;
import org.rocksdb.StatsLevel;
import org.rocksdb.TickerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.CollectionUtils;

import com.example.kafkarocksdbdemoapp.util.RocksDbStatsUtil;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;

public class RocksDbDao implements DisposableBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(RocksDbDao.class);
    private RocksDB rocksDB;
    private Filter bloomFilter;
    private Cache cache;
    private Statistics statistics;
    private DBOptions dbOptions;
    private boolean initialized = false;
    private final String dataPath;
    private final String topicName;
    private final Map<String, ColumnFamilyHandle> mColumnFamilyHandleMap = new ConcurrentHashMap<>();
    public static final String CONSUMER_OFFSETS_COLUMN_FAMILY = "consumerOffsets";
    private final String ROCKSDB_STATS_LEVEL = "EXCEPT_TIME_FOR_MUTEX"; // EXCEPT_DETAILED_TIMERS, EXCEPT_TIME_FOR_MUTEX, ALL
    private final int STATS_DUMP_PERIOD_SEC = 60;
    private final boolean ENABLE_LRU_CACHE = true;
    private final long TABLE_BLOCK_SIZE = 100;
    private final long TABLE_BLOCK_CACHE_CAPACITY = 100;

    public RocksDbDao(String dataPath, List<String> columnFamily) {
        this.topicName = "rocksdb";
        this.dataPath = dataPath;
        initializeDataStore(columnFamily);
    }

    private void initializeDataStore(List<String> columnFamilyNameList) {
        // Validate that all the column family names are not empty ahead of time, and throw IllegalArgumentException
        // before any file handles have been opened in order to avoid leaving file system handles unclosed.
        final Set<String> columnFamilyNameSet = validateColumnFamilyNames(columnFamilyNameList);

        if (initialized) {
            shutdownDataStore();
        }

        RocksDB.loadLibrary();

        // Creates columnFamily Descriptors.
        List<ColumnFamilyDescriptor> columnFamilyDescriptors = createColumnFamilyDescriptors(columnFamilyNameSet);

        // Create DBOptions
        createDbOptions();

        List<ColumnFamilyHandle> columnFamilyHandlers = new ArrayList<>();
        File dirPath = null;

        // for tests they can run into each other here
        for (int i = 0; i < 10; i++) {
            try {
                dirPath = new File(dataPath);

                if (!dirPath.exists()) {
                    dirPath.mkdirs();
                }

//                Files.createDirectories(dirPath.getParentFile().toPath());
//                Files.createDirectories(dirPath.getAbsoluteFile().toPath());

                try {
                    List<ColumnFamilyDescriptor> persistedColumnFamilyDescriptors = new ArrayList<>();

                    // Try with those options.
                    rocksDB = RocksDB.open(dbOptions, dataPath, columnFamilyDescriptors, columnFamilyHandlers);
                } catch (RocksDBException e) {
                    LOGGER.warn("Error while fetching rocksdb options, will reset db", e);
                }

                initialized = true;
                break;
            } catch (Exception e) {
                LOGGER.error("Trouble with rocks backend", e);
                // needs to trigger shutdown (or otherwise be unhealthy)
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException exception) {
                    LOGGER.debug("Interrupted ", exception);
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (!initialized) {
            LOGGER.error("Could not initialize data store");
            throw new RuntimeException("Could not initialize data store");
        }

    }

    private Set<String> validateColumnFamilyNames(List<String> columnFamilyNameList) {
        Set<String> columnFamilyNameSet = new TreeSet<>();
        Optional.ofNullable(columnFamilyNameList)
                .ifPresent(families -> {
                    families.forEach(columnFamily ->
                            // column family names should not be empty
                            Preconditions.checkArgument(!Strings.isNullOrEmpty(columnFamily), "column family name cannot be empty")
                    );
                    // Add all the column families
                    columnFamilyNameSet.addAll(columnFamilyNameList);
                });

        // Always add consumer offsets column family
        columnFamilyNameSet.add(CONSUMER_OFFSETS_COLUMN_FAMILY);
        return columnFamilyNameSet;
    }

    private List<ColumnFamilyDescriptor> createColumnFamilyDescriptors(Set<String> columnFamilyNameSet) {
        ColumnFamilyOptions columnFamilyOptions = createColumnFamilyOptions();
        List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
        columnFamilyDescriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, columnFamilyOptions));
        for (String cfName : columnFamilyNameSet) {
            byte[] cfNameBytes = cfName.getBytes(UTF_8);
            ColumnFamilyDescriptor descriptor = new ColumnFamilyDescriptor(cfNameBytes, columnFamilyOptions);
            columnFamilyDescriptors.add(descriptor);
        }
        return columnFamilyDescriptors;
    }

    private void createDbOptions() {
        this.statistics = new Statistics();
        this.statistics.setStatsLevel(StatsLevel.valueOf(ROCKSDB_STATS_LEVEL));
        LOGGER.info("RocksDB Stats Level: {}", this.statistics.statsLevel().getValue());

        this.dbOptions = new DBOptions();
        this.dbOptions.setCreateIfMissing(true)
                .setCreateMissingColumnFamilies(true)
                .setInfoLogLevel(InfoLogLevel.DEBUG_LEVEL)
                .setStatistics(this.statistics)
                .setStatsDumpPeriodSec(STATS_DUMP_PERIOD_SEC);
    }

    private ColumnFamilyOptions createColumnFamilyOptions() {
        bloomFilter = new BloomFilter();

        // Setup rockDb configuration
        final BlockBasedTableConfig blockBasedTableConfig = new BlockBasedTableConfig()
                .setBlockSize(TABLE_BLOCK_SIZE)
                .setCacheIndexAndFilterBlocks(true)
                .setFilter(bloomFilter);

        if (ENABLE_LRU_CACHE) {
            cache = new LRUCache(TABLE_BLOCK_CACHE_CAPACITY);
            blockBasedTableConfig.setBlockCache(cache);
        }

        /*
         * build column family options
         */
        ColumnFamilyOptions columnFamilyOptions = new ColumnFamilyOptions()
                .setTableFormatConfig(blockBasedTableConfig);

        return columnFamilyOptions;
    }

    @Override
    public void destroy() throws Exception {
        LOGGER.info("destroy() method called on RocksDb");
        shutdownDataStore();
    }

    public void shutdownDataStore() {
        LOGGER.info("Shutting down rocksDb");
        if (!CollectionUtils.isEmpty(mColumnFamilyHandleMap)) {
            for (ColumnFamilyHandle handle : mColumnFamilyHandleMap.values()) {
                handle.close();
            }
        }

        if (dbOptions != null) {
            dbOptions.close();
            dbOptions = null;
        }

        if (rocksDB != null) {
            rocksDB.close();
            rocksDB = null;
        }

        if (bloomFilter != null) {
            bloomFilter.close();
            bloomFilter = null;
        }

        if (statistics != null) {
            statistics.close();
            statistics = null;
        }

        if (cache != null) {
            cache.close();
            cache = null;
        }
    }

    public String getData(byte[] key) throws RocksDBException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        String result = null;

        try {
            var bytes = rocksDB.get(key);
            if (bytes == null) return null;
            result = new String(bytes);

            insertRocksdbReadOpStats(stopwatch, bytes);
        } finally {
            stopwatch.stop();
        }

        return result;
    }

    public void putData(byte[] key, byte[] value) throws RocksDBException {
        Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            rocksDB.put(key, value);

            insertRocksdbWriteOpStats(stopwatch, value);
        } finally {
            stopwatch.stop();
        }
    }

    public void deleteData(byte[] key) throws RocksDBException {
        Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            rocksDB.delete(key);

            insertRocksdbDeleteOpStats(stopwatch);
        } finally {
            stopwatch.stop();
        }
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> rocksDbStatistics = new HashMap<>();

        for (TickerType tickerType : TickerType.values()) {
            rocksDbStatistics.put(tickerType.name(), this.statistics.getTickerCount(tickerType));
        }

        for (HistogramType histogramType : HistogramType.values()) {
            rocksDbStatistics.put(histogramType.name(), this.statistics.getHistogramData(histogramType).toString());
        }

        // read stats using RocksDB properties
        for (Map.Entry<String, String> entry : RocksDbStatsUtil.ROCKSDB_PROPERTIES_MAP.entrySet()) {
            try {
//                rocksDbStatistics.put(entry.getKey(), rocksDB.getProperty(entry.getValue()));
            } catch (Exception ex) {
                LOGGER.error("Failed to fetch RocksDB Property={}:{} Exception={}",
                        entry.getKey(), entry.getValue(), ex.getMessage());
            }
        }

        // read custom stats and add them into Map to publish
        rocksDbStatistics.put("CUSTOM_READ", RocksDbStatsUtil.rocksdbReadOpStats.get(topicName));
        rocksDbStatistics.put("CUSTOM_WRITE", RocksDbStatsUtil.rocksdbWriteOpStats.get(topicName));
        rocksDbStatistics.put("CUSTOM_DELETE", RocksDbStatsUtil.rocksdbDeleteOpStats.get(topicName));

        // remove the entry from map to save memory
        RocksDbStatsUtil.rocksdbReadOpStats.remove(topicName);
        RocksDbStatsUtil.rocksdbWriteOpStats.remove(topicName);
        RocksDbStatsUtil.rocksdbDeleteOpStats.remove(topicName);

        LOGGER.debug("RocksDB Stats: {}", rocksDbStatistics.toString());

        return rocksDbStatistics;
    }

    /**
     * Calculate time elapsed for read operation, bytes read.
     * Store these custom stats into Map so that we can publish them.
     *
     * @param stopwatch Stopwatch to calculate time
     * @param dbValue   byte array read from db
     */
    private void insertRocksdbReadOpStats(Stopwatch stopwatch, byte[] dbValue) {
        try {
            long time = stopwatch.elapsed(TimeUnit.MICROSECONDS);
            long value = dbValue != null ? dbValue.length : 0;

            RocksDbCustomStats rocksDbCustomStats = RocksDbStatsUtil.rocksdbReadOpStats.getOrDefault(topicName, new RocksDbCustomStats());
            calculateRocksDbCustomStats(rocksDbCustomStats, time, value);
            RocksDbStatsUtil.rocksdbReadOpStats.put(topicName, rocksDbCustomStats);

        } catch (Exception ex) {
            LOGGER.error("Exception while inserting stats for RocksDb.", ex);
        }
    }

    /**
     * Calculate time elapsed for write operation, bytes read.
     * Store these custom stats into Map so that we can publish them.
     *
     * @param stopwatch Stopwatch to calculate time
     * @param dbValue   byte array write into db
     */
    private void insertRocksdbWriteOpStats(Stopwatch stopwatch, byte[] dbValue) {
        try {
            long time = stopwatch.elapsed(TimeUnit.MICROSECONDS);
            long value = dbValue != null ? dbValue.length : 0;

            RocksDbCustomStats rocksDbCustomStats = RocksDbStatsUtil.rocksdbWriteOpStats.getOrDefault(topicName, new RocksDbCustomStats());
            calculateRocksDbCustomStats(rocksDbCustomStats, time, value);
            RocksDbStatsUtil.rocksdbWriteOpStats.put(topicName, rocksDbCustomStats);
        } catch (Exception ex) {
            LOGGER.error("Exception while inserting stats for RocksDb.", ex);
        }
    }

    /**
     * Calculate time elapsed for delete operation.
     * Store these custom stats into Map so that we can publish them.
     *
     * @param stopwatch Stopwatch to calculate time
     */
    private void insertRocksdbDeleteOpStats(Stopwatch stopwatch) {
        try {
            long time = stopwatch.elapsed(TimeUnit.MICROSECONDS);


            RocksDbCustomStats rocksDbCustomStats = RocksDbStatsUtil.rocksdbDeleteOpStats.getOrDefault(topicName, new RocksDbCustomStats());
            calculateRocksDbCustomStats(rocksDbCustomStats, time, 0);
            RocksDbStatsUtil.rocksdbDeleteOpStats.put(topicName, rocksDbCustomStats);

        } catch (Exception ex) {
            LOGGER.error("Exception while inserting stats for RocksDb.", ex);
        }
    }

    /**
     * @param rocksDbCustomStats custom stats object
     * @param time               time taken in microseconds to read/write/delete data from db
     * @param value              byte array write into db
     */
    private void calculateRocksDbCustomStats(RocksDbCustomStats rocksDbCustomStats, long time, long value) {
        rocksDbCustomStats.setMinTime(Math.min(rocksDbCustomStats.getMinTime(), time));
        rocksDbCustomStats.setMaxTime(Math.max(rocksDbCustomStats.getMaxTime(), time));
        rocksDbCustomStats.setSumTime(rocksDbCustomStats.getSumTime() + time);
        rocksDbCustomStats.setCountTime(rocksDbCustomStats.getCountTime() + 1);
        double averageTime = (double) rocksDbCustomStats.getSumTime() / rocksDbCustomStats.getCountTime();
        rocksDbCustomStats.setAverageTime(averageTime);

        rocksDbCustomStats.setMinSize(Math.min(rocksDbCustomStats.getMinSize(), value));
        rocksDbCustomStats.setMaxSize(Math.max(rocksDbCustomStats.getMaxSize(), value));
        rocksDbCustomStats.setSumSize(rocksDbCustomStats.getSumSize() + value);
        rocksDbCustomStats.setCountSize(rocksDbCustomStats.getCountSize() + 1);
        double averageSize = (double) rocksDbCustomStats.getSumSize() / rocksDbCustomStats.getCountSize();
        rocksDbCustomStats.setAverageSize(averageSize);
    }
}

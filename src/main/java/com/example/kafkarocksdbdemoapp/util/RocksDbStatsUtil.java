package com.example.kafkarocksdbdemoapp.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.kafkarocksdbdemoapp.dao.RocksDbCustomStats;

public class RocksDbStatsUtil {
    public static final Map<String, RocksDbCustomStats> rocksdbReadOpStats = new ConcurrentHashMap<String, RocksDbCustomStats>();
    public static final Map<String, RocksDbCustomStats> rocksdbWriteOpStats = new ConcurrentHashMap<String, RocksDbCustomStats>();
    public static final Map<String, RocksDbCustomStats> rocksdbDeleteOpStats = new ConcurrentHashMap<String, RocksDbCustomStats>();
    public static final Map<String, String> ROCKSDB_PROPERTIES_MAP = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("ACTUAL_DELAYED_WRITE_RATE", "rocksdb.actual-delayed-write-rate");
        put("AGGREGATED_TABLE_PROPERTIES", "rocksdb.aggregated-table-properties");
        put("BACKGROUND_ERRORS", "rocksdb.background-errors");
        put("BASE_LEVEL", "rocksdb.base-level");
        put("BLOCK_CACHE_CAPACITY", "rocksdb.block-cache-capacity");
        put("BLOCK_CACHE_PINNED_USAGE", "rocksdb.block-cache-pinned-usage");
        put("BLOCK_CACHE_USAGE", "rocksdb.block-cache-usage");
        put("CFSTATS_NO_FILE_HISTOGRAM", "rocksdb.cfstats-no-file-histogram");
        put("CF_FILE_HISTOGRAM", "rocksdb.cf-file-histogram");
        put("COMPACTION_PENDING", "rocksdb.compaction-pending");
        put("CURRENT_SUPER_VERSION_NUMBER", "rocksdb.current-super-version-number");
        put("CUR_SIZE_ACTIVE_MEM_TABLE", "rocksdb.cur-size-active-mem-table");
        put("CUR_SIZE_ALL_MEM_TABLES", "rocksdb.cur-size-all-mem-tables");
        put("DBSTATS", "rocksdb.dbstats");
        put("ESTIMATE_LIVE_DATA_SIZE", "rocksdb.estimate-live-data-size");
        put("ESTIMATE_NUM_KEYS", "rocksdb.estimate-num-keys");
        put("ESTIMATE_PENDING_COMPACTION_BYTES", "rocksdb.estimate-pending-compaction-bytes");
        put("ESTIMATE_TABLE_READERS_MEM", "rocksdb.estimate-table-readers-mem");
        put("IS_FILE_DELETIONS_ENABLED", "rocksdb.is-file-deletions-enabled");
        put("IS_WRITE_STOPPED", "rocksdb.is-write-stopped");
        put("LEVELSTATS", "rocksdb.levelstats");
        put("LIVE_SST_FILES_SIZE", "rocksdb.live-sst-files-size");
        put("MEM_TABLE_FLUSH_PENDING", "rocksdb.mem-table-flush-pending");
        put("MIN_LOG_NUMBER_TO_KEEP", "rocksdb.min-log-number-to-keep");
        put("MIN_OBSOLETE_SST_NUMBER_TO_KEEP", "rocksdb.min-obsolete-sst-number-to-keep");
        put("NUM_DELETES_ACTIVE_MEM_TABLE", "rocksdb.num-deletes-active-mem-table");
        put("NUM_DELETES_IMM_MEM_TABLES", "rocksdb.num-deletes-imm-mem-tables");
        put("NUM_ENTRIES_ACTIVE_MEM_TABLE", "rocksdb.num-entries-active-mem-table");
        put("NUM_ENTRIES_IMM_MEM_TABLES", "rocksdb.num-entries-imm-mem-tables");
        put("NUM_IMMUTABLE_MEM_TABLE", "rocksdb.num-immutable-mem-table");
        put("NUM_IMMUTABLE_MEM_TABLE_FLUSHED", "rocksdb.num-immutable-mem-table-flushed");
        put("NUM_LIVE_VERSIONS", "rocksdb.num-live-versions");
        put("NUM_RUNNING_COMPACTIONS", "rocksdb.num-running-compactions");
        put("NUM_RUNNING_FLUSHES", "rocksdb.num-running-flushes");
        put("NUM_SNAPSHOTS", "rocksdb.num-snapshots");
        put("OLDEST_SNAPSHOT_TIME", "rocksdb.oldest-snapshot-time");
        put("SIZE_ALL_MEM_TABLES", "rocksdb.size-all-mem-tables");
        put("SSTABLES", "rocksdb.sstables");
        put("TOTAL_SST_FILES_SIZE", "rocksdb.total-sst-files-size");
    }});
}

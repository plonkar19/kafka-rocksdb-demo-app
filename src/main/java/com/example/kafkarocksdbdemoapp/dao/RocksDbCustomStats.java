/*
 * Copyright 2023 TiVo Inc.  All rights reserved.
 */

package com.example.kafkarocksdbdemoapp.dao;

/**
 * RocksDbCustomStats contains some aggregate values for time taken & size of data read/written/deleted from DB
 */
public class RocksDbCustomStats {
    private long minTime;
    private long maxTime;
    private long sumTime;
    private long countTime;
    private double averageTime;
    private long minSize;
    private long maxSize;
    private long sumSize;
    private long countSize;
    private double averageSize;

    public RocksDbCustomStats() {
        this.minTime = Long.MAX_VALUE;
        this.maxTime = Long.MIN_VALUE;
        this.minSize = Long.MAX_VALUE;
        this.maxSize = Long.MIN_VALUE;
    }

    public long getMinTime() {
        return minTime;
    }

    public void setMinTime(long minTime) {
        this.minTime = minTime;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }

    public long getSumTime() {
        return sumTime;
    }

    public void setSumTime(long sumTime) {
        this.sumTime = sumTime;
    }

    public long getCountTime() {
        return countTime;
    }

    public void setCountTime(long countTime) {
        this.countTime = countTime;
    }

    public double getAverageTime() {
        return averageTime;
    }

    public void setAverageTime(double averageTime) {
        this.averageTime = averageTime;
    }

    public long getMinSize() {
        return minSize;
    }

    public void setMinSize(long minSize) {
        this.minSize = minSize;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    public long getSumSize() {
        return sumSize;
    }

    public void setSumSize(long sumSize) {
        this.sumSize = sumSize;
    }

    public long getCountSize() {
        return countSize;
    }

    public void setCountSize(long countSize) {
        this.countSize = countSize;
    }

    public double getAverageSize() {
        return averageSize;
    }

    public void setAverageSize(double averageSize) {
        this.averageSize = averageSize;
    }

    @Override
    public String toString() {
        return "RocksDbCustomStats{" +
                "minTime=" + minTime +
                ", maxTime=" + maxTime +
                ", sumTime=" + sumTime +
                ", countTime=" + countTime +
                ", averageTime=" + averageTime +
                ", minSize=" + minSize +
                ", maxSize=" + maxSize +
                ", sumSize=" + sumSize +
                ", countSize=" + countSize +
                ", averageSize=" + averageSize +
                '}';
    }
}

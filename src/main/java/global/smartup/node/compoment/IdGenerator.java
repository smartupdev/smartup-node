package global.smartup.node.compoment;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Snowflake算法java实现
 * https://github.com/adyliu/idcenter/blob/master/src/main/java/com/sohu/idcenter/IdWorker.java
 */
@Component
public class IdGenerator {

    private long workerId =  0;
    private long datacenterId = 0;

    private static final long idepoch = 1554177600000L; // 起始时间
    private static final long workerIdBits = 5L;
    private static final long datacenterIdBits = 5L;
    private static final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    private static final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    private static final long sequenceBits = 12L;
    private static final long workerIdShift = sequenceBits;
    private static final long datacenterIdShift = sequenceBits + workerIdBits;
    private static final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    private static final long sequenceMask = -1L ^ (-1L << sequenceBits);

    private static long lastTimestamp = -1L;
    private static long sequence = 0L;

    @PostConstruct
    public void init() {
        this.workerId = 1;
        this.datacenterId = 0;
        check();
    }

    private void check() {
        if (workerId <= 0 || workerId > maxWorkerId) {
            throw new IllegalArgumentException("workerId is illegal: " + workerId);
        }
        if (datacenterId < 0 || datacenterId > maxDatacenterId) {
            throw new IllegalArgumentException("datacenterId is illegal: " + workerId);
        }
        if (idepoch >= System.currentTimeMillis()) {
            throw new IllegalArgumentException("idepoch is illegal: " + idepoch);
        }
    }

    public long getId() {
        long id = nextId();
        return id;
    }

    public String getStringId() {
        return Long.toString(getId(), 36);
    }

    private synchronized long nextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            throw new IllegalStateException("Clock moved backwards.");
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }
        lastTimestamp = timestamp;
        long id = ((timestamp - idepoch) << timestampLeftShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift)
                | sequence;
        return id;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }

    public long getIdTimestamp(long id){
        return idepoch + (id >> timestampLeftShift);
    }



}

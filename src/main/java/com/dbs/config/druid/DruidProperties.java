package com.dbs.config.druid;

/**
 * @author gaijf
 * @description Druid配置类
 * @date 2019/9/6
 */
public class DruidProperties {
    private int  initialSize;
    private int minIdle;
    private int maxActive;

    public int getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }
}

package com.dbs.config.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author gaijf
 * @description 多数据源的选择
 * @date 2019/9/6
 */
public class DynamicDataSourceToChoose extends AbstractRoutingDataSource {

    /**
     * 根据Key获取数据源的信息
     * @return
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return HandlerDataSource.getDataSource();
    }
}

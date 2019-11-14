package com.dbs.config.datasource;

/**
 * @author gaijf
 * @description 根据当前线程来选择具体的数据源
 * @date 2019/9/6
 */
public class HandlerDataSource {

    private static ThreadLocal<String> handlerThredLocal = new ThreadLocal<String>();

    /**
     * 设置当前使用的数据源
     * @param datasource
     */
    public static void putDataSource(String datasource) {
        handlerThredLocal.set(datasource);
    }

    /**
     * 取出当前要使用的数据源
     * @return
     */
    public static String getDataSource() {
        return handlerThredLocal.get();
    }

    /**
     * 使用默认的数据源
     */
    public static void clear() {
        handlerThredLocal.remove();
    }
}

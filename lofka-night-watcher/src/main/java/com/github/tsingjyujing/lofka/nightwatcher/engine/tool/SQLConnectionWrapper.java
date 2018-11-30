package com.github.tsingjyujing.lofka.nightwatcher.engine.tool;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.collect.Lists;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

/**
 * SQL连接的封装，允许以反射的方式获取各种类型的数据库连接
 * 需要将驱动放到POM中去
 */
public class SQLConnectionWrapper implements AutoCloseable {
    private Logger LOGGER = LoggerFactory.getLogger(SQLConnectionWrapper.class);
    private final DruidDataSource connection;

    public SQLConnectionWrapper(Properties settings) {
        connection = createDruidConnection(settings);
    }

    public Connection getConnection() throws SQLException {
        return connection.getConnection();
    }

    /**
     * 执行SQL（不考虑返回值）
     *
     * @param sql 需要执行的SQL语句
     * @throws SQLException
     */
    public void executeSQL(String sql) throws SQLException {
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    sql
            )) {
                boolean hasResultSet = ps.execute();
                if (hasResultSet) {
                    ps.getResultSet().close();
                    LOGGER.warn("Execute an query statement by executeStatement, ResultSet ignored.");
                }
            }
        }
    }

    /**
     * 查询并且以 Document 返回结果
     *
     * @param sql SQL语句
     * @return
     * @throws SQLException
     */
    public Document query(String sql) throws SQLException {
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    sql
            )) {
                boolean hasResultSet = ps.execute();
                if (!hasResultSet) {
                    throw new SQLException("No result set had returned.");
                } else {
                    final List<Document> data = Lists.newArrayList();
                    ResultSet rs = ps.getResultSet();
                    // Read all columns
                    int cc = rs.getMetaData().getColumnCount();
                    String[] columns = new String[cc];
                    for (int i = 1; i <= cc; i++) {
                        columns[i - 1] = rs.getMetaData().getColumnLabel(i);
                    }
                    // Read all rows
                    while (rs.next()) {
                        final Document datum = new Document();
                        for (int i = 1; i <= cc; i++) {
                            datum.put(columns[i - 1], rs.getString(i));
                        }
                        data.add(datum);
                    }
                    return new Document(
                            "result", data
                    ).append(
                            "sql", sql
                    ).append(
                            "columns", Lists.newArrayList(columns)
                    ).append(
                            "cols", cc
                    ).append(
                            "rows", data.size()
                    );
                }
            }
        }
    }


    /**
     * 创建连接所需要的信息
     *
     * @param settings
     * @return
     */
    public static DruidDataSource createDruidConnection(Properties settings) {
        final DruidDataSource druidDataSource;
        druidDataSource = new DruidDataSource(true);
        druidDataSource.setUrl(settings.getProperty("uri"));
        druidDataSource.setUsername(settings.getProperty("user"));
        druidDataSource.setPassword(settings.getProperty("password"));
        druidDataSource.setDriverClassName(settings.getProperty("driver"));
        druidDataSource.setMaxActive(Integer.parseInt(settings.getProperty("maxactive", "2")));
        druidDataSource.setMaxWait(Long.parseLong(settings.getProperty("maxwait", "10000")));
        druidDataSource.setTimeBetweenLogStatsMillis(Long.parseLong(settings.getProperty("log_interval", "30000")));
        druidDataSource.setAsyncInit(true);
        druidDataSource.setValidationQuery(settings.getProperty("validation_sql", "select user()"));
        druidDataSource.setTestOnBorrow(true);
        return druidDataSource;
    }

    /**
     * 通过JSON格式的配置信息创建连接代理
     *
     * @param jsonSettings JSON配置，和Properties差不多
     * @return
     */
    public static SQLConnectionWrapper createFromJsonString(String jsonSettings) {
        final Properties prop = new Properties();
        prop.putAll(Document.parse(jsonSettings));
        return new SQLConnectionWrapper(prop);
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}

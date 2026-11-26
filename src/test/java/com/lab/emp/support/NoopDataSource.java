package com.lab.emp.support;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * 不连接真实数据库的数据源。只满足事务管理器对 Connection 的调用。
 */
public class NoopDataSource implements DataSource {

    @Override
    public Connection getConnection() {
        return newConnection();
    }

    @Override
    public Connection getConnection(String username, String password) {
        return newConnection();
    }

    @Override
    public PrintWriter getLogWriter() {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) {
    }

    @Override
    public void setLoginTimeout(int seconds) {
    }

    @Override
    public int getLoginTimeout() {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException("不支持 unwrap");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return false;
    }

    private Connection newConnection() {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                String name = method.getName();
                if ("getAutoCommit".equals(name)) {
                    return Boolean.TRUE;
                }
                if ("setAutoCommit".equals(name) || "commit".equals(name) || "rollback".equals(name)
                        || "close".equals(name) || "setReadOnly".equals(name) || "clearWarnings".equals(name)) {
                    return null;
                }
                if ("isClosed".equals(name) || "isReadOnly".equals(name) || "isWrapperFor".equals(name)) {
                    return Boolean.FALSE;
                }
                if ("hashCode".equals(name)) {
                    return System.identityHashCode(proxy);
                }
                if ("equals".equals(name)) {
                    return Boolean.valueOf(proxy == args[0]);
                }
                if ("toString".equals(name)) {
                    return "NoopConnection";
                }
                if (method.getReturnType() == boolean.class) {
                    return Boolean.FALSE;
                }
                if (method.getReturnType() == int.class) {
                    return Integer.valueOf(0);
                }
                return null;
            }
        };
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                handler);
    }
}

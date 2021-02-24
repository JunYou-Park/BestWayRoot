package db;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Vector;

public class DBConnector {
    private Vector connections = new Vector(10);
    private String _driver = "com.mysql.cj.jdbc.Driver";
    private String _url = "jdbc:mysql://15.165.53.65/bestway?useUnicode=true&characterEncoding=utf-8";
    private String _user = "jypjun12";
    private String _password = "46821937QPwo#!@";
    private boolean _traceOn = false;
    private boolean initialized = false;
    private int _openConnections = 50;
    private static DBConnector instance = null;

    public DBConnector() {
    }

    public static DBConnector getInstance() {
        if (instance == null) {
            Class var0 = DBConnector.class;
            synchronized(DBConnector.class) {
                if (instance == null) {
                    instance = new DBConnector();
                }
            }
        }

        return instance;
    }

    public void setOpenConnectionCount(int count) {
        this._openConnections = count;
    }

    public void setEnableTrace(boolean enable) {
        this._traceOn = enable;
    }

    public Vector getConnectionList() {
        return this.connections;
    }

    public synchronized void setInitOpenConnections(int count) throws SQLException {
        Connection c = null;
        ConnectionObject co = null;

        for(int i = 0; i < count; ++i) {
            c = this.createConnection();
            co = new ConnectionObject(c, false);
            this.connections.addElement(co);
            this.trace("ConnectionPoolManager: Adding new DB connection to pool (" + this.connections.size() + ")");
        }

    }

    public int getConnectionCount() {
        return this.connections.size();
    }

    public synchronized Connection getConnection() throws Exception {
        if (!this.initialized) {
            Class c = Class.forName(this._driver);
            DriverManager.registerDriver((Driver)c.newInstance());
            this.initialized = true;
        }

        Connection c = null;
        ConnectionObject co = null;
        boolean badConnection = false;

        for(int i = 0; i < this.connections.size(); ++i) {
            co = (ConnectionObject)this.connections.elementAt(i);
            if (!co.inUse) {
                try {
                    badConnection = co.connection.isClosed();
                    if (!badConnection) {
                        badConnection = co.connection.getWarnings() != null;
                    }
                } catch (Exception var6) {
                    badConnection = true;
                    var6.printStackTrace();
                }

                if (!badConnection) {
                    c = co.connection;
                    co.inUse = true;
                    this.trace("ConnectionPoolManager: Using existing DB connection #" + (i + 1));
                    break;
                }

                this.connections.removeElementAt(i);
                this.trace("ConnectionPoolManager: Remove disconnected DB connection #" + i);
            }
        }

        if (c == null) {
            c = this.createConnection();
            co = new ConnectionObject(c, true);
            this.connections.addElement(co);
            this.trace("ConnectionPoolManager: Creating new DB connection #" + this.connections.size());
        }

        return c;
    }

    public synchronized void freeConnection(Connection c) {
        if (c != null) {
            ConnectionObject co = null;

            int i;
            for(i = 0; i < this.connections.size(); ++i) {
                co = (ConnectionObject)this.connections.elementAt(i);
                if (c == co.connection) {
                    co.inUse = false;
                    break;
                }
            }

            for(i = 0; i < this.connections.size(); ++i) {
                co = (ConnectionObject)this.connections.elementAt(i);
                if (i + 1 > this._openConnections && !co.inUse) {
                    this.removeConnection(co.connection);
                }
            }

        }
    }

    public void freeConnection(Connection c, PreparedStatement p, ResultSet r) {
        try {
            if (r != null) {
                r.close();
            }

            if (p != null) {
                p.close();
            }

            this.freeConnection(c);
        } catch (SQLException var5) {
            var5.printStackTrace();
        }

    }

    public void freeConnection(Connection c, Statement s, ResultSet r) {
        try {
            if (r != null) {
                r.close();
            }

            if (s != null) {
                s.close();
            }

            this.freeConnection(c);
        } catch (SQLException var5) {
            var5.printStackTrace();
        }

    }

    public void freeConnection(Connection c, PreparedStatement p) {
        try {
            if (p != null) {
                p.close();
            }

            this.freeConnection(c);
        } catch (SQLException var4) {
            var4.printStackTrace();
        }

    }

    public void freeConnection(Connection c, Statement s) {
        try {
            if (s != null) {
                s.close();
            }

            this.freeConnection(c);
        } catch (SQLException var4) {
            var4.printStackTrace();
        }

    }

    public synchronized void removeConnection(Connection c) {
        if (c != null) {
            ConnectionObject co = null;

            for(int i = 0; i < this.connections.size(); ++i) {
                co = (ConnectionObject)this.connections.elementAt(i);
                if (c == co.connection) {
                    try {
                        c.close();
                        this.connections.removeElementAt(i);
                        this.trace("Removed " + c.toString());
                    } catch (Exception var5) {
                        var5.printStackTrace();
                    }
                    break;
                }
            }

        }
    }

    private Connection createConnection() throws SQLException {
        Connection con = null;

        try {
            if (this._user == null) {
                this._user = "";
            }

            if (this._password == null) {
                this._password = "";
            }

            Properties props = new Properties();
            props.put("user", this._user);
            props.put("password", this._password);
            con = DriverManager.getConnection(this._url, props);
            return con;
        } catch (Throwable var3) {
            throw new SQLException(var3.getMessage());
        }
    }

    public void releaseFreeConnections() {
        this.trace("ConnectionPoolManager.releaseFreeConnections()");
        Connection c = null;
        ConnectionObject co = null;

        for(int i = 0; i < this.connections.size(); ++i) {
            co = (ConnectionObject)this.connections.elementAt(i);
            if (!co.inUse) {
                this.removeConnection(co.connection);
            }
        }

    }

    public void finalize() {
        this.trace("ConnectionPoolManager.finalize()");
        Connection c = null;
        ConnectionObject co = null;

        for(int i = 0; i < this.connections.size(); ++i) {
            co = (ConnectionObject)this.connections.elementAt(i);

            try {
                co.connection.close();
            } catch (Exception var5) {
                var5.printStackTrace();
            }

            co = null;
        }

        this.connections.removeAllElements();
    }

    private void trace(String s) {
        if (this._traceOn) {
            System.err.println(s);
        }

    }
}

package util.db;

import java.sql.Connection;

class ConnectionObject {
    public Connection connection = null;
    public boolean inUse = false;

    public ConnectionObject(Connection c, boolean useFlag) {
        this.connection = c;
        this.inUse = useFlag;
    }
}

package com.ejfirestar.querybuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A basic Query Builder for Java SQL.<br>
 * This is not a complete builder, and should be used primarily as a template.
 *
 * @author Evan
 */
public class QueryBuilder {

    private final long duration = System.currentTimeMillis();
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet result;
    private int row = 1;
    private int index = 1;
    private boolean debug = false;

    /**
     * Starts the QueryBuilder.
     *
     * @param connection The Connection to the database.
     * @throws SQLException
     */
    public QueryBuilder(Connection connection) throws SQLException {
        if (connection != null) {
            this.connection = connection;
        }
        else {
            throw new SQLException();
        }
    }

    /**
     * Starts the QueryBuilder.
     *
     * @param connection The Connection to the database.
     * @param debug      Boolean specifying whether to print the run duration.
     * @throws SQLException
     */
    public QueryBuilder(Connection connection, boolean debug) throws SQLException {
        if (connection != null) {
            this.connection = connection;
            this.debug = debug;
        }
        else {
            throw new SQLException();
        }
    }

    /**
     * Creates a new PreparedStatement from the given query String.
     *
     * @param raw_query The query String.
     * @return an updated QueryBuilder
     * @throws SQLException
     */
    public QueryBuilder setQuery(String raw_query) throws SQLException {
        if (raw_query != null) {
            statement = connection.prepareStatement(raw_query);
        }
        else {
            throw new SQLException();
        }
        return this;
    }

    /**
     * Sets the value of the current column to an Integer
     *
     * @param value The value to set.
     * @return an updated QueryBuilder
     * @throws SQLException
     */
    public QueryBuilder setInt(Integer value) throws SQLException {
        return setInt(index++, value);
    }

    /**
     * Sets the value of the specified column to an Integer
     *
     * @param column_index The column to set.
     * @param value        The value to set
     * @return an updated QueryBuilder
     * @throws SQLException
     */
    public QueryBuilder setInt(int column_index, Integer value) throws SQLException {
        statement.setInt(column_index, value);
        return this;
    }

    /**
     * Sets the value of the current column to a Double
     *
     * @param value The value to set.
     * @return an updated QueryBuilder
     * @throws SQLException
     */
    public QueryBuilder setDouble(Double value) throws SQLException {
        return setDouble(index++, value);
    }

    /**
     * Sets the value of the specified column to a Double
     *
     * @param column_index The column to set.
     * @param value        The value to set.
     * @return an updated QueryBuilder
     * @throws SQLException
     */
    public QueryBuilder setDouble(int column_index, Double value) throws SQLException {
        statement.setDouble(column_index, value);
        return this;
    }

    /**
     * Sets the value of the current column to a Long
     *
     * @param value The value to set.
     * @return an updated QueryBuilder
     * @throws SQLException
     */
    public QueryBuilder setLong(Long value) throws SQLException {
        return setLong(index++, value);
    }

    /**
     * Sets the value of the specified column to a Long
     *
     * @param column_index The column to set.
     * @param value        The value to set.
     * @return an updated QueryBuilder
     * @throws SQLException
     */
    public QueryBuilder setLong(int column_index, Long value) throws SQLException {
        statement.setDouble(column_index, value);
        return this;
    }

    /**
     * Sets the value of the current column to a String
     *
     * @param value The value to set.
     * @return an updated QueryBuilder
     * @throws SQLException
     */
    public QueryBuilder setString(String value) throws SQLException {
        return setString(index++, value);
    }

    /**
     * Sets the value of the specified column to a String
     *
     * @param column_index The column to set.
     * @param value        The value to set.
     * @return an updated QueryBuilder
     * @throws SQLException
     */
    public QueryBuilder setString(int column_index, String value) throws SQLException {
        statement.setString(column_index, value);
        return this;
    }

    /**
     * Sets the value of the current column to a Boolean
     *
     * @param value The value to set.
     * @return an updated QueryBuilder
     * @throws SQLException
     */
    public QueryBuilder setBoolean(Boolean value) throws SQLException {
        return setBoolean(index++, value);
    }

    /**
     * Sets the value of the specified column to a Boolean
     *
     * @param column_index The column to set.
     * @param value        The value to set.
     * @return an updated QueryBuilder
     * @throws SQLException
     */
    public QueryBuilder setBoolean(int column_index, Boolean value) throws SQLException {
        statement.setBoolean(column_index, value);
        return this;
    }

    /**
     * Executes the previously set PreparedStatement.<br>
     *
     * @return an updated QueryBuilder
     * @throws SQLException
     */
    public QueryBuilder executeQuery() throws SQLException {
        result = statement.executeQuery();
        closeConnection();
        return this;
    }

    /**
     * Executes the previously set PreparedStatement.<br>
     * This should be used when performing queries that do not require data to be returned<br>
     * such as <code>INSERT</code>, <code>UPDATE</code> or <code>DELETE</code>;
     *
     * @return an updated QueryBuilder
     * @throws SQLException
     */
    public int update() throws SQLException {
        int result = statement.executeUpdate();
        closeConnection();
        return result;
    }

    /**
     * Moves the cursor to the next row.
     *
     * @return an updated QueryBuilder
     * @throws SQLException
     */
    public boolean nextRow() throws SQLException {
        //Move to next row
        row++;
        return result.next();
    }

    /**
     * Moves the cursor to the specified row.
     *
     * @param row The index of the row<br>
     *            Remember that SQL is 1-based.
     * @return an updated QueryBuilder
     * @throws SQLException
     */
    public QueryBuilder setRow(int row) throws SQLException {
        //Update current row
        this.row = row;
        result.absolute(row);
        return this;
    }

    /**
     * Checks to see if the specified row exists.
     *
     * @param row The index of the row<br>
     *            Remember that SQL is 1-based.
     * @return an updated QueryBuilder
     */
    public boolean rowExists(int row) {
        try {
            boolean exists = result.absolute(row);
            // resets the cursor the the previous row
            result.absolute(this.row);
            return exists;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fetches a specific column value.
     *
     * @param column_index The column index to fetch<br>
     *                     Remember that SQL is 1-based.
     * @param clazz        The Class of the Object to fetch
     * @return An Object casted to the specified Class.
     */
    public <T> T fetch(int column_index, Class<T> clazz) {
        try {
            if (result.first()) {
                cast(result.getObject(column_index), clazz);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Fetches a single row with a single return value.
     *
     * @param clazz The Class of the Object to fetch
     * @return An Object casted to the specified Class.
     */
    public <T> T fetchOne(Class<T> clazz) {
        try {
            if (result.first()) {
                return cast(result.getObject(1), clazz);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Fetches a single row of N {@link Column}s<br>
     * Since the {@link Column}s are passed in by reference, there is no return value
     *
     * @param columns The {@link Column}s to fetch.
     */
    public void fetchOne(Column<?>... columns) {
        try {
            if (result.first()) {
                for (int i = 0; i < columns.length; i++) {
                    columns[i].value = result.getObject(i + 1);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches all rows for N {@link Column}s<br>
     * Since the {@link Column}s are passed in by reference, there is no return value
     *
     * @param columns The {@link Column}s to fetch
     */
    public void fetchAll(Column<?>... columns) {
        try {
            for (int i = 0; i < columns.length; i++) {
                result.beforeFirst();
                while (nextRow()) {
                    columns[i].add(result.getObject(i + 1));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches all rows for a single Column
     *
     * @param clazz The Class of the Object to fetch
     * @return An Object casted to the specified Class.
     */
    public <T> List<T> fetchAll(Class<T> clazz) {
        List<T> fetch_all = new ArrayList<>();
        try {
            while (result.next()) {
                Object o = result.getObject(1);
                fetch_all.add(cast(o, clazz));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return fetch_all;
    }

    /**
     * Closes the connection.<br>
     * Notes: The connection will be closed automatically if you use <code>executeQuery</code> or <code>update</code>
     *
     * @throws SQLException
     */
    public void closeConnection() throws SQLException {
        if (connection != null) {
            connection.close();
        }
        if (statement != null) {
            statement.close();
        }
        if (debug) {
            debugRunDuration();
        }
    }

    /**
     * Converts system milli-time to a nice eye-catching format for performance debugging.<br>
     * Output resembles: 4.5 seconds - !!!!<br>
     * Exclamation marks = number of seconds<br>
     */
    private void debugRunDuration() {
        double seconds = System.currentTimeMillis() - duration;
        seconds = seconds / 1000; //Convert to seconds
        String output = new DecimalFormat("0.000").format(seconds) + " seconds - ";
        int rounded_seconds = (int) Math.round(seconds);
        for (int i = 0; i < rounded_seconds; i++) {
            output += "!";
        }
        System.out.println("Query finished in " + output);
    }

    /**
     * Casts an Object to a generic type.
     *
     * @param o     The Object to cast
     * @param clazz The Class to cast to
     * @return A generic type
     */
    private <T> T cast(Object o, Class<T> clazz) {
        if (clazz == null || o == null) {
            return null;
        }
        // We want to use Integers, SQL tries to give us Longs
        if (o instanceof Long && !clazz.equals(Long.class)) {
            return clazz.cast(((Long) o).intValue());
        }
        return clazz.cast(o);
    }
}

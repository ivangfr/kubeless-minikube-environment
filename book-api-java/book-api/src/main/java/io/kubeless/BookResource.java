package io.kubeless;

// -- Uncomment those imports in the final function version
//import io.kubeless.Event;
//import io.kubeless.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BookResource {

    private static final Logger log = LoggerFactory.getLogger(BookResource.class);

    private Database database;

    public BookResource() throws SQLException {
        this.database = new Database();
    }

    public Database getDatabase() {
        return database;
    }

    public String getBooks(Event event, Context context) {
        try {
            return Response.ok(database.selectBooks());
        } catch (SQLException e) {
            return Response.internalServerError(e.getMessage());
        }
    }

    public String getBook(Event event, Context context) {
        try {
            final long bookId = validateAndGetBookId(event.Data);
            final JSONObject jsonObject = database.selectBook(bookId);
            return (jsonObject == null) ? Response.notFound(String.format("Book with id '%s' not found!", bookId)) : Response.ok(jsonObject);
        } catch (SQLException e) {
            return Response.internalServerError(e.getMessage());
        } catch (IllegalArgumentException e) {
            return Response.badRequest(e.getMessage());
        }
    }

    public String addBook(Event event, Context context) {
        try {
            final long bookId = database.insertBook(validateAndGetBookJsonObject(event.Data));
            return Response.ok(bookId);
        } catch (SQLException e) {
            return Response.internalServerError(e.getMessage());
        } catch (IllegalArgumentException e) {
            return Response.badRequest(e.getMessage());
        }
    }

    public String removeBook(Event event, Context context) {
        try {
            final long bookId = validateAndGetBookId(event.Data);
            final JSONObject jsonObject = database.selectBook(bookId);
            if (jsonObject == null) {
                return Response.notFound(String.format("Book with id '%s' not found!", bookId));
            } else {
                database.deleteBook(bookId);
                return Response.ok(bookId);
            }
        } catch (SQLException e) {
            return Response.internalServerError(e.getMessage());
        } catch (IllegalArgumentException e) {
            return Response.badRequest(e.getMessage());
        }
    }

    private long validateAndGetBookId(final String data) {
        try {
            return Long.parseLong(data);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid data value! You must inform the book id as a NUMERIC value, for example: --data='1'");
        }
    }

    private JSONObject validateAndGetBookJsonObject(final String data) {
        try {
            final JSONObject jsonObject = new JSONObject(data);
            jsonObject.getString("isbn");
            jsonObject.getString("title");
            return jsonObject;
        } catch (JSONException e) {
            throw new IllegalArgumentException("Invalid data value! You must inform the book data with the following JSON payload: --data='{\"isbn\": \"...\", \"title\": \"...\"}'");
        }
    }

    public static class Database {

        private Connection conn;
        private PreparedStatement psGetBooks;
        private PreparedStatement psGetBook;
        private PreparedStatement psAddBook;
        private PreparedStatement psDeleteBook;

        private Database() throws SQLException {
            connectToDatabase();
            initPrepareStatements();
            createBookTable();
        }

        public Connection getConn() {
            return conn;
        }

        private JSONArray selectBooks() throws SQLException {
            final JSONArray jsonArray = new JSONArray();
            final ResultSet rs = psGetBooks.executeQuery();
            while (rs.next()) {
                jsonArray.put(getBookJSONObject(rs));
            }
            rs.close();
            return jsonArray;
        }

        private JSONObject selectBook(final long bookId) throws SQLException {
            JSONObject jsonObject = null;
            psGetBook.setLong(1, bookId);
            final ResultSet rs = psGetBook.executeQuery();
            if (rs.next()) {
                jsonObject = getBookJSONObject(rs);
            }
            rs.close();
            return jsonObject;
        }

        private long insertBook(final JSONObject jsonObject) throws SQLException {
            conn.setAutoCommit(false);
            psAddBook.setString(1, jsonObject.getString("isbn"));
            psAddBook.setString(2, jsonObject.getString("title"));
            psAddBook.executeUpdate();
            conn.commit();
            conn.setAutoCommit(true);

            final ResultSet rs = psAddBook.getGeneratedKeys();
            long id = -1;
            if (rs.next()) {
                id = rs.getLong(1);
            }
            rs.close();
            return id;
        }

        private void deleteBook(final Long bookId) throws SQLException {
            conn.setAutoCommit(false);
            psDeleteBook.setLong(1, bookId);
            psDeleteBook.executeUpdate();
            conn.commit();
            conn.setAutoCommit(true);
        }

        private void connectToDatabase() throws SQLException {
            final String url = String.format("jdbc:mysql://%s:%s/%s?autoReconnect=true&useSSL=false&useUnicode=yes&characterEncoding=UTF-8&useLegacyDatetimeCode=false&serverTimezone=UTC",
                    EnvVars.MYSQL_HOST, EnvVars.MYSQL_PORT, EnvVars.MYSQL_DATABASE);
            log.info("Connecting to MySQL ... URL: {}", url);
            conn = DriverManager.getConnection(url, EnvVars.MYSQL_USER, EnvVars.MYSQL_PASSWORD);
            log.info("Connection to MySQL Connection completed successfully!");
        }

        private void initPrepareStatements() throws SQLException {
            psGetBooks = conn.prepareStatement("SELECT * FROM `books`");
            psGetBook = conn.prepareStatement("SELECT * FROM `books` WHERE `id` = (?)");
            psAddBook = conn.prepareStatement("INSERT INTO `books` (`isbn`, `title`) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS);
            psDeleteBook = conn.prepareStatement("DELETE FROM `books` WHERE `id` = (?)");
        }

        private void createBookTable() throws SQLException {
            final String sql = "CREATE TABLE `books` (" +
                    "  `id` bigint(20) NOT NULL AUTO_INCREMENT," +
                    "  `isbn` varchar(255) NOT NULL," +
                    "  `title` varchar(255) NOT NULL," +
                    "  PRIMARY KEY (`id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

            if (!checkBooksTableExists()) {
                final PreparedStatement ps = conn.prepareStatement(sql);
                ps.execute();
                log.info("Table 'books' created successfully!");
                ps.close();
            } else {
                log.info("The table 'books' already exists.");
            }
        }

        private boolean checkBooksTableExists() throws SQLException {
            return conn.getMetaData()
                    .getTables(null, null, "books", new String[]{"TABLE"})
                    .next();
        }

        private JSONObject getBookJSONObject(final ResultSet rs) throws SQLException {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", rs.getLong("id"));
            jsonObject.put("isbn", rs.getString("isbn"));
            jsonObject.put("title", rs.getString("title"));
            return jsonObject;
        }
    }

    private static class Response {

        private static String ok(final Object data) {
            return buildResponse(200, data, null).toString();
        }

        private static String notFound(final String error) {
            return buildResponse(404, null, error).toString();
        }

        private static String badRequest(final String error) {
            return buildResponse(400, null, error).toString();
        }

        private static String internalServerError(final String error) {
            return buildResponse(503, null, error).toString();
        }

        private static JSONObject buildResponse(final int code, final Object data, final String error) {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", code);
            if (data != null) {
                jsonObject.put("data", data);
            }
            if (error != null) {
                jsonObject.put("error", error);
            }
            return jsonObject;
        }
    }

    private static class EnvVars {

        private static String MYSQL_HOST;
        private static String MYSQL_PORT;
        private static String MYSQL_DATABASE;
        private static String MYSQL_USER;
        private static String MYSQL_PASSWORD;

        static {
            MYSQL_HOST = validateAndGetEnv("MYSQL_HOST", "localhost");
            MYSQL_PORT = validateAndGetEnv("MYSQL_PORT", "3306");
            MYSQL_DATABASE = validateAndGetEnv("MYSQL_DATABASE", "bookdb");
            MYSQL_USER = validateAndGetEnv("MYSQL_USER");
            MYSQL_PASSWORD = validateAndGetEnv("MYSQL_PASSWORD");
        }

        private static String validateAndGetEnv(final String key) {
            final String value = System.getenv(key);
            if (value == null) {
                throw new InvalidParameterException(String.format("Missing value for key %s!", key));
            }
            return value;
        }

        private static String validateAndGetEnv(final String key, final String defaultValue) {
            final String value = System.getenv(key);
            return (value == null) ? defaultValue : value;
        }
    }

}

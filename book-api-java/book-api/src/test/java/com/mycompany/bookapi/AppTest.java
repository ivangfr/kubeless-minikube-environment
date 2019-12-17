package com.mycompany.bookapi;

import io.kubeless.BookResource;
import io.kubeless.Context;
import io.kubeless.Event;
import org.junit.Rule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.containers.MySQLContainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppTest {

    private static BookResource bookResource;
    private static Connection conn;

    @Rule
    public static MySQLContainer mySQLContainer;

    @BeforeAll
    static void init() throws SQLException {
        mySQLContainer = new MySQLContainer("mysql:5.7.28")
                .withDatabaseName("bookdb")
                .withUsername("bookuser")
                .withPassword("bookpass");
        mySQLContainer.setPortBindings(Collections.singletonList("3306:3306"));
        mySQLContainer.start();
        bookResource = new BookResource();
        conn = bookResource.getDatabase().getConn();
    }

    @AfterEach
    void clearDatabase() throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("TRUNCATE TABLE `books`")) {
            ps.executeUpdate();
        }
    }

    @AfterAll
    static void cleanup() throws SQLException {
        conn.close();
        mySQLContainer.stop();
    }

    // Get books

    @Test
    void testGetBooks() {
        String expected = "{\"code\":200,\"data\":[]}";
        String actual = bookResource.getBooks(getEvent(""), getContext());
        assertEquals(expected, actual);
    }

    // Get book

    @Test
    void testGetBookExistingOne() {
        bookResource.addBook(getEvent("{\"isbn\":\"123\", \"title\":\"Learn Kubeless\"}"), getContext());

        String expected = "{\"code\":200,\"data\":{\"isbn\":\"123\",\"id\":1,\"title\":\"Learn Kubeless\"}}";
        String actual = bookResource.getBook(getEvent("1"), getContext());
        assertEquals(expected, actual);
    }

    @Test
    void testGetBookNonExistingOne() {
        String expected = "{\"code\":404,\"error\":\"Book with id '1' not found!\"}";
        String actual = bookResource.getBook(getEvent("1"), getContext());
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "{\"id\":1}"})
    void testGetBookInvalidInput(String input) {
        String expected = "{\"code\":400,\"error\":\"Invalid data value! You must inform the book id as a NUMERIC value, for example: --data='1'\"}";
        String actual = bookResource.getBook(getEvent(input), getContext());
        assertEquals(expected, actual);
    }

    // Add books

    @Test
    void testAddBookValidInput() {
        String expected = "{\"code\":200,\"data\":1}";
        String actual = bookResource.addBook(getEvent("{\"isbn\":\"123\",\"title\":\"Learn Kubeless\"}"), getContext());
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "a", "{\"id\":1}", "{\"isbn\":\"123\"}", "{\"title\":\"Learn Kubeless\"}"})
    void testAddBookInvalidInput(String input) {
        String expected = "{\"code\":400,\"error\":\"Invalid data value! You must inform the book data with the following JSON payload: --data='{\\\"isbn\\\": \\\"...\\\", \\\"title\\\": \\\"...\\\"}'\"}";
        String actual = bookResource.addBook(getEvent(input), getContext());
        assertEquals(expected, actual);
    }

    // Remove books

    @Test
    void testRemoveBookExistingOne() {
        bookResource.addBook(getEvent("{\"isbn\":\"123\", \"title\":\"Learn Kubeless\"}"), getContext());

        String expected = "{\"code\":200,\"data\":1}";
        String actual = bookResource.removeBook(getEvent("1"), getContext());
        assertEquals(expected, actual);
    }

    @Test
    void testRemoveBookNonExistingOne() {
        String expected = "{\"code\":404,\"error\":\"Book with id '1' not found!\"}";
        String actual = bookResource.removeBook(getEvent("1"), getContext());
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "{\"id\":1}"})
    void testRemoveBookInvalidInput(String input) {
        String expected = "{\"code\":400,\"error\":\"Invalid data value! You must inform the book id as a NUMERIC value, for example: --data='1'\"}";
        String actual = bookResource.removeBook(getEvent(input), getContext());
        assertEquals(expected, actual);
    }

    // Helper methods

    private Event getEvent(String data) {
        return new Event(data, "", "", "", "");
    }

    private Context getContext() {
        return new Context("", "", "", "");
    }

}

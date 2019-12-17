package com.mycompany.bookapi;

import io.kubeless.BookResource;
import io.kubeless.Context;
import io.kubeless.Event;

import java.sql.SQLException;

public class App {

    public static void main(String[] args) throws SQLException {
        BookResource bookResource = new BookResource();
        bookResource.getBooks(getEvent(""), getContext());
    }

    // Helper methods

    private static Event getEvent(String data) {
        return new Event(data, "", "", "", "");
    }

    private static Context getContext() {
        return new Context("", "", "", "");
    }

}

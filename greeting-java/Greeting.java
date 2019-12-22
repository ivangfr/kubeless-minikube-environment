package io.kubeless;

public class Greeting {

    public String greet(Event event, Context context) {
        String name = event.Data.trim().isEmpty() ? "World" : event.Data;
        return "Hello " + name + "!!!";
    }

}


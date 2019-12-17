package io.kubeless;

import io.kubeless.Event;
import io.kubeless.Context;

public class Greeting {

    public String greet(Event event, Context context) {
        String name = event.Data != null ? event.Data : "World";
        return "Hello " + name + "!!!\n";
    }

}


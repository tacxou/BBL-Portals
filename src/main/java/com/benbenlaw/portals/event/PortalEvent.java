package com.benbenlaw.portals.event;

import java.util.function.Function;

public class PortalEvent<I, O> {

    private Function<I, O> event;

    private O defaultOutput;

    public PortalEvent() {}

    public PortalEvent(O defaultOutput) {
        this.defaultOutput = defaultOutput;
    }

    public boolean hasEvent() {
        return event != null;
    }

    public void register(Function<I, O> execute) {
        event = execute;
    }

    public O execute(I input) {
        if (hasEvent())
            return event.apply(input);
        return defaultOutput;
    }
}
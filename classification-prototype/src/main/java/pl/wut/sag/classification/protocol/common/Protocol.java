package pl.wut.sag.classification.protocol.common;

import lombok.Getter;

public class Protocol {
    @Getter
    private final String name;

    public Protocol() {
        this.name = this.getClass().getSimpleName();
    }
}

package pl.wut.sag.classification.protocol.up;

import lombok.Data;

@Data
public class ImUp {
    private String hello;

    public static ImUp withNoGoodMorning() {
        return new ImUp();
    }
}

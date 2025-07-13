package com.recorder.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Locator {
    public String name;
    public LookupDetails lookupDetails;

    @Getter
    @Setter
    public static class LookupDetails {
        public String findBy;
        public String value;
    }
}

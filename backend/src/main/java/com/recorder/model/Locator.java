package com.recorder.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Locator {
    public String name;
    public LookupDetails lookupDetails;

    public Locator() {

    }

    public Locator(String name, LookupDetails lookupDetails) {
        this.name = name;
        this.lookupDetails = lookupDetails;
    }

    @Getter
    @Setter
    public static class LookupDetails {
        public String findBy;
        public String value;

        public LookupDetails(String findBy, String value) {
            this.findBy = findBy;
            this.value = value;
        }

    }
}

package com.example.myfirstapp;

import java.util.HashMap;

public class SampleGattAttributes
{
    private static HashMap<String, String> attributes = new HashMap();
    public static final String CLOCK_SERVICE_UUID = "51311102-030e-485f-b122-f8f381aa84ed";
    public static final String HOURS_CHARAC_UUID = "485f4145-52b9-4644-af1f-7a6b9322490f";
    public static final String MINUTES_CHARAC_UUID = "0a924ca7-87cd-4699-a3bd-abdcd9cf126a";
    public static final String SECONDS_CHARAC_UUID = "8dd6a1b7-bc75-4741-8a26-264af75807de";

    static
    {
        // Services
        attributes.put(CLOCK_SERVICE_UUID, "Clock service");

        // Characteristics
        attributes.put(HOURS_CHARAC_UUID, "Hours");
        attributes.put(MINUTES_CHARAC_UUID, "Minutes");
        attributes.put(SECONDS_CHARAC_UUID, "Seconds");
    }

    public static String lookup(String uuid, String defaultName)
    {
        String name = attributes.get(uuid);
        return (name == null) ? defaultName : name;
    }
}

package com.imprezer.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by robert on 02.05.16.
 */
public enum EventCategory {

    CONCERT("Koncert", "Concert", 0), OUTDOOR("Plener", "Outdoor", 1), CLUB("Klub",
            "Club", 2), PICNIC("Festyn", "Picnic", 3), OPERA_THEATER("Opera/Teatr", "Opera/Theater", 4), FIRE(
            "Ognisko", "Bonfire", 5), EXHIBITION("Wystawa", "Exhibition", 6), CULTURE(
            "Kultura", "Culture", 7), FESTIVAL("Festival", "Festival", 8), DANCE(
            "Zabawa taneczna", "Dancing", 9), OTHER("Inne", "Other", 10);

    private String _namePL;
    private String _nameUS;
    private Integer _numeric;

    EventCategory(String namePL, String nameUS, Integer numeric) {
        set_namePL(namePL);
        set_nameUS(nameUS);
        set_numeric(numeric);

    }


    public String get_namePL() {
        return _namePL;
    }

    public void set_namePL(String _namePL) {
        this._namePL = _namePL;
    }

    public String get_nameUS() {
        return _nameUS;
    }

    public void set_nameUS(String _nameUS) {
        this._nameUS = _nameUS;
    }

    public Integer get_numeric() {
        return _numeric;
    }

    public void set_numeric(Integer _numeric) {
        this._numeric = _numeric;
    }

    public static EventCategory ofValue(Integer next) {
        Optional<EventCategory> optVal = Arrays
                .stream(EventCategory.values())
                .filter(v -> v.get_numeric().equals(next))
                .findAny();
        return optVal.isPresent() ? optVal.get() : null;
    }
}
package com.imprezer.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by robert on 02.05.16.
 */
@Document
public class UserDetails {
    @Id
    private String id;


    public UserDetails(String id) {
        this.id = id;
    }
}

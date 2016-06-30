package com.imprezer.repositories;

import com.imprezer.model.EventDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by robert on 29.06.16.
 */
@Repository
public interface EventDetailsRepository extends MongoRepository<EventDetails, String> {

    EventDetails findById(String id);

}

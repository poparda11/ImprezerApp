package com.imprezer.repositories;

import com.imprezer.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by robert on 02.05.16.
 */
@Repository
public interface EventRepository extends MongoRepository<Event, String>  {

    Event findById(String id);

    Page<Event> findByName(String name, Pageable pageable);

    Page<Event> findAllByOrderByRankDesc(Pageable pageable);

    Event findByDetailsId(String id);
}
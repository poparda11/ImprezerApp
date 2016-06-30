package com.imprezer.repositories;

import java.util.List;

import com.imprezer.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by robert on 02.05.16.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    User findByIdentification(String identification);

    List<User> findByName(String name);

}
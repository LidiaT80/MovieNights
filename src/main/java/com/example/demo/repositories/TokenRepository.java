package com.example.demo.repositories;

import com.example.demo.models.Token;
import org.springframework.data.repository.CrudRepository;

public interface TokenRepository extends CrudRepository<Token, Integer> {
}

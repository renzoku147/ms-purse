package com.spring.mspurse.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.spring.mspurse.entity.Purse;

import reactor.core.publisher.Mono;

public interface PurseRepository extends ReactiveMongoRepository<Purse, String>{

	Mono<Purse> findByPhoneNumber(Integer number);
}

package com.spring.mspurse.service;

import java.util.Optional;

import com.spring.mspurse.entity.DebitCard;
import com.spring.mspurse.entity.Purse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PurseService {
	Mono<Purse> create(Purse t);

    Flux<Purse> findAll();

    Mono<Purse> findById(String id);
    
    Mono<Purse> findByPhoneNumber(Integer id);

    Mono<Purse> update(Purse t);
    
    Mono<Purse> updateDebitCard(Purse t);

    Mono<Boolean> delete(String t);
    
    Mono<Optional<DebitCard>> verifyCardNumber(String t);
    
    
}

package com.spring.mspurse.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.mspurse.entity.Card;
import com.spring.mspurse.entity.CreditCard;
import com.spring.mspurse.entity.DebitCard;
import com.spring.mspurse.entity.Purse;
import com.spring.mspurse.repository.PurseRepository;
import com.spring.mspurse.service.PurseService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class PurseServiceImpl implements PurseService{

	@Autowired
	PurseRepository purseRepository;
	
	WebClient webClientDebitCard = WebClient.create("http://localhost:8887/ms-debitcard/debitCard");
	
	WebClient webClientCreditCard = WebClient.create("http://localhost:8887/ms-creditcard/creditCard");
	
	
	
	@Override
	public Mono<Purse> create(Purse t) {
		return purseRepository.save(t);
	}

	@Override
	public Flux<Purse> findAll() {
		return purseRepository.findAll();
	}

	@Override
	public Mono<Purse> findById(String id) {
		return purseRepository.findById(id);
	}

	@Override
	public Mono<Purse> update(Purse t) {
		return purseRepository.save(t)
				.filter(purse -> purse.getBalance()>=0);
	}
	
	@Override
	public Mono<Purse> updateDebitCard(Purse t) {
		return purseRepository.save(t)
				.filter(purse -> purse.getBalance()>=0);
	}

	@Override
	public Mono<Boolean> delete(String t) {
		return purseRepository.findById(t)
                .flatMap(dc -> purseRepository.delete(dc).then(Mono.just(Boolean.TRUE)))
                .defaultIfEmpty(Boolean.FALSE);
	}

	@Override
	public Mono<Optional<DebitCard>> verifyCardNumber(String cardNumber) {
		if(cardNumber == null) {
			log.info("Verify Null");
			return Mono.just(Optional.empty());
		}else {
			log.info("Verify carNumber >>> " + cardNumber);
			return webClientDebitCard.get().uri("/findCreditCardByCardNumber/{cardNumber}", cardNumber)
	                .accept(MediaType.APPLICATION_JSON)
	                .retrieve()
	                .bodyToMono(DebitCard.class)
	                .map(debitCard -> {
	                    System.out.println("Encontro debitCard > " + debitCard.getId());
	                    return Optional.of(debitCard);
	                })
                    .defaultIfEmpty(Optional.empty());
		}
		
	}

//	
//	private Mono<Purse> getMonederoDto(Purse monederoAccount) {
//        return Mono.just(objectMapper.convertValue(monederoAccount, PurseDTO.class));
//    }

	@Override
	public Mono<Purse> findByPhoneNumber(Integer id) {
		return purseRepository.findByPhoneNumber(id);
	}
	
}

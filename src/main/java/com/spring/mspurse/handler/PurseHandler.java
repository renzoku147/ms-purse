package com.spring.mspurse.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.spring.mspurse.entity.Card;
import com.spring.mspurse.entity.DebitCard;
import com.spring.mspurse.entity.Purse;
import com.spring.mspurse.service.PurseService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;
import static org.springframework.web.reactive.function.BodyInserters.fromProducer;

@Component
@Slf4j
public class PurseHandler {
	
	@Autowired
	PurseService purseService;
	
	public Mono<ServerResponse> findAll(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(purseService.findAll(), Purse.class);
    }
	
	public Mono<ServerResponse> findByPhoneNumber(ServerRequest request) {
		String id = request.pathVariable("id");
		System.out.println("Llamando a findByPhoneNumber >>> " + id);
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(purseService.findByPhoneNumber(Integer.valueOf(id)), Purse.class);
    }
	
	public Mono<ServerResponse> create(ServerRequest request) {
		System.out.println("Iniciando CREATE");
		Mono<Purse> monoPurse = request.bodyToMono(Purse.class);
		System.out.println("RECIBIO EL POST CREATE");

		return monoPurse.flatMap(purse -> {
									System.out.println("Creacion del Monedero");
									return purseService.verifyCardNumber(purse.getDebitCard() == null ? null : purse.getDebitCard().getCardNumber())
        								.filter(card -> purse.getDebitCard() == null || (purse.getDebitCard().getCardNumber() != null && card.isPresent()))
						        		.flatMap(card -> {
						        			System.out.println("Paso el filtro ok");
						        			if(purse.getDebitCard() == null) {
						        				System.out.println("Creacion sin DebiCard");
						        				return purseService.create(purse)
						        						.flatMap(purseCreated -> ServerResponse
					        													.status(HttpStatus.CREATED)
					        													.contentType(MediaType.APPLICATION_JSON)
					        													.body(fromValue(purseCreated)));
						        			}else {
						        				System.out.println("Creacion con DebiCard");
						        				Card crd = card.get();
						        				if(crd instanceof DebitCard) {
						        					log.info("Entro DebitCard Monedero");
						        					purse.setDebitCard((DebitCard)crd);
						        					return purseService.create(purse).flatMap(purseCreate2 -> ServerResponse
												        													.status(HttpStatus.CREATED)
												        													.contentType(MediaType.APPLICATION_JSON)
												        													.body(fromValue(purseCreate2)));
						        				}
						        				return Mono.empty();
						        			}
						        		});
								}
						)
						.switchIfEmpty(
								ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
								.bodyValue("Error al crear"));
    }

	
	public Mono<ServerResponse> update(ServerRequest request) {
		System.out.println("Iniciando UPDATE");
		Mono<Purse> monoPurse = request.bodyToMono(Purse.class);
		
		System.out.println("Convirtio");
		return monoPurse.flatMap( p -> {
										System.out.println("Mapeo Purse del ServerRequest >>> " + p.getId());
										return purseService.findById(p.getId())
											.flatMap(purse -> {
															System.out.println("Encontro Purse usando el findById " + purse.getNumberDoc());
															purse.setBalance(p.getBalance());
															return purseService.update(p)
															.flatMap(purseUptade -> {
																System.out.println("Actualizo Purse >>> " + purseUptade.getBalance());
																				return ServerResponse
																					.status(HttpStatus.ACCEPTED)
																					.contentType(MediaType.APPLICATION_JSON)
																					.bodyValue(purseUptade);
																				}
																	);
															}
													);
										}
								)
								.switchIfEmpty(
										ServerResponse.status(HttpStatus.BAD_REQUEST)
										.bodyValue("Error actualizar el monedero"));
    }

	
	public Mono<ServerResponse> delete(ServerRequest request) {
		String id = request.pathVariable("id");
		System.out.println("Esta llegando al DELETE");
        return purseService.delete(id)
        		.flatMap(purse -> ServerResponse.status(HttpStatus.OK)
								.bodyValue("Monedero Eliminado"));
    }
	
}

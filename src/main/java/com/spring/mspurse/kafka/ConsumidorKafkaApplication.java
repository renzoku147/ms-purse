package com.spring.mspurse.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.mspurse.entity.BootCoin;
import com.spring.mspurse.entity.BootCoinRequest;
import com.spring.mspurse.entity.BootCoinTransfer;
import com.spring.mspurse.entity.PurseTransaction;
import com.spring.mspurse.service.PurseService;

import reactor.core.publisher.Mono;

@Configuration
public class ConsumidorKafkaApplication {
	@Autowired
	PurseService purseService;  
	
	@Autowired
	ProductorKafka productorKafka;
	
	@Autowired
	private KafkaTemplate<String, Object> template;
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	@Bean
    public NewTopic topic(){
        return TopicBuilder.name("topico-everis")
                .partitions(10)
                .replicas(1)
                .build();
    }

    @KafkaListener(id="myId", topics = "topico-everis")
    public void listen(String message) throws Exception{
    	System.out.println(">>>>> @KafkaListener <<<<<");
    	PurseTransaction ptr = objectMapper.readValue(message, PurseTransaction.class);
    	System.out.println(">>>>> ORIGIN : ");
    	System.out.println(ptr);
    	purseService.findByPhoneNumber(ptr.getNumberOrigin())
				.flatMap(origin -> {
								System.out.println("Encontro el ORIGEN " + origin.getPhoneNumber());								
								if(origin.getDebitCard() != null) {
									Double d = ptr.getBalance()*-1;
									origin.getDebitCard().setAmountPurseTransaction(d);
									System.out.println("DEBITCARD ORIGEN " + origin.getDebitCard().getCardNumber()+" > " + origin.getDebitCard().getAmountPurseTransaction());
									template.send("topico-everis2", origin.getDebitCard());
								}else {
									origin.setBalance(origin.getBalance()-ptr.getBalance());
									System.out.println("ELSE ORIGEN BALANCE : " + origin.getBalance());
								}
								System.out.println("*********** PASS ORIGEN ************");
								
								return purseService.findByPhoneNumber(ptr.getNumberDetiny())
									.flatMap(destiny -> {
										System.out.println("Encontro el DESTINY " + destiny.getPhoneNumber());
										if(destiny.getDebitCard() != null) {
											destiny.getDebitCard().setAmountPurseTransaction(ptr.getBalance()); 
											System.out.println("DEBITCARD DESTINY " + destiny.getDebitCard().getCardNumber() +" > " + destiny.getDebitCard().getAmountPurseTransaction());
											template.send("topico-everis2", destiny.getDebitCard());
										}else {
											destiny.setBalance(destiny.getBalance()+ptr.getBalance());
											System.out.println("ELSE DESTINY BALANCE : " + destiny.getBalance());
										}
										System.out.println("*********** PASS DESTINY ************");
										return purseService.update(origin)
												.flatMap(originUpdate -> purseService.update(destiny));
									});
						}
				).subscribe();
        	
    }
    
    @KafkaListener(id="myId22", topics = "topico-everis6")
    public void listen2(String message) throws Exception{
    	System.out.println(">>>>> BootCoin @KafkaListener <<<<< - " + message);
    	
    	BootCoinRequest bcr = objectMapper.readValue(message, BootCoinRequest.class);
    	System.out.println("NumberPhone = " + bcr.getBootCoin().getPhoneNumber());
    	purseService.findByPhoneNumber(bcr.getBootCoin().getPhoneNumber())
    		.flatMap(yanki -> {
    			if(yanki.getDebitCard() == null) {
    				System.out.println("Buyer. no tiene debit card ");
    				yanki.setBalance(yanki.getBalance() - bcr.getAmount()*bcr.getExchangeRate());
    				System.out.println("Monto actualizado = " + yanki.getBalance());
    				purseService.update(yanki);
    			}else{
    				System.out.println("Buyer DeditCard = " + yanki.getDebitCard().getCardNumber());
    				yanki.getDebitCard().setAmountPurseTransaction(bcr.getAmount()*bcr.getExchangeRate()*-1);
    				System.out.println("DeditCard monto = " + yanki.getDebitCard().getAmountPurseTransaction());
    				template.send("topico-everis2", yanki.getDebitCard());
    			}
    			
    			return Mono.empty();
    		})
    		.subscribe();
    }
    
    @KafkaListener(id="myId31", topics = "topico-everis8")
    public void listen3(String message) throws Exception{
    	System.err.println(">>>>> PhoneNumber @KafkaListener <<<<< - " + message);
    	BootCoinTransfer bct = objectMapper.readValue(message, BootCoinTransfer.class);
    	System.out.println("PhoneNumber = " + bct.getSeller().getPhoneNumber());
    	
    	purseService.findByPhoneNumber(bct.getSeller().getPhoneNumber())
    	.flatMap(yanki -> {
			if(yanki.getDebitCard() == null) {
				System.out.println("Seller. no tiene debit card ");
				yanki.setBalance(yanki.getBalance() + bct.getBuyer().getAmount()*bct.getBuyer().getExchangeRate());
				System.out.println("Monto actualizado = " + yanki.getBalance());
				purseService.update(yanki);
			}else{
				System.out.println("Seller DeditCard = " + yanki.getDebitCard().getCardNumber());
				yanki.getDebitCard().setAmountPurseTransaction(bct.getBuyer().getAmount()*bct.getBuyer().getExchangeRate());
				System.out.println("DeditCard monto = " + yanki.getDebitCard().getAmountPurseTransaction());
				template.send("topico-everis2", yanki.getDebitCard());
			}
			
			return Mono.empty();
		})
		.subscribe();
			
    }
}

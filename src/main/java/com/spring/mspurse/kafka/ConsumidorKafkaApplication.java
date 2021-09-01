package com.spring.mspurse.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.mspurse.entity.PurseTransaction;
import com.spring.mspurse.service.PurseService;

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
}

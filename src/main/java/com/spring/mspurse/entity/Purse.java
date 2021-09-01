package com.spring.mspurse.entity;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Document("Purse")
@AllArgsConstructor
@NoArgsConstructor
public class Purse {
	@Id
	private String id;
	
	@NotNull
	private String numberDoc;
	
	@NotNull
	private Integer phoneNumber;
	
	@NotNull
	private Integer imei;
	
	@NotNull
	private String email;
	
	private DebitCard debitCard;
	
	@NotNull
	private Double balance;
}

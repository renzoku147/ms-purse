package com.spring.mspurse.entity;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurseTransaction {
	String id;
	Integer numberOrigin;
	Integer numberDetiny;
	Double balance;
}

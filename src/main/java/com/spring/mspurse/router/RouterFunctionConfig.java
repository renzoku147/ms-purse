package com.spring.mspurse.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.spring.mspurse.handler.PurseHandler;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterFunctionConfig {

	@Bean
    public RouterFunction<ServerResponse> routes (PurseHandler handler) {
        return route(GET("/list"), handler::findAll)
        		.andRoute(POST("/create"), handler::create)
        		.andRoute(PUT("/update"), handler::update)
        		.andRoute(DELETE("/delete/{id}"), handler::delete)
        		.andRoute(GET("/findByPhoneNumber/{id}"), handler::findByPhoneNumber);
    }
	
}

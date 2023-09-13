package com.rajesh.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan({"com.rajesh.common.entity", "com.rajesh.admin.user"})
public class ShopmeBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopmeBackendApplication.class, args);
	}

}

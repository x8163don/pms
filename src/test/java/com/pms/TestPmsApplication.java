package com.pms;

import org.springframework.boot.SpringApplication;

public class TestPmsApplication {

	public static void main(String[] args) {
		SpringApplication.from(PmsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

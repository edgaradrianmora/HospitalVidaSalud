package com.pacientes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class PacientesApplication {

    public static void main(String[] args) {
        SpringApplication.run(PacientesApplication.class, args);
        System.out.println("MSPacientes - Hospital Vida Salud");
    }
}
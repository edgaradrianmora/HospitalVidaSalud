package com.atenciones;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class AtencionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtencionesApplication.class, args);
        System.out.println("MS Atenciones - Hospital Vida Salud ACTIVO");
    }
}
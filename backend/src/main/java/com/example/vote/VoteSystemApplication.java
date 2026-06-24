package com.example.vote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 線上投票系統 - 後端啟動類別
 */
@SpringBootApplication
@EnableTransactionManagement
public class VoteSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(VoteSystemApplication.class, args);
    }
}

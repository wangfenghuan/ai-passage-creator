package com.wfh.aipassagecreator;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author fenghuanwang
 */
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.wfh.aipassagecreator.mapper")
public class AiPassageCreatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiPassageCreatorApplication.class, args);
    }

}

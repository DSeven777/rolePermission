package com.dseven.rolepermission.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.dseven.rolepermission.gateway", "com.dseven.rolepermission.common"},
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.dseven\\.rolepermission\\.common\\.config\\.MybatisPlusConfig")
)
public class GatewayServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}

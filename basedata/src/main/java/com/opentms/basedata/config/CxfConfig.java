package com.opentms.basedata.config;

import com.opentms.basedata.controller.*;
import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CxfConfig {

    @Autowired
    private Bus bus;

    @Bean
    public Server jaxrsServer() {
        JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
        factory.setBus(bus);
        factory.setAddress("/v1");
        factory.setResourceClasses(
            CurrencyResource.class,
            BankResource.class,
            CountryResource.class,
            HolidayResource.class,
            TraderResource.class,
            BusinessUnitResource.class,
            CounterpartyResource.class,
            CounterpartyAccountResource.class
        );
        factory.setStart(true);
        return factory.create();
    }
}
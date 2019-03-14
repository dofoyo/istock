package com.rhb.istock;

import org.springframework.context.annotation.Configuration;  
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter; 
	
@SuppressWarnings("deprecation")
@Configuration  
public class CorsConfig extends WebMvcConfigurerAdapter {  

    @Override  
    public void addCorsMappings(CorsRegistry registry) {  
    	System.out.println("*********   CorsRegistry      ***************");
    	
        registry.addMapping("/**")  
                .allowedOrigins("*")  
                .allowCredentials(true)  
                .allowedMethods("GET", "POST", "DELETE", "PUT")  
                .maxAge(3600);  
    }

} 
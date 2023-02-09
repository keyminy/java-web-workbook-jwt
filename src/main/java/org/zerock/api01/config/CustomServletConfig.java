package org.zerock.api01.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class CustomServletConfig implements WebMvcConfigurer{
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// /files/로 시작하는 경로를, 스프링 MVC에서 일반 파일 경로로 처리하도록 지정
		registry
			.addResourceHandler("/files/**")
			.addResourceLocations("classpath:/static/");
	}
}

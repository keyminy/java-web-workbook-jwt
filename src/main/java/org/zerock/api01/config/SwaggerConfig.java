package org.zerock.api01.config;

import java.util.List;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfig {

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.OAS_30)
				.useDefaultResponseMessages(false)
				.select()
				//@RestController 어노테이션이 있는 컨트롤러에 대해서 API문서 작성
				.apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
				.paths(PathSelectors.any())
				.build()
				.securitySchemes(List.of(apiKey())) //추가된 부분
				.securityContexts(List.of(securityContext())) //추가된 부분
				.apiInfo(apiInfo());
	}
	
	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title("Boot API01 Project Swagger")
				.build();
	}
	
	private ApiKey apiKey() {
		//String name, String keyname, String passAs)
		return new ApiKey("Authorization","Bearer Token","header");
	}
	
	private SecurityContext securityContext() {
		return SecurityContext.builder().securityReferences(defaultAuth())
					.operationSelector(selector -> selector.requestMappingPattern()
												.startsWith("/api/")).build();
	}
	private List<SecurityReference> defaultAuth(){
		AuthorizationScope authorizationScope = new AuthorizationScope("global","global access");
		return List.of(new SecurityReference("Authorization"
							,new AuthorizationScope[] {authorizationScope}));
	}
}
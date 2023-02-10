package org.zerock.api01.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.zerock.api01.security.APIUserDetailsService;
import org.zerock.api01.security.filter.APILoginFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@EnableWebSecurity
@Log4j2
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class CustomSecurityConfig {
	//주입
	private final APIUserDetailsService apiUserDetailsService;
	
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        log.info("------------web configure-------------------");
        
        return (web) -> web.ignoring()
                .requestMatchers(
                        PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        log.info("------------configure-------------------");
        /* AuthenticationManager설정 */
        AuthenticationManagerBuilder authenticationManagerBuilder =
        		http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
        	//여기서 DI한거 넣어주네
        	.userDetailsService(apiUserDetailsService)
        	.passwordEncoder(passwordEncoder());
        
        //GET AuthenticationManager
        AuthenticationManager authenticationManager =
        		authenticationManagerBuilder.build();
        //반드시 필요,AuthenticationManager 객체 설정
        http.authenticationManager(authenticationManager);
        
        /* APILoginFilter */
        APILoginFilter apiLoginFilter = new APILoginFilter("/generateToken");
        //반드시 필요,APILoginFilter에 AuthenticationManager 객체 설정
        apiLoginFilter.setAuthenticationManager(authenticationManager);
        
        // APILoginFilter의 위치 조정, APILoginFilter앞에다가 UsernamePasswordAuthenticationFilter두기
        //UsernamePasswordAuthenticationFilter는 id랑 pw검사하는건가??
        http.addFilterBefore(apiLoginFilter, UsernamePasswordAuthenticationFilter.class);
        
        http.csrf().disable(); //1.csrf토큰 비활성화
        http.sessionManagement()
        	.sessionCreationPolicy(SessionCreationPolicy.STATELESS); //2.세션을 사용하지 않음
        return http.build();
    }
    
}

package org.zerock.api01.security.filter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import com.google.gson.Gson;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class APILoginFilter extends AbstractAuthenticationProcessingFilter{

	public APILoginFilter(String defaultFilterProcessesUrl) {
		super(defaultFilterProcessesUrl);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException {
		log.info("APILoginFilter=====================");
		if(request.getMethod().equalsIgnoreCase("GET")) {
			log.info("GET METHOD NOT SUPPORT");
			return null;
		}
		Map<String,String> jsonData = parseRequestJSON(request);
		log.info("jsonData : " + jsonData);
		UsernamePasswordAuthenticationToken authenticationToken
			= new UsernamePasswordAuthenticationToken(
					jsonData.get("mid"),
					jsonData.get("mpw")
					);
		//token Type으로 리턴하는구나
		return getAuthenticationManager().authenticate(authenticationToken);
	}
	
	private Map<String,String> parseRequestJSON(HttpServletRequest request){
		//JSON 데이터를 분석해서 mid, mpw 전달 값을 Map으로 처리
		//Reader클래스 : for reading character streams.
		//request.getInputStream() : Retrieves the body of the request as binary data 
		try(Reader reader = new InputStreamReader(request.getInputStream())) {
			Gson gson = new Gson();
			//클라이언트로부터 가져온 JSON데이터(mid,mpw)를 Map으로 변환
			return gson.fromJson(reader, Map.class);
		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.getMessage());
		}
		return null;
	}

}

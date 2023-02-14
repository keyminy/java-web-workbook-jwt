package org.zerock.api01.security.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;
import org.zerock.api01.security.exception.AccessTokenException;
import org.zerock.api01.util.JWTUtil;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
public class TokenCheckFilter extends OncePerRequestFilter {

	private final JWTUtil jwtUtil;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request
			, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String path = request.getRequestURI();
		System.out.println("path : " + path);
		if(!path.startsWith("/api/")) {
			//doFilter(req,resp)는 다음 필터나 목적지(서블릿,jsp)로 갈 수 있도록
			filterChain.doFilter(request, response);
			return;
		}
		log.info("Token Check Filter..............");
		log.info("JWTUtil : " + jwtUtil);
		
		try {
			validateAccessToken(request);
			filterChain.doFilter(request, response);		
		} catch (AccessTokenException accessTokenException) {
			//문제 발생 시, 브라우저에서 에러 메시지를 상태 코드와 함께 전송하도록 처리
			accessTokenException.sendResponseError(response);
		}
	}
	
	private Map<String,Object> validateAccessToken(HttpServletRequest request)
		throws AccessTokenException {
		//Access Token의 값은 HTTP Header중에 ‘Authorization’을 이용해서 전달된다.
		String headerStr = request.getHeader("Authorization");
		System.out.println("headerStr : " + headerStr);
		
		if(headerStr == null || headerStr.length() < 8) {
			throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.UNACCEPT);
		}
		
		//Bearer 생략
		String tokenType = headerStr.substring(0,6);
		String tokenStr = headerStr.substring(7);
		
		if(tokenType.equalsIgnoreCase("Bearer") == false) {
			throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.BADTYPE);
		}
		
		try {
			Map<String,Object> values = jwtUtil.validateToken(tokenStr);
			System.out.println("values : " + values);
			//values : {exp=1676362414, mid=apiuser10, iat=1676276014}
			return values;
		}
		catch (MalformedJwtException malformedJwtException) {
			log.error("MalformedJwtException----------------------");
			throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.MALFORM);
		}
		catch (SignatureException signatureException) {
			log.error("SignatureException----------------------");
			throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.BADSIGN);
		}
		catch (ExpiredJwtException expiredJwtException) {
			log.error("ExpiredJwtException------------------------");
			throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.EXPIRED);
		}
	}
}

package org.zerock.api01.security.filter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.zerock.api01.security.exception.RefreshTokenException;
import org.zerock.api01.util.JWTUtil;

import com.google.gson.Gson;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
public class RefreshTokenFilter extends OncePerRequestFilter{

	private final String refreshPath;
	
	private final JWTUtil jwtUtil;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request
			, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String path = request.getRequestURI();
		
		if(!path.equals(refreshPath)) {
			log.info("skip refresh token filter.....");
			filterChain.doFilter(request, response);
			return;
		}
		log.info("Refresh Token Filter...run........1");
		
		//1.화면에서 넘어온 JSON으로부터, accessToken과 refreshToken획득
		Map<String,String> tokens = parseRequestJSON(request);
		
		String accessToken = tokens.get("accessToken");
		String refreshToken = tokens.get("refreshToken");
		
		log.info("accessToken : " + accessToken);
		log.info("refreshToken : " + refreshToken);
		
		/* 2.accessToken 검증 */
		try {
			checkAccessToken(accessToken);//accessToken검증
		} catch (RefreshTokenException refreshTokenException) {
			refreshTokenException.sendResponseError(response);
			return;
		}
		/*3.Refresh Token검사*/
		Map<String,Object> refreshClaims = null;
		
		try {
			refreshClaims = checkRefreshToken(refreshToken);
			log.info("refreshClaims : " + refreshClaims);
		} catch (RefreshTokenException refreshTokenException) {
			refreshTokenException.sendResponseError(response);
			return;
		}
		
		/* 4.새로운 Access Token발행 */
		//Refresh Token의 유효 시간이 얼마 남지 않은 경우
		Integer exp = (Integer)refreshClaims.get("exp");
		Date expTime = new Date(Instant.ofEpochMilli(exp).toEpochMilli()*1000);
		Date current = new Date(System.currentTimeMillis());
		
		//만료 시간과 현재 시간의 간격 계산
		//만일 3일 미만인 경우에는 Refresh Token도 다시 생성되도록
		long gapTime = (expTime.getTime() - current.getTime());
		
		log.info("----------------------------------");
		log.info("current : " + current);
		log.info("expTime : " + expTime);
		log.info("gap : " + gapTime);
		
		String mid = (String)refreshClaims.get("mid");
		
		//이 상태까지 오면 무조건 AccessToken은 새로 생성됩니다.
		String accessTokenValue = jwtUtil.generateToken(Map.of("mid",mid),1);
		String refreshTokenValue = tokens.get("refreshToken");
		
		//RefreshToken이 3일도 안남았다면?? 이것도 다시 재발급
		if(gapTime < (1000 * 60 * 60 * 24 * 3)) {
			log.info("new Refresh Token required....");
			refreshTokenValue = jwtUtil.generateToken(Map.of("mid",mid),30);//생성
		}
		log.info("Refresh Token result..........");
		log.info("accessToken : " + accessTokenValue);
		log.info("refreshToken : " + refreshTokenValue);
		
		/* 최종적으로 만들어진 토큰들을 화면에 전송 */
		sendTokens(accessTokenValue, refreshTokenValue, response);
	}

	/*1.화면에서 전송된 JSON 데이터에서 accessToken과 refreshToken을 추출*/
	private Map<String,String> parseRequestJSON(HttpServletRequest request){
		//JSON 데이터를 분석해서 accessToken, refreshToken 전달 값을 Map으로 처리
		//Reader클래스 : for reading character streams.
		//request.getInputStream() : Retrieves the body of the request as binary data 
		try(Reader reader = new InputStreamReader(request.getInputStream())) {
			Gson gson = new Gson();
			//클라이언트로부터 가져온 JSON데이터(accessToken,refreshToken)를 Map으로 변환
			return gson.fromJson(reader, Map.class);
		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.getMessage());
		}
		return null;
	}
	/* 2.accessToken 검증 */
	public void checkAccessToken(String accessToken) throws RefreshTokenException{
		try {
			jwtUtil.validateToken(accessToken);
		} catch (ExpiredJwtException expiredJwtException) {
			//AccessToken만료 - 당연한 상황이므로 로그만 출력
			log.info("Access Token has expired");
		} catch (Exception e) {
			throw new RefreshTokenException(RefreshTokenException.ErrorCase.NO_ACCESS);
		}
	}
	
	/*3.Refresh Token검사*/
	private Map<String,Object> checkRefreshToken(String refreshToken)
		throws RefreshTokenException {
		try {
			 Map<String,Object> values = jwtUtil.validateToken(refreshToken);
			 return values;
		} catch (ExpiredJwtException expiredJwtException) {
			//RefreshToken만료
			throw new RefreshTokenException(RefreshTokenException.ErrorCase.OLD_REFRESH);
		} catch (MalformedJwtException malformedJwtException) {
			log.info("MalformedJwtException----------------------");
			throw new RefreshTokenException(RefreshTokenException.ErrorCase.NO_REFRESH);
		} catch (Exception exception) {
			throw new RefreshTokenException(RefreshTokenException.ErrorCase.NO_REFRESH);
		}
	}
	
	public void sendTokens(String accessTokenValue,String refreshTokenValue
			,HttpServletResponse response) {
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		Gson gson = new Gson();
		String jsonStr = gson.toJson(Map.of("accessToken",accessTokenValue
				,"refreshToken",refreshTokenValue));
		try {
			response.getWriter().println(jsonStr);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

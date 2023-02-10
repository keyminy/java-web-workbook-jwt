package org.zerock.api01.util;

import java.time.ZonedDateTime;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class JWTUtil {
	
	@Value("${org.zerock.jwt.secret}")
	private String key;
	
	/* 실제 JWT 생성 */
	public String generateToken(Map<String,Object> valueMap,int days) {
		log.info("generateKey..." + key);
		
		//header부분
		Map<String,Object> headers = new HashMap<>();
		headers.put("typ", "JWT");
		headers.put("alg", "HS256");
		
		//payload 부분 설정
		Map<String,Object> payloads = new HashMap<>();
		payloads.putAll(valueMap);
		
		//테스트 시에는 짧은 유효 기간
		//int time = (1) * days; //테스트는 분단위로 나중에 60*24(일)단위 변경
		int time = (60 * 24) * days; //60*24(일)단위
		
		String jwtStr = Jwts.builder()
				.setHeader(headers)
				//JWT 페이로드를 JSON 클레임 인스턴스로 설정합니다
				.setClaims(payloads)
				.setIssuedAt(Date.from(ZonedDateTime.now().toInstant()))
				//plusMinutes부분 : 유효기간을 분 단위로 처리(테스트할때는 짧게 유효기간)
				//실제 개발 완료되면 plusDays()로 변경하자
				.setExpiration(Date.from(ZonedDateTime.now().plusMinutes(time).toInstant()))
				.signWith(SignatureAlgorithm.HS256, key.getBytes())
				.compact();
		
		return jwtStr;
	
	}
	
	public Map<String,Object> validateToken(String token) throws JwtException{
		Map<String,Object> claim = null;
		claim = Jwts.parser()
					.setSigningKey(key.getBytes()) //Set key
					.parseClaimsJws(token) //파싱 및 검증,실패 시 에러(throws 여러 개 선언되있음)
					.getBody();
		
		return claim;
	}
	
}

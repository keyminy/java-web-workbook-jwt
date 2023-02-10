package org.zerock.api01.util;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.log4j.Log4j2;

@SpringBootTest
@Log4j2
public class JWTUtilTests {
	
	@Autowired
	private JWTUtil jwtUtil;
	
	@Test
	public void testGenerate() {
		Map<String,Object> claimMap = Map.of("mid","ABCDE");
		String jwtStr = jwtUtil.generateToken(claimMap, 1);//1day생성
		log.info(jwtStr);
	}
	
	@Test
	public void testValidate() {
		//유효 시간이 지난 토큰
		String jwtStr = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9"
				+ ".eyJleHAiOjE2NzYwMDc4OTQsIm1pZCI6IkFCQ0RFIiwiaWF0IjoxNjc2MDA3ODM0fQ"
				+ ".pTEExsnd_EnsSwvfk68jURHQwBpqTWzQuqUEDR92UeU";
		Map<String,Object> claim = jwtUtil.validateToken(jwtStr);
		log.info(claim);
	}
	
	//JWT 문자열을 생성해서 이를 검증하는 작업을 같이 수행하는 테스트 메소드
	@Test
	public void testAll() {
		String jwtStr = jwtUtil.generateToken(Map.of("mid","AAAA","email","aaaa@bbb.com"), 1);
		log.info(jwtStr);
		Map<String,Object> claim = jwtUtil.validateToken(jwtStr);
		log.info("MID : " + claim.get("mid"));
		log.info("EMAIL : " + claim.get("email"));
		
	}
	
}

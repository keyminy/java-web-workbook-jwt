package org.zerock.api01.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zerock.api01.domain.QAPIUser;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/sample")
public class SampleController {

	@ApiOperation("Sample GET doA")
	@PreAuthorize("hasRole('ROLE_USER')")
	@GetMapping("/doA")
	public List<String> doA(){
		return Arrays.asList("AAA","BBB","CCC");
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@GetMapping("/doB")
	public List<String> doB(){
		return Arrays.asList("AdminAAA","AdminBBB","AdminCCC");
	}
	
}

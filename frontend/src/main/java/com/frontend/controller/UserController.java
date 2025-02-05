package com.frontend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.frontend.config.message.ApiResponse;
import com.frontend.config.security.CustomUserDetails;
import com.frontend.config.security.SecurityUtils;
import com.frontend.req.user.UserReq;
import com.frontend.res.user.UserRes;
import com.frontend.service.UserService;
import com.frontend.utils.ResponseUtils;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;

	@GetMapping("/getUserInfo")
	public ResponseEntity<?> getUserInfo() {
		CustomUserDetails userDetails = SecurityUtils.getCurrentUserPrinciple();
		if (userDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		var user = userService.getUserById(userDetails.getId());
		if (user == null) {
			return ResponseEntity.ok(ResponseUtils.error(999, "找不到使用者", user));
		}
		return ResponseEntity.ok(ResponseUtils.success(200, null, user));
	}

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<UserRes>> registerUser(@RequestBody UserReq userReq) throws Exception {
		UserRes userRes = userService.registerUser(userReq);
		ApiResponse<UserRes> response = ResponseUtils.success(201, null, userRes);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/*
	 * 只能拿到自己的 不能拿別人的 使用token去帶userId 從CustomUserDetails拿到要的資訊
	 */
	@PutMapping("/updateUser")
	public ResponseEntity<?> updateUser(@RequestBody UserReq req) {
		CustomUserDetails userDetails = SecurityUtils.getCurrentUserPrinciple();
		if (userDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		var user = userService.getUserById(userDetails.getId());
		if (user == null) {
			return ResponseEntity.ok(ResponseUtils.error(999, "找不到使用者", user));
		}
		var userId = user.getId();
		try {
			var res = userService.updateUser(req, userId);
			return ResponseEntity.ok(ResponseUtils.success(200, "更新成功", res));
		} catch (Exception e) {
			return ResponseEntity.ok(ResponseUtils.error(999, "更新失敗", false));
		}
	}

//	@PutMapping("/updateUserInvoice")
//	public ResponseEntity<?> updateUserInvoice(@RequestBody UserReq req) {
//		CustomUserDetails userDetails = SecurityUtils.getCurrentUserPrinciple();
//		if (userDetails == null) {
//			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//		}
//		var user = userService.getUserById(userDetails.getId());
//		if (user == null) {
//			return ResponseEntity.ok(ResponseUtils.error(999, "找不到使用者", user));
//		}
//		var userId = user.getId();
//		try {
//			var res = userService.updateUserInvoice(req, userId);
//			return ResponseEntity.ok(ResponseUtils.success(200, "更新成功", res));
//		} catch (Exception e) {
//			return ResponseEntity.ok(ResponseUtils.error(999, "更新失敗", false));
//		}
//	}


	@PutMapping("/resetPwd")
	public ResponseEntity<?> resetPwd(@RequestBody UserReq req) {
		CustomUserDetails userDetails = SecurityUtils.getCurrentUserPrinciple();
		if (userDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		var user = userService.getUserById(userDetails.getId());
		if (user == null) {
			return ResponseEntity.ok(ResponseUtils.error(999, "找不到使用者", user));
		}
		var userId = user.getId();
		try {
			var res = userService.updateUser(req, userId);
			return ResponseEntity.ok(ResponseUtils.success(200, "更新成功", res));
		} catch (Exception e) {
			return ResponseEntity.ok(ResponseUtils.error(999, "更新失敗", false));
		}
	}
}

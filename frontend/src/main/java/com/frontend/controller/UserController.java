package com.frontend.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.frontend.config.message.ApiResponse;
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
		var userDetails = SecurityUtils.getSecurityUser();
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
		var userDetails = SecurityUtils.getSecurityUser();
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
		var userDetails = SecurityUtils.getSecurityUser();
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

//	@PostMapping("/{userId}/upload-profile-image")
//	public ResponseEntity<?> uploadProfileImage(@PathVariable Long userId, @RequestParam("file") MultipartFile file) {
//		try {
//			if (file == null || file.isEmpty()) {
//				return ResponseEntity.badRequest().body(ResponseUtils.error(400, "文件不能為空", null));
//			}
//
//			String uploadedFilePath = ImageUtil.upload(file);
//
//			userService.uploadProductImg(userId, uploadedFilePath);
//
//			ApiResponse<String> response = ResponseUtils.success(200, "文件上傳成功", uploadedFilePath);
//			return ResponseEntity.ok(response);
//		} catch (Exception e) {
//			ApiResponse<String> response = ResponseUtils.error(500, "文件上傳失敗", null);
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//		}
//	}

	@PostMapping("/{userId}/upload-profile-image")
	public ResponseEntity<?> uploadProfileImage(@PathVariable String userId, @RequestParam("file") MultipartFile file) {

		if (file.isEmpty()) {
			return ResponseEntity.badRequest().body(ResponseUtils.error(400, "請選擇要上傳的圖片", null));
		}

		// 指定存儲圖片的本地文件夾
		String uploadDir = "uploads/profile_pictures/";
		File uploadFolder = new File(uploadDir);
		if (!uploadFolder.exists()) {
			uploadFolder.mkdirs(); // 確保目錄存在
		}

		try {
			// 創建文件名：profile_{userId}.jpg
			String filename = "profile_" + userId + "_" + System.currentTimeMillis() + ".jpg";
			Path filePath = Paths.get(uploadDir, filename);

			// 保存文件到本地文件夾
			Files.write(filePath, file.getBytes());

			// 返回圖片的相對路徑
			String imageUrl = "/static/" + filename;
			return ResponseEntity.ok(ResponseUtils.success(200, "圖片上傳成功", imageUrl));

		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ResponseUtils.error(500, "圖片上傳失敗", e.getMessage()));
		}
	}

}

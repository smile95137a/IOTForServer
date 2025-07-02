package com.frontend.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frontend.entity.store.Store;
import com.frontend.entity.user.FaceRecognitionMember;
import com.frontend.repo.StoreRepository;
import com.frontend.service.FaceRecognitionMemberService;
import com.frontend.utils.DoorControlUtil;
import com.frontend.utils.FaceUploadUtil;
import com.frontend.utils.ImageUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

	@Autowired
	private FaceRecognitionMemberService faceRecognitionMemberService;

	@Autowired
	private StoreRepository storeRepository;

	// 建議實務上改為從設定檔讀取
	private static final String DEVICE_IP = "192.168.1.113";
	private static final int PORT = 80;
	private static final String USERNAME = "admin";
	private static final String PASSWORD = "Handsome0202@";
	@GetMapping("/getUserInfo")
	public ResponseEntity<?> getUserInfo() {
		try {
			var userDetails = SecurityUtils.getSecurityUser();
			var user = userService.getUserById(userDetails.getId());
			if (user == null) {
				return ResponseEntity.ok(ResponseUtils.error(999, "找不到使用者", user));
			}
			return ResponseEntity.ok(ResponseUtils.success(200, null, user));
		}catch (Exception e){
			ApiResponse<String> error = ResponseUtils.error(9999 ,"帳號或密碼錯誤" , null);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
		}


	}

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<UserRes>> registerUser(@RequestBody UserReq userReq) throws Exception {
		try {
			UserRes userRes = userService.registerUser(userReq);
			ApiResponse<UserRes> response = ResponseUtils.success(201, null, userRes);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (Exception e) {
			return ResponseEntity.ok(ResponseUtils.error(999, e.getMessage(), null));
		}
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

	@PostMapping("/{userId}/upload-profile-image")
	public ResponseEntity<?> uploadProfileImage(@PathVariable Long userId, @RequestParam("file") MultipartFile file) {
		try {
			if (file == null || file.isEmpty()) {
				return ResponseEntity.badRequest().body(ResponseUtils.error(400, "文件不能為空", null));
			}

			String uploadedFilePath = ImageUtil.upload(file);

			userService.uploadProductImg(userId, uploadedFilePath);

			ApiResponse<String> response = ResponseUtils.success(200, "文件上傳成功", uploadedFilePath);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			e.printStackTrace();
			ApiResponse<String> response = ResponseUtils.error(500, "文件上傳失敗", null);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

//	@PostMapping("/{userId}/upload-profile-image")
//	public ResponseEntity<?> uploadProfileImage(@PathVariable String userId, @RequestParam("file") MultipartFile file) {
//
//		if (file.isEmpty()) {
//			return ResponseEntity.badRequest().body(ResponseUtils.error(400, "請選擇要上傳的圖片", null));
//		}
//
//		// 指定存儲圖片的本地文件夾
//		String uploadDir = "uploads/profile_pictures/";
//		File uploadFolder = new File(uploadDir);
//		if (!uploadFolder.exists()) {
//			uploadFolder.mkdirs(); // 確保目錄存在
//		}
//
//		try {
//			// 創建文件名：profile_{userId}.jpg
//			String filename = "profile_" + userId + "_" + System.currentTimeMillis() + ".jpg";
//			Path filePath = Paths.get(uploadDir, filename);
//
//			// 保存文件到本地文件夾
//			Files.write(filePath, file.getBytes());
//
//			// 返回圖片的相對路徑
//			String imageUrl = "/static/" + filename;
//			return ResponseEntity.ok(ResponseUtils.success(200, "圖片上傳成功", imageUrl));
//
//		} catch (IOException e) {
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//					.body(ResponseUtils.error(500, "圖片上傳失敗", e.getMessage()));
//		}
//	}


	@PostMapping("/create-face-recognition/{userId}")
	public ResponseEntity<?> createFaceRecognitionMember(
			@PathVariable Long userId) {

		try {
			// 獲取當前操作者ID（從session或JWT中獲取）
			Long currentUserId = SecurityUtils.getSecurityUser().getId();

			FaceRecognitionMember member = faceRecognitionMemberService
					.createFaceRecognitionMember(userId, currentUserId);

			return ResponseEntity.ok(ResponseUtils.success(200, "人臉辨識會員創建成功", null));
		} catch (Exception e) {
			return ResponseEntity.ok(ResponseUtils.success(200, "人臉辨識會員創建成功", null));
		}
	}

	@GetMapping("/search-face-recognition/{userId}")
	public ResponseEntity<FaceRecognitionMember> searchFaceRecognitionMember(
			@PathVariable Long userId) {

		Optional<FaceRecognitionMember> member = faceRecognitionMemberService
				.findByUserId(userId);

		return member.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * 上傳人臉圖片並傳送至設備
	 *
	 * @param file MultipartFile 圖片檔案
	 * @return 回傳成功或失敗訊息
	 */
	@PostMapping("/upload")
	public ResponseEntity<?> uploadFace(
			@RequestParam("file") MultipartFile file
	) throws IOException, ParseException {
			if (file.isEmpty()) {
				return ResponseEntity.badRequest().body("❌ 圖片檔案不可為空");
			}

			// 儲存到暫存檔案（轉成 File）
			File tempFile = File.createTempFile("upload_", ".jpg");
			file.transferTo(tempFile);

			FaceRecognitionMember faceRecognitionMember = faceRecognitionMemberService.findByUserId(SecurityUtils.getSecurityUser().getId()).get();
			List<Store> all = storeRepository.findAll();
			boolean success = false;
			// 使用工具類上傳人臉
			for(Store store : all){
				success = FaceUploadUtil.uploadFace(store.getStoreIP(), PORT, USERNAME, PASSWORD, tempFile, faceRecognitionMember.getEmployeeNo());
			}


			// 清理暫存檔案
			if (tempFile.exists()) {
				tempFile.delete();
			}
			return ResponseEntity.ok(ResponseUtils.success(200, "人臉上傳成功", null));
	}

	/**
	 * 掃碼開門：需要先登入（token 認證）
	 */
	@PutMapping("/openDoor")
	public ResponseEntity<?> openDoor(@RequestBody String storeUid , Authentication authentication) throws JsonProcessingException {

		// 沒有登入或 token 無效，這邊不會進來，Spring Security 會自動回 401
		if (authentication == null || !authentication.isAuthenticated()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("請先登入後再操作");
		}
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = objectMapper.readTree(storeUid);
		String storeUidCode = jsonNode.get("storeUid").asText();
		Store store = storeRepository.findByUid(storeUidCode).get();
		// 執行開門
		boolean result = DoorControlUtil.openDoor(store.getStoreIP(),"65535");
			return ResponseEntity.ok(ResponseUtils.success(200 , "開門成功" , null));
	}
}

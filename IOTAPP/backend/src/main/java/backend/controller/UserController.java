package backend.controller;

import java.util.stream.Collectors;

import backend.req.user.UserReq;
import backend.res.user.UserRes;
import backend.service.UserService;
import backend.utils.ResponseUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping
	public ResponseEntity<?> getUsers() {
		var list = userService.getAllUsers();
		var result = list.stream().map(x -> {
			UserRes o = UserRes.builder().build();
			BeanUtils.copyProperties(x, o);
			return o;
		}).collect(Collectors.toList());

		var res = ResponseUtils.success(0000, null, result);
		return ResponseEntity.ok(res);
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getUserById(@PathVariable String id) {
		try {
			var result = userService.getUserByuid(id);
			var res = ResponseUtils.success(0000, null, result);
			return ResponseEntity.ok(res);
		} catch (Exception e) {
			log.error("UserController getUserById : {}", e.getMessage());
			var res = ResponseUtils.error(9999, "系統錯誤", e.getMessage());
			return ResponseEntity.badRequest().body(res);
		}

	}

	@PostMapping("/queryUser")
	public ResponseEntity<?> queryUser(@RequestBody UserReq req) {
		try {
			var result = userService.queryUser(req);
			var res = ResponseUtils.success(result);
			return ResponseEntity.ok(res);
		} catch (Exception e) {
			var res = ResponseUtils.error(9999, "系統錯誤", e.getMessage());
			return ResponseEntity.badRequest().body(res);
		}
	}

	@PostMapping("/createUser")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> createUser(@RequestBody UserReq req) {
		try {
			var result = userService.createUser(req);
			var res = ResponseUtils.success(result);
			return ResponseEntity.ok(res);
		} catch (Exception e) {
			log.error("UserController registerUser : {}", e.getMessage());
			var res = ResponseUtils.error(9999, "系統錯誤", e.getMessage());
			return ResponseEntity.badRequest().body(res);
		}
	}

	@PutMapping
	public ResponseEntity<?> updateUser(@RequestBody UserReq req) {
		try {
			var result = userService.updateUser(req);
			var res = ResponseUtils.success(0000, null, result);
			return ResponseEntity.ok(res);
		} catch (Exception e) {
			log.error("UserController updateUpdate : {}", e.getMessage());
			var res = ResponseUtils.error(9999, "系統錯誤", e.getMessage());
			return ResponseEntity.badRequest().body(res);
		}
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> deleteUser(@PathVariable String id) {
		try {
			userService.deleteUser(id);
			var res = ResponseUtils.success(0000, null, true);
			return ResponseEntity.ok(res);
		} catch (Exception e) {
			log.error("UserController deleteUser : {}", e.getMessage());
			var res = ResponseUtils.error(9999, "系統錯誤", e.getMessage());
			return ResponseEntity.badRequest().body(res);
		}
	}

}
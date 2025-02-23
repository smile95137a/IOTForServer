package com.frontend.controller.admin;

import java.util.List;
import java.util.stream.Collectors;

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

import com.frontend.config.message.ApiResponse;
import com.frontend.req.user.UserReq;
import com.frontend.res.user.UserRes;
import com.frontend.service.UserService;
import com.frontend.utils.ResponseUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/b/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService userService;

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
            var result = userService.getByUid(id);
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
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
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


    /**
     * 加入黑名單
     */
    @PutMapping("/addBlackList")
    public ResponseEntity<?> updateBlackList(@RequestBody List<Long> userId){
        try {
            userService.updateBlack(userId);
            ApiResponse<String> success = ResponseUtils.success("加入成功");
            return ResponseEntity.ok(success);
        }catch (Exception e){
            ApiResponse<String> error = ResponseUtils.error(e.getMessage());
            return ResponseEntity.ok(error);
        }
    }

    /**
     * 加入黑名單
     */
    @PutMapping("/removeBlackList")
    public ResponseEntity<?> removeBlackList(@RequestBody List<Long> userId){
        try {
            userService.removeBlackList(userId);
            ApiResponse<String> success = ResponseUtils.success("移除成功");
            return ResponseEntity.ok(success);
        }catch (Exception e){
            ApiResponse<String> error = ResponseUtils.error(e.getMessage());
            return ResponseEntity.ok(error);
        }
    }
}
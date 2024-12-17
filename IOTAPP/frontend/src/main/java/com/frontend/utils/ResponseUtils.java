package com.frontend.utils;

import com.mo.app.config.message.ApiResponse;

public class ResponseUtils {

	public static <T> ApiResponse<T> success(int code, String message, T data) {
		return ApiResponse.<T>builder().code(code).message(message).success(true).data(data).build();
	}
	
    public static <T> ApiResponse<T> success(int code, T data) {
        return success(code, "系統成功", data);
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(200, data);
    }

	public static <T> ApiResponse<T> error(int code, String message, T data) {
		return ApiResponse.<T>builder().code(code).message(message).success(false).data(data).build();
	}
	
    public static <T> ApiResponse<T> error(int code, T data) {
        return error(code, "系統錯誤", data);
    }

    public static <T> ApiResponse<T> error(T data) {
        return error(500, data); 
    }

}
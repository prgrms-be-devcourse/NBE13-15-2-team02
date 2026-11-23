package com.team02.catservice.global.response;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final int status;
    private final String message;
    private final T data;

    private ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    /** 200 (조회·수정·삭제) */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    /** 데이터 없이 메시지만 (삭제 등) */
    public static ApiResponse<Void> ok(String message) {
        return new ApiResponse<>(200, message, null);
    }

    /** 201 (생성). 컨트롤러 메서드에 @ResponseStatus(HttpStatus.CREATED)를 함께 붙인다. */
    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(201, message, data);
    }
}

package com.pig4cloud.pig.config;

import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.edu.fund.exception.BizException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器（脚手架内置）
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public R<?> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
				.map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
				.reduce((a, b) -> a + "; " + b).orElse("参数校验失败");
		log.warn("参数校验异常: {}", message);
		return R.failed(message);
	}

	@ExceptionHandler(BindException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public R<?> handleBindException(BindException e) {
		String message = e.getFieldErrors().stream()
				.map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
				.reduce((a, b) -> a + "; " + b).orElse("参数绑定失败");
		log.warn("参数绑定异常: {}", message);
		return R.failed(message);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public R<?> handleIllegalArgument(IllegalArgumentException e) {
		log.warn("非法参数: {}", e.getMessage());
		return R.failed(e.getMessage());
	}

	@ExceptionHandler(BizException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public R<?> handleBizException(BizException e, HttpServletRequest request) {
		log.warn("业务异常, 请求路径: {}, 错误码: {}, 错误信息: {}", request.getRequestURI(), e.getCode(), e.getMessage());
		return R.failed(e.getMessage());
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	public R<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
		return R.failed("不支持的请求方式: " + e.getMethod());
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public R<?> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
		log.error("运行时异常, 请求路径: {}, 错误信息: {}", request.getRequestURI(), e.getMessage(), e);
		return R.failed("系统内部错误: " + e.getMessage());
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public R<?> handleException(Exception e, HttpServletRequest request) {
		log.error("系统异常, 请求路径: {}, 错误信息: {}", request.getRequestURI(), e.getMessage(), e);
		return R.failed("系统异常，请联系管理员");
	}

}

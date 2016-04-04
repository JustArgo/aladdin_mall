package com.maiquan.aladdin_mall.aspect;

import java.util.UUID;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 日志切面
 * 
 * @author JSC
 *
 */
public class LogAspect {

	public void aspect() {
	}

	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		Logger logger = LogManager
				.getLogger(joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
		String requestId=UUID.randomUUID().toString().replace("-", "");
		Object[] params=joinPoint.getArgs();
		params[0]=requestId;
		try {
			logger.info(">>>>>>>>>> requestId:" + requestId);
			Object o = joinPoint.proceed(params);
			logger.info("<<<<<<<<<< requestId:" + requestId);
			return o;
		} catch (Throwable e) {
			logger.error(e.getMessage(),e);
			logger.error("<<<<<<<<<< requestId:" + requestId);
			throw e;
		}
	}

}

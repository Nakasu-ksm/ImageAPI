//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.animetrace.animeimage.Limit;

import com.animetrace.animeimage.Utils.Utils;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class LimitRequestAspect {
    public static ConcurrentHashMap<String, ExpiringMap<String, Integer>> book = new ConcurrentHashMap();

    public LimitRequestAspect() {
    }

    @Pointcut("@annotation(limitRequest)")
    public void excudeService(LimitRequest limitRequest) {
    }

    @Around("excudeService(limitRequest)")
    public Object doAround(ProceedingJoinPoint pjp, LimitRequest limitRequest) throws Throwable {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes)ra;
        HttpServletRequest request = sra.getRequest();
        HttpServletResponse response = sra.getResponse();
        ExpiringMap<String, Integer> uc = (ExpiringMap)book.getOrDefault(request.getRequestURI(), ExpiringMap.builder().variableExpiration().build());
        Integer uCount = (Integer)uc.getOrDefault(request.getRemoteAddr(), 0);
        if (uCount >= limitRequest.count()) {
            if(limitRequest.mode()==0){
//                return Function.APIerror(limitRequest.content(), limitRequest.status());
            }else if (limitRequest.mode()==1){

//                return Function.error(limitRequest.content());
            }else{
                Utils.return_Json("您的访问过快，请稍后重试",400,response);
                return false;
            }

        } else {
            if (uCount == 0) {
                uc.put(request.getRemoteAddr(), uCount + 1, ExpirationPolicy.CREATED, limitRequest.time(), TimeUnit.MILLISECONDS);
            } else {
                uc.put(request.getRemoteAddr(),  uCount+ 1);
            }

            book.put(request.getRequestURI(), uc);
            Object result = pjp.proceed();
            return result;
        }
        return true;
    }
}

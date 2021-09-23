package com.citihub.configr.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import com.citihub.configr.exception.BadURIException;
import com.google.common.base.Strings;

public class URIValidationInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
      Object handler) throws Exception {
    if (Strings.isNullOrEmpty(request.getRequestURI()) || 
        request.getRequestURI().split("/").length < 3)
      throw new BadURIException();

    return true;
  }
}

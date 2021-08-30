package com.citihub.configr.api;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerMapping;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping(path = "/configuration")
public class ConfigurationController {

  @Autowired
  private ConfigurationRepository configRepo;

  @GetMapping(path = "/**")
  public void getData(HttpServletRequest request, HttpServletResponse response) {
    log.info("You asked for: " + request.getRequestURI());
    String fullPath =
        (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    log.info("You asked for: " + fullPath);
  }

  @PostMapping(consumes = {"application/json", "application/yaml", "application/yml"},
      value = "/**")
  public void postData(@RequestBody Map<String, Object> body, HttpServletRequest request,
      HttpServletResponse response) {
    log.info("You asked for me to put: " + body + " to the namespace " + request.getRequestURI());
  }
}

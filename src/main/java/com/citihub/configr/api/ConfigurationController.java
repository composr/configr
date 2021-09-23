package com.citihub.configr.api;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerMapping;
import com.citihub.configr.exception.BadURIException;
import com.citihub.configr.namespace.Namespace;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping(path = "/configuration")
public class ConfigurationController {

  private ConfigurationService configurationService;

  private ObjectMapper objectMapper;
  
  public ConfigurationController(@Autowired ConfigurationService configurationService,
      @Autowired ObjectMapper objectMapper) {
    this.configurationService = configurationService;
    this.objectMapper = objectMapper;
  }
  
  @GetMapping(path = "/**")
  public @ResponseBody Map<String, Object> getData(
      HttpServletRequest request, HttpServletResponse response) {
    log.info("You asked for: " + request.getRequestURI());
    String fullPath =
        (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    log.info("You asked for: " + fullPath);

    if (ConfigurationRequestValidation.isRequestURIAValidNamespace(request.getRequestURI())) {
      return configurationService.fetchNamespaceBodyByPath(fullPath);
    } else
      throw new BadURIException();
  }

  @PostMapping(consumes = {"application/json", "application/yaml", "application/yml"},
      value = "/**")
  public @ResponseBody Namespace postData(
      @RequestBody Map<String, Object> json, 
      HttpServletRequest request, HttpServletResponse response) throws IOException {
    log.info("You asked for me to put: " + json + " to the namespace " + request.getRequestURI());

    if (ConfigurationRequestValidation.isRequestURIAValidNamespace(request.getRequestURI()))
      return configurationService.storeNamespace(json, request.getRequestURI());
    else
      throw new BadURIException();
  }

}

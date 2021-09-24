package com.citihub.configr.version;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.citihub.configr.namespace.NamespaceService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping(path = "/version")
public class VersionController {

  private NamespaceService configurationService;
  
  public VersionController(@Autowired NamespaceService configurationService) {
    this.configurationService = configurationService;
  }
  
  @GetMapping(path = "/**", produces = { MediaType.APPLICATION_JSON_VALUE })
  public @ResponseBody Map<String, Object> getData(
      HttpServletRequest request, HttpServletResponse response) {
    return configurationService.fetchNamespaceBodyByPath(getTrimmedPath(request));
  }

  String getTrimmedPath(HttpServletRequest request) {
    return request.getRequestURI().replace("/configuration", "");
  }
    
}

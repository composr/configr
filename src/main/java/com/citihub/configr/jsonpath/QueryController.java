package com.citihub.configr.jsonpath;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.citihub.configr.namespace.NamespaceService;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping(path = "/query")
public class QueryController {

  private NamespaceService namespaceService;

  public QueryController(@Autowired NamespaceService namespaceService) {
    this.namespaceService = namespaceService;
  }

  @PreAuthorize("@authorizer.canRead()")
  @GetMapping(path = "/**", produces = {MediaType.APPLICATION_JSON_VALUE})
  public @ResponseBody Map<String, Object> getData(@RequestParam String jsonPath,
      HttpServletRequest request, HttpServletResponse response) {
    JsonPath jsonPathCompiled = JsonPath.compile(jsonPath);
    Map<String, Object> json = namespaceService.getNamespaceValue(getTrimmedPath(request));
    return wrapResults(jsonPathCompiled, json);
  }

  private Map<String, Object> wrapResults(JsonPath jsonPathCompiled, Map<String, Object> json) {
    return Map.of("results", jsonPathCompiled.read(json));
  }

  @PreAuthorize("@authorizer.canRead()")
  @PostMapping(path = "/**", produces = {MediaType.APPLICATION_JSON_VALUE})
  public @ResponseBody Map<String, Object> postQuery(@RequestBody String query,
      HttpServletRequest request, HttpServletResponse response) {
    JsonPath jsonPath = JsonPath.compile(query);
    Map<String, Object> json = namespaceService.getNamespaceValue(getTrimmedPath(request));
    return wrapResults(jsonPath, json);
  }

  String getTrimmedPath(HttpServletRequest request) {
    return request.getRequestURI().replace("/query", "");
  }

}

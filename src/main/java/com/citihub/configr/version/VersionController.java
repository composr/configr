package com.citihub.configr.version;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping(path = "/version")
public class VersionController {

  private VersionService versionService;

  public VersionController(@Autowired VersionService versionService) {
    this.versionService = versionService;
  }

  @GetMapping(path = "/**", produces = {MediaType.APPLICATION_JSON_VALUE})
  public @ResponseBody List<Document> getData(HttpServletRequest request,
      HttpServletResponse response) {
    return versionService.fetchVersions(getTrimmedPath(request));
  }

  String getTrimmedPath(HttpServletRequest request) {
    return request.getRequestURI().replace("/version", "");
  }

}

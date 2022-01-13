package com.citihub.configr.metadata;

import org.springframework.beans.factory.annotation.Autowired;
import com.citihub.configr.base.UnitTest;

public class SchemaValidationServiceTest extends UnitTest {

  @Autowired
  private SchemaValidationService validationService;

  public void testValidate() {
    validationService.validate("", "");
  }

}

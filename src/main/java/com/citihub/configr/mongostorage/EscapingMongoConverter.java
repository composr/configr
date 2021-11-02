package com.citihub.configr.mongostorage;

import javax.annotation.PostConstruct;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component("mongoConverter")
public class EscapingMongoConverter extends MappingMongoConverter {

  public EscapingMongoConverter(MongoDatabaseFactory mongoDatabaseFactory,
      MongoMappingContext mongoMappingContext) {
    super(mongoDatabaseFactory, mongoMappingContext);
  }

  @PostConstruct
  private void setup() {
    this.setMapKeyDotReplacement("#{dot}");
    this.afterPropertiesSet();
  }

  @Override
  protected String potentiallyEscapeMapKey(String source) {
    source = super.potentiallyEscapeMapKey(source);

    if (!source.contains("$"))
      return source;
    else
      return StringUtils.replace(source, "$", "#{dollarSign}");
  }

  @Override
  protected String potentiallyUnescapeMapKey(String source) {
    source = super.potentiallyUnescapeMapKey(source);
    return StringUtils.replace(source, "#{dollarSign}", "$");
  }

}

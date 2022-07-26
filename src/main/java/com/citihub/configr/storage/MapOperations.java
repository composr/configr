package com.citihub.configr.storage;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MapOperations {

  public static Object delete(Map<String, Object> mapExtant, String[] path) {
    Map<String, Object> curValue = mapExtant;
    for (int i = 0; i <= path.length - 2; i++) {
      if (curValue != null)
        if (curValue.get(path[i]) instanceof Map)
          curValue = (Map<String, Object>) curValue.get(path[i]);
        else
          curValue = null;
      else
        break;
      log.info("Traversing with key {} with value {}", path[i], curValue);
    }

    if (curValue != null) // I found where I'm going to remove, woo hoo
      return curValue.remove(path[path.length - 1]);
    else
      return null;
  }

  public static void merge(Map<String, Object> mapExtant, Map<String, Object> mapNew) {
    for (String key : mapNew.keySet()) {
      if (mapExtant.containsKey(key) && mapExtant.get(key) instanceof Map) {
        merge((Map<String, Object>) mapExtant.get(key), (Map<String, Object>) mapNew.get(key));
      } else
        mapExtant.put(key, mapNew.get(key));
    }
  }

  public static void replace(Map<String, Object> mapExtant, Map<String, Object> mapNew,
      String[] path) {
    if (path.length == 1) {// Wipe it from the root
      mapExtant.clear();
      mapExtant.putAll(mapNew);
    } else {
      Map<String, Object> curValue = mapExtant;
      for (int i = 0; i <= path.length - 2; i++) {
        if (curValue != null)
          if (curValue.get(path[i]) instanceof Map)
            curValue = (Map<String, Object>) curValue.get(path[i]);
          else
            curValue = null;
        else
          break;
        log.info("Traversing with key {} with value {}", path[i], curValue);
      }

      if (curValue != null) // I found where I'm going to do the replacement
        curValue.remove(path[path.length - 1]);

      merge(mapExtant, mapNew);
    }
  }

  public static boolean wouldReplace(Map<String, Object> mapExtant, Map<String, Object> mapNew,
      String[] path) {
    Map<String, Object> curValue = mapExtant;
    for (int i = 0; i <= path.length - 2; i++) {
      if (curValue == null)
        return false;
      else if (curValue.get(path[i]) instanceof Map)
        curValue = (Map<String, Object>) curValue.get(path[i]);
      else if (curValue.get(path[i]) == null)
        return false; // path doesn't exist, won't replace anything
      else
        return true; // would result in replacing a primitive/array
    }

    return curValue.containsKey(path[path.length - 1]);
  }
}

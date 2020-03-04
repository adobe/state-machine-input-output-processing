/*
Copyright 2020 Adobe. All rights reserved.
This file is licensed to you under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License. You may obtain a copy
of the License at http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under
the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
OF ANY KIND, either express or implied. See the License for the specific language
governing permissions and limitations under the License.
*/

package com.adobe.stateslanguage.util;

import com.adobe.stateslanguage.exception.GenericException;
import com.adobe.stateslanguage.exception.InvalidPathException;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.PathNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

public class JsonProcessorUtil {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static void generateExtractedValue(org.json.JSONObject inputObj,
      DocumentContext filteredInput)
      throws JSONException, InvalidPathException {
    Iterator<?> keys = inputObj.keys();

    List<String> keysToDelete = new ArrayList<>();
    Map<String, Object> keysToAdd = new HashMap<>();

    try {
      while (keys.hasNext()) {
        String key = (String) keys.next();
        if (!(inputObj.get(key) instanceof org.json.JSONObject)) {
          if (key.endsWith(".$")) {

            ArrayNode node;
            try {
              node = filteredInput.read((String) inputObj.get(key));
            } catch (PathNotFoundException e) {
              throw new InvalidPathException(
                  "JsonProcessor: InputPath does not exist in Input", e);
            }
            String filteredKeyName = key.substring(0, key.length() - 2);

            JsonNode jsonNode = node.get(0);
            if (jsonNode instanceof TextNode) {
              keysToAdd.put(filteredKeyName, jsonNode.textValue());
            } else {
              Object object = MAPPER.convertValue(jsonNode, Object.class);
              keysToAdd.put(filteredKeyName, object);
            }
            keysToDelete.add(key);
          }
        } else {
          generateExtractedValue((org.json.JSONObject) inputObj.get(key), filteredInput);
        }
      }
      for (String key : keysToDelete) {
        inputObj.remove(key);
      }
      for (Map.Entry<String, Object> entry : keysToAdd.entrySet()) {
        inputObj.put(entry.getKey(), entry.getValue());
      }
    } catch (PathNotFoundException e) {
      throw new InvalidPathException(
          "JsonProcessor: generateExtractedValue: Path does not exist in Input", e);
    }
  }

  public static void addStateResultToStateInput(ObjectNode node, JsonPointer pointer,
      JsonNode value) throws GenericException {
    JsonPointer parentPointer = pointer.head();
    JsonNode parentNode = node.at(parentPointer);
    String fieldName = pointer.last().toString().substring(1);

    if (parentNode.isMissingNode() || parentNode.isNull()) {
      parentNode =
          StringUtils.isNumeric(fieldName) ? MAPPER.createArrayNode() : MAPPER.createObjectNode();
      addStateResultToStateInput(node, parentPointer,
          parentNode); // recursively reconstruct hierarchy
    }

    if (parentNode.isArray()) {
      ArrayNode arrayNode = (ArrayNode) parentNode;
      int index = Integer.parseInt(fieldName);
      // expand array in case index is greater than array size (like JavaScript does)
      for (int i = arrayNode.size(); i <= index; i++) {
        arrayNode.addNull();
      }
      arrayNode.set(index, value);
    } else if (parentNode.isObject()) {
      ((ObjectNode) parentNode).set(fieldName, value);
    } else {
      throw new GenericException(
          "JsonProcessor: `" + fieldName + "` can't be set for parent node `"
              + parentPointer + "` because parent is not a container but " + parentNode
              .getNodeType()
              .name());
    }
  }

  public static JsonPointer convertJsonPathStringToJsonPointer(String path)
      throws InvalidPathException {

    if ("$".equals(path)) {
      return JsonPointer.compile("/");
    }

    if (!path.startsWith("$.") || path.length() <= 2) {
      throw new InvalidPathException("JsonProcessor: Invalid ResultPath");
    }
    String truncatedPath = path.substring(2);
    String[] nodes = truncatedPath.split("\\.");
    StringBuilder jsonPointerStringBuilder = new StringBuilder();

    for (String node : nodes) {
      jsonPointerStringBuilder.append("/");
      jsonPointerStringBuilder.append(node);
    }

    return JsonPointer.compile(jsonPointerStringBuilder.toString());
  }
}

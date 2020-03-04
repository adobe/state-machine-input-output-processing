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

package com.adobe.stateslanguage;

import static com.adobe.stateslanguage.util.JsonProcessorUtil.addStateResultToStateInput;
import static com.adobe.stateslanguage.util.JsonProcessorUtil.convertJsonPathStringToJsonPointer;
import static com.adobe.stateslanguage.util.JsonProcessorUtil.generateExtractedValue;

import com.adobe.stateslanguage.exception.GenericException;
import com.adobe.stateslanguage.exception.InvalidPathException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import java.io.IOException;
import org.json.JSONException;

public class JsonProcessor {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final Configuration CONFIGURATION =
      Configuration.builder().jsonProvider(new JacksonJsonNodeJsonProvider())
          .options(Option.ALWAYS_RETURN_LIST).options(Option.DEFAULT_PATH_LEAF_TO_NULL).build();

  public static String processInput(String input, String inputPath, String parameter)
      throws InvalidPathException, GenericException {

    if (inputPath == null || inputPath.equals("")) {
      return "{}";
    }

    ArrayNode node;
    try {
      node = JsonPath.using(CONFIGURATION).parse(input).read(inputPath);
      if (node == null || (node.size() == 0)) {
        throw new InvalidPathException("JsonProcessor: InputPath does not exist in Input");
      }

    } catch (PathNotFoundException e) {
      throw new InvalidPathException("JsonProcessor: InputPath does not exist in Input", e);
    }

    if (node.size() != 1) {
      throw new InvalidPathException("JsonProcessor: Invalid Input or InputPath");
    }

    String result = node.get(0).toString();

    if (parameter == null || parameter.equals("")) {
      return result;
    }
    DocumentContext filteredInput;
    org.json.JSONObject paramObj;
    try {
      filteredInput = JsonPath.using(CONFIGURATION).parse(result);
      paramObj = new org.json.JSONObject(parameter);
    } catch (JSONException e) {
      throw new GenericException(e);
    }
    generateExtractedValue(paramObj, filteredInput);
    return paramObj.toString();
  }

  public static String processOutput(String stateInput, String stateResult, String resultPath,
      String outputPath)
      throws InvalidPathException, GenericException {

    if (outputPath == null || outputPath.equals("")) {
      return "{}";
    }

    if ("$".equals(outputPath)) {
      return stateInput;
    }

    if ((resultPath == null || resultPath.equals("")) ||
        (stateResult == null || stateResult.equals(""))) {
      return getEffectiveOutputUsingOutputPath(outputPath, stateInput);
    }

    if ("$".equals(resultPath)) {
      return getEffectiveOutputUsingOutputPath(outputPath, stateResult);
    }

    try {
      ObjectNode stateInputNode = (ObjectNode) MAPPER.readTree(stateInput);
      JsonNode stateOutputNode = MAPPER.readTree(stateResult);

      /* Add state's result to state's input in the specified resultPath */
      addStateResultToStateInput(stateInputNode, convertJsonPathStringToJsonPointer(resultPath),
          stateOutputNode);

      /* Convert the state's constructed input to String */
      String result = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stateInputNode);
      return getEffectiveOutputUsingOutputPath(outputPath, result);

    } catch (IOException e) {
      throw new GenericException(e.getMessage(), e);
    }

  }

  private static String getEffectiveOutputUsingOutputPath(String outputPath, String result)
      throws InvalidPathException {
    /* Read out outputPath from the state's newly constructed input */
    ArrayNode node;
    try {
      node = JsonPath.using(CONFIGURATION).parse(result).read(outputPath);
      if (node == null || (node.size() == 0)) {
        throw new InvalidPathException(
            "JsonProcessor: OutputPath does not exist in constructed Input");
      }
    } catch (PathNotFoundException e) {
      throw new InvalidPathException(
          "JsonProcessor: OutputPath does not exist in constructed Input", e);
    }

    if (node.size() != 1) {
      throw new InvalidPathException(
          "JsonProcessor: Invalid effective output or OutputPath");
    }
    return node.get(0).toString();
  }
}

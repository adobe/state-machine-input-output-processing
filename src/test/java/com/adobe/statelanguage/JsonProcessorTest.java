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

package com.adobe.statelanguage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.adobe.statelanguage.exception.GenericException;
import com.adobe.statelanguage.exception.InvalidPathException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;

public class JsonProcessorTest {
  private static final String input1 = "{\n"
      + "  \"root\" : {\n"
      + "    \"array\" : [ {\n"
      + "      \"name\" : \"John\",\n"
      + "      \"age\" : 17\n"
      + "    }, null, null, null, 12 ],\n"
      + "    \"object\" : {\n"
      + "      \"num\" : 81,\n"
      + "      \"str\" : \"text\"\n"
      + "    }\n"
      + "  },\n"
      + "  \"descr\" : \"description\"\n"
      + "}\n";

  private static final String input2 = "{\n"
      + "  \"a\": 1,\n"
      + "  \"numbers\": 1,\n"
      + "  \"b\": {\n"
      + "    \"greeting\": \"Hi!\"\n"
      + "  }\n"
      + "}";

  private static final String input3 = "{\n"
      + "  \"a\": 1,\n"
      + "  \"numbers\": 1,\n"
      + "  \"b\": {\n"
      + "    \"greeting\": [1,2,3,4]\n"
      + "  }\n"
      + "}";

  private static final String input4 = "{\n"
      + "  \"a\": 1,\n"
      + "  \"numbers\": 1,\n"
      + "  \"b\": {\n"
      + "    \"greeting\": {\"c\": {\"x\": [6,7]}}\n"
      + "  }\n"
      + "}";

  private static final String input5 = "{\n"
      + "  \"comment\": \"Example for Parameters.\",\n"
      + "  \"product\": {\n"
      + "    \"details\": {\n"
      + "       \"color\": \"blue\",\n"
      + "       \"size\": \"small\",\n"
      + "       \"material\": \"cotton\"\n"
      + "    },\n"
      + "    \"availability\": \"in stock\",\n"
      + "    \"sku\": \"2317\",\n"
      + "    \"arr\": [1,2,3,4],\n"
      + "    \"cost\": \"$23\"\n"
      + "  }\n"
      + "}";

  private static final String parameter = "{\n"
      + "   \"comment\": \"Selecting what I care about.\",\n"
      + "   \"MyDetails\": {\n"
      + "       \"size.$\": \"$.product.details.size\",\n"
      + "       \"details.$\": \"$.product.details\",\n"
      + "       \"exists.$\": \"$.product.availability\",\n"
      + "       \"array.$\": \"$.product.arr\",\n"
      + "       \"StaticValue\": {\n"
      + "         \"price.$\": \"$.product.cost\"\n"
      + "       }\n"
      + "   }\n"
      + "}";

  private static boolean isValidJSON(String jsonStr) {
    try {
      final ObjectMapper mapper = new ObjectMapper();
      mapper.readTree(jsonStr);
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  /* ProcessInput Tests */
  @Test
  public void testInputProcessWithEmptyInputPath()
      throws InvalidPathException, GenericException {
    String inputPath = "";
    String result = JsonProcessor.processInput(input1, inputPath, parameter);
    assertTrue(isValidJSON(result));
    assertEquals("{}", result);
  }

  @Test
  public void testInputProcess() throws InvalidPathException, GenericException {
    String inputPath = "$";
    String result = JsonProcessor.processInput(input5, inputPath, parameter);
    assertTrue(isValidJSON(result));
    assertEquals("{\"MyDetails\":{\"size\":\"small\",\"array\":[1,2,3,4],"
            + "\"StaticValue\":{\"price\":\"$23\"},\"exists\":\"in stock\","
            + "\"details\":{\"color\":\"blue\",\"size\":\"small\",\"material\":\"cotton\"}},"
            + "\"comment\":\"Selecting what I care about.\"}",
        result);
  }

  @Test
  public void testInputProcessWhenParameterIsEmpty()
      throws InvalidPathException, GenericException {
    String inputPath = "$.b.greeting";
    String result = JsonProcessor.processInput(input2, inputPath, "");
    assertTrue(isValidJSON(result));
    assertEquals("\"Hi!\"", result);
  }

  @Test(expected = InvalidPathException.class)
  public void testInputProcessWhenInputPathDoesNotExist()
      throws InvalidPathException, GenericException {
    String inputPath = "$.root.abc";
    JsonProcessor.processInput(input1, inputPath, parameter);
  }

  @Test(expected = InvalidPathException.class)
  public void testInputProcessWhenInputJsonIsInvalid()
      throws GenericException, InvalidPathException {
    String inputPath = "$.root.abc";
    JsonProcessor.processInput(input1.substring(1), inputPath, parameter);
  }

  /*------------------------------------------------------------------------------------------ */
  /* ProcessOutput Tests */

  @Test(expected = GenericException.class)
  public void testInputProcessWhenParameterJsonIsInvalid()
      throws GenericException, InvalidPathException {
    String inputPath = "$.root.abc";
    JsonProcessor.processInput(input1, inputPath, parameter.substring(2));
  }

  @Test
  public void testOutputProcessWithEmptyOutputPath()
      throws GenericException, InvalidPathException {
    String outputPath = "";
    String resultPath = "$";
    String result = JsonProcessor.processOutput(input1, input2, resultPath, outputPath);
    assertTrue(isValidJSON(result));
    assertEquals("{}", result);
  }

  @Test
  public void testOutputProcessWithWholeOutputPath()
      throws GenericException, InvalidPathException {
    String outputPath = "$";
    String stateInput = input1;
    String resultPath = "$";
    String result = JsonProcessor.processOutput(stateInput, input2, resultPath, outputPath);
    assertTrue(isValidJSON(result));
    assertEquals(stateInput, result);
  }

  @Test
  public void testOutputProcessWithEmptyResultPath()
      throws GenericException, InvalidPathException {
    String outputPath = "$.b.greeting";
    String resultPath = "";
    String result = JsonProcessor.processOutput(input3, input2, resultPath, outputPath);
    assertTrue(isValidJSON(result));
    assertEquals("[1,2,3,4]", result);
  }

  @Test
  public void testOutputProcessWithEmptyStateResult()
      throws GenericException, InvalidPathException {
    String outputPath = "$.b.greeting";
    String stateOutput = "";
    String resultPath = "$.x";
    String result = JsonProcessor.processOutput(input3, stateOutput, resultPath, outputPath);
    assertTrue(isValidJSON(result));
    assertEquals("[1,2,3,4]", result);
  }

  @Test
  public void testOutputProcessWithWholeResultPath()
      throws GenericException, InvalidPathException {
    String outputPath = "$.b.greeting";
    String resultPath = "$";
    String result = JsonProcessor.processOutput(input1, input4, resultPath, outputPath);
    assertTrue(isValidJSON(result));
    assertEquals("{\"c\":{\"x\":[6,7]}}", result);
  }

  @Test
  public void testOutputProcessWhenResultPathExistsInInput()
      throws GenericException, InvalidPathException {
    String outputPath = "$.root.array.a";
    String resultPath = "$.root.array";
    String result = JsonProcessor.processOutput(input1, input2, resultPath, outputPath);
    assertTrue(isValidJSON(result));
    assertEquals("1", result);
  }

  @Test
  public void testOutputProcessWhenResultPathDoesNotExistInInput()
      throws GenericException, InvalidPathException {
    String outputPath = "$.root.test.next.a";
    String resultPath = "$.root.test.next";
    String result = JsonProcessor.processOutput(input1, input2, resultPath, outputPath);
    assertTrue(isValidJSON(result));
    assertEquals("1", result);
  }

  @Test
  public void testOutputProcessWhenOutputPathDoesNotExist()
      throws GenericException, InvalidPathException {
    String outputPath = "$.root.abc";
    String resultPath = "$.root.test.next";
    JsonProcessor.processOutput(input1, input2, resultPath, outputPath);
  }
}

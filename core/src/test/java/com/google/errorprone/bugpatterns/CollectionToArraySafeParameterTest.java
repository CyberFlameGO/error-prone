/*
 * Copyright 2017 The Error Prone Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** @author mariasam@google.com (Maria Sam) on 6/27/17. */
@RunWith(JUnit4.class)
public class CollectionToArraySafeParameterTest {
  private final CompilationTestHelper compilationHelper =
      CompilationTestHelper.newInstance(CollectionToArraySafeParameter.class, getClass());

  @Test
  public void testPositiveCases() {
    compilationHelper.addSourceFile("CollectionToArraySafeParameterPositiveCases.java").doTest();
  }

  @Test
  public void testNegativeCases() {
    compilationHelper.addSourceFile("CollectionToArraySafeParameterNegativeCases.java").doTest();
  }

  // regression test for b/67022899
  @Test
  public void b67022899() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "import java.util.Collection;",
            "class Test {",
            "  <C extends Collection<Integer>> void f(C cx) {",
            "    cx.toArray(new Integer[0]);",
            "  }",
            "}")
        .doTest();
  }
}

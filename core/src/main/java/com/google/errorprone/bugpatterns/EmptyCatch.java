/*
 * Copyright 2020 The Error Prone Authors.
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

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.matchers.Description.NO_MATCH;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker.CatchTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CatchTree;

/** A {@link BugChecker}; see the associated {@link BugPattern} annotation for details. */
@BugPattern(
    name = "EmptyCatch",
    summary = "Caught exceptions should not be ignored",
    severity = WARNING,
    tags = BugPattern.StandardTags.STYLE,
    documentSuppression = false,
    linkType = CUSTOM,
    link = "https://google.github.io/styleguide/javaguide.html#s6.2-caught-exceptions")
public class EmptyCatch extends BugChecker implements CatchTreeMatcher {

  @Override
  public Description matchCatch(CatchTree tree, VisitorState state) {
    BlockTree block = tree.getBlock();
    if (!block.getStatements().isEmpty()) {
      return NO_MATCH;
    }
    if (state.getTokensForNode(block).stream().anyMatch(t -> !t.comments().isEmpty())) {
      return NO_MATCH;
    }
    if (ASTHelpers.isJUnitTestCode(state)) {
      return NO_MATCH;
    }
    if (ASTHelpers.isTestNgTestCode(state)) {
      return NO_MATCH;
    }
    return describeMatch(tree);
  }
}

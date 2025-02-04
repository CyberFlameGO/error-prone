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

import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.fixes.SuggestedFixes.renameVariable;
import static com.google.errorprone.matchers.Description.NO_MATCH;
import static com.google.errorprone.util.ASTHelpers.getType;
import static com.google.errorprone.util.ASTHelpers.isSubtype;

import com.google.common.base.Ascii;
import com.google.common.base.CaseFormat;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker.VariableTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import java.util.Objects;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

/** A {@link BugChecker}; see the associated {@link BugPattern} annotation for details. */
@BugPattern(
    name = "DateFormatConstant",
    summary = "DateFormat is not thread-safe, and should not be used as a constant field.",
    severity = WARNING,
    tags = StandardTags.FRAGILE_CODE)
public class DateFormatConstant extends BugChecker implements VariableTreeMatcher {

  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {
    if (tree.getInitializer() == null) {
      return NO_MATCH;
    }
    VarSymbol sym = ASTHelpers.getSymbol(tree);
    if (sym == null || sym.getKind() != ElementKind.FIELD) {
      return NO_MATCH;
    }
    String name = sym.getSimpleName().toString();
    if (!(sym.isStatic() && sym.getModifiers().contains(Modifier.FINAL))) {
      return NO_MATCH;
    }
    if (!name.equals(Ascii.toUpperCase(name))) {
      return NO_MATCH;
    }
    if (!isSubtype(getType(tree), JAVA_TEXT_DATEFORMAT.get(state), state)) {
      return NO_MATCH;
    }
    SuggestedFix rename =
        renameVariable(
            tree,
            CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, tree.getName().toString()),
            state);
    return buildDescription(tree)
        .addFix(threadLocalFix(tree, state, sym, rename))
        .addFix(rename)
        .build();
  }

  private static Fix threadLocalFix(
      VariableTree tree, VisitorState state, final VarSymbol sym, SuggestedFix rename) {
    SuggestedFix.Builder fix =
        SuggestedFix.builder()
            .merge(rename)
            .replace(
                tree.getType(),
                String.format("ThreadLocal<%s>", state.getSourceForNode(tree.getType())))
            .prefixWith(tree.getInitializer(), "ThreadLocal.withInitial(() -> ")
            .postfixWith(tree.getInitializer(), ")");
    CompilationUnitTree unit = state.getPath().getCompilationUnit();
    unit.accept(
        new TreeScanner<Void, Void>() {
          @Override
          public Void visitIdentifier(IdentifierTree tree, Void unused) {
            if (Objects.equals(ASTHelpers.getSymbol(tree), sym)) {
              fix.postfixWith(tree, ".get()");
            }
            return null;
          }
        },
        null);
    return fix.build();
  }

  private static final Supplier<Type> JAVA_TEXT_DATEFORMAT =
      VisitorState.memoize(state -> state.getTypeFromString("java.text.DateFormat"));
}

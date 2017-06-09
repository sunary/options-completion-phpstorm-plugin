package main.com.sunary;

/**
 * Created by sunary on 6/5/17.
 */

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.logging.Logger;


public class CompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        PsiElement element = parameters.getPosition().getParent();

        ParameterList parameterList = PsiTreeUtil.getParentOfType(element, ParameterList.class);
        AssignmentExpression assignment = PsiTreeUtil.getParentOfType(element, AssignmentExpression.class);
        if (parameterList != null) {
            PsiElement[] givenParameters = parameterList.getParameters();
            PsiElement context = parameterList.getContext();

            if (context instanceof FunctionReference) {
                FunctionReference function = (FunctionReference) context;
                addCompletionForFunctionOptions(function, element, givenParameters, result);
            } else if (context instanceof NewExpression) {
                NewExpression newExpression = (NewExpression) context;
                addCompletionForConstructorOptions(newExpression, element, givenParameters, result);
            }
        }else if (assignment != null){
            addCompletionForAssignmentOptions(assignment, result);
        }
    }

    private void addCompletionForAssignmentOptions(AssignmentExpression assignment, CompletionResultSet result){
        PhpDocComment docComment = resolveConstructor(assignment).getDocComment();
        if (docComment != null){
            List<OptionsParam> optionsParams = new PhpDocCommentParser().parseEnum(docComment.getText());

            for (OptionsParam optionsParam : optionsParams){
                if (optionsParam.getKey().equals(assignment.getVariable().getText()) ||
                        optionsParam.getKey().equals(assignment.getVariable().getText().replace("$this->", ""))){
                    for (String[] opt: optionsParam.getOptions()) {
                        result.addElement(
                                PrioritizedLookupElement.withPriority(
                                        LookupElementBuilder.create(opt[0]).withTypeText(optionsParam.getKey() + " @enum"), Double.MAX_VALUE));
                    }
                }
            }
        }
    }

    private void addCompletionForConstructorOptions(NewExpression newExpression, PsiElement element, PsiElement[] givenParameters, CompletionResultSet result) {
        ClassReference classReference = newExpression.getClassReference();
        if (classReference != null) {
            PsiElement resolvedReference = classReference.resolve();
            Method constructor =  resolveConstructor(resolvedReference);
            if (constructor != null) {
                PhpDocComment docComment = constructor.getDocComment();
                if (docComment != null) {
                    addCompletionForOptions(result, element, givenParameters, docComment.getText());
                }
            }
        }
    }

    private Method resolveConstructor(PsiElement resolvedReference) {
        if (resolvedReference instanceof Method) {
            return (Method) resolvedReference;
        }

        if (resolvedReference instanceof PhpClass) {
            PhpClass phpClass = (PhpClass) resolvedReference;
            return phpClass.getConstructor();
        }

        while (resolvedReference instanceof AssignmentExpression || resolvedReference instanceof Statement){
            return resolveConstructor(resolvedReference.getParent());
        }
        return null;
    }

    private void addCompletionForFunctionOptions(FunctionReference function, PsiElement element, PsiElement[] givenParameters, CompletionResultSet result) {
        PhpIndex phpIndex = PhpIndex.getInstance(element.getProject());
        String signature = function.getSignature();
        String[] variants = signature.split("\\|");
        for (String variant : variants) {
            Collection<? extends PhpNamedElement> bySignature = phpIndex.getBySignature(variant);
            for (PhpNamedElement phpNamedElement : bySignature) {
                PhpDocComment docComment = phpNamedElement.getDocComment();
                if (docComment != null) {
                    addCompletionForOptions(result, element, givenParameters, docComment.getText());
                }
            }
        }
    }

    private void addCompletionForOptions(CompletionResultSet result, PsiElement element, PsiElement[] givenParameters, String docCommentText) {
        ArrayCreationExpression arrayCreation = PsiTreeUtil.getParentOfType(element, ArrayCreationExpression.class);
        if (arrayCreation != null && canBecomeKey(element)) {
            Integer parameterIndex = getParameterIndex(givenParameters, arrayCreation);
            if (parameterIndex != null) {
                Map<String, OptionsParam> optionsParams = new PhpDocCommentParser().parse(docCommentText);
                OptionsParam optionsParam = optionsParams.get(Integer.toString(parameterIndex));
                if (optionsParam != null) {
                    List<String[]> options = optionsParam.getOptions();
                    for (String[] opt: options) {
                        result.addElement(LookupElementBuilder.create(opt[0]).withTypeText(opt[1]).withPresentableText(opt[0] + ": " + opt[2]));
                    }
                }
            }
        }
    }

    private boolean canBecomeKey(PsiElement element) {
        return (PsiTreeUtil.getParentOfType(element, ArrayHashElement.class) == null || PlatformPatterns.psiElement(PhpElementTypes.ARRAY_KEY).accepts(element.getParent()));
    }

    private Integer getParameterIndex(PsiElement[] methodParameters, PsiElement parameterElement) {
        for (int i = 0; i < methodParameters.length; i++) {
            if (methodParameters[i].equals(parameterElement)) {
                return i;
            }
        }
        return null;
    }
}

package liquibase.command;

import liquibase.util.StringUtil;
import lombok.Getter;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
@SupportedAnnotationTypes({"liquibase.command.CommandOverride"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class CommandOverrideProcessor extends AbstractProcessor {
    private final Map<String, List<String>> overrides = new ConcurrentHashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(CommandOverride.class)) {
            Optional<? extends AnnotationMirror> maybeMirror = getAnnotationMirror(element, CommandOverride.class);
            maybeMirror.ifPresent(mirror -> {
                Optional<? extends AnnotationValue> maybeValue = getAnnotationValue(mirror, "override");
                maybeValue.ifPresent(value -> {
                    overrides.computeIfAbsent(value.getValue().toString(), val -> new ArrayList<>()).add(element.getSimpleName().toString());
                });
            });
        }
        Map<String, List<String>> invalidOverrides = new HashMap<>();
        overrides.forEach((step, overrideSteps) -> {
            if (overrideSteps.size() > 1) {
                invalidOverrides.put(step, overrideSteps);
            }
        });

        invalidOverrides.forEach((step, overrideSteps) -> {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format("Found multiple command steps overriding %s! A command may have at most one override. Invalid overrides include: %s",
                    step,
                    StringUtil.join(overrideSteps, ", ")));
        });
        return invalidOverrides.isEmpty();
    }

    public Optional<? extends AnnotationMirror> getAnnotationMirror(final Element element, final Class<? extends Annotation> annotationClass) {
        final String annotationClassName = annotationClass.getName();
        return element.getAnnotationMirrors().stream()
                .filter(clazz -> clazz.getAnnotationType().toString().equals(annotationClassName))
                .findFirst();
    }

    public Optional<? extends AnnotationValue> getAnnotationValue(final AnnotationMirror annotationMirror, final String name) {
        final Elements elementUtils = this.processingEnv.getElementUtils();
        final Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = elementUtils.getElementValuesWithDefaults(annotationMirror);
        return elementValues.keySet().stream()
                .filter(clazz -> clazz.getSimpleName().toString().equals(name))
                .map(elementValues::get)
                .findAny();
    }
}

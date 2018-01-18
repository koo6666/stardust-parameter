package com.github.koo6666.stardust.parameter.http;

import com.github.koo6666.stardust.parameter.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public final class RequestParameterResolver {

    public static <T> T resolver(HttpServletRequest httpServletRequest, Class<? extends T> clazz) {

        try {
            final Constructor<? extends T> constructor = clazz.getConstructor();
            final T domain = constructor.newInstance();
            return resolver(httpServletRequest, domain);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("No default Constructor");
    }

    public static <T> T resolver(HttpServletRequest httpServletRequest, T domain) {

        for (Field field : domain.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            final RequestParam annotation = field.getAnnotation(RequestParam.class);
            if (annotation != null) {

                final PropertyEditor editor = PropertyEditorManager.findEditor(annotation.clazz());
                if (editor == null) {
                    throw new RuntimeException("this class no editor,class:" + annotation.clazz().getName());
                }

                if (annotation.param().isEmpty()) {
                    editor.setAsText(httpServletRequest.getParameter(field.getName()));
                } else {
                    editor.setAsText(httpServletRequest.getParameter(annotation.param()));
                }

                try {
                    field.set(domain, editor.getValue());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new RuntimeException("can't parser http param");
                }

            }
        }

        return domain;
    }
}

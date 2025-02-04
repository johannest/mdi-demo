package com.example.application.views;

import com.example.application.components.window.Window;
import com.example.application.components.window.WindowData;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import jakarta.annotation.PostConstruct;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.*;

@VaadinSessionScope
@Component(value = "windowFactory")
public class WindowFactory {

    private final ApplicationContext applicationContext;
    Map<String, List<WindowData>> windows = new HashMap<>();
    Map<String, String> windowNameToTitle = new HashMap<>();
    Map<String, Pair<WindowContent, Class<?>>> windowNameToContentAndClass = new HashMap<>();

    public WindowFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Initialize window names with corresponding WindowContent annotations and classes
     */
    @PostConstruct
    public void init() {
        String[] beansNames = applicationContext
                .getBeanNamesForAnnotation(WindowContent.class);

        for (String beanName : beansNames) {
            Class<?> contentClass = applicationContext.getType(beanName);
            if (contentClass != null) {
                WindowContent contentAnnotation = contentClass
                        .getAnnotation(WindowContent.class);
                String windowName = contentAnnotation.value();
                // cache these for faster easier access
                windowNameToTitle.put(windowName, contentAnnotation.title());
                windowNameToContentAndClass.put(windowName, Pair.of(contentAnnotation, contentClass));
            }
        }
    }

    /**
     * Create Window for the contentAnnotation and the content class
     *
     * @param contentAnnotationAndClass window definition in the annotation
     * @return newly created Window instance
     */
    private Optional<Window> createWindow(Pair<WindowContent, Class<?>> contentAnnotationAndClass) {
        if (contentAnnotationAndClass != null) {
            WindowContent contentAnnotation = contentAnnotationAndClass.getFirst();
            com.vaadin.flow.component.Component content = (com.vaadin.flow.component.Component)
                    applicationContext.getBean(contentAnnotationAndClass.getSecond());
            String windowName = contentAnnotation.value();
            Window window = new Window(contentAnnotation.title(),
                    contentAnnotation.left(), contentAnnotation.top(),
                    contentAnnotation.width(), contentAnnotation.height());

            window.addOpenedChangeListener(event -> {
                // TODO window was closed remove it from windows map (high priority)

            });

            List<WindowData> windowList = windows.computeIfAbsent(windowName, k -> new ArrayList<>());
            int windowNumber = windowList.size() + 1;
            WindowData windowData = new WindowData(windowName, contentAnnotation.title(), windowNumber, window);
            windowList.add(windowData);
            window.add(content);
            return Optional.of(window);
        }
        return Optional.empty();
    }


    public Set<String> getWindowNames() {
        return windowNameToTitle.keySet();
    }

    public String getWindowTitle(String windowName) {
        return windowNameToTitle.get(windowName);
    }

    public Window getWindow(String name, Integer windowNumber) {
        Optional<WindowData> any = windows.get(name).stream().filter(windowData -> windowData.getWindowNumber().equals(windowNumber)).findAny();
        return any.map(WindowData::getInstance).orElse(null);
    }

    public Optional<Window> getWindow(String name) {
        return createWindow(windowNameToContentAndClass.get(name));
    }

    public List<WindowData> getOpenedWindows() {
        List<WindowData> openedWindows = new ArrayList<>();
        windows.forEach((key, value) -> openedWindows.addAll(value));
        return openedWindows;
    }
}

package com.example.application.components.window;

import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import jakarta.annotation.PostConstruct;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

@VaadinSessionScope
@Component(value = "windowFactory")
public class WindowFactory {

    private final ApplicationContext applicationContext;
    Map<String, List<WindowData>> windows = new HashMap<>();
    Map<String, String> windowNameToTitle = new HashMap<>();
    Map<String, Pair<WindowContent, Class<?>>> windowNameToContentAndClass = new HashMap<>();
	private List<Consumer<Window>> eventHandlers = new ArrayList<Consumer<Window>>();

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
     * Returns Window for the contentAnnotation and the content class
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
            List<WindowData> windowList = windows.computeIfAbsent(windowName, k -> new ArrayList<>());

            if (windowList.isEmpty() || contentAnnotation.multiWindow()) {
                // this window does not yet exist, or it is multi window
                Window window = createNewWindowInstance(contentAnnotation, windowList, windowName, content);
                return Optional.of(window);
            } else {
                // only one instance of this window is allowed, just return it
                Window instance = windowList.get(0).getInstance();
                if (instance.isAttached()) {
                    return Optional.of(instance);
                } else {
                    windowList.remove(0);
                    return Optional.of(createNewWindowInstance(contentAnnotation, windowList, windowName, content));
                }
            }
        }
        return Optional.empty();
    }

    private Window createNewWindowInstance(WindowContent contentAnnotation, List<WindowData> windowList, String windowName, com.vaadin.flow.component.Component content) {
        Window window = new Window(contentAnnotation.title(),
                contentAnnotation.left(), contentAnnotation.top(),
                contentAnnotation.width(), contentAnnotation.height());

        window.addOpenedChangeListener(event -> {
            if (!event.isOpened()) {
                Optional<WindowData> windowToRemove = windows.getOrDefault(windowName, List.of()).stream().filter(data -> data.getInstance().equals(window)).findAny();
                windowToRemove.ifPresent(data -> windows.get(windowName).remove(data));
                eventHandlers.forEach(e -> e.accept(null));
            }
        });

        int windowNumber = windowList.size() + 1;
        WindowData windowData = new WindowData(windowName, contentAnnotation.title(), windowNumber, contentAnnotation, window);
        windowList.add(windowData);
        window.add(content);
        eventHandlers.forEach(e -> e.accept(window));
        return window;
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
        windows.forEach((key, value) -> {
            openedWindows.addAll(value.stream().filter(windowData -> windowData.getWindowContent().showInManager()).toList());
        });
        return openedWindows;
    }
    
    // TODO improve listener, should probably replicate openedChangeEvent in functionality
    public Registration addWindowCreatedListener(Consumer<Window> eventHandler) {
    	eventHandlers.add(eventHandler);
    	return new Registration() {
			
			@Override
			public void remove() {
				eventHandlers.remove(eventHandler);
			}
		};
    }

    public void closeAllWindows() {
        // TODO make sure all references are cleaned from memory
        List<WindowData> openedWindows = new ArrayList<>(getOpenedWindows());
        openedWindows.forEach(windowData -> {
            windowData.getInstance().close();
        });
        eventHandlers.clear();
        windows = new HashMap<>();
    }
}

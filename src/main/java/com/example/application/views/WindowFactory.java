package com.example.application.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import com.example.application.components.window.Window;
import com.example.application.components.window.WindowData;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;

import jakarta.annotation.PostConstruct;

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
            	if (!event.isOpened()) {
		            String name = windowNameToTitle.entrySet().stream().filter(e -> window.getHeaderTitle().equals(e.getValue())).findAny().map(Entry::getKey).orElse(null);
		            Optional<WindowData> windowToRemove = windows.getOrDefault(name, List.of()).stream().filter(data -> data.getInstance().equals(window)).findAny();
		            windowToRemove.ifPresent(data -> windows.get(name).remove(data));
		            eventHandlers.forEach(e -> e.accept(null));
            	}
            });

            List<WindowData> windowList = windows.computeIfAbsent(windowName, k -> new ArrayList<>());
            int windowNumber = windowList.size() + 1;
            WindowData windowData = new WindowData(windowName, contentAnnotation.title(), windowNumber, window);
            windowList.add(windowData);
            window.add(content);
            eventHandlers.forEach(e -> e.accept(window));
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
}

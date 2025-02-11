package com.example.application.components.window;

import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;

@UIScope
@Component(value = "windowFactory")
public class WindowFactory {

    private final ApplicationContext applicationContext;
    private final Map<String, List<WindowData>> windows = new HashMap<>();
    private final Map<String, String> windowNameToTitle = new HashMap<>();
    private final Map<String, Pair<WindowContent, Class<?>>> windowNameToContentAndClass = new HashMap<>();
    private final Map<Object, Window> beanToWindow = new HashMap<>();
    private final List<Consumer<Window>> eventHandlers = new ArrayList<Consumer<Window>>();

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
    private Optional<WindowAndContent> createWindow(Pair<WindowContent, Class<?>> contentAnnotationAndClass) {
        if (contentAnnotationAndClass != null) {
            WindowContent contentAnnotation = contentAnnotationAndClass.getFirst();
            com.vaadin.flow.component.Component content = (com.vaadin.flow.component.Component)
                    applicationContext.getBean(contentAnnotationAndClass.getSecond());
            String windowName = contentAnnotation.value();
            List<WindowData> windowList = windows.computeIfAbsent(windowName, k -> new ArrayList<>());

            if (windowList.isEmpty() || contentAnnotation.multiWindow()) {
                // this window does not yet exist, or it is multi window
                Window window = createNewWindowInstance(contentAnnotation, windowList, windowName, content);
                return Optional.of(new WindowAndContent(window, content));
            } else {
                // only one instance of this window is allowed, just return it
                Window instance = windowList.get(0).getInstance();
                if (instance.isAttached()) {
                    return Optional.of(new WindowAndContent(instance, content));
                } else {
                    windowList.remove(0);
                    return Optional.of(new WindowAndContent(createNewWindowInstance(contentAnnotation, windowList, windowName, content), content));
                }
            }
        }
        return Optional.empty();
    }

    private Window createNewWindowInstance(WindowContent contentAnnotation, List<WindowData> windowList, String windowName, com.vaadin.flow.component.Component content) {
    	int windowNumber = windowList.stream().map(WindowData::getWindowNumber).max(Integer::compare).map(e -> e + 1).orElse(1);
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

    public Window getOrCreateWindow(String name, Integer windowNumber) {
        Optional<WindowData> any = windows.get(name).stream().filter(windowData -> windowData.getWindowNumber().equals(windowNumber)).findAny();
        return any.map(WindowData::getInstance).orElse(null);
    }

    public Optional<WindowAndContent> getOrCreateWindow(String name) {
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
        windows.clear();
    }

    /**
     * Should this window be listed in navigation menu
     *
     * @param name window name
     * @return true if it should be shown in navigation menu
     */
    public boolean showWindowInMenu(String name) {
        return windowNameToContentAndClass.get(name).getFirst().showInMenu();
    }

    /**
     * Way to connect bean that is being in edited to its window instance
     *
     * @param bean   any Java bean being edited in the window
     * @param window actual Window object where the bean is edited
     */
    public void addBeanToWindow(Object bean, Window window) {
        beanToWindow.put(bean, window);
    }

    /**
     * Close the window where the given bean was edited
     *
     * @param bean bean that was edited
     */
    public void closeWindowOfBean(Object bean) {
        Window window = beanToWindow.get(bean);
        if (window != null) {
            window.close();
        }
        beanToWindow.remove(bean);
    }

    /**
     * Lists possible roles in the @RolesAllowed annotation
     *
     * @param name window name
     * @return all roles defined in possible @RolesAllowed annotation
     */
    public List<String> rolesAllowed(String name) {
        List<String> roles = new ArrayList<>();
        Pair<WindowContent, Class<?>> windowContentClassPair = windowNameToContentAndClass.get(name);
        if (windowContentClassPair != null) {
            RolesAllowed annotation = windowContentClassPair.getSecond().getAnnotation(RolesAllowed.class);
            if (annotation != null) {
                roles.addAll(Arrays.stream(annotation.value()).toList());
            }
        }
        return roles;
    }

    /**
     * Check whether the Window is annotated @PermitAll annotation
     *
     * @param name window name
     * @return true if window has an @PermitAll annotation
     */
    public boolean isPermitAll(String name) {
        Pair<WindowContent, Class<?>> windowContentClassPair = windowNameToContentAndClass.get(name);
        if (windowContentClassPair != null) {
            PermitAll annotation = windowContentClassPair.getSecond().getAnnotation(PermitAll.class);
            return annotation != null;
        }
        return false;
    }

    public boolean isWindowAllowed(AuthenticationContext authenticationContext, String windowName) {
        if (isPermitAll(windowName)) {
            return true;
        }
        List<String> rolesAllowed = rolesAllowed(windowName);
        if (!rolesAllowed.isEmpty()) {
            return authenticationContext.hasAnyRole(rolesAllowed);
        }
        return false;
    }
}

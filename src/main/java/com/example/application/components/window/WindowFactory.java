package com.example.application.components.window;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.spring.security.AuthenticationContext;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

/**
 * Multi Document Interface (MDI) architecture for Vaadin Flow.
 * <p>
 * The main offered functionalities:
 * <ul>
 * <li>WindowsFactory is responsible for instantiating new Windows when requested</li>
 * <li>It can be used to get access of open windows</li>
 * <li>It provides helpers for access control</li>
 * </ul>
 */
@UIScope
@SpringComponent(value = "windowFactory")
public class WindowFactory {
    private Logger logger = LoggerFactory.getLogger(WindowFactory.class);
    private final ApplicationContext applicationContext;
    private final AuthenticationContext authenticationContext;

    /* Internal data structures */
    private final Map<String, List<WindowData>> windows = new HashMap<>();
    private final Map<String, String> windowNameToTitle = new HashMap<>();
    private final Map<String, WindowContentAndClass> windowNameToContentAndClass = new HashMap<>();
    private final Map<Object, Window> beanToWindow = new HashMap<>();
    private final List<Consumer<Window>> eventHandlers = new ArrayList<Consumer<Window>>();
    private final Map<Component, WindowData> viewToWindowData = new HashMap<>();
    private final Map<Window, Registration> windowListenerRegistrations = new HashMap<>();

    public WindowFactory(ApplicationContext applicationContext, AuthenticationContext authenticationContext) {
        this.applicationContext = applicationContext;
        this.authenticationContext = authenticationContext;
    }

    /**
     * Initialize window names with corresponding WindowContent annotations and classes
     */
    @PostConstruct
    @SuppressWarnings("unchecked")
    public void init() {
        String[] beansNames = applicationContext
                .getBeanNamesForAnnotation(WindowContent.class);

        for (String beanName : beansNames) {
            try {
                Class<? extends com.vaadin.flow.component.Component> contentClass =
                        (Class<? extends com.vaadin.flow.component.Component>) applicationContext.getType(beanName);
                if (contentClass != null) {
                    WindowContent contentAnnotation = contentClass
                            .getAnnotation(WindowContent.class);
                    String windowName = contentAnnotation.value();
                    // cache these for faster easier access
                    windowNameToTitle.put(windowName, contentAnnotation.title());
                    windowNameToContentAndClass.put(windowName, new WindowContentAndClass(contentAnnotation, contentClass));
                }
            } catch (ClassCastException e) {
                logger.error("Failed to case bean to Vaadin Component Class: {}", e.getMessage());
            }
        }
    }

    /**
     * Returns Window for the contentAnnotation and the content class
     *
     * @param contentAnnotationAndClass window definition in the annotation
     * @return newly created Window instance
     */
    private Optional<WindowAndContent> createWindow(WindowContentAndClass contentAnnotationAndClass) {
        if (contentAnnotationAndClass != null) {
            WindowContent contentAnnotation = contentAnnotationAndClass.windowContent();
            com.vaadin.flow.component.Component content = applicationContext.getBean(contentAnnotationAndClass.windowContentClass());
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

    private Window createNewWindowInstance(WindowContent contentAnnotation, List<WindowData> windowList, String windowName, Component content) {
        int windowNumber = windowList.stream().map(WindowData::getWindowNumber).max(Integer::compare).map(e -> e + 1).orElse(1);
        Window window = new Window(contentAnnotation.title(),
                contentAnnotation.left(), contentAnnotation.top(),
                contentAnnotation.width(), contentAnnotation.height());

        Registration registration = window.addOpenedChangeListener(event -> {
            if (!event.isOpened()) {
                Optional<WindowData> windowToRemove = windows.getOrDefault(windowName, List.of()).stream().filter(data -> data.getInstance().equals(window)).findAny();
                windowToRemove.ifPresent(data -> windows.get(windowName).remove(data));
                eventHandlers.forEach(e -> e.accept(null));
            }
        });
        windowListenerRegistrations.put(window, registration);

        WindowData windowData = new WindowData(windowName, contentAnnotation.title(), windowNumber, contentAnnotation, window);
        windowList.add(windowData);
        window.add(content);
        viewToWindowData.put(content, windowData);
        eventHandlers.forEach(e -> e.accept(window));
        return window;
    }

    /**
     * @return all available window names
     */
    public Set<String> getWindowNames() {
        return windowNameToTitle.keySet();
    }

    /**
     * @param windowName return title of the given window
     * @return return title of the given window
     */
    public String getWindowTitle(String windowName) {
        return windowNameToTitle.get(windowName);
    }

    /**
     * Get specific instance of Window for given name and number
     *
     * @param name         window name
     * @param windowNumber window number
     * @return Window instance
     */
    public Window getWindowInstance(String name, Integer windowNumber) {
        Optional<WindowData> any = windows.get(name).stream().filter(windowData -> windowData.getWindowNumber().equals(windowNumber)).findAny();
        return any.map(WindowData::getInstance).orElse(null);
    }

    /**
     * Get or create Window for the given window name
     *
     * @param name unique window name identifier
     * @return if the window can be instantiated multiple times, new instance is returned,
     * otherwise the possible existing instance is returned or first new one
     */
    public Optional<WindowAndContent> getWindowInstance(String name) {
        return createWindow(windowNameToContentAndClass.get(name));
    }

    /**
     * @return all opened windows with a list of WindowData references
     */
    public List<WindowData> getOpenedWindows() {
        List<WindowData> openedWindows = new ArrayList<>();
        windows.forEach((key, value) -> {
            openedWindows.addAll(value.stream().filter(windowData -> windowData.getWindowContent().showInManager()).toList());
        });
        return openedWindows;
    }

    /**
     * Listen the Window creation event
     *
     * @param eventHandler event consumer
     * @return Registration for removing the listener
     */
    public Registration addWindowCreatedListener(Consumer<Window> eventHandler) {
        eventHandlers.add(eventHandler);
        return () -> eventHandlers.remove(eventHandler);
    }

    /**
     * Close all currently open windows and clean references
     */
    public void closeAllWindows() {
        List<WindowData> openedWindows = new ArrayList<>(getOpenedWindows());
        openedWindows.forEach(windowData -> {
            windowData.getInstance().close();
            Registration registration = windowListenerRegistrations.get(windowData.getInstance());
            if (registration != null) {
                registration.remove();
            }
        });
        windowListenerRegistrations.clear();
        beanToWindow.clear();
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
        return windowNameToContentAndClass.get(name).windowContent().showInMenu();
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
        WindowContentAndClass windowContentClassPair = windowNameToContentAndClass.get(name);
        if (windowContentClassPair != null) {
            RolesAllowed annotation = windowContentClassPair.windowContentClass().getAnnotation(RolesAllowed.class);
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
        WindowContentAndClass windowContentClassPair = windowNameToContentAndClass.get(name);
        if (windowContentClassPair != null) {
            PermitAll annotation = windowContentClassPair.windowContentClass().getAnnotation(PermitAll.class);
            return annotation != null;
        }
        return false;
    }

    /**
     * Does the currently authenticated user have access to the window of the given name
     *
     * @param windowName window name for which user's access right are evaluated against
     * @return true if user is allowed to access the window
     */
    public boolean isWindowAllowed(String windowName) {
        if (isPermitAll(windowName)) {
            return true;
        }
        List<String> rolesAllowed = rolesAllowed(windowName);
        if (!rolesAllowed.isEmpty()) {
            return authenticationContext.hasAnyRole(rolesAllowed);
        }
        return false;
    }
    
    public Optional<WindowData> getWindowDataForView(Component view) {
    	return Optional.ofNullable(viewToWindowData.get(view));
    }
}

package com.mycloth.core.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(
        adaptables = Resource.class,
        resourceType = MainNavigationImp.RESOURCE_TYPE,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class MainNavigationImp {

    static final String RESOURCE_TYPE = "mycloth/components/experience-fragment";

    @Self
    private Resource resource;

    @ValueMapValue
    private String category1Path;

    @ValueMapValue
    private Long category1Depth;

    @ValueMapValue
    private String category2Path;

    @ValueMapValue
    private Long category2Depth;

    @ValueMapValue
    private String category3Path;

    @ValueMapValue
    private Long category3Depth;


    private List<NavigationItem> items = Collections.emptyList();

    @PostConstruct
    protected void init() {
        PageManager pageManager = resource.getResourceResolver().adaptTo(PageManager.class);
        if (pageManager == null) {
            return;
        }

        List<NavigationItem> configuredItems = new ArrayList<>();
        addCategory(pageManager, configuredItems, category1Path, category1Depth);
        addCategory(pageManager, configuredItems, category2Path, category2Depth);
        addCategory(pageManager, configuredItems, category3Path, category3Depth);
        items = Collections.unmodifiableList(configuredItems);
    }

    private void addCategory(PageManager pageManager, List<NavigationItem> configuredItems, String path, Long depth) {
        if (path == null || path.isBlank()) {
            return;
        }

        Page rootPage = pageManager.getPage(path);
        if (rootPage != null) {
            long childDepth = depth == null ? 1 : Math.max(1, depth);
            configuredItems.add(createItem(rootPage, childDepth));
        }
    }

    private NavigationItem createItem(Page page, long remainingDepth) {
        List<NavigationItem> children = new ArrayList<>();
        if (remainingDepth > 0) {
            Iterator<Page> childPages = page.listChildren();
            while (childPages.hasNext()) {
                children.add(createItem(childPages.next(), remainingDepth - 1));
            }
        }

        String title = page.getNavigationTitle();
        if (title == null || title.isBlank()) {
            title = page.getPageTitle();
        }
        if (title == null || title.isBlank()) {
            title = page.getTitle();
        }
        if (title == null || title.isBlank()) {
            title = page.getName();
        }

        return new NavigationItem(title, page.getPath(), Collections.unmodifiableList(children));
    }

    public List<NavigationItem> getItems() {
        return items;
    }

    public static class NavigationItem {
        private final String title;
        private final String path;
        private final List<NavigationItem> children;

        NavigationItem(String title, String path, List<NavigationItem> children) {
            this.title = title;
            this.path = path;
            this.children = children;
        }

        public String getTitle() {
            return title;
        }

        public String getPath() {
            return path;
        }

        public List<NavigationItem> getChildren() {
            return children;
        }
    }
}

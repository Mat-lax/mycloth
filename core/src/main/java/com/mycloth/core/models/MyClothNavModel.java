package com.mycloth.core.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct; // use jakarta.annotation.PostConstruct on AEM as a Cloud Service

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

@Model(
    adaptables = Resource.class,
    resourceType = "mycloth/components/myclothnav",
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class MyClothNavModel {

    private static final Logger LOG = LoggerFactory.getLogger(MyClothNavModel.class);

    private static final String ALLOWED_ROOT_PREFIX = "/content/mycloth/";
    private static final String HTML_EXTENSION = ".html";

    // Levels of child pages shown in each dropdown. 1 = direct children only.
    private static final int DROPDOWN_DEPTH = 1;

    @SlingObject
    private ResourceResolver resourceResolver;

    @ChildResource(name = "navigationItems")
    private Resource navigationItemsResource;

    private List<NavItem> navigationItems;

    @PostConstruct
    protected void init() {
        try {
            navigationItems = buildNavigation();
        } catch (Exception e) {
            LOG.error("Failed to build MyCloth navigation", e);
            navigationItems = Collections.emptyList();
        }
    }

    public List<NavItem> getNavigationItems() {
        return navigationItems;
    }

    private List<NavItem> buildNavigation() {
        if (navigationItemsResource == null) {
            return Collections.emptyList();
        }

        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        if (pageManager == null) {
            return Collections.emptyList();
        }

        List<NavItem> items = new ArrayList<>();
        for (Resource child : navigationItemsResource.getChildren()) {
            MyClothNavItemModel authored = child.adaptTo(MyClothNavItemModel.class);
            if (authored == null) {
                continue;
            }
            NavItem item = toTopLevelItem(authored, pageManager);
            if (item != null) {
                items.add(item);
            }
        }
        return Collections.unmodifiableList(items);
    }

    private NavItem toTopLevelItem(MyClothNavItemModel authored, PageManager pageManager) {
        String path = authored.getPagePath();
        if (!isAllowedPath(path)) {
            return null; // skip invalid or out-of-site entries
        }

        Page page = pageManager.getPage(path);
        if (page == null) {
            return null;
        }
        List<NavItem> children = authored.isShowChildPages()
                ? buildChildren(page, 1)
                : Collections.emptyList();

        String title = authored.getTitle() != null ? authored.getTitle() : titleOf(page);
        return new NavItem(title, mapUrl(path), children);
    }

    private List<NavItem> buildChildren(Page parent, int depth) { 
        if (depth > DROPDOWN_DEPTH) {
            return Collections.emptyList();
        }

        List<NavItem> items = new ArrayList<>();
        Iterator<Page> children = parent.listChildren(); // direct children only

        while (children.hasNext()) {
            Page child = children.next();
            if (isHidden(child)) {
                continue;
            }
            items.add(new NavItem(
                titleOf(child),
                mapUrl(child.getPath()),
                buildChildren(child, depth + 1)
            ));
        }
        return Collections.unmodifiableList(items);
    }

    private boolean isAllowedPath(String path) {
        return path != null
            && !path.contains("..")
            && path.startsWith(ALLOWED_ROOT_PREFIX);
    }

    private boolean isHidden(Page page) {
        return page.getProperties().get("hideInNav", false);
    }

    private String titleOf(Page page) {
        return page.getTitle() != null ? page.getTitle() : page.getName();
    }

    private String mapUrl(String path) {
        return resourceResolver.map(path + HTML_EXTENSION);
    }

    /**
     * One menu entry. Nested so the nav lives in a single file.
     */
    public static class NavItem {

        private final String title;
        private final String url;
        private final List<NavItem> children;

        public NavItem(String title, String url, List<NavItem> children) {
            this.title = title;
            this.url = url;
            this.children = children;
        }

        public String getTitle() {
            return title;
        }

        public String getUrl() {
            return url;
        }

        public List<NavItem> getChildren() {
            return children;
        }
    }
}
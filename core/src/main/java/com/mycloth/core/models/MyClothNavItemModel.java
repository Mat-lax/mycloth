package com.mycloth.core.models;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Model(
    adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class MyClothNavItemModel {

    private static final String ALLOWED_ROOT = "/content/mycloth";
    private static final String ALLOWED_ROOT_PREFIX = ALLOWED_ROOT + "/";
    private static final int MAX_CHILD_PAGES_DEPTH = 5;
    private static final String INVALID_LINK = "#";
    private static final String HTML_EXTENSION = ".html";

    @ValueMapValue
    private String title;

    @ValueMapValue
    private String pagePath;

    @ValueMapValue
    private int childPagesDepth;

    @SlingObject
    private ResourceResolver resolver;

    public String getTitle() {
        return title;
    }

    public String getPagePath() {
        if (!isAllowedPath(pagePath)) {
            return INVALID_LINK;
        }
        return pagePath + HTML_EXTENSION;
    }

    public int getChildPagesDepth() {
        return childPagesDepth;
    }

    public List<Page> getChildPages() {
        if (resolver == null
                || !isAllowedPath(pagePath)
                || childPagesDepth <= 0
                || childPagesDepth > MAX_CHILD_PAGES_DEPTH) {
            return Collections.emptyList();
        }

        PageManager pageManager = resolver.adaptTo(PageManager.class);
        if (pageManager == null) {
            return Collections.emptyList();
        }

        Page parentPage = pageManager.getPage(pagePath);
        if (parentPage == null) {
            return Collections.emptyList();
        }

        List<Page> childPages = new ArrayList<>();
        collectChildPages(parentPage, childPagesDepth, childPages);
        return childPages;
    }

    /**
     * Returns true only when the path is non-null, lies under the allowed root,
     * and contains no ".." segments that could escape it.
     */
    private boolean isAllowedPath(String path) {
        if (path == null || path.contains("..")) {
            return false;
        }
        return path.equals(ALLOWED_ROOT) || path.startsWith(ALLOWED_ROOT_PREFIX);
    }

    /**
     * Appends the descendants of {@code parentPage} down to {@code childDepth} levels
     * to {@code collected}.
     */
    private void collectChildPages(Page parentPage, int childDepth, List<Page> collected) {
        if (childDepth <= 0) {
            return;
        }

        Iterator<Page> children = parentPage.listChildren();
        while (children.hasNext()) {
            Page child = children.next();
            collected.add(child);
            collectChildPages(child, childDepth - 1, collected);
        }
    }
}
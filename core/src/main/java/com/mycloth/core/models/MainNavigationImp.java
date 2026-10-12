
package com.mycloth.core.models;

import java.util.ArrayList;
import java.util.Collections;
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
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class MainNavigationImp {

    static final String RESOURCE_TYPE =
        "mycloth/components/experience-fragment";

    private static final String ALLOWED_ROOT = "/content/mycloth";
    private static final int DEFAULT_DEPTH = 1;
    private static final int MAX_DEPTH = 5;

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
        PageManager pageManager =
            resource.getResourceResolver().adaptTo(PageManager.class);

        if (pageManager == null) {
            return;
        }

        List<NavigationItem> configuredItems = new ArrayList<>();

        addCategory(
            pageManager, configuredItems,
            "category1", category1Path, category1Depth
        );

        addCategory(
            pageManager, configuredItems,
            "category2", category2Path, category2Depth
        );

        addCategory(
            pageManager, configuredItems,
            "category3", category3Path, category3Depth
        );

        items = Collections.unmodifiableList(configuredItems);
    }

    private void addCategory(
            PageManager pageManager,
            List<NavigationItem> configuredItems,
            String id,
            String path,
            Long depth) {

        if (path == null || path.trim().isEmpty()) {
            return;
        }

        // Allow only pages under the site's content root.
        if (!path.equals(ALLOWED_ROOT)
                && !path.startsWith(ALLOWED_ROOT + "/")) {
            return;
        }

        Page parentPage = pageManager.getPage(path);

        if (parentPage == null) {
            return;
        }

        long configuredDepth =
            depth == null ? DEFAULT_DEPTH : depth.longValue();

        // Keep depth within a safe range.
        if (configuredDepth < 1 || configuredDepth > MAX_DEPTH) {
            configuredDepth = DEFAULT_DEPTH;
        }

        String title = getPageTitle(parentPage);

        configuredItems.add(
            new NavigationItem(
                id,
                title,
                parentPage.getPath(),
                configuredDepth
            )
        );
    }

    private String getPageTitle(Page page) {
        String title = page.getNavigationTitle();

        if (title == null || title.trim().isEmpty()) {
            title = page.getPageTitle();
        }

        if (title == null || title.trim().isEmpty()) {
            title = page.getTitle();
        }

        if (title == null || title.trim().isEmpty()) {
            title = page.getName();
        }

        return title;
    }

    public List<NavigationItem> getItems() {
        return items;
    }

    public static class NavigationItem {

        private final String id;
        private final String title;
        private final String path;
        private final long depth;

        public NavigationItem(
                String id,
                String title,
                String path,
                long depth) {

            this.id = id;
            this.title = title;
            this.path = path;
            this.depth = depth;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getPath() {
            return path;
        }

        public long getDepth() {
            return depth;
        }
    }
}

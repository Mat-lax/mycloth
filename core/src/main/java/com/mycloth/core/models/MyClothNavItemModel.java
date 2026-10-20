package com.mycloth.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(
    adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class MyClothNavItemModel {

    @ValueMapValue
    private String title;

    @ValueMapValue
    private String pagePath;

    @ValueMapValue
    private boolean showChildPages;


    public String getTitle() {
        return title;
    }

    public String getPagePath() {
        return pagePath;
    }

    public boolean isShowChildPages() {
    return showChildPages;
}
}
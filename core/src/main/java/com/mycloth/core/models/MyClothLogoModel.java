package com.mycloth.core.models;

import com.mycloth.core.models.MyClothLogoModel;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(
    adaptables = Resource.class,
    adapters = MyClothLogoModel.class,
    resourceType = MyClothLogoModel.RESOURCE_TYPE,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class MyClothLogoModel {

    private static final String HOME_LINK = "/content/mycloth/us/en.html";
    public static final String RESOURCE_TYPE = "mycloth/components/myclothlogo";

    @ValueMapValue
    private String text;

    @ValueMapValue
    private String link;

    public String getText() {
        return text == null || text.isBlank() ? "MyCloth" : text;
    }

    public String getLink() {
        String target = link == null || link.isBlank() ? HOME_LINK : link;
        return target.startsWith("/content/") && !target.endsWith(".html")
                ? target + ".html"
                : target;
    }
}

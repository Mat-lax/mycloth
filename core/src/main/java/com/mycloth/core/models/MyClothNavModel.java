
package com.mycloth.core.models;

import java.util.Collections;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;


@Model(
    adaptables = Resource.class,
    resourceType = "mycloth/components/myclothnav",
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class MyClothNavModel {

    @ChildResource(name = "navigationItems")
    private List <MyClothNavItemModel> navigationItems;

    public List<MyClothNavItemModel> getNavigationItems() {
        return navigationItems != null
            ? navigationItems
            : Collections.emptyList();
    }
}

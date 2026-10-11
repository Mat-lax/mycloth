package com.mycloth.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class PolicyMultiField {

    @ValueMapValue(name = "subtitle")
    private String subtitle;

    @ValueMapValue(name = "text")
    private String text;

    public String getSubtitle() {
        return subtitle;
    }

    public String getText() {
        return text;
    }
    
}

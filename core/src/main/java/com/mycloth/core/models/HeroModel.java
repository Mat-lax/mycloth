package com.mycloth.core.models;

import org.apache.sling.models.annotations.Model;

import org.apache.sling.models.annotations.DefaultInjectionStrategy;

import org.apache.sling.api.resource.Resource;

import org.apache.sling. models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class HeroModel {

    @ValueMapValue
    protected String headline;

    @ValueMapValue
    protected String subheadline;

    @ValueMapValue  
    protected String heroImage;

    @ValueMapValue
    protected String eyecatcher;

    public String getHeadline() {
        return headline;
    }

    public String getSubheadline(){
        return subheadline; 
    }
    public String getHeroImage(){
        return heroImage; 
    }
    public String getEyecatcher(){
        return eyecatcher; 
    }

}

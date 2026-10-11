package com.mycloth.core.models;

import javax.annotation.PostConstruct;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;


@Model(adaptables = Resource.class, resourceType = LegalPolicyModel.RESOURCE_TYPE, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class LegalPolicyModel {

    private static final Logger LOG = LoggerFactory.getLogger(LegalPolicyModel.class);

    protected static final String RESOURCE_TYPE = "mycloth/components/legal-policy";

    @Self
    private Resource currentResource;

    @ValueMapValue(name = "privacyCfLink")
    private String privacyCfLink;

    @ValueMapValue(name = "legalCfLink")
    private String legalCfLink;

    @ChildResource(name = "privacySections")
    private List<PolicyMultiField> privacySections;

    @ChildResource(name = "legalSections")
    private List<PolicyMultiField> legalSections;



    private String privacyTitle;
    private String privacyDescription;
    private String privacyRolloutDate;
    private String legalTitle;
    private String legalDescription;
    private String legalRolloutDate;

    @PostConstruct
    protected void init() {
        if (currentResource == null) {
            LOG.warn("Legal policy component resource is not available.");
            return;
        }

        ContentFragment privacyFragment = getContentFragment(privacyCfLink, "Privacy");
        privacyTitle = getElementContent(privacyFragment, privacyCfLink, "title");
        privacyDescription = getElementContent(privacyFragment, privacyCfLink, "description");
        privacyRolloutDate = formatRolloutDate(getElementContent(privacyFragment, privacyCfLink, "rolloutDate"));

        ContentFragment legalFragment = getContentFragment(legalCfLink, "Legal");
        legalTitle = getElementContent(legalFragment, legalCfLink, "title");
        legalDescription = getElementContent(legalFragment, legalCfLink, "description");
        legalRolloutDate = formatRolloutDate(getElementContent(legalFragment, legalCfLink, "rolloutDate"));
    }

    //fragmentPath is the path to the content fragment, section is either "Privacy" or "Legal"
    private ContentFragment getContentFragment(String fragmentPath, String section) {
        if (fragmentPath == null || fragmentPath.isEmpty()) {
            LOG.warn("{} Content Fragment path is not configured.", section);
            return null;
        }

        Resource cfResource = currentResource.getResourceResolver().getResource(fragmentPath);
        if (cfResource == null) {
            LOG.warn("{} Content Fragment resource was not found at {}.", section, fragmentPath);
            return null;
        }

        ContentFragment contentFragment = cfResource.adaptTo(ContentFragment.class);
        if (contentFragment == null) {
            LOG.warn("Resource at {} could not be adapted to a Content Fragment.", fragmentPath);
        }
        return contentFragment;
    }

    //fragmentPath is the path to the content fragment, contentFragment is the content fragment, elementName is the name of the element to retrieve
    private String getElementContent(ContentFragment contentFragment, String fragmentPath, String elementName) {
        if (contentFragment == null) {
            return null;
        }

        ContentElement element = contentFragment.getElement(elementName);
        if (element == null) {
            Iterator<ContentElement> elements = contentFragment.getElements();
            while (elements.hasNext()) {
                ContentElement candidate = elements.next();
                if (elementName.equalsIgnoreCase(candidate.getName())) {
                    element = candidate;
                    break;
                }
            }
        }
        if (element == null) {
            LOG.warn("Content Fragment at {} does not contain an element named '{}'.",
                    fragmentPath, elementName);
            return null;
        }
        return element.getContent();
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, uuuu");

    private String formatRolloutDate(String rolloutDate) {
        if (rolloutDate == null || rolloutDate.isBlank()) {
            return null;
        }
        try {
            LocalDate date;
            try {
                date = DateTimeFormatter.ISO_DATE_TIME.parse(rolloutDate, LocalDate::from);
            } catch (DateTimeParseException e) {
                date = LocalDate.parse(rolloutDate, DateTimeFormatter.ISO_LOCAL_DATE);
            }
            return DATE_FORMATTER.format(date);
        } catch (DateTimeParseException e) {
            LOG.warn("Could not parse rollout date '{}'; rendering the original value.", rolloutDate, e);
            return rolloutDate;
        }
    }


    public String getPrivacyTitle() {
        return privacyTitle;
    }

    public String getPrivacyDescription() {
        return privacyDescription;
    }

    public String getPrivacyRolloutDate() {
        return privacyRolloutDate;
    }

    public String getLegalTitle() {
        return legalTitle;
    }

    public String getLegalDescription() {
        return legalDescription;
    }

    public String getLegalRolloutDate() {
        return legalRolloutDate;
    }

    public List<PolicyMultiField> getPrivacySections() {
        return privacySections;
    }

    public List<PolicyMultiField> getLegalSections() {
        return legalSections;
    }
}

package com.mycloth.core.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = "mycloth/components/myclothsuggestions",
        methods = "GET",
        selectors = "suggestion",
        extensions = "json"
)
public class MyClothSuggestionServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;

    private static final String ALLOWED_ROOT = "/content/mycloth";

    @Reference
    private QueryBuilder queryBuilder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(
            final SlingHttpServletRequest request,
            final SlingHttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (queryBuilder == null) {
            response.sendError(
                    SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "QueryBuilder service is not available");
            return;
        }

        Resource componentResource = request.getResource();
        ResourceResolver resolver = request.getResourceResolver();

        PageManager pageManager = resolver.adaptTo(PageManager.class);

        if (pageManager == null) {
            response.sendError(
                    SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to obtain PageManager");
            return;
        }

        Page currentPage = pageManager.getContainingPage(componentResource);

        if (currentPage == null) {
            response.sendError(
                    SlingHttpServletResponse.SC_BAD_REQUEST,
                    "Unable to resolve the current page");
            return;
        }

        String currentPagePath = currentPage.getPath();

        if (!(currentPagePath.equals(ALLOWED_ROOT)
                || currentPagePath.startsWith(ALLOWED_ROOT + "/"))) {

            response.sendError(
                    SlingHttpServletResponse.SC_FORBIDDEN,
                    "Page is outside the allowed content root");
            return;
        }

        Session session = resolver.adaptTo(Session.class);

        if (session == null) {
            response.sendError(
                    SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to obtain JCR Session");
            return;
        }

        Map<String, String> predicates = new HashMap<>();
        predicates.put("path", currentPagePath);
        predicates.put("path.flat", "true");
        predicates.put("type", "cq:Page");
        predicates.put("p.limit", "-1");

        Query query = queryBuilder.createQuery(
                PredicateGroup.create(predicates),
                session);

        SearchResult searchResult = query.getResult();

        ArrayNode items = objectMapper.createArrayNode();

        for (Hit hit : searchResult.getHits()) {

            String pagePath;

            try {
                pagePath = hit.getPath();
            } catch (RepositoryException e) {
                continue;
            }

            Page childPage = pageManager.getPage(pagePath);

            if (childPage == null) {
                continue;
            }

            // Skip the current page itself if the query ever returns it
            if (childPage.getPath().equals(currentPagePath)) {
                continue;
            }

            Resource contentResource = childPage.getContentResource();

            if (contentResource != null
                    && contentResource.getValueMap()
                            .get("hideInNav", false)) {
                continue;
            }

            String title = childPage.getNavigationTitle();

            if (title == null || title.trim().isEmpty()) {
                title = childPage.getTitle();
            }

            if (title == null || title.trim().isEmpty()) {
                title = childPage.getName();
            }

            ObjectNode item = objectMapper.createObjectNode();
            item.put("title", title);
            item.put("url", pagePath + ".html");

            items.add(item);
        }

        ObjectNode jsonResponse = objectMapper.createObjectNode();
        jsonResponse.set("items", items);

        objectMapper.writeValue(response.getWriter(), jsonResponse);
    }
}
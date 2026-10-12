package com.mycloth.core.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.framework.Constants;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.gson.Gson;
import java.util.Iterator;

@Component(service = { Servlet.class }, property = {
    Constants.SERVICE_DESCRIPTION + "=Experience Fragment Navigation Servlet",
    "sling.servlet.paths=" + "/bin/mycloth/navigation",
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.extensions=" + "json"
})
@ServiceDescription("Experience Fragment Navigation Servlet")
public class ExperienceFragmentServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;
    private static final String ALLOWED_ROOT = "/content/mycloth";

    private final transient Gson gson = new Gson();

    @Override
    protected void doGet(
            SlingHttpServletRequest request,
            SlingHttpServletResponse response)
            throws ServletException, IOException {

        org.slf4j.LoggerFactory.getLogger(ExperienceFragmentServlet.class)
        .error("INCOMING CATEGORY PATH: '{}'", request.getParameter("categoryPath"));

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> result = new LinkedHashMap<>();

        String categoryPath = request.getParameter("categoryPath");
        String depthValue = request.getParameter("depth");

        // Add this log to check what your JS is actually sending
        org.slf4j.LoggerFactory.getLogger(ExperienceFragmentServlet.class)
            .info("Received categoryPath: '{}', depth: '{}'", categoryPath, depthValue);

        if (categoryPath == null || categoryPath.trim().isEmpty()) {
            response.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
            result.put("status", "error");
            result.put("message", "categoryPath is required. Received value was: " + categoryPath);
            response.getWriter().write(gson.toJson(result));
            return;
        }

        if (!categoryPath.equals(ALLOWED_ROOT)
                && !categoryPath.startsWith(ALLOWED_ROOT + "/")) {
            response.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
            result.put("status", "error");
            result.put("message", "Invalid category path.");
            response.getWriter().write(gson.toJson(result));
            return;
        }

        int depth = 1;

        try {
            if (depthValue != null) {
                depth = Integer.parseInt(depthValue);
            }
        } catch (NumberFormatException exception) {
            response.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
            result.put("status", "error");
            result.put("message", "Depth must be a number.");
            response.getWriter().write(gson.toJson(result));
            return;
        }

        if (depth < 1 || depth > 5) {
            response.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
            result.put("status", "error");
            result.put("message", "Depth must be between 1 and 5.");
            response.getWriter().write(gson.toJson(result));
            return;
        }

        ResourceResolver resolver = request.getResourceResolver();
        PageManager pageManager = resolver.adaptTo(PageManager.class);

        if (pageManager == null) {
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("status", "error");
            result.put("message", "PageManager is unavailable.");
            response.getWriter().write(gson.toJson(result));
            return;
        }

        Page parentPage = pageManager.getPage(categoryPath);

        if (parentPage == null) {
            Resource resource = resolver.getResource(categoryPath);
            if (resource != null) {
                parentPage = pageManager.getContainingPage(resource);
            }
        }

        if (parentPage == null) {
            response.setStatus(SlingHttpServletResponse.SC_NOT_FOUND);
            result.put("status", "error");
            result.put("message", "No page found for categoryPath.");
            response.getWriter().write(gson.toJson(result));
            return;
        }

        result.put("status", "success");
        result.put("categoryPath", parentPage.getPath());
        result.put("depth", depth);
        result.put("items", getChildPages(parentPage, depth));

        response.getWriter().write(gson.toJson(result));
    }

    private List<Map<String, Object>> getChildPages(
            Page parentPage, int remainingDepth) {

        List<Map<String, Object>> children = new ArrayList<>();

        if (remainingDepth <= 0) {
            return children;
        }

        Iterator<Page> iterator = parentPage.listChildren();

        while (iterator.hasNext()) {
            Page childPage = iterator.next();

            Map<String, Object> item = new LinkedHashMap<>();
            String title = childPage.getNavigationTitle();

            if (title == null || title.trim().isEmpty()) {
                title = childPage.getPageTitle();
            }
            if (title == null || title.trim().isEmpty()) {
                title = childPage.getTitle();
            }
            if (title == null || title.trim().isEmpty()) {
                title = childPage.getName();
            }

            item.put("title", title);
            item.put("path", childPage.getPath());
            item.put("children", getChildPages(childPage, remainingDepth - 1));

            children.add(item);
        }

        return children;
    }
}
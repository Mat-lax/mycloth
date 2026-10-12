(function () {
    "use strict";

    console.log("[MyCloth Nav] JavaScript loaded");

    document.querySelectorAll(".cmp-nav").forEach(function (nav) {
        var servletUrl = nav.dataset.servletUrl;
        var toggle = nav.querySelector(".cmp-nav__toggle");
        var cache = new Map();

        console.log("[MyCloth Nav] Servlet URL:", servletUrl);

        if (!servletUrl) {
            console.error("[MyCloth Nav] data-servlet-url is missing");
            return;
        }

        function renderItems(items, list) {
            list.replaceChildren();

            if (!items.length) {
                var empty = document.createElement("li");
                empty.textContent = "No child pages found";
                empty.className = "cmp-nav__empty";
                list.appendChild(empty);
                return;
            }

            items.forEach(function (page) {
                if (typeof page.path !== "string" ||
                    !page.path.startsWith("/content/mycloth/")) {
                    console.warn("Skipping invalid page path:", page.path);
                    return;
                }

                var title = typeof page.title === "string"
                    ? page.title.trim()
                    : "";
                var pageName = page.path.split("/").pop();
                var li = document.createElement("li");
                li.className = "cmp-nav__child-item";

                var link = document.createElement("a");
                link.className = "cmp-nav__child-link";
                link.textContent = title || pageName || page.path;
                link.href = page.path + ".html";
                li.appendChild(link);

                if (Array.isArray(page.children) &&
                    page.children.length > 0) {
                    var nested = document.createElement("ul");
                    nested.className = "cmp-nav__nested-list";

                    renderItems(page.children, nested);
                    li.appendChild(nested);
                }

                list.appendChild(li);
            });
        }

        function loadCategory(item) {
            var path = item.dataset.categoryPath;
            var depth = item.dataset.categoryDepth || "1";
            var dropdown = item.querySelector(".cmp-nav__dropdown");
            var list = item.querySelector(".cmp-nav__dropdown-list");

            console.log("[MyCloth Nav] Loading category:", path);

            if (!path || !dropdown || !list) {
                console.error("[MyCloth Nav] Missing path or dropdown elements", item);
                return Promise.resolve();
            }

            var key = path + "|" + depth;

            if (cache.has(key)) {
                console.log("[MyCloth Nav] Using cached pages");
                renderItems(cache.get(key), list);
                return Promise.resolve();
            }

            // Append selector and extension to hit the resource type servlet correctly
            var url = new URL("/bin/mycloth/navigation.json", window.location.origin);
            url.searchParams.set("categoryPath", path);
            url.searchParams.set("depth", depth);

            console.log("[MyCloth Nav] Sending request:", url.toString());

            return fetch(url.toString(), {
                method: "GET",
                headers: {
                    "Accept": "application/json"
                },
                credentials: "same-origin"
            })
                .then(function (response) {
                    console.log("[MyCloth Nav] HTTP status:", response.status);

                    if (!response.ok) {
                        throw new Error("Servlet returned HTTP " + response.status);
                    }

                    return response.json();
                })
                .then(function (data) {
                    console.log("[MyCloth Nav] JSON response:", data);

                    if (data.status !== "success" || !Array.isArray(data.items)) {
                        throw new Error(data.message || "Unexpected servlet response");
                    }

                    cache.set(key, data.items);
                    renderItems(data.items, list);
                })
                .catch(function (error) {
                    console.error("[MyCloth Nav] Dropdown loading failed:", error);

                    list.replaceChildren();

                    var errorItem = document.createElement("li");
                    errorItem.textContent = "Unable to load pages";
                    errorItem.className = "cmp-nav__empty";
                    list.appendChild(errorItem);
                });
        }

        function openDropdown(item) {
            var dropdown = item.querySelector(".cmp-nav__dropdown");
            var link = item.querySelector(".cmp-nav__link");

            if (!dropdown || !link) {
                return;
            }

            nav.querySelectorAll(".cmp-nav__item").forEach(function (other) {
                if (other !== item) {
                    other.classList.remove("is-expanded");
                    var otherDropdown = other.querySelector(".cmp-nav__dropdown");
                    var otherLink = other.querySelector(".cmp-nav__link");

                    if (otherDropdown) { otherDropdown.hidden = true; }
                    if (otherLink) { otherLink.setAttribute("aria-expanded", "false"); }
                }
            });

            item.classList.add("is-expanded");
            dropdown.hidden = false;
            link.setAttribute("aria-expanded", "true");

            return loadCategory(item);
        }

        function closeDropdown(item) {
            item.classList.remove("is-expanded");
            var dropdown = item.querySelector(".cmp-nav__dropdown");
            var link = item.querySelector(".cmp-nav__link");

            if (dropdown) { dropdown.hidden = true; }
            if (link) { link.setAttribute("aria-expanded", "false"); }
        }

        nav.querySelectorAll(".cmp-nav__item").forEach(function (item) {
            item.addEventListener("mouseenter", function () { openDropdown(item); });
            item.addEventListener("mouseleave", function () { closeDropdown(item); });
            item.addEventListener("focusin", function () { openDropdown(item); });
        });

        if (toggle) {
            toggle.addEventListener("click", function () {
                var isOpen = toggle.getAttribute("aria-expanded") !== "true";
                toggle.setAttribute("aria-expanded", String(isOpen));
                nav.classList.toggle("is-open", isOpen);
            });
        }

        nav.addEventListener("keydown", function (event) {
            if (event.key === "Escape") {
                nav.querySelectorAll(".cmp-nav__item").forEach(closeDropdown);
                if (toggle) {
                    toggle.setAttribute("aria-expanded", "false");
                    nav.classList.remove("is-open");
                    toggle.focus();
                }
            }
        });
    });
})();
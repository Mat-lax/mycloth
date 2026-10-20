(function () {
    "use strict";

    function initializeSuggestions() {
        const components = document.querySelectorAll("[data-suggestion]");

        components.forEach((component) => {
            const list = component.querySelector("[data-suggestion-list]");
            const servletUrl = component.dataset.servletUrl;

            if (!list || !servletUrl) {
                return;
            }

            list.textContent = "Loading pages...";

            fetch(servletUrl, {
                method: "GET",
                headers: {
                    "Accept": "application/json"
                }
            })
                .then((response) => {
                    if (!response.ok) {
                        throw new Error("Request failed: " + response.status);
                    }
                    return response.json();
                })
                .then((data) => {
                    list.replaceChildren();

                    if (!Array.isArray(data.items) || data.items.length === 0) {
                        list.textContent = "No child pages found.";
                        return;
                    }

                    data.items.forEach((item) => {
                        const li = document.createElement("li");
                        const link = document.createElement("a");

                        link.textContent = item.title;
                        link.href = item.url;

                        li.appendChild(link);
                        list.appendChild(li);
                    });
                })
                .catch((error) => {
                    console.error("Unable to load child pages:", error);
                    list.textContent = "Unable to load pages.";
                });
        });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initializeSuggestions);
    } else {
        initializeSuggestions();
    }
})();

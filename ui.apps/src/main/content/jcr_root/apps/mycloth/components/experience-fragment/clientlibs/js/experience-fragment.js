(function () {
    "use strict";

    document.querySelectorAll(".cmp-nav").forEach(function (nav) {
        var toggle = nav.querySelector(".cmp-nav__toggle");

        if (!toggle) {
            return;
        }

        function setMenuOpen(isOpen) {
            nav.classList.toggle("is-open", isOpen);
            toggle.setAttribute("aria-expanded", String(isOpen));
        }

        toggle.addEventListener("click", function () {
            setMenuOpen(toggle.getAttribute("aria-expanded") !== "true");
        });

        nav.addEventListener("click", function (event) {
            if (event.target.closest("a")) {
                setMenuOpen(false);
            }
        });

        nav.addEventListener("keydown", function (event) {
            if (event.key === "Escape") {
                setMenuOpen(false);
                toggle.focus();
            }
        });
    });
})();

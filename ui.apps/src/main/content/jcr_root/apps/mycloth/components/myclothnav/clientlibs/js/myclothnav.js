(function () {
    'use strict';

    var nav = document.querySelector('.mycloth-menu_nav');
    if (!nav) {
        return;
    }

    var menuToggle = nav.querySelector('.mycloth-menu__toggle');
    var arrows = nav.querySelectorAll('.cmp-mycloth-nav__arrow');
    var desktopQuery = window.matchMedia('(min-width: 768px)');

    function setMenuOpen(open) {
        nav.classList.toggle('is-open', open);
        menuToggle.setAttribute('aria-expanded', String(open));
    }

    function setItemExpanded(arrow, expanded) {
        var item = arrow.closest('.cmp-mycloth-nav__item');
        item.classList.toggle('is-expanded', expanded);
        arrow.setAttribute('aria-expanded', String(expanded));
        arrow.querySelector('span').textContent = expanded ? '\u2212' : '+';
    }

    // Hamburger toggles the mobile panel
    menuToggle.addEventListener('click', function () {
        setMenuOpen(!nav.classList.contains('is-open'));
    });

    // Arrows toggle child dropdowns
    arrows.forEach(function (arrow) {
        arrow.addEventListener('click', function () {
            var item = arrow.closest('.cmp-mycloth-nav__item');
            setItemExpanded(arrow, !item.classList.contains('is-expanded'));
        });
    });

    // Escape closes the panel
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') {
            setMenuOpen(false);
        }
    });

    // Returning to desktop resets the mobile state
    desktopQuery.addEventListener('change', function (e) {
        if (e.matches) {
            setMenuOpen(false);
        }
    });
})();
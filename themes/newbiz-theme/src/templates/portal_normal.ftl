<!DOCTYPE html>

<#include init />

<html class="${root_css_class}" dir="<@liferay.language key="lang.dir" />" lang="${w3c_language_id}">

<head>
    <title>${the_title} - ${company_name}</title>

    <meta content="initial-scale=1.0, width=device-width" name="viewport"/>

    <link href="https://fonts.googleapis.com/css?family=Poppins:100,300,400,500,600" rel="stylesheet">

    <!-- Libraries CSS Files -->
    <link href="${lib_folder}/animate/animate.min.css" rel="stylesheet">
    <link href="${lib_folder}/ionicons/css/ionicons.min.css" rel="stylesheet">
    <link href="${lib_folder}/owlcarousel/assets/owl.carousel.min.css" rel="stylesheet">
    <link href="${lib_folder}/lightbox/css/lightbox.min.css" rel="stylesheet">

    <@liferay_util["include"] page=top_head_include />
</head>

<body class="${css_class}">

<@liferay_ui["quick-access"] contentId="#main-content" />

<@liferay_util["include"] page=body_top_include />

<@liferay.control_menu />

<div id="wrapper">
    <header id="header" class="affix" role="banner">
        <#if has_navigation && is_setup_complete>
            <div class="container">
                <div class="row">
                    <div class="col-2">
                        <a href="${site_default_url}" title="<@liferay.language_format arguments="${site_name}"
                        key="go-to-x" />">
                            <img class="site-logo" alt="${logo_description}" height="${site_logo_height}"
                                 src="${site_logo}"/>
                        </a>
                    </div>
                    <div id="lang-selector-wrapper" class="col-3">
                        <@liferay.languages />
                    </div>
                    <div id="nav-menu-wrapper" class="col">
                        <div class="main-nav float-right d-none d-lg-block">
                            <@liferay.navigation_menu
                            instance_id="top_navigation_menu"
                            />
                        </div>
                    </div>
                    <div id="loginfo-wrapper" class="col-2">
                        <#if !is_signed_in>
                            <div class="sign-in d-inline-block g-hidden-xs-down g-pos-rel g-valign-middle g-pl-30 g-pl-0--lg">
                                <a class="btn btn-outline-primary"
                                   data-redirect="${is_login_redirect_required?string}" href="${sign_in_url}"
                                   id="sign-in"
                                   rel="nofollow">${sign_in_text}</a>
                            </div>
                        <#else>
                            <div class="user-personal-bar">
                                <@liferay.user_personal_bar />
                            </div>
                        </#if>
                    </div>
                </div>
            </div>
        </#if>
    </header>

    <section id="content" class="container-fluid">
        <h1 class="hide-accessible">${the_title}</h1>

        <#if selectable>
            <@liferay_util["include"] page=content_include />
        <#else>
            ${portletDisplay.recycle()}

            ${portletDisplay.setTitle(the_title)}

            <@liferay_theme["wrap-portlet"] page="portlet.ftl">
                <@liferay_util["include"] page=content_include />
            </@>
        </#if>

    </section>

    <#include "${full_templates_path}/footer.ftl" />

    <a href="#" class="back-to-top">
        <svg aria-hidden="true" class="lexicon-icon lexicon-icon-caret-top">
            <use xlink:href="${images_folder}/lexicon/icons.svg#caret-top"></use>
        </svg>
    </a>

</div>

<@liferay_util["include"] page=body_bottom_include />

<@liferay_util["include"] page=bottom_include />

<!-- JavaScript Libraries -->
<script src="${lib_folder}/easing/easing.min.js"></script>
<script src="${lib_folder}/mobile-nav/mobile-nav.js"></script>
<script src="${lib_folder}/wow/wow.min.js"></script>
<script src="${lib_folder}/waypoints/waypoints.min.js"></script>
<script src="${lib_folder}/counterup/counterup.min.js"></script>
<script src="${lib_folder}/owlcarousel/owl.carousel.min.js"></script>
<script src="${lib_folder}/isotope/isotope.pkgd.min.js"></script>

<!-- inject:js -->
<!-- endinject -->

</body>

</html>

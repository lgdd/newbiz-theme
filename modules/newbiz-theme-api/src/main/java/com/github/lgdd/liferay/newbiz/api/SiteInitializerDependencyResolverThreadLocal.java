package com.github.lgdd.liferay.newbiz.api;

import com.liferay.petra.lang.CentralizedThreadLocal;

public class SiteInitializerDependencyResolverThreadLocal {

	public static SiteInitializerDependencyResolver
	getSiteInitializerDependencyResolver() {

		return _siteInitializerDependencyResolver.get();
	}

	public static void setSiteInitializerDependencyResolver(
									SiteInitializerDependencyResolver siteInitializerDependencyResolver) {

		_siteInitializerDependencyResolver.set(
										siteInitializerDependencyResolver);
	}

	private static final ThreadLocal<SiteInitializerDependencyResolver>
									_siteInitializerDependencyResolver = new CentralizedThreadLocal<>(
									SiteInitializerDependencyResolverThreadLocal.class.getName() +
																	"._siteInitializerDependencyResolver");

}

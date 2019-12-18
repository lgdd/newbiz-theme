package com.github.lgdd.liferay.newbiz.site.initializer;

import com.github.lgdd.liferay.newbiz.api.SiteInitializerDependencyResolver;
import com.liferay.portal.kernel.util.StringUtil;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;

@Component(
	immediate = true,
	property = "site.initializer.key=" + NewbizSiteInitializer.KEY,
	service = SiteInitializerDependencyResolver.class
)
public class SiteInitializerDependencyResolverImpl implements SiteInitializerDependencyResolver {

	@Override
	public String getDependenciesPath() {
		return _DEPENDENCIES_PATH;
	}

	@Override
	public ClassLoader getDisplayTemplatesClassLoader() {
		return SiteInitializerDependencyResolverImpl.class.getClassLoader();
	}

	@Override
	public String getDisplayTemplatesDependencyPath() {
		return _DEPENDENCIES_PATH + "display_templates/";
	}

	@Override
	public ClassLoader getDocumentsClassLoader() {
		return SiteInitializerDependencyResolverImpl.class.getClassLoader();
	}

	@Override
	public String getDocumentsDependencyPath() {
		return _DEPENDENCIES_PATH + "documents/";
	}

	@Override
	public String getFragmentsDependencyPath() {
		return _DEPENDENCIES_PATH + "fragments/";
	}

	@Override
	public ClassLoader getImageClassLoader() {
		return SiteInitializerDependencyResolverImpl.class.getClassLoader();
	}

	@Override
	public String getImageDependencyPath() {
		return _DEPENDENCIES_PATH + "images/";
	}

	@Override
	public String getJSON(String name) throws IOException {
		return StringUtil.read(
										SiteInitializerDependencyResolverImpl.class.getClassLoader(),
										_DEPENDENCIES_PATH + name);
	}

	private static final String _DEPENDENCIES_PATH =
									"com/github/lgdd/liferay/newbiz/site/initializer/internal/dependencies/";

}

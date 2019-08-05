package com.github.lgdd.liferay.newbiz.site.initializer;

import com.github.lgdd.liferay.newbiz.api.FileImporter;
import com.github.lgdd.liferay.newbiz.api.SiteInitializerDependencyResolver;
import com.github.lgdd.liferay.newbiz.api.SiteInitializerDependencyResolverThreadLocal;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.service.ServiceContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Map;

@Component(
	service = NewbizLayoutsInitializer.class
)
public class NewbizLayoutsInitializer {

	public void initialize(
									Map<String, FragmentEntry> fragmentEntriesMap, ServiceContext serviceContext) throws Exception {
		SiteInitializerDependencyResolver siteInitializerDependencyResolver =
										SiteInitializerDependencyResolverThreadLocal.
																		getSiteInitializerDependencyResolver();

		if (siteInitializerDependencyResolver != null) {
			_siteInitializerDependencyResolver =
											siteInitializerDependencyResolver;
		}

		_fileImporter.cleanLayouts(serviceContext);

		_createLayouts(fragmentEntriesMap, serviceContext);
	}

	private void _createLayouts(
									Map<String, FragmentEntry> fragmentEntriesMap, ServiceContext serviceContext)
									throws Exception {

		String json = _siteInitializerDependencyResolver.getJSON(
										"layouts.json");

		JSONArray jsonArray = _jsonFactory.createJSONArray(json);

		_fileImporter.createLayouts(
										jsonArray, _siteInitializerDependencyResolver.getImageClassLoader(),
										_siteInitializerDependencyResolver.getImageDependencyPath(),
										fragmentEntriesMap, serviceContext);
	}

	@Reference
	private FileImporter _fileImporter;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference(
		target = "(site.initializer.key=" + NewbizSiteInitializer.KEY + ")"
	)
	private SiteInitializerDependencyResolver
									_siteInitializerDependencyResolver;
}

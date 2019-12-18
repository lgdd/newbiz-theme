package com.github.lgdd.liferay.newbiz.site.initializer;

import com.github.lgdd.liferay.newbiz.api.FileImporter;
import com.github.lgdd.liferay.newbiz.api.SiteInitializerDependencyResolver;
import com.github.lgdd.liferay.newbiz.api.SiteInitializerDependencyResolverThreadLocal;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Theme;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.service.*;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.site.exception.InitializationException;
import com.liferay.site.initializer.SiteInitializer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

@Component(
	immediate = true,
	property = "site.initializer.key=" + NewbizSiteInitializer.KEY,
	service = SiteInitializer.class
)
public class NewbizSiteInitializer implements SiteInitializer {

	@Override
	public String getDescription(Locale locale) {
		ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
										"content.Language", locale, getClass());
		return LanguageUtil.get(resourceBundle, "newbiz-description");
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName(Locale locale) {
		ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
										"content.Language", locale, getClass());
		return LanguageUtil.get(resourceBundle, "newbiz");
	}

	@Override
	public String getThumbnailSrc() {
		_log.info("Servlet Context Path: " + _servletContext.getContextPath());
		return _servletContext.getContextPath() + "/images/thumbnail.jpg";
	}

	@Override
	public void initialize(long groupId)
									throws InitializationException {
		try {
			SiteInitializerDependencyResolver
											siteInitializerDependencyResolver =
											SiteInitializerDependencyResolverThreadLocal.
																			getSiteInitializerDependencyResolver();

			if (siteInitializerDependencyResolver != null) {
				_siteInitializerDependencyResolver =
												siteInitializerDependencyResolver;
			}

			ServiceContext serviceContext = getServiceContext(groupId);

			_updateLogo(serviceContext);
			_updateLookAndFeel(serviceContext);
			Map<String, FragmentEntry> fragmentEntriesMap = _importFragments(serviceContext);
			_createLayouts(fragmentEntriesMap, serviceContext);

		}
		catch (InitializationException ie) {
			throw ie;
		}
		catch (Exception e) {
			_log.error(e, e);
			throw new InitializationException(e);
		}
	}

	private void _createLayouts(
									Map<String, FragmentEntry> fragmentEntriesMap, ServiceContext serviceContext)
									throws Exception {
		_newbizLayoutsInitializer.initialize(fragmentEntriesMap, serviceContext);
	}

	@Override
	public boolean isActive(long companyId) {
		Theme theme = _themeLocalService.fetchTheme(
										companyId, THEME_ID);

		if (theme == null) {
			if (_log.isInfoEnabled()) {
				_log.info(THEME_ID + " is not registered");
			}

			return false;
		}

		return true;
	}

	private Map<String, FragmentEntry> _importFragments(ServiceContext serviceContext)
									throws Exception {

		JSONArray jsonArray = _getJSONArray("fragments.json");

		Map<String, FragmentEntry> fragmentEntriesMap = _fileImporter.createFragments(jsonArray,
										_siteInitializerDependencyResolver.getDocumentsClassLoader(),
										_siteInitializerDependencyResolver.getFragmentsDependencyPath(), serviceContext);

		return fragmentEntriesMap;
	}

	private void _updateLogo(ServiceContext serviceContext) throws Exception {
		ClassLoader classLoader =
										_siteInitializerDependencyResolver.getImageClassLoader();

		InputStream inputStream = classLoader.getResourceAsStream(
										_siteInitializerDependencyResolver.getImageDependencyPath() +
																		"logo.jpg");

		File file = FileUtil.createTempFile(inputStream);

		_fileImporter.updateLogo(file, true, true, serviceContext);
	}

	private void _updateLookAndFeel(ServiceContext serviceContext)
									throws PortalException {

		Theme theme = _themeLocalService.fetchTheme(
										serviceContext.getCompanyId(), THEME_ID);

		if (theme == null) {
			if (_log.isInfoEnabled()) {
				_log.info("No theme found for " + THEME_ID);
			}

			return;
		}

		_layoutSetLocalService.updateLookAndFeel(
										serviceContext.getScopeGroupId(), THEME_ID, StringPool.BLANK,
										StringPool.BLANK);
	}


	private JSONArray _getJSONArray(String name) throws Exception {
		return _jsonFactory.createJSONArray(
										_siteInitializerDependencyResolver.getJSON(name));
	}

	private JSONObject _getJSONObject(String name) throws Exception {
		return _jsonFactory.createJSONObject(
										_siteInitializerDependencyResolver.getJSON(name));
	}

	protected ServiceContext getServiceContext(long groupId)
									throws PortalException {

		User user = _userLocalService.getUser(PrincipalThreadLocal.getUserId());
		Group group = _groupLocalService.getGroup(groupId);

		Locale locale = LocaleUtil.getSiteDefault();

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setAddGroupPermissions(true);
		serviceContext.setAddGuestPermissions(true);
		serviceContext.setCompanyId(group.getCompanyId());
		serviceContext.setLanguageId(LanguageUtil.getLanguageId(locale));
		serviceContext.setScopeGroupId(groupId);
		serviceContext.setTimeZone(user.getTimeZone());
		serviceContext.setUserId(user.getUserId());

		return serviceContext;
	}

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private FileImporter _fileImporter;

	@Reference
	private NewbizLayoutsInitializer _newbizLayoutsInitializer;

	@Reference
	private LayoutSetLocalService _layoutSetLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private UserLocalService _userLocalService;

	@Reference
	private ThemeLocalService _themeLocalService;

	@Reference(
		target = "(site.initializer.key=" + NewbizSiteInitializer.KEY + ")"
	)
	private SiteInitializerDependencyResolver _siteInitializerDependencyResolver;

	@Reference(
		target = "(osgi.web.symbolicname=com.github.lgdd.liferay.newbiz.site.initializer)"
	)
	private ServletContext _servletContext;

	private static final Log _log = LogFactoryUtil.getLog(NewbizSiteInitializer.class);

	private static final String THEME_ID = "newbiz_WAR_newbiztheme";

	public static final String KEY = "newbiz-initializer";
}

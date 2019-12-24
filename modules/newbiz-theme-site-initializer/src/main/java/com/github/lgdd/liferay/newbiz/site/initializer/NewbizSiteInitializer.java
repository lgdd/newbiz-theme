package com.github.lgdd.liferay.newbiz.site.initializer;

import com.liferay.fragment.contributor.FragmentCollectionContributorTracker;
import com.liferay.fragment.importer.FragmentsImporter;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.renderer.FragmentRenderer;
import com.liferay.fragment.renderer.FragmentRendererTracker;
import com.liferay.fragment.service.FragmentEntryLinkLocalService;
import com.liferay.fragment.service.FragmentEntryLocalService;
import com.liferay.layout.page.template.service.LayoutPageTemplateStructureLocalService;
import com.liferay.layout.util.LayoutCopyHelper;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.Theme;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.service.*;
import com.liferay.portal.kernel.transaction.TransactionCommitCallbackUtil;
import com.liferay.portal.kernel.util.*;
import com.liferay.site.exception.InitializationException;
import com.liferay.site.initializer.SiteInitializer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Site Initializer for NewBiz theme.
 * See https://github.com/liferay/liferay-portal/tree/7.2.x/modules/apps/site/site-buildings-site-initializer
 */
@Component(immediate = true,
					 property = "site.initializer.key=" + NewbizSiteInitializer.KEY,
					 service = SiteInitializer.class)
public class NewbizSiteInitializer implements SiteInitializer {

	public static final String KEY = "newbiz-theme";

	@Override
	public String getDescription(Locale locale) {

		ResourceBundle resourceBundle = ResourceBundleUtil.getBundle("content.Language", locale, getClass());
		return LanguageUtil.get(resourceBundle, "newbiz-description");
	}

	@Override
	public String getKey() {

		return KEY;
	}

	@Override
	public String getName(Locale locale) {

		ResourceBundle resourceBundle = ResourceBundleUtil.getBundle("content.Language", locale, getClass());
		return LanguageUtil.get(resourceBundle, "newbiz");
	}

	@Override
	public String getThumbnailSrc() {

		return _servletContext.getContextPath() + "/images/thumbnail.jpg";
	}

	@Override
	public void initialize(long groupId)
									throws InitializationException {

		try {
			_createServiceContext(groupId);
			_addFragments();

			_addLayouts();
			_updateLookAndFeel();
			_updateLogo();

		}
		catch (Exception e) {
			_log.error(e, e);

			throw new InitializationException(e);
		}
	}

	@Override
	public boolean isActive(long companyId) {

		Theme theme = _themeLocalService.fetchTheme(companyId, _THEME_ID);

		if (theme == null) {
			if (_log.isInfoEnabled()) {
				_log.info(_THEME_ID + " is not registered");
			}
			return false;
		}
		return true;
	}

	@Activate
	protected void activate(BundleContext bundleContext) {

		_bundle = bundleContext.getBundle();
		if (_log.isInfoEnabled()) {
			_log.info("Bundle ID: " + _bundle.getBundleId());
			_log.info("Bundle SymbolicName: " + _bundle.getSymbolicName());
		}
	}

	private void _addFragments()
									throws InitializationException {

		URL url = _bundle.getEntry("/fragments.zip");

		try {
			File file = FileUtil.createTempFile(url.openStream());
			_fragmentsImporter.importFile(_serviceContext.getUserId(), _serviceContext.getScopeGroupId(), 0, file, false);
		}
		catch (Exception e) {
			throw new InitializationException(e);
		}
	}

	private void _addLayouts()
									throws Exception {

		_addLayout(LayoutConstants.DEFAULT_PARENT_LAYOUT_ID, "Home", LayoutConstants.TYPE_CONTENT, "home.json");

	}

	private Layout _addLayout(
									long parentLayoutId, String name, String type, String dataPath)
									throws Exception {

		Map<Locale, String> nameMap = new HashMap<>();

		nameMap.put(LocaleUtil.getSiteDefault(), name);

		Layout layout = _layoutLocalService.addLayout(_serviceContext.getUserId(), _serviceContext.getScopeGroupId(), false,
										parentLayoutId, nameMap, new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), type,
										null, false, false, new HashMap<>(), _serviceContext);

		if (Validator.isNotNull(dataPath)) {
			Layout draftLayout = _layoutLocalService.fetchLayout(_portal.getClassNameId(Layout.class), layout.getPlid());

			_layoutPageTemplateStructureLocalService.
											addLayoutPageTemplateStructure(_serviceContext.getUserId(), _serviceContext.getScopeGroupId(),
																			_portal.getClassNameId(Layout.class), draftLayout.getPlid(),
																			_parseLayoutContent(draftLayout.getPlid(), _readFile("/layouts/" + dataPath)),
																			_serviceContext);
		}

		TransactionCommitCallbackUtil.registerCallback(() -> {
			_copyLayout(layout);

			return null;
		});

		return layout;
	}

	private void _copyLayout(Layout layout)
									throws Exception {

		Layout draftLayout = _layoutLocalService.fetchLayout(_portal.getClassNameId(Layout.class), layout.getPlid());

		if (draftLayout != null) {
			_layoutCopyHelper.copyLayout(draftLayout, layout);
		}

		_layoutLocalService.updateLayout(layout.getGroupId(), layout.isPrivateLayout(), layout.getLayoutId(), new Date());
	}

	private void _createServiceContext(long groupId)
									throws InitializationException {

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setAddGroupPermissions(true);
		serviceContext.setAddGuestPermissions(true);

		User user;
		try {
			user = _userLocalService.getUser(PrincipalThreadLocal.getUserId());
		}
		catch (PortalException e) {
			_log.error(e);
			throw new InitializationException(e);
		}

		Locale locale = LocaleUtil.getSiteDefault();

		serviceContext.setLanguageId(LanguageUtil.getLanguageId(locale));

		serviceContext.setScopeGroupId(groupId);
		serviceContext.setTimeZone(user.getTimeZone());
		serviceContext.setUserId(user.getUserId());

		_serviceContext = serviceContext;
	}

	private String _parseLayoutContent(long plid, String data)
									throws Exception {

		JSONObject dataJSONObject = JSONFactoryUtil.createJSONObject(data);

		JSONArray structureJSONArray = dataJSONObject.getJSONArray("structure");

		for (int i = 0; i < structureJSONArray.length(); i++) {
			JSONObject rowJSONObject = structureJSONArray.getJSONObject(i);

			JSONArray columnsJSONArray = rowJSONObject.getJSONArray("columns");

			for (int j = 0; j < columnsJSONArray.length(); j++) {
				JSONObject columnJSONObject = columnsJSONArray.getJSONObject(j);

				JSONArray fragmentEntriesJSONArray = columnJSONObject.getJSONArray("fragmentEntries");

				JSONArray fragmentEntryLinkIdsJSONArray = JSONFactoryUtil.createJSONArray();

				for (int k = 0; k < fragmentEntriesJSONArray.length(); k++) {
					JSONObject fragmentEntryJSONObject = fragmentEntriesJSONArray.getJSONObject(k);

					String fragmentEntryKey = fragmentEntryJSONObject.getString("fragmentEntryKey");

					String editableValues = fragmentEntryJSONObject.getString("editableValues");

					editableValues = StringUtil.replace(editableValues, StringPool.DOLLAR, StringPool.DOLLAR, _resourcesMap);

					FragmentEntryLink fragmentEntryLink = _addFragmentEntryLink(plid, fragmentEntryKey, editableValues);

					if (fragmentEntryLink != null) {
						fragmentEntryLinkIdsJSONArray.put(fragmentEntryLink.getFragmentEntryLinkId());
					}
				}

				columnJSONObject.remove("fragmentEntries");

				columnJSONObject.put("fragmentEntryLinkIds", fragmentEntryLinkIdsJSONArray);
			}
		}

		return StringUtil.replace(dataJSONObject.toString(), StringPool.DOLLAR, StringPool.DOLLAR, _resourcesMap);
	}

	private FragmentEntryLink _addFragmentEntryLink(
									long plid, String fragmentEntryKey, String editableValues)
									throws PortalException {

		FragmentEntry fragmentEntry = _getFragmentEntry(fragmentEntryKey);

		FragmentRenderer fragmentRenderer = _fragmentRendererTracker.getFragmentRenderer(fragmentEntryKey);

		if ((fragmentEntry == null) && (fragmentRenderer == null)) {
			return null;
		}

		if (fragmentEntry != null) {
			String contributedRendererKey = null;

			if (fragmentEntry.getFragmentEntryId() == 0) {
				contributedRendererKey = fragmentEntryKey;
			}

			return _fragmentEntryLinkLocalService.addFragmentEntryLink(_serviceContext.getUserId(),
											_serviceContext.getScopeGroupId(), 0, fragmentEntry.getFragmentEntryId(),
											_portal.getClassNameId(Layout.class), plid, fragmentEntry.getCss(), fragmentEntry.getHtml(),
											fragmentEntry.getJs(), fragmentEntry.getConfiguration(), editableValues, StringPool.BLANK, 0,
											contributedRendererKey, _serviceContext);
		}

		return _fragmentEntryLinkLocalService.addFragmentEntryLink(_serviceContext.getUserId(),
										_serviceContext.getScopeGroupId(), 0, 0, _portal.getClassNameId(Layout.class), plid,
										StringPool.BLANK, StringPool.BLANK, StringPool.BLANK, StringPool.BLANK, editableValues,
										StringPool.BLANK, 0, fragmentEntryKey, _serviceContext);
	}

	private FragmentEntry _getFragmentEntry(String fragmentEntryKey) {

		FragmentEntry fragmentEntry =
										_fragmentEntryLocalService.fetchFragmentEntry(_serviceContext.getScopeGroupId(), fragmentEntryKey);

		if (fragmentEntry != null) {
			return fragmentEntry;
		}

		Map<String, FragmentEntry> fragmentEntries = _fragmentCollectionContributorTracker.getFragmentEntries();

		return fragmentEntries.get(fragmentEntryKey);
	}

	private void _updateLookAndFeel()
									throws PortalException {

		_layoutSetLocalService.updateLookAndFeel(_serviceContext.getScopeGroupId(), _THEME_ID, StringPool.BLANK,
										StringPool.BLANK);
	}

	private void _updateLogo()
									throws Exception {

		Class<?> clazz = getClass();
		byte[] logoAsBytes = FileUtil.getBytes(clazz, _DEPENDENCIES_PATH + "/images/logo.jpg");
		_layoutSetLocalService.updateLogo(_serviceContext.getScopeGroupId(), true, true, logoAsBytes);
	}

	private String _readFile(String fileName)
									throws IOException {

		Class<?> clazz = getClass();

		return StringUtil.read(clazz.getClassLoader(), _DEPENDENCIES_PATH + fileName);
	}

	private static final String _THEME_ID = "newbiz_WAR_newbiztheme";
	private static final String _DEPENDENCIES_PATH =
									"com/github/lgdd/liferay/newbiz/site/initializer/internal/dependencies";

	private static final Log _log = LogFactoryUtil.getLog(NewbizSiteInitializer.class);

	private Bundle _bundle;
	private ServiceContext _serviceContext;
	private final Map<String, String> _resourcesMap = new HashMap<>();

	@Reference
	FragmentEntryLocalService _fragmentEntryLocalService;

	@Reference
	FragmentEntryLinkLocalService _fragmentEntryLinkLocalService;

	@Reference
	FragmentCollectionContributorTracker _fragmentCollectionContributorTracker;

	@Reference
	FragmentRendererTracker _fragmentRendererTracker;

	@Reference
	LayoutPageTemplateStructureLocalService _layoutPageTemplateStructureLocalService;

	@Reference
	LayoutCopyHelper _layoutCopyHelper;

	@Reference
	LayoutSetLocalService _layoutSetLocalService;

	@Reference
	LayoutLocalService _layoutLocalService;

	@Reference
	private UserLocalService _userLocalService;

	@Reference
	private ThemeLocalService _themeLocalService;

	@Reference
	private FragmentsImporter _fragmentsImporter;

	@Reference
	private Portal _portal;

	@Reference(target = "(osgi.web.symbolicname=com.github.lgdd.liferay.newbiz.site.initializer)")
	private ServletContext _servletContext;

}

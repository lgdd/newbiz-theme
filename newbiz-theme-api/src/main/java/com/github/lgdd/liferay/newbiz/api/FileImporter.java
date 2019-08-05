package com.github.lgdd.liferay.newbiz.api;

import aQute.bnd.annotation.ProviderType;
import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.service.ServiceContext;

import java.io.File;
import java.util.Map;

@ProviderType
public interface FileImporter {

	public void cleanLayouts(ServiceContext serviceContext)
									throws PortalException;

	public void createJournalArticles(
									JSONArray journalArticleJSONArray, ClassLoader classLoader,
									String dependenciesFilePath, ServiceContext serviceContext)
									throws Exception;

	public void createLayouts(
									JSONArray jsonArray, ClassLoader classLoader, String dependenciesFilePath,
									Map<String, FragmentEntry> fragmentEntriesMap, ServiceContext serviceContext)
									throws Exception;

	public void createRoles(JSONArray jsonArray, ServiceContext serviceContext)
									throws PortalException;

	public DDMTemplate getDDMTemplate(
									File file, long classNameId, long classPK, long resourceClassNameId,
									String name, String type, String mode, String language,
									ServiceContext serviceContext)
									throws Exception;

	public void updateLogo(
									File file, boolean privateLayout, boolean logo,
									ServiceContext serviceContext)
									throws PortalException;

	public void updateLookAndFeel(
									String themeId, boolean privateLayout,
									ServiceContext serviceContext)
									throws PortalException;

	Map<String, FragmentEntry> createFragments(
									JSONArray jsonArray, ClassLoader classLoader,
									String dependenciesFilePath, ServiceContext serviceContext)
									throws Exception;
}

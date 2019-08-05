package com.github.lgdd.liferay.newbiz.api;

import aQute.bnd.annotation.ProviderType;

import java.io.IOException;

@ProviderType
public interface SiteInitializerDependencyResolver {

	public String getDependenciesPath();

	public ClassLoader getDisplayTemplatesClassLoader();

	public String getDisplayTemplatesDependencyPath();

	public ClassLoader getDocumentsClassLoader();

	public String getDocumentsDependencyPath();

	public String getFragmentsDependencyPath();

	public ClassLoader getImageClassLoader();

	public String getImageDependencyPath();

	public String getJSON(String name)
									throws IOException;

}

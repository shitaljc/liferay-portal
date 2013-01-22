/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portlet.journal.util;

import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.BaseIndexer;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentImpl;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchEngineUtil;
import com.liferay.portal.kernel.search.Summary;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.util.PortletKeys;
import com.liferay.portlet.expando.model.ExpandoBridge;
import com.liferay.portlet.expando.util.ExpandoBridgeFactoryUtil;
import com.liferay.portlet.expando.util.ExpandoBridgeIndexerUtil;
import com.liferay.portlet.journal.model.JournalFolder;
import com.liferay.portlet.journal.service.JournalFolderLocalServiceUtil;
import com.liferay.portlet.journal.service.permission.JournalFolderPermission;
import com.liferay.portlet.journal.service.persistence.JournalFolderActionableDynamicQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import javax.portlet.PortletURL;

/**
 * @author Eduardo Garcia
 */
public class JournalFolderIndexer extends BaseIndexer {

	public static final String[] CLASS_NAMES = {JournalFolder.class.getName()};

	public static final String PORTLET_ID = PortletKeys.JOURNAL;

	public JournalFolderIndexer() {
		setFilterSearch(true);
		setPermissionAware(true);
	}

	public String[] getClassNames() {
		return CLASS_NAMES;
	}

	public String getPortletId() {
		return PORTLET_ID;
	}

	@Override
	public boolean hasPermission(
			PermissionChecker permissionChecker, String entryClassName,
			long entryClassPK, String actionId)
		throws Exception {

		JournalFolder folder = JournalFolderLocalServiceUtil.getFolder(
			entryClassPK);

		return JournalFolderPermission.contains(
			permissionChecker, folder, ActionKeys.VIEW);
	}

	@Override
	protected void doDelete(Object obj) throws Exception {
		JournalFolder folder = (JournalFolder)obj;

		Document document = new DocumentImpl();

		document.addUID(PORTLET_ID, folder.getFolderId());

		SearchEngineUtil.deleteDocument(
			getSearchEngineId(), folder.getCompanyId(),
			document.get(Field.UID));
	}

	@Override
	protected Document doGetDocument(Object obj) throws Exception {
		JournalFolder folder = (JournalFolder)obj;

		if (_log.isDebugEnabled()) {
			_log.debug("Indexing folder " + folder);
		}

		Document document = getBaseModelDocument(PORTLET_ID, folder);

		document.addText(Field.DESCRIPTION, folder.getDescription());
		document.addKeyword(Field.FOLDER_ID, folder.getParentFolderId());
		document.addText(Field.TITLE, folder.getName());

		ExpandoBridge expandoBridge =
			ExpandoBridgeFactoryUtil.getExpandoBridge(
				folder.getCompanyId(), JournalFolder.class.getName(),
				folder.getFolderId());

		ExpandoBridgeIndexerUtil.addAttributes(document, expandoBridge);

		if (_log.isDebugEnabled()) {
			_log.debug("Document " + folder + " indexed successfully");
		}

		return document;
	}

	@Override
	protected Summary doGetSummary(
		Document document, Locale locale, String snippet,
		PortletURL portletURL) {

		return null;
	}

	@Override
	protected void doReindex(Object obj) throws Exception {
		JournalFolder folder = (JournalFolder)obj;

		Document document = getDocument(folder);

		SearchEngineUtil.updateDocument(
			getSearchEngineId(), folder.getCompanyId(), document);
	}

	@Override
	protected void doReindex(String className, long classPK) throws Exception {
		JournalFolder folder = JournalFolderLocalServiceUtil.getFolder(classPK);

		doReindex(folder);
	}

	@Override
	protected void doReindex(String[] ids) throws Exception {
		long companyId = GetterUtil.getLong(ids[0]);

		reindexFolders(companyId);
	}

	@Override
	protected String getPortletId(SearchContext searchContext) {
		return PORTLET_ID;
	}

	protected void reindexFolders(long companyId)
		throws PortalException, SystemException {

		final Collection<Document> documents = new ArrayList<Document>();

		ActionableDynamicQuery actionableDynamicQuery =
			new JournalFolderActionableDynamicQuery() {

			@Override
			protected void performAction(Object object) throws PortalException {
				JournalFolder folder = (JournalFolder)object;

				Document document = getDocument(folder);

				if (document != null) {
					documents.add(document);
				}
			}

		};

		actionableDynamicQuery.setCompanyId(companyId);

		actionableDynamicQuery.performActions();

		SearchEngineUtil.updateDocuments(
			getSearchEngineId(), companyId, documents);
	}

	private static Log _log = LogFactoryUtil.getLog(JournalFolderIndexer.class);

}
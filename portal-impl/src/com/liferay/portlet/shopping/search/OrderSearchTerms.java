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

package com.liferay.portlet.shopping.search;

import com.liferay.portlet.shopping.model.ShoppingOrderConstants;

import javax.portlet.PortletRequest;

/**
 * @author Brian Wing Shun Chan
 */
public class OrderSearchTerms extends OrderDisplayTerms {

	public OrderSearchTerms(PortletRequest portletRequest) {
		super(portletRequest);
	}

	@Override
	public String getStatus() {
		if (status == null) {
			return null;
		}
		else if (status.equals(ShoppingOrderConstants.STATUSES[0])) {
			return ShoppingOrderConstants.STATUS_CHECKOUT;
		}
		else if (status.equals(ShoppingOrderConstants.STATUSES[1])) {
			return ShoppingOrderConstants.STATUS_COMPLETED;
		}
		else if (status.equals(ShoppingOrderConstants.STATUSES[2])) {
			return ShoppingOrderConstants.STATUS_DENIED;
		}
		else if (status.equals(ShoppingOrderConstants.STATUSES[3])) {
			return ShoppingOrderConstants.STATUS_PENDING;
		}
		else if (status.equals(ShoppingOrderConstants.STATUSES[4])) {
			return ShoppingOrderConstants.STATUS_REFUNDED;
		}
		else {
			return null;
		}
	}

}
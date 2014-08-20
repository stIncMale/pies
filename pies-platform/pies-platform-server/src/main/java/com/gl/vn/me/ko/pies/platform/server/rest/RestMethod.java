package com.gl.vn.me.ko.pies.platform.server.rest;

import javax.annotation.concurrent.Immutable;

/**
 * Represents {@link RestRequest} methods.
 * This methods is a subset of HTTP methods.
 */
@Immutable
public enum RestMethod {
	/**
	 * SHOULD be used to retrieve a resource.
	 */
	GET,
	/**
	 * SHOULD be used to create a resource.
	 */
	POST,
	/**
	 * SHOULD be used to change a resource.
	 */
	PUT,
	/**
	 * SHOULD be used to delete a resource.
	 */
	DELETE
}

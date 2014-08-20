package com.gl.vn.me.ko.pies.platform.server.rest;

import static org.junit.Assert.assertEquals;
import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.util.List;
import org.junit.Test;

public final class TestRestRequest {
	public TestRestRequest() {
	}

	@Test
	public final void equals() {
		final RestRequest request1 = RestRequest.valueOf("POST /one/");
		final RestRequest request2 = RestRequest.valueOf("POST /one");
		assertEquals("Assert that requests are equal", request1, request2);
	}

	@Test
	public final void getUri() {
		final String stringUri = "/one/two/";
		final URI uri = URI.create(stringUri);
		final RestRequest request = RestRequest.valueOf("DELETE " + stringUri);
		assertEquals("Assert that getUri returns correct value", uri, request.getUri());
	}

	@Test
	public final void getUriNodes() {
		final String supplementaryCharacters = new String(new int[] {0x494F8}, 0, 1);
		final String stringUri = "/one/two%20three/𤧰_" + supplementaryCharacters + "/";
		final List<String> nodes = ImmutableList.of("one", "two three", "𤧰_" + supplementaryCharacters);
		final RestRequest request = RestRequest.valueOf("POST " + stringUri);
		assertEquals("Assert that getUriNodes returns correct value", nodes, request.getUriNodes());
	}

	@Test
	public final void getUriNodesMultipleEmpty() {
		final String stringUri = "///qwe//";
		final List<String> nodes = ImmutableList.of("qwe");
		final RestRequest request = RestRequest.valueOf("PUT " + stringUri);
		assertEquals("Assert that getUriNodes returns correct value", nodes, request.getUriNodes());
	}

	@Test
	public final void getUriNodesSingleEmpty() {
		final String stringUri = "/";
		final List<String> nodes = ImmutableList.of("");
		final RestRequest request = RestRequest.valueOf("PUT " + stringUri);
		assertEquals("Assert that getUriNodes returns correct value", nodes, request.getUriNodes());
	}

	@Test(expected = RestRequestSyntaxException.class)
	public final void incorrectValueOfArgs() {
		final String method = "DELETE";
		final String uri = "one/two";// there is no '/' at the beginning
		RestRequest.valueOf(method, uri);
	}

	@Test(expected = RestRequestSyntaxException.class)
	public final void incorrectValueOfStringArgs1() {
		RestRequest.valueOf("GE_T /one/two/");// incorrect REST method
	}

	@Test
	public final void t0String() {
		final String method = "DELETE";
		final String encodedUri = "/one/two%20/";// raw URL
		final String decodedUri = "/one/two /";
		final RestRequest request = RestRequest.valueOf(method, encodedUri);
		assertEquals("Assert that toString returns correct value", method + " " + decodedUri, request.toString());
	}

	@Test
	public final void toRawString() {
		final String method = "DELETE";
		final String encodedUri = "/one/two%20/";// raw URL
		final RestRequest request = RestRequest.valueOf(method, encodedUri);
		assertEquals("Assert that toString returns correct value", method + " " + encodedUri, request.toRawString());
	}

	@Test
	public final void valueOfString() {
		final String stringRequest = "GET /one/two/";
		final RestMethod method = RestMethod.GET;
		final List<String> nodes = ImmutableList.of("one", "two");
		final RestRequest request = RestRequest.valueOf(stringRequest);
		assertEquals("Assert that method is correct", method, request.getMethod());
		assertEquals("Assert that nodes is correct", nodes, request.getUriNodes());
	}

	@Test
	public final void valueOfStringUri() {
		final String stringMethod = "PUT";
		final String stringUri = "/one/two/";
		final RestMethod method = RestMethod.PUT;
		final List<String> nodes = ImmutableList.of("one", "two");
		final RestRequest request = RestRequest.valueOf(stringMethod, stringUri);
		assertEquals("Assert that method is correct", method, request.getMethod());
		assertEquals("Assert that nodes is correct", nodes, request.getUriNodes());
	}
}

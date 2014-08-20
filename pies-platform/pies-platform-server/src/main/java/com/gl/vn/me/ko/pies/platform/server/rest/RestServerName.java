package com.gl.vn.me.ko.pies.platform.server.rest;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Qualifier;

/**
 * Required for injection into a {@link RestServer} and {@link JsonRestServer}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface RestServerName {
}

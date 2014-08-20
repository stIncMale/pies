package com.gl.vn.me.ko.pies.platform.server.tcp;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Qualifier;

/**
 * Required for injection into a {@link TcpServer}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface TcpServerAddress {
}

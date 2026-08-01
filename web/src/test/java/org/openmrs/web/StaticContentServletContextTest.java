/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.web.controller.PseudoStaticContentController;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Loads {@code webapp/src/main/webapp/WEB-INF/openmrs_static_content-servlet.xml} in a real Spring
 * context so the descriptor is verified against the Spring generation actually on the classpath.
 * <p>
 * The descriptor references its schemas without a version component, which is what lets it bind to
 * whichever Spring generation is present rather than pinning a retired grammar. Nothing else in the
 * build parses this file: it is read by the servlet container at deployment time, so a bean class
 * that had been moved or removed, a property that no longer exists, or an unresolvable schema would
 * surface only as a deployment failure. This test moves that failure into the build.
 * <p>
 * The descriptor itself is treated as read-only input; the test locates it in the reactor rather
 * than copying it, so the file that ships is the file that is verified.
 */
public class StaticContentServletContextTest {

	private static final String DESCRIPTOR_RELATIVE_PATH = "webapp/src/main/webapp/WEB-INF"
	        + "/openmrs_static_content-servlet.xml";

	/**
	 * Matches a Spring schema reference that pins a generation, for example
	 * {@code spring-beans-3.0.xsd}, which is exactly what the versionless form replaces.
	 */
	private static final Pattern VERSIONED_SPRING_SCHEMA = Pattern.compile("spring-[a-z]+-\\d+\\.\\d+\\.xsd");

	private static final List<String> EXPECTED_BEAN_NAMES = Arrays.asList("viewResolver", "handlerMapping",
	    "staticResourceDispatcher", "urlRewrites", "staticContentController", "jstlContentController");

	private XmlWebApplicationContext context;

	/**
	 * Walks up from the working directory to find the descriptor, so the test is independent of which
	 * reactor module surefire happens to run it from.
	 *
	 * @return the descriptor file
	 */
	private static File locateDescriptor() {
		File candidate = new File(System.getProperty("user.dir")).getAbsoluteFile();
		for (int depth = 0; candidate != null && depth < 5; depth++) {
			File descriptor = new File(candidate, DESCRIPTOR_RELATIVE_PATH);
			if (descriptor.isFile()) {
				return descriptor;
			}
			candidate = candidate.getParentFile();
		}
		throw new IllegalStateException(
		        "Could not locate " + DESCRIPTOR_RELATIVE_PATH + " from " + System.getProperty("user.dir"));
	}

	@BeforeEach
	public void loadDescriptor() {
		context = new XmlWebApplicationContext();
		context.setConfigLocation(locateDescriptor().toURI().toString());
		MockServletContext servletContext = new MockServletContext();
		servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, context);
		context.setServletContext(servletContext);

		// a parse failure, a missing bean class or an unknown property fails here
		context.refresh();
	}

	@AfterEach
	public void closeContext() {
		if (context != null) {
			context.close();
		}
	}

	/**
	 * Every declared bean must be instantiable, which is the whole point of loading the descriptor.
	 */
	@Test
	public void refresh_shouldInstantiateEveryDeclaredBean() {
		for (String beanName : EXPECTED_BEAN_NAMES) {
			assertTrue(context.containsBean(beanName), "missing bean: " + beanName);
			assertNotNull(context.getBean(beanName), "null bean: " + beanName);
		}
	}

	/**
	 * The view resolver must still produce JSTL views under the WEB-INF/view prefix; the prefix and
	 * suffix are asserted through the resolved URL because the accessors are not public.
	 */
	@Test
	public void viewResolver_shouldResolveViewsUnderTheWebInfViewPrefix() throws Exception {
		ViewResolver viewResolver = context.getBean("viewResolver", InternalResourceViewResolver.class);

		View view = viewResolver.resolveViewName("/module/legacyui/page.jsp", Locale.ENGLISH);

		assertNotNull(view);
		assertEquals("org.springframework.web.servlet.view.JstlView", view.getClass().getName());
		// prefix "/WEB-INF/view" plus the view name, with an empty suffix
		assertEquals("/WEB-INF/view/module/legacyui/page.jsp", ((AbstractUrlBasedView) view).getUrl());
	}

	/**
	 * Asserts the resolved handler, not merely the declared mapping, so a broken bean reference would
	 * be caught.
	 */
	@Test
	public void handlerMapping_shouldMapEveryPathToTheStaticResourceDispatcher() {
		SimpleUrlHandlerMapping handlerMapping = context.getBean("handlerMapping", SimpleUrlHandlerMapping.class);

		Map<String, Object> handlerMap = handlerMapping.getHandlerMap();

		assertEquals(1, handlerMap.size());
		assertSame(context.getBean("staticResourceDispatcher"), handlerMap.get("/**"));
	}

	/**
	 * Verifies the instances that were actually injected rather than the declaration, since the two
	 * controllers differ only in configuration and are easy to transpose.
	 */
	@Test
	public void staticResourceDispatcher_shouldBeWiredToBothPseudoStaticControllers() {
		StaticResourceDispatcher dispatcher = context.getBean("staticResourceDispatcher", StaticResourceDispatcher.class);
		DirectFieldAccessor fields = new DirectFieldAccessor(dispatcher);

		assertSame(context.getBean("staticContentController"), fields.getPropertyValue("staticContentController"));
		assertSame(context.getBean("jstlContentController"), fields.getPropertyValue("jstlContentController"));
	}

	/**
	 * The util:map element is the one non-bean element in the descriptor, so it exercises the second of
	 * the two schemas the file declares.
	 */
	@Test
	public void urlRewrites_shouldContainTheLegacyScriptRedirects() {
		Map<?, ?> urlRewrites = context.getBean("urlRewrites", Map.class);

		assertEquals(3, urlRewrites.size());
		assertEquals("/scripts/jquery/jquery.min.js", urlRewrites.get("/scripts/jquery/jquery-1.3.2.min.js"));
		assertEquals("/scripts/jquery-ui/js/jquery-ui.custom.min.js",
		    urlRewrites.get("/scripts/jquery-ui/js/jquery-ui-1.7.2.custom.min.js"));
		assertEquals("/scripts/jquery-ui/css/redmond/jquery-ui.custom.css",
		    urlRewrites.get("/scripts/jquery-ui/css/redmond/jquery-ui-1.7.2.custom.css"));
	}

	/**
	 * The static controller must not interpret JSTL and must own the rewrite map.
	 */
	@Test
	public void staticContentController_shouldNotInterpretJstlAndShouldCarryTheRewrites() {
		PseudoStaticContentController controller = context.getBean("staticContentController",
		    PseudoStaticContentController.class);

		assertFalse(controller.getInterpretJstl());
		assertSame(context.getBean("urlRewrites"), controller.getRewrites());
	}

	/**
	 * The JSTL controller is the same class with the opposite switch and no rewrites, which is the
	 * distinction the dispatcher relies on.
	 */
	@Test
	public void jstlContentController_shouldInterpretJstlAndCarryNoRewrites() {
		PseudoStaticContentController controller = context.getBean("jstlContentController",
		    PseudoStaticContentController.class);

		assertTrue(controller.getInterpretJstl());
		assertNull(controller.getRewrites());
	}

	/**
	 * The two controllers must be distinct instances; sharing one would silently disable JSTL
	 * interpretation for half the mappings.
	 */
	@Test
	public void theTwoPseudoStaticControllers_shouldBeSeparateInstances() {
		Object staticController = context.getBean("staticContentController");
		Object jstlController = context.getBean("jstlContentController");

		assertInstanceOf(PseudoStaticContentController.class, staticController);
		assertInstanceOf(PseudoStaticContentController.class, jstlController);
		assertNotSame(staticController, jstlController);
	}

	/**
	 * Pins the schema declaration itself. A versionless reference resolves against whichever Spring
	 * generation is on the classpath; re-pinning a retired grammar such as spring-beans-3.0.xsd would
	 * still parse today and would break on the next upgrade, so the absence is asserted directly.
	 */
	@Test
	public void descriptor_shouldDeclareSpringSchemasWithoutAVersionComponent() throws IOException {
		String descriptor = Files.readString(locateDescriptor().toPath(), StandardCharsets.UTF_8);

		assertTrue(descriptor.contains("http://www.springframework.org/schema/beans/spring-beans.xsd"),
		    "expected the versionless beans schema");
		assertTrue(descriptor.contains("http://www.springframework.org/schema/util/spring-util.xsd"),
		    "expected the versionless util schema");
		assertFalse(VERSIONED_SPRING_SCHEMA.matcher(descriptor).find(), "descriptor still pins a versioned Spring schema");
	}
}

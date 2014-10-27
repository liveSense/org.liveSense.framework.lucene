package org.apache.lucene.util;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.util.Iterator;

/**
 * OSGI based helper class for loading SPI classes from OSGi registered services (META-INF files).
 * The LuceneOSGiSPIRegistrationService manages the services, which is managed by LuceneOSGiSPIBundleListener
 * This is a light impl of {@link java.util.ServiceLoader} but is guaranteed to
 * be bug-free regarding classpath order and does not instantiate or initialize
 * the classes found.
 *
 * @lucene.internal
 */
public final class SPIClassIterator<S> implements Iterator<Class<? extends S>> {

	private final Class<S> clazz;
	private final Iterator iterator;

	public static <S> SPIClassIterator<S> get(Class<S> clazz) {
		return SPIClassIterator.get(clazz, null);
	}

	public static <S> SPIClassIterator<S> get(Class<S> clazz, ClassLoader loader) {
		return new SPIClassIterator<S>(clazz, SPIClassIterator.class.getClass().getClassLoader(), getService());
	}

	/** Utility method to check if some class loader is a (grand-)parent of or the same as another one.
	 * This means the child will be able to load all classes from the parent, too. */
	public static boolean isParentClassLoader(final ClassLoader parent, ClassLoader child) {
		while (child != null) {
			if (child == parent) {
				return true;
			}
			child = child.getParent();
		}
		return false;
	}

	private SPIClassIterator(Class<S> clazz, ClassLoader loader, LuceneOSGiSPIRegistrationService service) {
		this.clazz = clazz;
		iterator = service.get(clazz).iterator();
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public Class<? extends S> next() {
		return (Class<? extends S>) iterator.next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	private static LuceneOSGiSPIRegistrationService getService()
	{
		// get bundle instance via the OSGi Framework Util class
		BundleContext ctx = FrameworkUtil.getBundle(SPIClassIterator.class).getBundleContext();
		ServiceReference serviceReference = ctx.getServiceReference(LuceneOSGiSPIRegistrationService.class.getName());
		return LuceneOSGiSPIRegistrationService.class.cast(ctx.getService(serviceReference));
	}
}

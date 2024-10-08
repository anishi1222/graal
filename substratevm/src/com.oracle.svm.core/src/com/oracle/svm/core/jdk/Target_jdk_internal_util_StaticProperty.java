/*
 * Copyright (c) 2019, 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.svm.core.jdk;

import java.util.function.BooleanSupplier;

import com.oracle.svm.core.SubstrateUtil;
import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import com.oracle.svm.core.annotate.TargetElement;
import com.oracle.svm.util.ReflectionUtil;

import jdk.internal.util.StaticProperty;

/**
 * This class provides JDK-internal access to values that are also available via system properties.
 * However, it must not return values changes by the user. We do not want to query the values during
 * VM startup, because doing that is expensive. So we perform lazy initialization by calling the
 * same methods also used to initialize the system properties.
 */
@Substitute
@TargetClass(jdk.internal.util.StaticProperty.class)
@SuppressWarnings("unused")
final class Target_jdk_internal_util_StaticProperty {

    // Checkstyle: stop
    @Alias//
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)//
    public static String USER_LANGUAGE;

    @Alias//
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)//
    public static String USER_LANGUAGE_DISPLAY;

    @Alias//
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)//
    public static String USER_LANGUAGE_FORMAT;

    @Alias//
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)//
    public static String USER_SCRIPT;

    @Alias//
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)//
    public static String USER_SCRIPT_DISPLAY;

    @Alias//
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)//
    public static String USER_SCRIPT_FORMAT;

    @Alias//
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)//
    public static String USER_COUNTRY;

    @Alias//
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)//
    public static String USER_COUNTRY_DISPLAY;

    @Alias//
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)//
    public static String USER_COUNTRY_FORMAT;

    @Alias//
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)//
    public static String USER_VARIANT;

    @Alias//
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)//
    public static String USER_VARIANT_DISPLAY;

    @Alias//
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)//
    public static String USER_VARIANT_FORMAT;

    @Alias//
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)//
    public static String USER_EXTENSIONS;

    @Alias//
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)//
    public static String USER_EXTENSIONS_DISPLAY;

    @Alias//
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)//
    public static String USER_EXTENSIONS_FORMAT;

    @Alias//
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)//
    public static String USER_REGION;
    // Checkstyle: resume

    static {
        if (!SubstrateUtil.HOSTED) {
            USER_LANGUAGE = SystemPropertiesSupport.singleton().getSavedProperty(SystemPropertiesSupport.UserSystemProperty.USER_LANGUAGE, "en");
            USER_LANGUAGE_DISPLAY = SystemPropertiesSupport.singleton().getSavedProperty(SystemPropertiesSupport.UserSystemProperty.USER_LANGUAGE_DISPLAY, USER_LANGUAGE);
            USER_LANGUAGE_FORMAT = SystemPropertiesSupport.singleton().getSavedProperty(SystemPropertiesSupport.UserSystemProperty.USER_SCRIPT_FORMAT, USER_LANGUAGE);
            USER_SCRIPT = SystemPropertiesSupport.singleton().getSavedProperty(SystemPropertiesSupport.UserSystemProperty.USER_SCRIPT, "");
            USER_SCRIPT_DISPLAY = SystemPropertiesSupport.singleton().getSavedProperty(SystemPropertiesSupport.UserSystemProperty.USER_SCRIPT_DISPLAY, USER_SCRIPT);
            USER_SCRIPT_FORMAT = SystemPropertiesSupport.singleton().getSavedProperty(SystemPropertiesSupport.UserSystemProperty.USER_SCRIPT_FORMAT, USER_SCRIPT);
            USER_COUNTRY = SystemPropertiesSupport.singleton().getSavedProperty(SystemPropertiesSupport.UserSystemProperty.USER_COUNTRY, "");
            USER_COUNTRY_DISPLAY = SystemPropertiesSupport.singleton().getSavedProperty(SystemPropertiesSupport.UserSystemProperty.USER_COUNTRY_DISPLAY, USER_COUNTRY);
            USER_COUNTRY_FORMAT = SystemPropertiesSupport.singleton().getSavedProperty(SystemPropertiesSupport.UserSystemProperty.USER_COUNTRY_FORMAT, USER_COUNTRY);
            USER_VARIANT = SystemPropertiesSupport.singleton().getSavedProperty(SystemPropertiesSupport.UserSystemProperty.USER_VARIANT, "");
            USER_VARIANT_DISPLAY = SystemPropertiesSupport.singleton().getSavedProperty(SystemPropertiesSupport.UserSystemProperty.USER_VARIANT_DISPLAY, USER_VARIANT);
            USER_VARIANT_FORMAT = SystemPropertiesSupport.singleton().getSavedProperty(SystemPropertiesSupport.UserSystemProperty.USER_VARIANT_FORMAT, USER_VARIANT);
            USER_EXTENSIONS = SystemPropertiesSupport.singleton().getSavedProperty(SystemPropertiesSupport.UserSystemProperty.USER_EXTENSIONS, "");
            USER_EXTENSIONS_DISPLAY = SystemPropertiesSupport.singleton().getSavedProperty(SystemPropertiesSupport.UserSystemProperty.USER_EXTENSIONS_DISPLAY, USER_EXTENSIONS);
            USER_EXTENSIONS_FORMAT = SystemPropertiesSupport.singleton().getSavedProperty(SystemPropertiesSupport.UserSystemProperty.USER_EXTENSIONS_FORMAT, USER_EXTENSIONS);
            USER_REGION = SystemPropertiesSupport.singleton().getSavedProperty(SystemPropertiesSupport.UserSystemProperty.USER_REGION, "");
        }
    }

    @Substitute
    private static String javaHome() {
        /* Native images do not have a Java home directory. */
        return null;
    }

    @Substitute
    private static String userHome() {
        return SystemPropertiesSupport.singleton().userHome();
    }

    @Substitute
    private static String userDir() {
        return SystemPropertiesSupport.singleton().userDir();
    }

    @Substitute
    private static String userName() {
        return SystemPropertiesSupport.singleton().userName();
    }

    @Substitute
    private static String javaIoTmpDir() {
        return SystemPropertiesSupport.singleton().javaIoTmpDir();
    }

    @Substitute
    private static String javaLibraryPath() {
        return SystemPropertiesSupport.singleton().javaLibraryPath();
    }

    @Substitute
    private static String sunBootLibraryPath() {
        String value = SystemPropertiesSupport.singleton().savedProperties.get("sun.boot.library.path");
        return value == null ? "" : value;
    }

    @Substitute
    private static String jdkSerialFilter() {
        return SystemPropertiesSupport.singleton().savedProperties.get("jdk.serialFilter");
    }

    @Substitute
    @TargetElement(onlyWith = StaticPropertyJdkSerialFilterFactoryAvailable.class)
    private static String jdkSerialFilterFactory() {
        return SystemPropertiesSupport.singleton().savedProperties.get("jdk.serialFilterFactory");
    }

    private abstract static class StaticPropertyMethodAvailable implements BooleanSupplier {

        private final String methodName;

        protected StaticPropertyMethodAvailable(String methodName) {
            this.methodName = methodName;
        }

        @Override
        public boolean getAsBoolean() {
            return ReflectionUtil.lookupMethod(true, StaticProperty.class, methodName) != null;
        }
    }

    /*
     * Method jdkSerialFilterFactory is present in some versions of the JDK11 and not in the other.
     * It is always present in the JDK17. We need to check if this method should be substituted by
     * checking if it exists in the running JDK version.
     */
    private static class StaticPropertyJdkSerialFilterFactoryAvailable extends StaticPropertyMethodAvailable {
        protected StaticPropertyJdkSerialFilterFactoryAvailable() {
            super("jdkSerialFilterFactory");
        }
    }

    @Substitute
    public static String nativeEncoding() {
        return SystemPropertiesSupport.singleton().savedProperties.get("native.encoding");
    }

    @Substitute
    public static String fileEncoding() {
        return SystemPropertiesSupport.singleton().savedProperties.get("file.encoding");
    }

    @Substitute
    public static String javaPropertiesDate() {
        return SystemPropertiesSupport.singleton().savedProperties.getOrDefault("java.properties.date", null);
    }

    @Substitute
    public static String jnuEncoding() {
        return SystemPropertiesSupport.singleton().savedProperties.get("sun.jnu.encoding");
    }

    @Substitute
    public static String javaLocaleUseOldISOCodes() {
        return SystemPropertiesSupport.singleton().savedProperties.getOrDefault("java.locale.useOldISOCodes", "");
    }
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package liquibase.util;

import liquibase.logging.LogService;
import liquibase.logging.LogType;

import java.io.File;

/**
 * Code taken from <a href="http://commons.apache.org/lang">Commons lang utils</a>
 * <p>Helpers for <code>java.lang.System</code>.</p>
 *
 * <p>If a system property cannot be read due to security restrictions,
 * the corresponding field in this class will be set to <code>null</code>
 * and a message will be written to <code>System.err</code>.</p>
 *
 * @author Apache Software Foundation
 * @author Based on code from Avalon Excalibur
 * @author Based on code from Lucene
 * @author <a href="mailto:sdowney@panix.com">Steve Downey</a>
 * @author Gary Gregory
 * @author Michael Becke
 * @author Tetsuya Kaneuchi
 * @author Rafal Krupinski
 * @author Jason Gritman
 * @since 1.0
 * @version $Id: SystemUtils.java 905707 2010-02-02 16:59:59Z niallp $
 */
public class SystemUtils {

    /**
     * The prefix String for all Windows OS.
     */
    private static final String OS_NAME_WINDOWS_PREFIX = "Windows";

    // System property constants
    //-----------------------------------------------------------------------
    // These MUST be declared first. Other constants depend on this.

    /**
     * The System property key for the user home directory.
     */
    private static final String USER_HOME_KEY = "user.home";

    /**
     * The System property key for the user directory.
     */
    private static final String USER_DIR_KEY = "user.dir";

    /**
     * The System property key for the Java IO temporary directory.
     */
    private static final String JAVA_IO_TMPDIR_KEY = "java.io.tmpdir";

    /**
     * The System property key for the Java home directory.
     */
    private static final String JAVA_HOME_KEY = "java.home";

    /**
     * <p>The <code>awt.toolkit</code> System Property.</p>
     * <p>Holds a class name, on Windows XP this is <code>sun.awt.windows.WToolkit</code>.</p>
     * <p><b>On platforms without a GUI, this value is <code>null</code>.</b></p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since 2.1
     */
    public static final String AWT_TOOLKIT = getSystemProperty("awt.toolkit");

    /**
     * <p>The <code>file.encoding</code> System Property.</p>
     * <p>File encoding, such as <code>Cp1252</code>.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since 2.0
     * @since Java 1.2
     */
    public static final String FILE_ENCODING = getSystemProperty("file.encoding");

    /**
     * <p>The <code>file.separator</code> System Property.
     * File separator (<code>&quot;/&quot;</code> on UNIX).</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.1
     */
    public static final String FILE_SEPARATOR = getSystemProperty("file.separator");

    /**
     * <p>The <code>java.awt.fonts</code> System Property.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since 2.1
     */
    public static final String JAVA_AWT_FONTS = getSystemProperty("java.awt.fonts");

    /**
     * <p>The <code>java.awt.graphicsenv</code> System Property.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since 2.1
     */
    public static final String JAVA_AWT_GRAPHICSENV = getSystemProperty("java.awt.graphicsenv");

    /**
     * <p>
     * The <code>java.awt.headless</code> System Property.
     * The value of this property is the String <code>"true"</code> or <code>"false"</code>.
     * </p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @see #isJavaAwtHeadless()
     * @since 2.1
     * @since Java 1.4
     */
    public static final String JAVA_AWT_HEADLESS = getSystemProperty("java.awt.headless");

    /**
     * <p>The <code>java.awt.printerjob</code> System Property.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since 2.1
     */
    public static final String JAVA_AWT_PRINTERJOB = getSystemProperty("java.awt.printerjob");

    /**
     * <p>The <code>java.class.path</code> System Property. Java class path.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.1
     */
    public static final String JAVA_CLASS_PATH = getSystemProperty("java.class.path");

    /**
     * <p>The <code>java.class.version</code> System Property.
     * Java class format version number.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.1
     */
    public static final String JAVA_CLASS_VERSION = getSystemProperty("java.class.version");

    /**
     * <p>The <code>java.compiler</code> System Property. Name of JIT compiler to use.
     * First in JDK version 1.2. Not used in Sun JDKs after 1.2.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.2. Not used in Sun versions after 1.2.
     */
    public static final String JAVA_COMPILER = getSystemProperty("java.compiler");

    /**
     * <p>The <code>java.endorsed.dirs</code> System Property. Path of endorsed directory
     * or directories.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.4
     */
    public static final String JAVA_ENDORSED_DIRS = getSystemProperty("java.endorsed.dirs");

    /**
     * <p>The <code>java.ext.dirs</code> System Property. Path of extension directory
     * or directories.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.3
     */
    public static final String JAVA_EXT_DIRS = getSystemProperty("java.ext.dirs");

    /**
     * <p>The <code>java.home</code> System Property. Java installation directory.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.1
     */
    public static final String JAVA_HOME = getSystemProperty(JAVA_HOME_KEY);

    /**
     * <p>The <code>java.io.tmpdir</code> System Property. Default temp file path.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.2
     */
    public static final String JAVA_IO_TMPDIR = getSystemProperty(JAVA_IO_TMPDIR_KEY);

    /**
     * <p>The <code>java.library.path</code> System Property. List of paths to search
     * when loading libraries.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.2
     */
    public static final String JAVA_LIBRARY_PATH = getSystemProperty("java.library.path");

    /**
     * <p>The <code>java.runtime.name</code> System Property. Java Runtime Environment
     * name.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since 2.0
     * @since Java 1.3
     */
    public static final String JAVA_RUNTIME_NAME = getSystemProperty("java.runtime.name");

    /**
     * <p>The <code>java.runtime.version</code> System Property. Java Runtime Environment
     * version.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since 2.0
     * @since Java 1.3
     */
    public static final String JAVA_RUNTIME_VERSION = getSystemProperty("java.runtime.version");

    /**
     * <p>The <code>java.specification.name</code> System Property. Java Runtime Environment
     * specification name.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.2
     */
    public static final String JAVA_SPECIFICATION_NAME = getSystemProperty("java.specification.name");

    /**
     * <p>The <code>java.specification.vendor</code> System Property. Java Runtime Environment
     * specification vendor.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.2
     */
    public static final String JAVA_SPECIFICATION_VENDOR = getSystemProperty("java.specification.vendor");

    /**
     * <p>The <code>java.specification.version</code> System Property. Java Runtime Environment
     * specification version.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.3
     */
    public static final String JAVA_SPECIFICATION_VERSION = getSystemProperty("java.specification.version");

    /**
     * <p>The <code>java.util.prefs.PreferencesFactory</code> System Property. A class name.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since 2.1
     * @since Java 1.4
     */
    public static final String JAVA_UTIL_PREFS_PREFERENCES_FACTORY =
        getSystemProperty("java.util.prefs.PreferencesFactory");

    /**
     * <p>The <code>java.vendor</code> System Property. Java vendor-specific string.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.1
     */
    public static final String JAVA_VENDOR = getSystemProperty("java.vendor");

    /**
     * <p>The <code>java.vendor.url</code> System Property. Java vendor URL.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.1
    */
    public static final String JAVA_VENDOR_URL = getSystemProperty("java.vendor.url");

    /**
     * <p>The <code>java.version</code> System Property. Java version number.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.1
     */
    public static final String JAVA_VERSION = getSystemProperty("java.version");

    /**
     * <p>The <code>java.vm.info</code> System Property. Java Virtual Machine implementation
     * info.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since 2.0
     * @since Java 1.2
     */
    public static final String JAVA_VM_INFO = getSystemProperty("java.vm.info");

    /**
     * <p>The <code>java.vm.name</code> System Property. Java Virtual Machine implementation
     * name.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.2
     */
    public static final String JAVA_VM_NAME = getSystemProperty("java.vm.name");

    /**
     * <p>The <code>java.vm.specification.name</code> System Property. Java Virtual Machine
     * specification name.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.2
     */
    public static final String JAVA_VM_SPECIFICATION_NAME = getSystemProperty("java.vm.specification.name");

    /**
     * <p>The <code>java.vm.specification.vendor</code> System Property. Java Virtual
     * Machine specification vendor.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.2
     */
    public static final String JAVA_VM_SPECIFICATION_VENDOR = getSystemProperty("java.vm.specification.vendor");

    /**
     * <p>The <code>java.vm.specification.version</code> System Property. Java Virtual Machine
     * specification version.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.2
     */
    public static final String JAVA_VM_SPECIFICATION_VERSION = getSystemProperty("java.vm.specification.version");

    /**
     * <p>The <code>java.vm.vendor</code> System Property. Java Virtual Machine implementation
     * vendor.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.2
     */
    public static final String JAVA_VM_VENDOR = getSystemProperty("java.vm.vendor");

    /**
     * <p>The <code>java.vm.version</code> System Property. Java Virtual Machine
     * implementation version.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.2
     */
    public static final String JAVA_VM_VERSION = getSystemProperty("java.vm.version");

    /**
     * <p>The <code>line.separator</code> System Property. Line separator
     * (<code>&quot;\n&quot;</code> on UNIX).</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.1
     */
    public static final String LINE_SEPARATOR = getSystemProperty("line.separator");

    /**
     * <p>The <code>os.arch</code> System Property. Operating system architecture.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.1
     */
    public static final String OS_ARCH = getSystemProperty("os.arch");

    /**
     * <p>The <code>os.name</code> System Property. Operating system name.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.1
     */
    public static final String OS_NAME = getSystemProperty("os.name");

    /**
     * <p>The <code>os.version</code> System Property. Operating system version.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.1
     */
    public static final String OS_VERSION = getSystemProperty("os.version");

    /**
     * <p>The <code>path.separator</code> System Property. Path separator
     * (<code>&quot;:&quot;</code> on UNIX).</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.1
     */
    public static final String PATH_SEPARATOR = getSystemProperty("path.separator");

    /**
     * <p>The <code>user.country</code> or <code>user.region</code> System Property.
     * User's country code, such as <code>GB</code>. First in JDK version 1.2 as
     * <code>user.region</code>. Renamed to <code>user.country</code> in 1.4</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since 2.0
     * @since Java 1.2
     */
    public static final String USER_COUNTRY = (getSystemProperty("user.country") == null) ? getSystemProperty("user" +
        ".region") : getSystemProperty("user.country");

    /**
     * <p>The <code>user.dir</code> System Property. User's current working
     * directory.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.1
     */
    public static final String USER_DIR = getSystemProperty(USER_DIR_KEY);

    /**
     * <p>The <code>user.home</code> System Property. User's home directory.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.1
     */
    public static final String USER_HOME = getSystemProperty(USER_HOME_KEY);

    /**
     * <p>The <code>user.language</code> System Property. User's language code,
     * such as <code>"en"</code>.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since 2.0
     * @since Java 1.2
     */
    public static final String USER_LANGUAGE = getSystemProperty("user.language");

    /**
     * <p>The <code>user.name</code> System Property. User's account name.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.1
     */
    public static final String USER_NAME = getSystemProperty("user.name");

    /**
     * <p>The <code>user.timezone</code> System Property.
     * For example: <code>"America/Los_Angeles"</code>.</p>
     *
     * <p>Defaults to <code>null</code> if the runtime does not have
     * security access to read this property or the property does not exist.</p>
     *
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)}
     * or {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value
     * will be out of sync with that System property.
     * </p>
     *
     * @since 2.1
     */
    public static final String USER_TIMEZONE = getSystemProperty("user.timezone");

    // Java version
    //-----------------------------------------------------------------------
    // This MUST be declared after those above as it depends on the
    // values being set up

    /**
     * <p>Gets the Java version as a <code>String</code> trimming leading letters.</p>
     *
     * <p>The field will return <code>null</code> if {@link #JAVA_VERSION} is <code>null</code>.</p>
     *
     * @since 2.1
     */
    public static final String JAVA_VERSION_TRIMMED = getJavaVersionTrimmed();

    // Java version values
    //-----------------------------------------------------------------------
    // These MUST be declared after the trim above as they depend on the
    // value being set up

    /**
     * <p>Gets the Java version as a <code>float</code>.</p>
     *
     * <p>Example return values:</p>
     * <ul>
     *  <li><code>1.2f</code> for JDK 1.2
     *  <li><code>1.31f</code> for JDK 1.3.1
     * </ul>
     *
     * <p>The field will return zero if {@link #JAVA_VERSION} is <code>null</code>.</p>
     *
     * @since 2.0
     */
    public static final float JAVA_VERSION_FLOAT = getJavaVersionAsFloat();

    /**
     * <p>Gets the Java version as an <code>int</code>.</p>
     *
     * <p>Example return values:</p>
     * <ul>
     *  <li><code>120</code> for JDK 1.2
     *  <li><code>131</code> for JDK 1.3.1
     * </ul>
     *
     * <p>The field will return zero if {@link #JAVA_VERSION} is <code>null</code>.</p>
     *
     * @since 2.0
     */
    public static final int JAVA_VERSION_INT = getJavaVersionAsInt();

    // Java version checks
    //-----------------------------------------------------------------------
    // These MUST be declared after those above as they depend on the
    // values being set up

    /**
     * <p>Is <code>true</code> if this is Java version 1.1 (also 1.1.x versions).</p>
     *
     * <p>The field will return <code>false</code> if {@link #JAVA_VERSION} is
     * <code>null</code>.</p>
     */
    public static final boolean IS_JAVA_1_1 = getJavaVersionMatches("1.1");

    /**
     * <p>Is <code>true</code> if this is Java version 1.2 (also 1.2.x versions).</p>
     *
     * <p>The field will return <code>false</code> if {@link #JAVA_VERSION} is
     * <code>null</code>.</p>
     */
    public static final boolean IS_JAVA_1_2 = getJavaVersionMatches("1.2");

    /**
     * <p>Is <code>true</code> if this is Java version 1.3 (also 1.3.x versions).</p>
     *
     * <p>The field will return <code>false</code> if {@link #JAVA_VERSION} is
     * <code>null</code>.</p>
     */
    public static final boolean IS_JAVA_1_3 = getJavaVersionMatches("1.3");

    /**
     * <p>Is <code>true</code> if this is Java version 1.4 (also 1.4.x versions).</p>
     *
     * <p>The field will return <code>false</code> if {@link #JAVA_VERSION} is
     * <code>null</code>.</p>
     */
    public static final boolean IS_JAVA_1_4 = getJavaVersionMatches("1.4");

    /**
     * <p>Is <code>true</code> if this is Java version 1.5 (also 1.5.x versions).</p>
     *
     * <p>The field will return <code>false</code> if {@link #JAVA_VERSION} is
     * <code>null</code>.</p>
     */
    public static final boolean IS_JAVA_1_5 = getJavaVersionMatches("1.5");

    /**
     * <p>Is <code>true</code> if this is Java version 1.6 (also 1.6.x versions).</p>
     *
     * <p>The field will return <code>false</code> if {@link #JAVA_VERSION} is
     * <code>null</code>.</p>
     */
    public static final boolean IS_JAVA_1_6 = getJavaVersionMatches("1.6");

    /**
     * <p>Is <code>true</code> if this is Java version 1.7 (also 1.7.x versions).</p>
     *
     * <p>The field will return <code>false</code> if {@link #JAVA_VERSION} is
     * <code>null</code>.</p>
     *
     * @since 2.5
     */
    public static final boolean IS_JAVA_1_7 = getJavaVersionMatches("1.7");

    // Operating system checks
    //-----------------------------------------------------------------------
    // These MUST be declared after those above as they depend on the
    // values being set up
    // OS names from http://www.vamphq.com/os.html
    // Selected ones included - please advise dev@commons.apache.org
    // if you want another added or a mistake corrected

    /**
     * <p>Is <code>true</code> if this is AIX.</p>
     *
     * <p>The field will return <code>false</code> if <code>OS_NAME</code> is
     * <code>null</code>.</p>
     *
     * @since 2.0
     */
    public static final boolean IS_OS_AIX = getOSMatches("AIX");

    /**
     * <p>Is <code>true</code> if this is HP-UX.</p>
     *
     * <p>The field will return <code>false</code> if <code>OS_NAME</code> is
     * <code>null</code>.</p>
     *
     * @since 2.0
     */
    public static final boolean IS_OS_HP_UX = getOSMatches("HP-UX");

    /**
     * <p>Is <code>true</code> if this is Irix.</p>
     *
     * <p>The field will return <code>false</code> if <code>OS_NAME</code> is
     * <code>null</code>.</p>
     *
     * @since 2.0
     */
    public static final boolean IS_OS_IRIX = getOSMatches("Irix");

    /**
     * <p>Is <code>true</code> if this is Linux.</p>
     *
     * <p>The field will return <code>false</code> if <code>OS_NAME</code> is
     * <code>null</code>.</p>
     *
     * @since 2.0
     */
    public static final boolean IS_OS_LINUX = getOSMatches("Linux") || getOSMatches("LINUX");

    /**
     * <p>Is <code>true</code> if this is Mac.</p>
     *
     * <p>The field will return <code>false</code> if <code>OS_NAME</code> is
     * <code>null</code>.</p>
     *
     * @since 2.0
     */
    public static final boolean IS_OS_MAC = getOSMatches("Mac");

    /**
     * <p>Is <code>true</code> if this is Mac.</p>
     *
     * <p>The field will return <code>false</code> if <code>OS_NAME</code> is
     * <code>null</code>.</p>
     *
     * @since 2.0
     */
    public static final boolean IS_OS_MAC_OSX = getOSMatches("Mac OS X");

    /**
     * <p>Is <code>true</code> if this is OS/2.</p>
     *
     * <p>The field will return <code>false</code> if <code>OS_NAME</code> is
     * <code>null</code>.</p>
     *
     * @since 2.0
     */
    public static final boolean IS_OS_OS2 = getOSMatches("OS/2");

    /**
     * <p>Is <code>true</code> if this is Solaris.</p>
     *
     * <p>The field will return <code>false</code> if <code>OS_NAME</code> is
     * <code>null</code>.</p>
     *
     * @since 2.0
     */
    public static final boolean IS_OS_SOLARIS = getOSMatches("Solaris");

    /**
     * <p>Is <code>true</code> if this is SunOS.</p>
     *
     * <p>The field will return <code>false</code> if <code>OS_NAME</code> is
     * <code>null</code>.</p>
     *
     * @since 2.0
     */
    public static final boolean IS_OS_SUN_OS = getOSMatches("SunOS");

    /**
     * <p>Is <code>true</code> if this is a POSIX compilant system,
     * as in any of AIX, HP-UX, Irix, Linux, MacOSX, Solaris or SUN OS.</p>
     *
     * <p>The field will return <code>false</code> if <code>OS_NAME</code> is
     * <code>null</code>.</p>
     *
     * @since 2.1
     */
    public static final boolean IS_OS_UNIX =
        IS_OS_AIX || IS_OS_HP_UX || IS_OS_IRIX || IS_OS_LINUX ||
        IS_OS_MAC_OSX || IS_OS_SOLARIS || IS_OS_SUN_OS;

    /**
     * <p>Is <code>true</code> if this is Windows.</p>
     *
     * <p>The field will return <code>false</code> if <code>OS_NAME</code> is
     * <code>null</code>.</p>
     *
     * @since 2.0
     */
    public static final boolean IS_OS_WINDOWS = getOSMatches(OS_NAME_WINDOWS_PREFIX);

    /**
     * <p>Is <code>true</code> if this is Windows 2000.</p>
     *
     * <p>The field will return <code>false</code> if <code>OS_NAME</code> is
     * <code>null</code>.</p>
     *
     * @since 2.0
     */
    public static final boolean IS_OS_WINDOWS_2000 = getOSMatches(OS_NAME_WINDOWS_PREFIX, "5.0");

    /**
     * <p>Is <code>true</code> if this is Windows 95.</p>
     *
     * <p>The field will return <code>false</code> if <code>OS_NAME</code> is
     * <code>null</code>.</p>
     *
     * @since 2.0
     */
    public static final boolean IS_OS_WINDOWS_95 = getOSMatches(OS_NAME_WINDOWS_PREFIX + " 9", "4.0");
    // JDK 1.2 running on Windows98 returns 'Windows 95', hence the above

    /**
     * <p>Is <code>true</code> if this is Windows 98.</p>
     *
     * <p>The field will return <code>false</code> if <code>OS_NAME</code> is
     * <code>null</code>.</p>
     *
     * @since 2.0
     */
    public static final boolean IS_OS_WINDOWS_98 = getOSMatches(OS_NAME_WINDOWS_PREFIX + " 9", "4.1");
    // JDK 1.2 running on Windows98 returns 'Windows 95', hence the above

    /**
     * <p>Is <code>true</code> if this is Windows ME.</p>
     *
     * <p>The field will return <code>false</code> if <code>OS_NAME</code> is
     * <code>null</code>.</p>
     *
     * @since 2.0
     */
    public static final boolean IS_OS_WINDOWS_ME = getOSMatches(OS_NAME_WINDOWS_PREFIX, "4.9");
    // JDK 1.2 running on WindowsME may return 'Windows 95', hence the above

    /**
     * <p>Is <code>true</code> if this is Windows NT.</p>
     *
     * <p>The field will return <code>false</code> if <code>OS_NAME</code> is
     * <code>null</code>.</p>
     *
     * @since 2.0
     */
    public static final boolean IS_OS_WINDOWS_NT = getOSMatches(OS_NAME_WINDOWS_PREFIX + " NT");
    // Windows 2000 returns 'Windows 2000' but may suffer from same JDK1.2 problem

    /**
     * <p>Is <code>true</code> if this is Windows XP.</p>
     *
     * <p>The field will return <code>false</code> if <code>OS_NAME</code> is
     * <code>null</code>.</p>
     *
     * @since 2.0
     */
    public static final boolean IS_OS_WINDOWS_XP = getOSMatches(OS_NAME_WINDOWS_PREFIX, "5.1");

    //-----------------------------------------------------------------------
    /**
     * <p>Is <code>true</code> if this is Windows Vista.</p>
     *
     * <p>The field will return <code>false</code> if <code>OS_NAME</code> is
     * <code>null</code>.</p>
     *
     * @since 2.4
     */
    public static final boolean IS_OS_WINDOWS_VISTA = getOSMatches(OS_NAME_WINDOWS_PREFIX, "6.0");

    /**
     * <p>Is <code>true</code> if this is Windows 7.</p>
     *
     * <p>The field will return <code>false</code> if <code>OS_NAME</code> is
     * <code>null</code>.</p>
     *
     * @since 2.5
     */
    public static final boolean IS_OS_WINDOWS_7 = getOSMatches(OS_NAME_WINDOWS_PREFIX, "6.1");

    //-----------------------------------------------------------------------
    /**
     * <p>SystemUtils instances should NOT be constructed in standard
     * programming. Instead, the class should be used as
     * <code>SystemUtils.FILE_SEPARATOR</code>.</p>
     *
     * <p>This constructor is public to permit tools that require a JavaBean
     * instance to operate.</p>
     */
    public SystemUtils() {
        super();
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Gets the Java version number as a <code>float</code>.</p>
     *
     * <p>Example return values:</p>
     * <ul>
     *  <li><code>1.2f</code> for JDK 1.2
     *  <li><code>1.31f</code> for JDK 1.3.1
     * </ul>
     *
     * @return the version, for example 1.31f for JDK 1.3.1
     * @deprecated Use {@link #JAVA_VERSION_FLOAT} instead.
     *             Method will be removed in Commons Lang 3.0.
     */
    public static float getJavaVersion() {
        return JAVA_VERSION_FLOAT;
    }

    /**
     * <p>Gets the Java version number as a <code>float</code>.</p>
     *
     * <p>Example return values:</p>
     * <ul>
     *  <li><code>1.2f</code> for JDK 1.2
     *  <li><code>1.31f</code> for JDK 1.3.1
     * </ul>
     *
     * <p>Patch releases are not reported.
     * Zero is returned if {@link #JAVA_VERSION_TRIMMED} is <code>null</code>.</p>
     *
     * @return the version, for example 1.31f for JDK 1.3.1
     */
    private static float getJavaVersionAsFloat() {
        if (JAVA_VERSION_TRIMMED == null) {
            return 0f;
        }
        String str = StringUtils.substring(JAVA_VERSION_TRIMMED, 0, 3);
        if (JAVA_VERSION_TRIMMED.length() >= 5) {
            str = str + JAVA_VERSION_TRIMMED.substring(4, 5);
        }
        try {
            return Float.parseFloat(str);
        } catch (Exception ex) {
            return 0;
        }
    }

    /**
     * <p>Gets the Java version number as an <code>int</code>.</p>
     *
     * <p>Example return values:</p>
     * <ul>
     *  <li><code>120</code> for JDK 1.2
     *  <li><code>131</code> for JDK 1.3.1
     * </ul>
     *
     * <p>Patch releases are not reported.
     * Zero is returned if {@link #JAVA_VERSION_TRIMMED} is <code>null</code>.</p>
     *
     * @return the version, for example 131 for JDK 1.3.1
     */
    private static int getJavaVersionAsInt() {
        if (JAVA_VERSION_TRIMMED == null) {
            return 0;
        }
        String str = JAVA_VERSION_TRIMMED.substring(0, 1);
        str = str + (JAVA_VERSION_TRIMMED.length() < 3 ? "0" : JAVA_VERSION_TRIMMED.substring(2, 3));
        if (JAVA_VERSION_TRIMMED.length() >= 5) {
            str = str + JAVA_VERSION_TRIMMED.substring(4, 5);
        } else {
            str = str + "0";
        }
        try {
            return Integer.parseInt(str);
        } catch (Exception ex) {
            return 0;
        }
    }

    /**
     * Trims the text of the java version to start with numbers.
     *
     * @return the trimmed java version
     */
    private static String getJavaVersionTrimmed() {
        if (JAVA_VERSION != null) {
            for (int i = 0; i < JAVA_VERSION.length(); i++) {
                char ch = JAVA_VERSION.charAt(i);
                if ((ch >= '0') && (ch <= '9')) {
                    return JAVA_VERSION.substring(i);
                }
            }
        }
        return null;
    }

    /**
     * <p>Decides if the java version matches.</p>
     *
     * @param versionPrefix  the prefix for the java version
     * @return true if matches, or false if not or can't determine
     */
    private static boolean getJavaVersionMatches(String versionPrefix) {
        if (JAVA_VERSION_TRIMMED == null) {
            return false;
        }
        return JAVA_VERSION_TRIMMED.startsWith(versionPrefix);
    }

    /**
     * <p>Decides if the operating system matches.</p>
     *
     * @param osNamePrefix  the prefix for the os name
     * @return true if matches, or false if not or can't determine
     */
    private static boolean getOSMatches(String osNamePrefix) {
        if (OS_NAME == null) {
            return false;
        }
        return OS_NAME.startsWith(osNamePrefix);
    }

    /**
     * <p>Decides if the operating system matches.</p>
     *
     * @param osNamePrefix  the prefix for the os name
     * @param osVersionPrefix  the prefix for the version
     * @return true if matches, or false if not or can't determine
     */
    private static boolean getOSMatches(String osNamePrefix, String osVersionPrefix) {
        if ((OS_NAME == null) || (OS_VERSION == null)) {
            return false;
        }
        return OS_NAME.startsWith(osNamePrefix) && OS_VERSION.startsWith(osVersionPrefix);
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Gets a System property, defaulting to <code>null</code> if the property
     * cannot be read.</p>
     *
     * <p>If a <code>SecurityException</code> is caught, the return
     * value is <code>null</code> and a message is written to <code>System.err</code>.</p>
     *
     * @param property the system property name
     * @return the system property value or <code>null</code> if a security problem occurs
     */
    private static String getSystemProperty(String property) {
        try {
            return System.getProperty(property);
        } catch (SecurityException ex) {
            // we are not allowed to look at this property
            LogService.getLog(SystemUtils.class).severe(
                    LogType.LOG, "Caught a SecurityException reading the system property '" + property
                + "'; the SystemUtils property value will default to null.",
                    ex
            );
            return null;
        }
    }

    /**
     * <p>Is the Java version at least the requested version.</p>
     *
     * <p>Example input:</p>
     * <ul>
     *  <li><code>1.2f</code> to test for JDK 1.2</li>
     *  <li><code>1.31f</code> to test for JDK 1.3.1</li>
     * </ul>
     *
     * @param requiredVersion  the required version, for example 1.31f
     * @return <code>true</code> if the actual version is equal or greater
     *  than the required version
     */
    public static boolean isJavaVersionAtLeast(float requiredVersion) {
        return JAVA_VERSION_FLOAT >= requiredVersion;
    }

    /**
     * <p>Is the Java version at least the requested version.</p>
     *
     * <p>Example input:</p>
     * <ul>
     *  <li><code>120</code> to test for JDK 1.2 or greater</li>
     *  <li><code>131</code> to test for JDK 1.3.1 or greater</li>
     * </ul>
     *
     * @param requiredVersion  the required version, for example 131
     * @return <code>true</code> if the actual version is equal or greater
     *  than the required version
     * @since 2.0
     */
    public static boolean isJavaVersionAtLeast(int requiredVersion) {
        return JAVA_VERSION_INT >= requiredVersion;
    }

    /**
     * Returns whether the {@link #JAVA_AWT_HEADLESS} value is <code>true</code>.
     *
     * @return <code>true</code> if <code>JAVA_AWT_HEADLESS</code> is <code>"true"</code>,
     * <code>false</code> otherwise.
     *
     * @see #JAVA_AWT_HEADLESS
     * @since 2.1
     * @since Java 1.4
     */
    public static boolean isJavaAwtHeadless() {
        return (JAVA_AWT_HEADLESS != null) ? JAVA_AWT_HEADLESS.equals(Boolean.TRUE.toString()) : false;
    }
    /**
     * <p>Gets the Java home directory as a <code>File</code>.</p>
     *
     * @return a directory
     * @throws  SecurityException  if a security manager exists and its
     *             <code>checkPropertyAccess</code> method doesn't allow
     *              access to the specified system property.
     * @see System#getProperty(String)
     * @since 2.1
     */
    public static File getJavaHome() {
        return new File(System.getProperty(JAVA_HOME_KEY));
    }

    /**
     * <p>Gets the Java IO temporary directory as a <code>File</code>.</p>
     *
     * @return a directory
     * @throws  SecurityException  if a security manager exists and its
     *             <code>checkPropertyAccess</code> method doesn't allow
     *              access to the specified system property.
     * @see System#getProperty(String)
     * @since 2.1
     */
    public static File getJavaIoTmpDir() {
        return new File(System.getProperty(JAVA_IO_TMPDIR_KEY));
    }

    /**
     * <p>Gets the user directory as a <code>File</code>.</p>
     *
     * @return a directory
     * @throws  SecurityException  if a security manager exists and its
     *             <code>checkPropertyAccess</code> method doesn't allow
     *              access to the specified system property.
     * @see System#getProperty(String)
     * @since 2.1
     */
    public static File getUserDir() {
        return new File(System.getProperty(USER_DIR_KEY));
    }

    /**
     * <p>Gets the user home directory as a <code>File</code>.</p>
     *
     * @return a directory
     * @throws  SecurityException  if a security manager exists and its
     *             <code>checkPropertyAccess</code> method doesn't allow
     *              access to the specified system property.
     * @see System#getProperty(String)
     * @since 2.1
     */
    public static File getUserHome() {
        return new File(System.getProperty(USER_HOME_KEY));
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows ");
    }

}

/*
 * $Header: /home/cvs/jakarta-commons-sandbox/cli/src/java/org/apache/commons/cli/TypeHandler.java,v 1.2 2002/06/06 22:49:36 bayard Exp $
 * $Revision: 1.2 $
 * $Date: 2002/06/06 22:49:36 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package liquibase.migrator.commandline.cli;

import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Date;


/**
  * This is a temporary implementation. TypeHandler will handle the 
  * pluggableness of OptionTypes and it will direct all of these types 
  * of conversion functionalities to ConvertUtils component in Commons 
  * alreayd. BeanUtils I think.
  *
  * @author Henri Yandell (bayard @ generationjava.com)
  * @version $Revision: 1.2 $
  */    
public class TypeHandler {

    /**
     * <p>Returns the <code>Object</code> of type <code>obj</code>
     * with the value of <code>str</code>.</p>
     *
     * @param str the command line value
     * @param obj the type of argument
     * @return The instance of <code>obj</code> initialised with
     * the value of <code>str</code>.
     */
    public static Object createValue(String str, Object obj) {
        return createValue(str, (Class)obj);
    }

    /**
     * <p>Returns the <code>Object</code> of type <code>clazz</code>
     * with the value of <code>str</code>.</p>
     *
     * @param str the command line value
     * @param clazz the type of argument
     * @return The instance of <code>clazz</code> initialised with
     * the value of <code>str</code>.
     */
    public static Object createValue(String str, Class clazz) {
        if( PatternOptionBuilder.STRING_VALUE == clazz) {
            return str;
        } else
        if( PatternOptionBuilder.OBJECT_VALUE == clazz) {
            return createObject(str);
        } else
        if( PatternOptionBuilder.NUMBER_VALUE == clazz) {
            return createNumber(str);
        } else
        if( PatternOptionBuilder.DATE_VALUE   == clazz) {
            return createDate(str);
        } else
        if( PatternOptionBuilder.CLASS_VALUE  == clazz) {
            return createClass(str);
        } else
        if( PatternOptionBuilder.FILE_VALUE   == clazz) {
            return createFile(str);
        } else
        if( PatternOptionBuilder.EXISTING_FILE_VALUE   == clazz) {
            return createFile(str);
        } else
        if( PatternOptionBuilder.FILES_VALUE  == clazz) {
            return createFiles(str);
        } else
        if( PatternOptionBuilder.URL_VALUE    == clazz) {
            return createURL(str);
        } else {
            return null;
        }
    }

    /**
      * <p>Create an Object from the classname and empty constructor.</p>
      *
      * @param str the argument value
      * @return the initialised object, or null if it couldn't create the Object.
      */
    public static Object createObject(String str) {
        Class cl = null;
        try {
            cl = Class.forName(str);
        } catch (ClassNotFoundException cnfe) {
            System.err.println("Unable to find: "+str);
            return null;
        }

        Object instance = null;

        try {
            instance = cl.newInstance();
        } catch (InstantiationException cnfe) {
            System.err.println("InstantiationException; Unable to create: "+str);
            return null;
        }
        catch (IllegalAccessException cnfe) {
            System.err.println("IllegalAccessException; Unable to create: "+str);
            return null;
        }

        return instance;
    }

    /**
     * <p>Create a number from a String.</p>
     *
     * @param str the value
     * @return the number represented by <code>str</code>, if <code>str</code>
     * is not a number, null is returned.
     */
    public static Number createNumber(String str) {
        // Needs to be able to create
        try {
            // do searching for decimal point etc, but atm just make an Integer
            if (str.indexOf(".") >= 0) {
                return Double.valueOf(str);
            } else {
                return Integer.valueOf(str);
            }
        } catch (NumberFormatException nfe) {
            System.err.println(nfe.getMessage());
            return null;
        }
    }

    /**
     * <p>Returns the class whose name is <code>str</code>.</p>
     *
     * @param str the class name
     * @return The class if it is found, otherwise return null
     */
    public static Class createClass(String str) {
        try {
            return Class.forName(str);
        } catch (ClassNotFoundException cnfe) {
            System.err.println("Unable to find: "+str);
            return null;
        }
    }

    /**
     * <p>Returns the date represented by <code>str</code>.</p>
     *
     * @param str the date string
     * @return The date if <code>str</code> is a valid date string,
     * otherwise return null.
     */
    public static Date createDate(String str) {
        Date date = null;
        if(date == null) {
            System.err.println("Unable to parse: "+str);
        }
        return date;
    }

    /**
     * <p>Returns the URL represented by <code>str</code>.</p>
     *
     * @param str the URL string
     * @return The URL is <code>str</code> is well-formed, otherwise
     * return null.
     */
    public static URL createURL(String str) {
        try {
            return new URL(str);
        } catch (MalformedURLException mue) {
            System.err.println("Unable to parse: "+str);
            return null;
        }
    }

    /**
     * <p>Returns the File represented by <code>str</code>.</p>
     *
     * @param str the File location
     * @return The file represented by <code>str</code>.
     */
    public static File createFile(String str) {
        return new File(str);
    }

    /**
     * <p>Returns the File[] represented by <code>str</code>.</p>
     *
     * @param str the paths to the files
     * @return The File[] represented by <code>str</code>.
     */
    public static File[] createFiles(String str) {
// to implement/port:
//        return FileW.findFiles(str);
        return null;
    }

}

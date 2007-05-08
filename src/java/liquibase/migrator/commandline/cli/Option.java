/*
 * $Header: /home/cvs/jakarta-commons-sandbox/cli/src/java/org/apache/commons/cli/Option.java,v 1.6 2002/06/06 22:50:14 bayard Exp $
 * $Revision: 1.6 $
 * $Date: 2002/06/06 22:50:14 $
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

/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 * 
 * $Id: Option.java,v 1.6 2002/06/06 22:50:14 bayard Exp $
 */

package liquibase.migrator.commandline.cli;

import java.util.ArrayList;

/**
 * <p>Describes a single command-line option.  It maintains
 * information regarding the short-name of the option, the long-name,
 * if any exists, a flag indicating if an argument is required for
 * this option, and a self-documenting description of the option.</p>
 * <p/>
 * <p>An Option is not created independantly, but is create through
 * an instance of {@link Options}.<p>
 *
 * @author bob mcwhirter (bob @ werken.com)
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.6 $
 * @see org.apache.commons.cli.Options
 * @see org.apache.commons.cli.CommandLine
 */

public class Option implements Cloneable {

    /**
     * constant that specifies the number of argument values has not been specified
     */
    public static final int UNINITIALIZED = -1;

    /**
     * constant that specifies the number of argument values is infinite
     */
    public static final int UNLIMITED_VALUES = -2;

    /**
     * opt the single character representation of the option
     */
    private String opt;

    /**
     * longOpt is the long representation of the option
     */
    private String longOpt;

    /**
     * hasArg specifies whether this option has an associated argument
     */
    private boolean hasArg;

    /**
     * argName specifies the name of the argument for this option
     */
    private String argName;

    /**
     * description of the option
     */
    private String description;

    /**
     * required specifies whether this option is required to be present
     */
    private boolean required;

    /**
     * specifies whether the argument value of this Option is optional
     */
    private boolean optionalArg;

    /**
     * numberOfArgs specifies the number of argument values this option
     * can have
     */
    private int numberOfArgs = UNINITIALIZED;

    /**
     * the type of this Option
     */
    private Object type;

    /**
     * the list of argument values *
     */
    private ArrayList values = new ArrayList();

    /**
     * option char (only valid for single character options)
     */
    private char id;

    /**
     * the character that is the value separator
     */
    private char valuesep;

    /**
     * <p>Validates whether <code>opt</code> is a permissable Option
     * shortOpt.  The rules that specify if the <code>opt</code>
     * is valid are:</p>
     * <ul>
     * <li><code>opt</code> is not NULL</li>
     * <li>a single character <code>opt</code> that is either
     * ' '(special case), '?', '@' or a letter</li>
     * <li>a multi character <code>opt</code> that only contains
     * letters.</li>
     * </ul>
     *
     * @param opt The option string to validate
     * @throws IllegalArgumentException if the Option is not valid.
     */
    private void validateOption(String opt)
            throws IllegalArgumentException {
        // check that opt is not NULL
        if (opt == null) {
            throw new IllegalArgumentException("opt is null");
        }
        // handle the single character opt
        else if (opt.length() == 1) {
            char ch = opt.charAt(0);
            if (!isValidOpt(ch)) {
                throw new IllegalArgumentException("illegal option value '"
                        + ch + "'");
            }
            id = ch;
        }
        // handle the multi character opt
        else {
            char[] chars = opt.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (!isValidChar(chars[i])) {
                    throw new IllegalArgumentException("opt contains illegal character value '" + chars[i] + "'");
                }
            }
        }
    }

    /**
     * <p>Returns whether the specified character is a valid Option.</p>
     *
     * @param c the option to validate
     * @return true if <code>c</code> is a letter, ' ', '?' or '@', otherwise false.
     */
    private boolean isValidOpt(char c) {
        return (isValidChar(c) || c == ' ' || c == '?' || c == '@');
    }

    /**
     * <p>Returns whether the specified character is a valid character.</p>
     *
     * @param c the character to validate
     * @return true if <code>c</code> is a letter.
     */
    private boolean isValidChar(char c) {
        return Character.isJavaIdentifierPart(c);
    }

    /**
     * <p>Returns the id of this Option.  This is only set when the
     * Option shortOpt is a single character.  This is used for switch
     * statements.</p>
     *
     * @return the id of this Option
     */
    public int getId() {
        return id;
    }

    /**
     * Creates an Option using the specified parameters.
     *
     * @param opt         short representation of the option
     * @param hasArg      specifies whether the Option takes an argument or not
     * @param description describes the function of the option
     */
    public Option(String opt, String description)
            throws IllegalArgumentException {
        this(opt, null, false, description);
    }

    /**
     * Creates an Option using the specified parameters.
     *
     * @param opt         short representation of the option
     * @param hasArg      specifies whether the Option takes an argument or not
     * @param description describes the function of the option
     */
    public Option(String opt, boolean hasArg, String description)
            throws IllegalArgumentException {
        this(opt, null, hasArg, description);
    }

    /**
     * <p>Creates an Option using the specified parameters.</p>
     *
     * @param opt         short representation of the option
     * @param longOpt     the long representation of the option
     * @param hasArg      specifies whether the Option takes an argument or not
     * @param description describes the function of the option
     */
    public Option(String opt, String longOpt, boolean hasArg, String description)
            throws IllegalArgumentException {
        // ensure that the option is valid
        validateOption(opt);

        this.opt = opt;
        this.longOpt = longOpt;

        // if hasArg is set then the number of arguments is 1
        if (hasArg) {
            this.numberOfArgs = 1;
        }

        this.hasArg = hasArg;
        this.description = description;
    }

    /**
     * <p>Retrieve the name of this Option.</p>
     * <p/>
     * <p>It is this String which can be used with
     * {@link CommandLine#hasOption(String opt)} and
     * {@link CommandLine#getOptionValue(String opt)} to check
     * for existence and argument.<p>
     *
     * @return The name of this option
     */
    public String getOpt() {
        return this.opt;
    }

    /**
     * <p>Retrieve the type of this Option.</p>
     *
     * @return The type of this option
     */
    public Object getType() {
        return this.type;
    }

    /**
     * <p>Sets the type of this Option.</p>
     *
     * @param type the type of this Option
     */
    public void setType(Object type) {
        this.type = type;
    }

    /**
     * <p>Retrieve the long name of this Option.</p>
     *
     * @return Long name of this option, or null, if there is no long name
     */
    public String getLongOpt() {
        return this.longOpt;
    }

    /**
     * <p>Sets the long name of this Option.</p>
     *
     * @param longOpt the long name of this Option
     */
    public void setLongOpt(String longOpt) {
        this.longOpt = longOpt;
    }

    /**
     * <p>Sets whether this Option can have an optional argument.</p>
     *
     * @param optionalArg specifies whether the Option can have
     *                    an optional argument.
     */
    public void setOptionalArg(boolean optionalArg) {
        this.optionalArg = optionalArg;
    }

    /**
     * @return whether this Option can have an optional argument
     */
    public boolean hasOptionalArg() {
        return this.optionalArg;
    }

    /**
     * <p>Query to see if this Option has a long name</p>
     *
     * @return boolean flag indicating existence of a long name
     */
    public boolean hasLongOpt() {
        return (this.longOpt != null);
    }

    /**
     * <p>Query to see if this Option requires an argument</p>
     *
     * @return boolean flag indicating if an argument is required
     */
    public boolean hasArg() {
        return this.numberOfArgs > 0 || numberOfArgs == UNLIMITED_VALUES;
    }

    /**
     * <p>Retrieve the self-documenting description of this Option</p>
     *
     * @return The string description of this option
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * <p>Query to see if this Option requires an argument</p>
     *
     * @return boolean flag indicating if an argument is required
     */
    public boolean isRequired() {
        return this.required;
    }

    /**
     * <p>Sets whether this Option is mandatory.</p>
     *
     * @param required specifies whether this Option is mandatory
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * <p>Sets the display name for the argument value.</p>
     *
     * @param argName the display name for the argument value.
     */
    public void setArgName(String argName) {
        this.argName = argName;
    }

    /**
     * <p>Gets the display name for the argument value.</p>
     *
     * @return the display name for the argument value.
     */
    public String getArgName() {
        return this.argName;
    }

    /**
     * <p>Returns whether the display name for the argument value
     * has been set.</p>
     *
     * @return if the display name for the argument value has been
     *         set.
     */
    public boolean hasArgName() {
        return (this.argName != null && this.argName.length() > 0);
    }

    /**
     * <p>Query to see if this Option can take many values</p>
     *
     * @return boolean flag indicating if multiple values are allowed
     */
    public boolean hasArgs() {
        return (this.numberOfArgs > 1 || this.numberOfArgs == UNLIMITED_VALUES);
    }

    /**
     * <p>Sets the number of argument values this Option can take.</p>
     *
     * @param num the number of argument values
     */
    public void setArgs(int num) {
        this.numberOfArgs = num;
    }

    /**
     * <p>Sets the value separator.  For example if the argument value
     * was a Java property, the value separator would be '='.</p>
     *
     * @param sep The value separator.
     */
    public void setValueSeparator(char sep) {
        this.valuesep = sep;
    }

    /**
     * <p>Returns the value separator character.</p>
     *
     * @return the value separator character.
     */
    public char getValueSeparator() {
        return this.valuesep;
    }

    /**
     * <p>Returns the number of argument values this Option can take.</p>
     *
     * @return num the number of argument values
     */
    public int getArgs() {
        return this.numberOfArgs;
    }

    /**
     * <p>Dump state, suitable for debugging.</p>
     *
     * @return Stringified form of this object
     */
    public String toString() {
        StringBuffer buf = new StringBuffer().append("[ option: ");

        buf.append(this.opt);

        if (this.longOpt != null) {
            buf.append(" ")
                    .append(this.longOpt);
        }

        buf.append(" ");

        if (hasArg) {
            buf.append("+ARG");
        }

        buf.append(" :: ")
                .append(this.description);

        if (this.type != null) {
            buf.append(" :: ")
                    .append(this.type);
        }

        buf.append(" ]");
        return buf.toString();
    }

    /**
     * <p>Adds the specified value to this Option.</p>
     *
     * @param value is a/the value of this Option
     */
    public boolean addValue(String value) {

        switch (numberOfArgs) {
            case UNINITIALIZED:
                return false;
            case UNLIMITED_VALUES:
                if (getValueSeparator() > 0) {
                    int index = 0;
                    while ((index = value.indexOf(getValueSeparator())) != -1) {
                        this.values.add(value.substring(0, index));
                        value = value.substring(index + 1);
                    }
                }
                this.values.add(value);
                return true;
            default:
                if (getValueSeparator() > 0) {
                    int index = 0;
                    while ((index = value.indexOf(getValueSeparator())) != -1) {
                        if (values.size() > numberOfArgs - 1) {
                            return false;
                        }
                        this.values.add(value.substring(0, index));
                        value = value.substring(index + 1);
                    }
                }
                if (values.size() > numberOfArgs - 1) {
                    return false;
                }
                this.values.add(value);
                return true;
        }
    }

    /**
     * @return the value/first value of this Option or
     *         <code>null</code> if there are no values.
     */
    public String getValue() {
        return this.values.size() == 0 ? null : (String) this.values.get(0);
    }

    /**
     * @return the specified value of this Option or
     *         <code>null</code> if there are no values.
     */
    public String getValue(int index)
            throws IndexOutOfBoundsException {
        return (this.values.size() == 0) ? null : (String) this.values.get(index);
    }

    /**
     * @return the value/first value of this Option or the
     *         <code>defaultValue</code> if there are no values.
     */
    public String getValue(String defaultValue) {
        String value = getValue();
        return (value != null) ? value : defaultValue;
    }

    /**
     * @return the values of this Option as a String array
     *         or null if there are no values
     */
    public String[] getValues() {
        return this.values.size() == 0 ? null : (String[]) this.values.toArray(new String[]{});
    }

    /**
     * @return the values of this Option as a List
     *         or null if there are no values
     */
    public java.util.List getValuesList() {
        return this.values;
    }

    /**
     * @return a copy of this Option
     */
    public Object clone() {
        Option option = new Option(getOpt(), getDescription());
        option.setArgs(getArgs());
        option.setOptionalArg(hasOptionalArg());
        option.setRequired(isRequired());
        option.setLongOpt(getLongOpt());
        option.setType(getType());
        option.setValueSeparator(getValueSeparator());
        return option;
    }
}

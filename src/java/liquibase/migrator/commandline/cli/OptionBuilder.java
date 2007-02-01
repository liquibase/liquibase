/*
 * $Header: /home/cvs/jakarta-commons/cli/src/java/org/apache/commons/cli/OptionBuilder.java,v 1.12 2002/10/15 22:50:45 jkeyes Exp $
 * $Revision: 1.12 $
 * $Date: 2002/10/15 22:50:45 $
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

/**
 * <p>OptionBuilder allows the user to create Options using descriptive
 * methods.</p>
 * <p>Details on the Builder pattern can be found at 
 * <a href="http://c2.com/cgi-bin/wiki?BuilderPattern">http://c2.com/cgi-bin/wiki?BuilderPattern</a>.</p>
 *
 * @author John Keyes ( john at integralsource.com )
 * @since 1.0
 */
public class OptionBuilder {

    /** long option */
    private static String longopt;
    /** option description */
    private static String description;
    /** argument name */
    private static String argName;
    /** is required? */
    private static boolean required;
    /** the number of arguments */
    private static int numberOfArgs = Option.UNINITIALIZED;
    /** option type */
    private static Object type;
    /** option can have an optional argument value */
    private static boolean optionalArg;
    /** value separator for argument value */
    private static char valuesep;

    /** option builder instance */
    private static OptionBuilder instance = new OptionBuilder();

    // private constructor
    private OptionBuilder() {
    }

    /**
     * <p>Resets the member variables to their default values.</p>
     */
    private static void reset() {
        description = null;
        argName = null;
        longopt = null;
        type = null;
        required = false;
        numberOfArgs = Option.UNINITIALIZED;

        // PMM 9/6/02 - these were missing
        optionalArg = false;
        valuesep = (char) 0;
    }

    /**
     * <p>The next Option created will have the following long option value.</p>
     *
     * @param longopt the long option value
     * @return the OptionBuilder instance
     */
    public static OptionBuilder withLongOpt( String longopt ) {
        instance.longopt = longopt;
        return instance;
    }

    /**
     * <p>The next Option created will require an argument value.</p>
     *
     * @return the OptionBuilder instance
     */
    public static OptionBuilder hasArg( ) {
        instance.numberOfArgs = 1;
        return instance;
    }

    /**
     * <p>The next Option created will require an argument value if
     * <code>hasArg</code> is true.</p>
     *
     * @param hasArg if true then the Option has an argument value
     * @return the OptionBuilder instance
     */
    public static OptionBuilder hasArg( boolean hasArg ) {
        instance.numberOfArgs = ( hasArg == true ) ? 1 : Option.UNINITIALIZED;
        return instance;
    }

    /**
     * <p>The next Option created will have the specified argument value 
     * name.</p>
     *
     * @param name the name for the argument value
     * @return the OptionBuilder instance
     */
    public static OptionBuilder withArgName( String name ) {
        instance.argName = name;
        return instance;
    }

    /**
     * <p>The next Option created will be required.</p>
     *
     * @return the OptionBuilder instance
     */
    public static OptionBuilder isRequired( ) {
        instance.required = true;
        return instance;
    }

    /**
     * <p>The next Option created uses <code>sep</code> as a means to
     * separate argument values.</p>
     *
     * <b>Example:</b>
     * <pre>
     * Option opt = OptionBuilder.withValueSeparator( ':' )
     *                           .create( 'D' );
     *
     * CommandLine line = parser.parse( args );
     * String propertyName = opt.getValue( 0 );
     * String propertyValue = opt.getValue( 1 );
     * </pre>
     *
     * @return the OptionBuilder instance
     */
    public static OptionBuilder withValueSeparator( char sep ) {
        instance.valuesep = sep;
        return instance;
    }

    /**
     * <p>The next Option created uses '<code>=</code>' as a means to
     * separate argument values.</p>
     *
     * <b>Example:</b>
     * <pre>
     * Option opt = OptionBuilder.withValueSeparator( )
     *                           .create( 'D' );
     *
     * CommandLine line = parser.parse( args );
     * String propertyName = opt.getValue( 0 );
     * String propertyValue = opt.getValue( 1 );
     * </pre>
     *
     * @return the OptionBuilder instance
     */
    public static OptionBuilder withValueSeparator( ) {
        instance.valuesep = '=';
        return instance;
    }

    /**
     * <p>The next Option created will be required if <code>required</code>
     * is true.</p>
     *
     * @param required if true then the Option is required
     * @return the OptionBuilder instance
     */
    public static OptionBuilder isRequired( boolean required ) {
        instance.required = required;
        return instance;
    }

    /**
     * <p>The next Option created can have unlimited argument values.</p>
     *
     * @return the OptionBuilder instance
     */
    public static OptionBuilder hasArgs( ) {
        instance.numberOfArgs = Option.UNLIMITED_VALUES;
        return instance;
    }

    /**
     * <p>The next Option created can have <code>num</code> 
     * argument values.</p>
     *
     * @param num the number of args that the option can have
     * @return the OptionBuilder instance
     */
    public static OptionBuilder hasArgs( int num ) {
        instance.numberOfArgs = num;
        return instance;
    }

    /**
     * <p>The next Option can have an optional argument.</p>
     *
     * @return the OptionBuilder instance
     */
    public static OptionBuilder hasOptionalArg( ) {
        instance.numberOfArgs = 1;
        instance.optionalArg = true;
        return instance;
    }

    /**
     * <p>The next Option can have an unlimited number of
     * optional arguments.</p>
     *
     * @return the OptionBuilder instance
     */
    public static OptionBuilder hasOptionalArgs( ) {
        instance.numberOfArgs = Option.UNLIMITED_VALUES;
        instance.optionalArg = true;
        return instance;
    }

    /**
     * <p>The next Option can have the specified number of 
     * optional arguments.</p>
     *
     * @param numArgs - the maximum number of optional arguments
     * the next Option created can have.
     * @return the OptionBuilder instance
     */
    public static OptionBuilder hasOptionalArgs( int numArgs ) {
        instance.numberOfArgs = numArgs;
        instance.optionalArg = true;
        return instance;
    }

    /**
     * <p>The next Option created will have a value that will be an instance 
     * of <code>type</code>.</p>
     *
     * @param type the type of the Options argument value
     * @return the OptionBuilder instance
     */
    public static OptionBuilder withType( Object type ) {
        instance.type = type;
        return instance;
    }

    /**
     * <p>The next Option created will have the specified description</p>
     *
     * @param description a description of the Option's purpose
     * @return the OptionBuilder instance
     */
    public static OptionBuilder withDescription( String description ) {
        instance.description = description;
        return instance;
    }

    /**
     * <p>Create an Option using the current settings and with 
     * the specified Option <code>char</code>.</p>
     *
     * @param opt the character representation of the Option
     * @return the Option instance
     * @throws IllegalArgumentException if <code>opt</code> is not
     * a valid character.  See Option.
     */
    public static Option create( char opt )
    throws IllegalArgumentException
    {
        return create( String.valueOf( opt ) );
    }

    /**
     * <p>Create an Option using the current settings</p>
     *
     * @return the Option instance
     * @throws IllegalArgumentException if <code>longOpt</code> has
     * not been set.  
     */
    public static Option create() 
    throws IllegalArgumentException
    {
        if( longopt == null ) {
            throw new IllegalArgumentException( "must specify longopt" );
        }

        return create( " " );
    }

    /**
     * <p>Create an Option using the current settings and with 
     * the specified Option <code>char</code>.</p>
     *
     * @param opt the <code>java.lang.String</code> representation 
     * of the Option
     * @return the Option instance
     * @throws IllegalArgumentException if <code>opt</code> is not
     * a valid character.  See Option.
     */
    public static Option create( String opt ) 
    throws IllegalArgumentException
    {
        // create the option
        Option option = new Option( opt, description );

        // set the option properties
        option.setLongOpt( longopt );
        option.setRequired( required );
        option.setOptionalArg( optionalArg );
        option.setArgs( numberOfArgs );
        option.setType( type );
        option.setValueSeparator( valuesep );
        option.setArgName( argName );
        // reset the OptionBuilder properties
        instance.reset();

        // return the Option instance
        return option;
    }
}
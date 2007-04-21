/*
 * $Header: /home/cvs/jakarta-commons-sandbox/cli/src/java/org/apache/commons/cli/Options.java,v 1.5 2002/06/06 22:32:37 bayard Exp $
 * $Revision: 1.5 $
 * $Date: 2002/06/06 22:32:37 $
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.*;

/** <p>Main entry-point into the library.</p>
 *
 * <p>Options represents a collection of {@link Option} objects, which
 * describe the possible options for a command-line.<p>
 *
 * <p>It may flexibly parse long and short options, with or without
 * values.  Additionally, it may parse only a portion of a commandline,
 * allowing for flexible multi-stage parsing.<p>
 *
 * @see org.apache.commons.cli.CommandLine
 *
 * @author bob mcwhirter (bob @ werken.com)
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.5 $
 */
public class Options {

    /** a map of the options with the character key */
    private Map  shortOpts    = new HashMap();

    /** a map of the options with the long key */
    private Map  longOpts     = new HashMap();

    /** a map of the required options */
    private List requiredOpts = new ArrayList();
    
    /** a map of the option groups */
    private Map optionGroups  = new HashMap();

    /** <p>Construct a new Options descriptor</p>
     */
    public Options() {        
    }

    /**
     * <p>Add the specified option group.</p>
     *
     * @param group the OptionGroup that is to be added
     * @return the resulting Options instance
     */
    public Options addOptionGroup( OptionGroup group ) {
        Iterator options = group.getOptions().iterator();

        if( group.isRequired() ) {
            requiredOpts.add( group );
        }

        while( options.hasNext() ) {
            Option option = (Option)options.next();
            // an Option cannot be required if it is in an
            // OptionGroup, either the group is required or
            // nothing is required
            option.setRequired( false );
            addOption( option );

            optionGroups.put( option.getOpt(), group );
        }

        return this;
    }

    /** <p>Add an option that only contains a short-name</p>
     * <p>It may be specified as requiring an argument.</p>
     *
     * @param opt Short single-character name of the option.
     * @param hasArg flag signally if an argument is required after this option
     * @param description Self-documenting description
     * @return the resulting Options instance
     */
    public Options addOption(String opt, boolean hasArg, String description) {
        addOption( opt, null, hasArg, description );
        return this;
    }
    
    /** <p>Add an option that contains a short-name and a long-name</p>
     * <p>It may be specified as requiring an argument.</p>
     *
     * @param opt Short single-character name of the option.
     * @param longOpt Long multi-character name of the option.
     * @param hasArg flag signally if an argument is required after this option
     * @param description Self-documenting description
     * @return the resulting Options instance
     */
    public Options addOption(String opt, String longOpt, boolean hasArg, String description) {
        addOption( new Option( opt, longOpt, hasArg, description ) );        
        return this;
    }

    /**
     * <p>Adds an option instance</p>
     *
     * @param opt the option that is to be added 
     * @return the resulting Options instance
     */
    public Options addOption(Option opt)  {
        String shortOpt = "-" + opt.getOpt();
        
        // add it to the long option list
        if ( opt.hasLongOpt() ) {
            longOpts.put( "--" + opt.getLongOpt(), opt );
        }
        
        // if the option is required add it to the required list
        if ( opt.isRequired() ) {
            requiredOpts.add( shortOpt );
        }

        shortOpts.put( shortOpt, opt );
        
        return this;
    }
    
    /** <p>Retrieve a read-only list of options in this set</p>
     *
     * @return read-only Collection of {@link Option} objects in this descriptor
     */
    public Collection getOptions() {
        List opts = new ArrayList( shortOpts.values() );

        // now look through the long opts to see if there are any Long-opt
        // only options
        Iterator iter = longOpts.values().iterator();
        while (iter.hasNext())
        {
            
        	
        	Object item = iter.next();
            System.out.println("sadasda"+item);
        	if (!opts.contains(item))
            {
                opts.add(item);
            }
        }
        return Collections.unmodifiableCollection( opts );
    }

    /**
     * <p>Returns the Options for use by the HelpFormatter.</p>
     *
     * @return the List of Options
     */
    List helpOptions() {
        return new ArrayList( shortOpts.values() );
    }

    /** <p>Returns the required options as a 
     * <code>java.util.Collection</code>.</p>
     *
     * @return Collection of required options
     */
    public List getRequiredOptions() {
        return requiredOpts;
    }
    
    /** <p>Retrieve the named {@link Option}</p>
     *
     * @param opt short or long name of the {@link Option}
     * @return the option represented by opt
     */
    public Option getOption( String opt ) {

        Option option = null;

        // short option
        if( opt.length() == 1 ) {
            option = (Option)shortOpts.get( "-" + opt );
        }
        // long option
        else if( opt.startsWith( "--" ) ) {
            option = (Option)longOpts.get( opt );
        }
        // a just-in-case
        else {
            option = (Option)shortOpts.get( opt );
        }

        return (option == null) ? null : (Option)option.clone();
    }

    /** 
     * <p>Returns whether the named {@link Option} is a member
     * of this {@link Options}</p>
     *
     * @param opt short or long name of the {@link Option}
     * @return true if the named {@link Option} is a member
     * of this {@link Options}
     */
    public boolean hasOption( String opt ) {

        // short option
        if( opt.length() == 1 ) {
            return shortOpts.containsKey( "-" + opt );
        }
        // long option
        else if( opt.startsWith( "--" ) ) {
            return longOpts.containsKey( opt );
        }
        // a just-in-case
        else {
            return shortOpts.containsKey( opt );
        }
    }

    /** <p>Returns the OptionGroup the <code>opt</code>
     * belongs to.</p>
     * @param opt the option whose OptionGroup is being queried.
     *
     * @return the OptionGroup if <code>opt</code> is part
     * of an OptionGroup, otherwise return null
     */
    public OptionGroup getOptionGroup( Option opt ) {
        return (OptionGroup)optionGroups.get( opt.getOpt() );
    }
    
    /** <p>Dump state, suitable for debugging.</p>
     *
     * @return Stringified form of this object
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        
        buf.append("[ Options: [ short ");
        buf.append( shortOpts.toString() );
        buf.append( " ] [ long " );
        buf.append( longOpts );
        buf.append( " ]");
        
        return buf.toString();
    }
}

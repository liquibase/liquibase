/*
 * $Header: /home/cvs/jakarta-commons-sandbox/cli/src/java/org/apache/commons/cli/OptionGroup.java,v 1.2 2002/06/06 09:37:26 jstrachan Exp $
 * $Revision: 1.2 $
 * $Date: 2002/06/06 09:37:26 $
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A group of mutually exclusive options.
 * @author John Keyes ( john at integralsource.com )
 * @version $Revision: 1.2 $
 */
public class OptionGroup {

    /** hold the options */
    private HashMap optionMap = new HashMap();

    /** the name of the selected option */
    private String selected;

    /** specified whether this group is required */
    private boolean required;

    /**
     * add <code>opt</code> to this group
     *
     * @param opt the option to add to this group
     * @return this option group with opt added
     */
    public OptionGroup addOption(Option opt) {
        // key   - option name
        // value - the option
        optionMap.put( "-" + opt.getOpt(), opt );
        return this;
    }

    /**
     * @return the names of the options in this group as a 
     * <code>Collection</code>
     */
    public Collection getNames() {
        // the key set is the collection of names
        return optionMap.keySet();
    }

    /**
     * @return the options in this group as a <code>Collection</code>
     */
    public Collection getOptions() {
        // the values are the collection of options
        return optionMap.values();
    }

    /**
     * set the selected option of this group to <code>name</code>.
     * @param opt the option that is selected
     * @throws AlreadySelectedException if an option from this group has 
     * already been selected.
     */
    public void setSelected(Option opt) throws AlreadySelectedException {
        // if no option has already been selected or the 
        // same option is being reselected then set the
        // selected member variable

        if ( this.selected == null || this.selected.equals( opt.getOpt() ) ) {
            this.selected = opt.getOpt();
        }
        else {
            throw new AlreadySelectedException( "an option from this group has " + 
                                                "already been selected: '" + 
                                                selected + "'");
        }
    }

    /**
     * @return the selected option name
     */
    public String getSelected() {
        return selected;
    }

    /**
     * @param required specifies if this group is required
     */
    public void setRequired( boolean required ) {
        this.required = required;
    }

    /**
     * Returns whether this option group is required.
     *
     * @returns whether this option group is required
     */
    public boolean isRequired() {
        return this.required;
    }

    /**
     * <p>Returns the stringified version of this OptionGroup.</p>
     * @return the stringified representation of this group
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();

        Iterator iter = getOptions().iterator();

        buff.append( "[" );
        while( iter.hasNext() ) {
            Option option = (Option)iter.next();

            buff.append( "-" );
            buff.append( option.getOpt() );
            buff.append( " " );
            buff.append( option.getDescription( ) );

            if( iter.hasNext() ) {
                buff.append( ", " );
            }
        }
        buff.append( "]" );

        return buff.toString();
    }
}

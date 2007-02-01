/*
 * $Header: /home/cvs/jakarta-commons/cli/src/java/org/apache/commons/cli/GnuParser.java,v 1.10 2002/09/19 22:59:43 jkeyes Exp $
 * $Revision: 1.10 $
 * $Date: 2002/09/19 22:59:43 $
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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * The class GnuParser provides an implementation of the 
 * {@link Parser#flatten(Options,String[],boolean) flatten} method.
 *
 * @author John Keyes (john at integralsource.com)
 * @see Parser
 * @version $Revision: 1.10 $
 */
public class GnuParser extends Parser {

    /** holder for flattened tokens */
    private ArrayList tokens = new ArrayList();

    /**
     * <p>Resets the members to their original state i.e. remove
     * all of <code>tokens</code> entries.
     */
    private void init() {
        tokens.clear();
    }

    /**
     * <p>This flatten method does so using the following rules:
     * <ol>
     *  <li>If an {@link Option} exists for the first character of 
     *  the <code>arguments</code> entry <b>AND</b> an {@link Option} 
     *  does not exist for the whole <code>argument</code> then
     *  add the first character as an option to the processed tokens
     *  list e.g. "-D" and add the rest of the entry to the also.</li>
     *  <li>Otherwise just add the token to the processed tokens list.
     *  </li>
     * </ol>
     * </p>
     */
    protected String[] flatten( Options options, 
                                String[] arguments, 
                                boolean stopAtNonOption )
    {
        init();
        boolean eatTheRest = false;
        Option currentOption = null;

        for( int i = 0; i < arguments.length; i++ ) {
            if( "--".equals( arguments[i] ) ) {
                eatTheRest = true;
                tokens.add( "--" );
            }
            else if ( "-".equals( arguments[i] ) ) {
                tokens.add( "-" );
            }
            else if( arguments[i].startsWith( "-" ) ) {
                Option option = options.getOption( arguments[i] );

                // this is not an Option
                if( option == null ) {
                    // handle special properties Option
                    Option specialOption = options.getOption( arguments[i].substring(0,2) );
                    if( specialOption != null ) {
                        tokens.add( arguments[i].substring(0,2) );
                        tokens.add( arguments[i].substring(2) );
                    }
                    else if( stopAtNonOption ) {
                        eatTheRest = true;
                        tokens.add( arguments[i] );
                    }
                    else {
                        tokens.add( arguments[i] );
                    }
                }
                else {
                    currentOption = option;
                    // special option
                    Option specialOption = options.getOption( arguments[i].substring(0,2) );
                    if( specialOption != null && option == null ) {
                        tokens.add( arguments[i].substring(0,2) );
                        tokens.add( arguments[i].substring(2) );
                    }
                    else if( currentOption != null && currentOption.hasArg() ) {
                        if( currentOption.hasArg() ) {
                            tokens.add( arguments[i] );
                            currentOption= null;
                        }
                        else if ( currentOption.hasArgs() ) {
                            tokens.add( arguments[i] );
                        }
                        else if ( stopAtNonOption ) {
                            eatTheRest = true;
                            tokens.add( "--" );
                            tokens.add( arguments[i] );
                        }
                        else {
                            tokens.add( arguments[i] );
                        }
                    } 
                    else if (currentOption != null ) {
                        tokens.add( arguments[i] );
                    } 
                    else if ( stopAtNonOption ) {
                        eatTheRest = true;
                        tokens.add( "--" );
                        tokens.add( arguments[i] );
                    }
                    else {
                        tokens.add( arguments[i] );
                    }
                }
            }
            else {
                tokens.add( arguments[i] );
            }

            if( eatTheRest ) {
                for( i++; i < arguments.length; i++ ) {
                    tokens.add( arguments[i] );
                }
            }
        }
        return (String[])tokens.toArray( new String[] {} );
    }
}
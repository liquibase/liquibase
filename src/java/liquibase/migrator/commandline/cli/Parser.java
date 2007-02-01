/*
 * $Header: /home/cvs/jakarta-commons/cli/src/java/org/apache/commons/cli/Parser.java,v 1.7 2002/10/24 23:17:49 jkeyes Exp $
 * $Revision: 1.7 $
 * $Date: 2002/10/24 23:17:49 $
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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * <p><code>Parser</code> creates {@link CommandLine}s.</p>
 *
 * @author John Keyes (john at integralsource.com)
 * @see Parser
 * @version $Revision: 1.7 $
 */
public abstract class Parser implements CommandLineParser {

    /** commandline instance */
    private CommandLine cmd;
    /** current Options */
    private Options options;
    /** list of required options strings */
    private List requiredOptions;

    /**
     * <p>Subclasses must implement this method to reduce
     * the <code>arguments</code> that have been passed to the parse 
     * method.</p>
     *
     * @param opts The Options to parse the arguments by.
     * @param args The arguments that have to be flattened.
     * @param stopAtNonOption specifies whether to stop 
     * flattening when a non option has been encountered
     * @return a String array of the flattened arguments
     */
    abstract protected String[] flatten( Options opts, 
                                         String[] arguments, 
                                         boolean stopAtNonOption );

    /**
     * <p>Parses the specified <code>arguments</code> 
     * based on the specifed {@link Options}.</p>
     *
     * @param options the <code>Options</code>
     * @param arguments the <code>arguments</code>
     * @return the <code>CommandLine</code>
     * @throws ParseException if an error occurs when parsing the
     * arguments.
     */
    public CommandLine parse( Options options, String[] arguments ) 
    throws ParseException 
    {
        return parse( options, arguments, false );
    }

    /**
     * <p>Parses the specified <code>arguments</code> 
     * based on the specifed {@link Options}.</p>
     *
     * @param options the <code>Options</code>
     * @param arguments the <code>arguments</code>
     * @param stopAtNonOption specifies whether to stop 
     * interpreting the arguments when a non option has 
     * been encountered and to add them to the CommandLines
     * args list.
     * @return the <code>CommandLine</code>
     * @throws ParseException if an error occurs when parsing the
     * arguments.
     */
    public CommandLine parse( Options opts, 
                              String[] arguments, 
                              boolean stopAtNonOption ) 
    throws ParseException 
    {
        // initialise members
        options = opts;
        requiredOptions = options.getRequiredOptions();
        cmd = new CommandLine();

        boolean eatTheRest = false;

        List tokenList = Arrays.asList( flatten( opts, arguments, stopAtNonOption ) );
        ListIterator iterator = tokenList.listIterator();

        // process each flattened token
        while( iterator.hasNext() ) {
            String t = (String)iterator.next();

            // the value is the double-dash
            if( "--".equals( t ) ) {
                eatTheRest = true;
            }
            // the value is a single dash
            else if( "-".equals( t ) ) {
                if( stopAtNonOption ) {
                    eatTheRest = true;
                }
                else {
                    cmd.addArg(t );
                }
            }
            // the value is an option
            else if( t.startsWith( "-" ) ) {
                if ( stopAtNonOption && !options.hasOption( t ) ) {
                    eatTheRest = true;
                    cmd.addArg( t );
                }
                else {
                    processOption( t, iterator );
                }
            }
            // the value is an argument
            else {
                cmd.addArg( t );
                if( stopAtNonOption ) {
                    eatTheRest = true;
                }
            }

            // eat the remaining tokens
            if( eatTheRest ) {
                while( iterator.hasNext() ) {
                    String str = (String)iterator.next();
                    // ensure only one double-dash is added
                    if( !"--".equals( str ) ) {
                        cmd.addArg( str );
                    }
                }
            }
        }
        checkRequiredOptions();
        return cmd;
    }

    /**
     * <p>Throws a {@link MissingOptionException} if all of the
     * required options are no present.</p>
     */
    private void checkRequiredOptions()
    throws MissingOptionException 
    {

        // if there are required options that have not been
        // processsed
        if( requiredOptions.size() > 0 ) {
            Iterator iter = requiredOptions.iterator();
            StringBuffer buff = new StringBuffer();

            // loop through the required options
            while( iter.hasNext() ) {
                buff.append( iter.next() );
            }

            throw new MissingOptionException( buff.toString() );
        }
    }

    public void processArgs( Option opt, ListIterator iter ) 
    throws ParseException
    {
        // loop until an option is found
        while( iter.hasNext() ) {
            String var = (String)iter.next();

            // found an Option
            if( options.hasOption( var ) ) {
                iter.previous();
                break;
            }
            // found a value
            else if( !opt.addValue( var ) ) {
                iter.previous();
                break;
            }
        }

        if( opt.getValues() == null && !opt.hasOptionalArg() ) {
            throw new MissingArgumentException( "no argument for:" + opt.getOpt() );
        }
    }

    private void processOption( String arg, ListIterator iter ) 
    throws ParseException
    {
        // get the option represented by arg
        Option opt = null;

        boolean hasOption = options.hasOption( arg );

        // if there is no option throw an UnrecognisedOptionException
        if( !hasOption ) {
            throw new UnrecognizedOptionException("Unrecognized option: " + arg);
        }
        else {
            opt = (Option) options.getOption( arg );
        }

        // if the option is a required option remove the option from
        // the requiredOptions list
        if ( opt.isRequired() ) {
            requiredOptions.remove( "-" + opt.getOpt() );
        }

        // if the option is in an OptionGroup make that option the selected
        // option of the group
        if ( options.getOptionGroup( opt ) != null ) {
            OptionGroup group = ( OptionGroup ) options.getOptionGroup( opt );
            if( group.isRequired() ) {
                requiredOptions.remove( group );
            }
            group.setSelected( opt );
        }

        // if the option takes an argument value
        if ( opt.hasArg() ) {
            processArgs( opt, iter );
        }

        // set the option on the command line
        cmd.addOption( opt );
    }
}
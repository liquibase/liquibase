/*
 * $Header: /home/cvs/jakarta-commons/cli/src/java/org/apache/commons/cli/CommandLineParser.java,v 1.4 2002/09/19 22:59:43 jkeyes Exp $
 * $Revision: 1.4 $
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

/**
 * A class that implements the <code>CommandLineParser</code> interface 
 * can parse a String array according to the {@link Options} specified
 * and return a {@link CommandLine}.
 *
 * @author John Keyes (john at integralsource.com)
 */
public interface CommandLineParser {
    
    /**
     * Parse the arguments according to the specified options.
     *
     * @param options the specified Options
     * @param arguments the command line arguments
     * @return the list of atomic option and value tokens
     * @throws ParseException if there are any problems encountered
     * while parsing the command line tokens.
     */
    public CommandLine parse( Options options, String[] arguments )
    throws ParseException;

    /**
     * Parse the arguments according to the specified options.
     *
     * @param options the specified Options
     * @param arguments the command line arguments
     * @param stopAtNonOption specifies whether to continue parsing the
     * arguments if a non option is encountered.
     * @return the list of atomic option and value tokens
     * @throws ParseException if there are any problems encountered
     * while parsing the command line tokens.
     */
    public CommandLine parse( Options options, String[] arguments, boolean stopAtNonOption )
    throws ParseException;
}
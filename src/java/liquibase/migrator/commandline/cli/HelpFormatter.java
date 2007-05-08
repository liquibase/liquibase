/*
 * $Header: /home/cvs/jakarta-commons-sandbox/cli/src/java/org/apache/commons/cli/CommandLine.java,v 1.4 2002/06/06 22:32:37 bayard Exp $
 * $Revision: 1.4 $
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

import java.io.PrintWriter;
import java.util.*;

/**
 * A formatter of help messages for the current command line options
 *
 * @author Slawek Zachcial
 * @author John Keyes (john at integralsource.com)
 */
public class HelpFormatter {
    // --------------------------------------------------------------- Constants

    public static final int DEFAULT_WIDTH = 74;
    public static final int DEFAULT_LEFT_PAD = 1;
    public static final int DEFAULT_DESC_PAD = 3;
    public static final String DEFAULT_SYNTAX_PREFIX = "usage: ";
    public static final String DEFAULT_OPT_PREFIX = "-";
    public static final String DEFAULT_LONG_OPT_PREFIX = "--";
    public static final String DEFAULT_ARG_NAME = "arg";

    // ------------------------------------------------------------------ Static

    // -------------------------------------------------------------- Attributes

    public int defaultWidth;
    public int defaultLeftPad;
    public int defaultDescPad;
    public String defaultSyntaxPrefix;
    public String defaultNewLine;
    public String defaultOptPrefix;
    public String defaultLongOptPrefix;
    public String defaultArgName;

    // ------------------------------------------------------------ Constructors
    public HelpFormatter() {
        defaultWidth = DEFAULT_WIDTH;
        defaultLeftPad = DEFAULT_LEFT_PAD;
        defaultDescPad = DEFAULT_DESC_PAD;
        defaultSyntaxPrefix = DEFAULT_SYNTAX_PREFIX;
        defaultNewLine = System.getProperty("line.separator");
        defaultOptPrefix = DEFAULT_OPT_PREFIX;
        defaultLongOptPrefix = DEFAULT_LONG_OPT_PREFIX;
        defaultArgName = DEFAULT_ARG_NAME;
    }

    // ------------------------------------------------------------------ Public

    public void printHelp(String cmdLineSyntax,
                          Options options) {
        printHelp(defaultWidth, cmdLineSyntax, null, options, null, false);
    }

    public void printHelp(String cmdLineSyntax,
                          Options options,
                          boolean autoUsage) {
        printHelp(defaultWidth, cmdLineSyntax, null, options, null, autoUsage);
    }

    public void printHelp(String cmdLineSyntax,
                          String header,
                          Options options,
                          String footer) {
        printHelp(cmdLineSyntax, header, options, footer, false);
    }

    public void printHelp(String cmdLineSyntax,
                          String header,
                          Options options,
                          String footer,
                          boolean autoUsage) {
        printHelp(defaultWidth, cmdLineSyntax, header, options, footer, autoUsage);
    }

    public void printHelp(int width,
                          String cmdLineSyntax,
                          String header,
                          Options options,
                          String footer) {
        printHelp(width, cmdLineSyntax, header, options, footer, false);
    }

    public void printHelp(int width,
                          String cmdLineSyntax,
                          String header,
                          Options options,
                          String footer,
                          boolean autoUsage) {
        PrintWriter pw = new PrintWriter(System.out);
        printHelp(pw, width, cmdLineSyntax, header,
                options, defaultLeftPad, defaultDescPad, footer, autoUsage);
        pw.flush();
    }

    public void printHelp(PrintWriter pw,
                          int width,
                          String cmdLineSyntax,
                          String header,
                          Options options,
                          int leftPad,
                          int descPad,
                          String footer)
            throws IllegalArgumentException {
        printHelp(pw, width, cmdLineSyntax, header, options, leftPad, descPad, footer, false);
    }

    public void printHelp(PrintWriter pw,
                          int width,
                          String cmdLineSyntax,
                          String header,
                          Options options,
                          int leftPad,
                          int descPad,
                          String footer,
                          boolean autoUsage)
            throws IllegalArgumentException {
        if (cmdLineSyntax == null || cmdLineSyntax.length() == 0) {
            throw new IllegalArgumentException("cmdLineSyntax not provided");
        }

        if (autoUsage) {
            printUsage(pw, width, cmdLineSyntax, options);
        } else {
            printUsage(pw, width, cmdLineSyntax);
        }

        if (header != null && header.trim().length() > 0) {
            printWrapped(pw, width, header);
        }
        printOptions(pw, width, options, leftPad, descPad);
        if (footer != null && footer.trim().length() > 0) {
            printWrapped(pw, width, footer);
        }
    }

    /**
     * <p>Prints the usage statement for the specified application.</p>
     *
     * @param pw      The PrintWriter to print the usage statement
     * @param width   ??
     * @param options The command line Options
     */
    public void printUsage(PrintWriter pw, int width, String app, Options options) {
        // initialise the string buffer
        StringBuffer buff = new StringBuffer(defaultSyntaxPrefix).append(app).append(" ");

        // create a list for processed option groups
        ArrayList list = new ArrayList();

        // temp variable
        Option option;

        // iterate over the options
        for (Iterator i = options.getOptions().iterator(); i.hasNext();) {
            // get the next Option
            option = (Option) i.next();

            // check if the option is part of an OptionGroup
            OptionGroup group = options.getOptionGroup(option);

            // if the option is part of a group and the group has not already
            // been processed
            if (group != null && !list.contains(group)) {

                // add the group to the processed list
                list.add(group);

                // get the names of the options from the OptionGroup
                Collection names = group.getNames();

                buff.append("[");

                // for each option in the OptionGroup
                for (Iterator iter = names.iterator(); iter.hasNext();) {
                    buff.append(iter.next());
                    if (iter.hasNext()) {
                        buff.append(" | ");
                    }
                }
                buff.append("]");
            }
            // if the Option is not part of an OptionGroup
            else {
                // if the Option is not a required option
                if (!option.isRequired()) {
                    buff.append("[");
                }

                if (!" ".equals(option.getOpt())) {
                    buff.append("-").append(option.getOpt());
                } else {
                    buff.append("--").append(option.getLongOpt());
                }

                if (option.hasArg()) {
                    buff.append(" ");
                }

                // if the Option has a value
                if (option.hasArg()) {
                    buff.append(option.getArgName());
                }

                // if the Option is not a required option
                if (!option.isRequired()) {
                    buff.append("]");
                }
                buff.append(" ");
            }
        }

        // call printWrapped
        printWrapped(pw, width, buff.toString().indexOf(' ') + 1,
                buff.toString());
    }

    public void printUsage(PrintWriter pw, int width, String cmdLineSyntax) {
        int argPos = cmdLineSyntax.indexOf(' ') + 1;
        printWrapped(pw, width, defaultSyntaxPrefix.length() + argPos,
                defaultSyntaxPrefix + cmdLineSyntax);
    }

    public void printOptions(PrintWriter pw, int width, Options options, int leftPad, int descPad) {
        StringBuffer sb = new StringBuffer();
        renderOptions(sb, width, options, leftPad, descPad);
        pw.println(sb.toString());
    }

    public void printWrapped(PrintWriter pw, int width, String text) {
        printWrapped(pw, width, 0, text);
    }

    public void printWrapped(PrintWriter pw, int width, int nextLineTabStop, String text) {
        StringBuffer sb = new StringBuffer(text.length());
        renderWrappedText(sb, width, nextLineTabStop, text);
        pw.println(sb.toString());
    }

    // --------------------------------------------------------------- Protected

    protected StringBuffer renderOptions(StringBuffer sb,
                                         int width,
                                         Options options,
                                         int leftPad,
                                         int descPad) {
        final String lpad = createPadding(leftPad);
        final String dpad = createPadding(descPad);

        //first create list containing only <lpad>-a,--aaa where -a is opt and --aaa is
        //long opt; in parallel look for the longest opt string
        //this list will be then used to sort options ascending
        int max = 0;
        StringBuffer optBuf;
        List prefixList = new ArrayList();
        Option option;
        List optList = options.helpOptions();
        Collections.sort(optList, new StringBufferComparator());
        for (Iterator i = optList.iterator(); i.hasNext();) {
            option = (Option) i.next();
            optBuf = new StringBuffer(8);

            if (" ".equals(option.getOpt())) {
                optBuf.append(lpad).append("   ").append(defaultLongOptPrefix).append(option.getLongOpt());
            } else {
                optBuf.append(lpad).append(defaultOptPrefix).append(option.getOpt());
                if (option.hasLongOpt()) {
                    optBuf.append(',').append(defaultLongOptPrefix).append(option.getLongOpt());
                }

            }

            if (option.hasArg()) {
                if (option.hasArgName()) {
                    optBuf.append(" <").append(option.getArgName()).append('>');
                } else {
                    optBuf.append(' ');
                }
            }

            prefixList.add(optBuf);
            max = optBuf.length() > max ? optBuf.length() : max;
        }
        int x = 0;
        for (Iterator i = optList.iterator(); i.hasNext();) {
            option = (Option) i.next();
            optBuf = new StringBuffer(prefixList.get(x++).toString());

            if (optBuf.length() < max) {
                optBuf.append(createPadding(max - optBuf.length()));
            }
            optBuf.append(dpad);

            int nextLineTabStop = max + descPad;
            renderWrappedText(sb, width, nextLineTabStop,
                    optBuf.append(option.getDescription()).toString());
            if (i.hasNext()) {
                sb.append(defaultNewLine);
            }
        }

        return sb;
    }

    protected StringBuffer renderWrappedText(StringBuffer sb,
                                             int width,
                                             int nextLineTabStop,
                                             String text) {
        int pos = findWrapPos(text, width, 0);
        if (pos == -1) {
            sb.append(rtrim(text));
            return sb;
        } else {
            sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);
        }

        //all following lines must be padded with nextLineTabStop space characters
        final String padding = createPadding(nextLineTabStop);

        while (true) {
            text = padding + text.substring(pos).trim();
            pos = findWrapPos(text, width, nextLineTabStop);
            if (pos == -1) {
                sb.append(text);
                return sb;
            }

            sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);
        }

    }

    /**
     * Finds the next text wrap position after <code>startPos</code> for the text
     * in <code>sb</code> with the column width <code>width</code>.
     * The wrap point is the last postion before startPos+width having a whitespace
     * character (space, \n, \r).
     *
     * @param width    width of the wrapped text
     * @param startPos position from which to start the lookup whitespace character
     * @return postion on which the text must be wrapped or -1 if the wrap position is at the end
     *         of the text
     */
    protected int findWrapPos(String text, int width, int startPos) {
        int pos = -1;
        // the line ends before the max wrap pos or a new line char found
        if (((pos = text.indexOf('\n', startPos)) != -1 && pos <= width) ||
                ((pos = text.indexOf('\t', startPos)) != -1 && pos <= width)) {
            return pos;
        } else if ((startPos + width) >= text.length()) {
            return -1;
        }

        //look for the last whitespace character before startPos+width
        pos = startPos + width;
        char c;
        while (pos >= startPos && (c = text.charAt(pos)) != ' ' && c != '\n' && c != '\r') {
            --pos;
        }
        //if we found it - just return
        if (pos > startPos) {
            return pos;
        } else {
            //must look for the first whitespace chearacter after startPos + width
            pos = startPos + width;
            while (pos <= text.length() && (c = text.charAt(pos)) != ' ' && c != '\n' && c != '\r') {
                ++pos;
            }
            return pos == text.length() ? -1 : pos;
        }
    }

    protected String createPadding(int len) {
        StringBuffer sb = new StringBuffer(len);
        for (int i = 0; i < len; ++i) {
            sb.append(' ');
        }
        return sb.toString();
    }

    protected String rtrim(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }

        int pos = s.length();
        while (pos >= 0 && Character.isWhitespace(s.charAt(pos - 1))) {
            --pos;
        }
        return s.substring(0, pos);
    }

    // ------------------------------------------------------- Package protected

    // ----------------------------------------------------------------- Private

    // ----------------------------------------------------------- Inner classes

    private static class StringBufferComparator
            implements Comparator {
        public int compare(Object o1, Object o2) {
            String str1 = stripPrefix(o1.toString());
            String str2 = stripPrefix(o2.toString());
            return (str1.compareTo(str2));
        }

        private String stripPrefix(String strOption) {
            // Strip any leading '-' characters
            int iStartIndex = strOption.lastIndexOf('-');
            if (iStartIndex == -1) {
                iStartIndex = 0;
            }
            return strOption.substring(iStartIndex);

        }
    }
}

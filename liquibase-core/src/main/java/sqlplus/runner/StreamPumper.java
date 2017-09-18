/*
*  Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  See the NOTICE file distributed with
*  this work for additional information regarding copyright ownership.
*  The ASF licenses this file to You under the Apache License, Version 2.0
*  (the "License"); you may not use this file except in compliance with
*  the License.  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
*/

package sqlplus.runner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

// Inner class for continually pumping the input stream during
// Process's runtime.

@Deprecated
public class StreamPumper extends Thread {
    private BufferedReader din;
    private boolean endOfStream = false;
    private static final int SLEEP_TIME = 5;

    public StreamPumper(InputStream is) {
        this.din = new BufferedReader(new InputStreamReader(is));
    }

    protected void outputLog(String line) {
        System.out.println(line);
    }

    public void pumpStream() throws IOException {
        if (!endOfStream) {
            String line = din.readLine();

            if (line != null) {

               if ( !line.contains("Connected to:") &&
                       !line.contains("Oracle Database 11g Enterprise Edition Release 11.2.0.4.0 - 64bit Production") &&
                       !line.contains("With the Partitioning, Oracle Label Security, OLAP, Data Mining") &&
                       !line.contains("and Real Application Testing options")&&
                       !line.contains("Copyright (c) 1982, 2010, Oracle.  All rights reserved.") &&
                       !line.contains("SQL*Plus: Release 11.2.0.1.0 Production") &&
                       !line.contains("set echo off") &&
                       !line.contains("@@exit.sql") &&
                       !line.isEmpty())
               {
                   System.out.println(line);
               }

            } else {
                endOfStream = true;
            }
        }
    }

    public void run() {
        try {
            try {
                while (!endOfStream) {
                    pumpStream();
                    sleep(SLEEP_TIME);
                }
            } catch (InterruptedException ie) {
                //ignore
            }
            din.close();
        } catch (IOException ioe) {
            // ignore
        }
    }
}

/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.io

import org.gradle.util.MultithreadedTestRule
import org.gradle.util.TextUtil
import org.junit.Rule
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.assertThat

class LinePerThreadBufferingOutputStreamTest {

    @Rule
    public MultithreadedTestRule parallel = new MultithreadedTestRule();

    @Test
    public void interleavesLinesFromEachThread() {
        List<String> output = [].asSynchronized()
        TextStream action = { String line -> output << line.replace(TextUtil.platformLineSeparator, "<EOL>") } as TextStream
        LinePerThreadBufferingOutputStream outstr = new LinePerThreadBufferingOutputStream(action)
        10.times {
            parallel.start {
                100.times {
                    outstr.write('write '.getBytes())
                    outstr.print(it)
                    outstr.println()
                }
            }
        }
        parallel.waitForAll()

        assertThat(output.size(), equalTo(1000))
        assertThat(output.findAll({!it.matches('write \\d+<EOL>')}), equalTo([]))
    }
}

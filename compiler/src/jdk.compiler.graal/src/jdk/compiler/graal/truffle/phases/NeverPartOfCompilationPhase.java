/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package jdk.compiler.graal.truffle.phases;

import jdk.compiler.graal.core.common.GraalBailoutException;
import jdk.compiler.graal.nodes.StructuredGraph;
import jdk.compiler.graal.nodes.util.GraphUtil;
import jdk.compiler.graal.phases.BasePhase;
import jdk.compiler.graal.truffle.nodes.asserts.NeverPartOfCompilationNode;
import jdk.compiler.graal.truffle.TruffleTierContext;

public final class NeverPartOfCompilationPhase extends BasePhase<TruffleTierContext> {
    @Override
    protected void run(StructuredGraph graph, TruffleTierContext context) {
        graph.checkCancellation();
        for (NeverPartOfCompilationNode neverPartOfCompilationNode : graph.getNodes(NeverPartOfCompilationNode.TYPE)) {
            final NeverPartOfCompilationException neverPartOfCompilationException = new NeverPartOfCompilationException(neverPartOfCompilationNode.getMessage());
            neverPartOfCompilationException.setStackTrace(GraphUtil.approxSourceStackTraceElement(neverPartOfCompilationNode));
            throw neverPartOfCompilationException;
        }
    }

    @SuppressWarnings("serial")
    private static class NeverPartOfCompilationException extends GraalBailoutException {

        NeverPartOfCompilationException(String message) {
            super(null, message, new Object[]{});
        }

        @Override
        public boolean isCausedByCompilerAssert() {
            return true;
        }
    }
}
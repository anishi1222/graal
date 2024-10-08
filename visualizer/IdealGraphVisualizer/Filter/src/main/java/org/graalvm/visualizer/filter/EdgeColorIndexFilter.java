/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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
package org.graalvm.visualizer.filter;

import org.graalvm.visualizer.graph.Connection;
import org.graalvm.visualizer.graph.Diagram;
import org.graalvm.visualizer.graph.Figure;
import org.graalvm.visualizer.graph.InputSlot;
import org.graalvm.visualizer.graph.OutputSlot;
import org.graalvm.visualizer.graph.Slot;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

public class EdgeColorIndexFilter extends AbstractFilter {

    public static final String INPUTS = "INPUTS";
    public static final String OUTPUTS = "OUTPUTS";
    private final String applyTo;
    private final Color[] colors;

    public EdgeColorIndexFilter(String applyTo, Color... color) {
        if (!applyTo.equals(INPUTS) && !applyTo.equals(OUTPUTS)) {
            throw new IllegalArgumentException("applyTo");
        }

        this.applyTo = applyTo;
        this.colors = color;
    }

    @Override
    public String getName() {
        return "Edge Color Index Filter";
    }

    @Override
    public void apply(Diagram d) {
        Collection<Figure> figures = d.getFigures();
        for (Figure f : figures) {
            Slot[] slots;
            if (applyTo.equals(INPUTS)) {
                List<InputSlot> inputSlots = f.getInputSlots();
                slots = inputSlots.toArray(new Slot[inputSlots.size()]);
            } else {
                List<OutputSlot> outputSlots = f.getOutputSlots();
                slots = outputSlots.toArray(new Slot[outputSlots.size()]);
            }
            int index = 0;
            for (Slot slot : slots) {
                checkCancelled();
                if (index < colors.length && colors[index] != null) {
                    slot.setColor(colors[index]);
                    for (Connection c : slot.getConnections()) {

                        c.setColor(colors[index]);
                    }
                }
                index++;
            }

        }
    }
}

/*
 * Copyright (c) 2014, 2021, Oracle and/or its affiliates. All rights reserved.
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
package jdk.compiler.graal.nodes.extended;

import static jdk.compiler.graal.nodeinfo.InputType.Guard;
import static jdk.compiler.graal.nodeinfo.NodeCycles.CYCLES_0;
import static jdk.compiler.graal.nodeinfo.NodeSize.SIZE_0;

import org.graalvm.collections.EconomicSet;
import jdk.compiler.graal.core.common.type.StampFactory;
import jdk.compiler.graal.graph.Node;
import jdk.compiler.graal.graph.NodeBitMap;
import jdk.compiler.graal.graph.NodeClass;
import jdk.compiler.graal.graph.NodeInputList;
import jdk.compiler.graal.nodes.spi.Canonicalizable;
import jdk.compiler.graal.nodes.spi.CanonicalizerTool;
import jdk.compiler.graal.nodes.spi.Simplifiable;
import jdk.compiler.graal.nodes.spi.SimplifierTool;
import jdk.compiler.graal.nodeinfo.NodeInfo;
import jdk.compiler.graal.nodes.StructuredGraph;
import jdk.compiler.graal.nodes.ValueNode;
import jdk.compiler.graal.nodes.calc.FloatingNode;
import jdk.compiler.graal.nodes.spi.LIRLowerable;
import jdk.compiler.graal.nodes.spi.NodeLIRBuilderTool;
import jdk.compiler.graal.nodes.util.GraphUtil;

import java.util.ArrayList;

@NodeInfo(allowedUsageTypes = Guard, cycles = CYCLES_0, size = SIZE_0)
public final class MultiGuardNode extends FloatingNode implements GuardingNode, LIRLowerable, Simplifiable, Canonicalizable, Node.ValueNumberable {
    public static final NodeClass<MultiGuardNode> TYPE = NodeClass.create(MultiGuardNode.class);

    @OptionalInput(Guard) NodeInputList<ValueNode> guards;

    public MultiGuardNode(ValueNode... guards) {
        super(TYPE, StampFactory.forVoid());
        this.guards = new NodeInputList<>(this, guards);
    }

    @Override
    public void generate(NodeLIRBuilderTool generator) {
    }

    @Override
    public Node canonical(CanonicalizerTool tool) {
        // Make sure there are no nulls remaining in the set of guards references.
        guards.trim();
        if (guards.size() == 0) {
            // No guards left => can delete the multi-guard.
            return null;
        } else if (guards.size() == 1) {
            // Only a single guard left => replace multi-guard with that single guard.
            return guards.get(0);
        } else {
            // Merge chain of MultiGuardNodes.
            if (guards.filter(MultiGuardNode.class).isNotEmpty()) {
                final ArrayList<ValueNode> list = new ArrayList<>();
                for (ValueNode guard : guards) {
                    if (guard instanceof MultiGuardNode) {
                        list.addAll(((MultiGuardNode) guard).guards);
                    } else {
                        list.add(guard);
                    }
                }
                return new MultiGuardNode(list.toArray(ValueNode.EMPTY_ARRAY));
            }
        }

        // if the guard contains duplicates we want to create a de-duplicated guard
        NodeBitMap guardsSeen = new NodeBitMap(graph());
        boolean duplicatesFound = false;
        for (ValueNode guard : guards) {
            if (guardsSeen.isMarked(guard)) {
                duplicatesFound = true;
                break;
            }
            guardsSeen.mark(guard);
        }
        if (duplicatesFound) {
            EconomicSet<ValueNode> uniqueGuards = EconomicSet.create();
            for (ValueNode guard : guards) {
                uniqueGuards.add(guard);
            }

            if (uniqueGuards.size() == 1) {
                return uniqueGuards.iterator().next();
            }

            return new MultiGuardNode(uniqueGuards.toArray(new ValueNode[uniqueGuards.size()]));
        }

        return this;
    }

    @Override
    public void simplify(SimplifierTool tool) {
        if (usages().filter(node -> node instanceof ValueAnchorNode).isNotEmpty()) {
            /*
             * For ValueAnchorNode usages, we can optimize MultiGuardNodes away if they depend on
             * zero or one floating nodes (as opposed to fixed nodes).
             */
            Node singleFloatingGuard = null;
            for (ValueNode guard : guards) {
                if (GraphUtil.isFloatingNode(guard)) {
                    if (singleFloatingGuard == null) {
                        singleFloatingGuard = guard;
                    } else if (singleFloatingGuard != guard) {
                        return;
                    }
                }
            }
            for (Node usage : usages().snapshot()) {
                if (usage instanceof ValueAnchorNode) {
                    usage.replaceFirstInput(this, singleFloatingGuard);
                    tool.addToWorkList(usage);
                }
            }
            if (usages().isEmpty()) {
                GraphUtil.killWithUnusedFloatingInputs(this);
            }
        }
    }

    public void addGuard(GuardingNode g) {
        this.guards.add(g.asNode());
    }

    public static GuardingNode combine(GuardingNode first, GuardingNode second) {
        if (first == null) {
            return second;
        } else if (second == null) {
            return first;
        } else {
            StructuredGraph graph = first.asNode().graph();
            return graph.unique(new MultiGuardNode(first.asNode(), second.asNode()));
        }
    }

    public static GuardingNode addGuard(GuardingNode first, GuardingNode second) {
        if (first instanceof MultiGuardNode && second != null) {
            MultiGuardNode multi = (MultiGuardNode) first;
            multi.addGuard(second);
            return multi;
        } else {
            return combine(first, second);
        }
    }
}
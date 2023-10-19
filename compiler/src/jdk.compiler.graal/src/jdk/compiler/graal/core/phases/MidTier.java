/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates. All rights reserved.
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
package jdk.compiler.graal.core.phases;

import jdk.compiler.graal.core.common.GraalOptions;
import jdk.compiler.graal.core.common.SpectrePHTMitigations;
import jdk.compiler.graal.loop.phases.LoopFullUnrollPhase;
import jdk.compiler.graal.loop.phases.LoopPartialUnrollPhase;
import jdk.compiler.graal.loop.phases.LoopPredicationPhase;
import jdk.compiler.graal.loop.phases.LoopSafepointEliminationPhase;
import jdk.compiler.graal.loop.phases.SpeculativeGuardMovementPhase;
import jdk.compiler.graal.nodes.loop.DefaultLoopPolicies;
import jdk.compiler.graal.nodes.loop.LoopPolicies;
import jdk.compiler.graal.options.OptionValues;
import jdk.compiler.graal.phases.common.CanonicalizerPhase;
import jdk.compiler.graal.phases.common.DeoptimizationGroupingPhase;
import jdk.compiler.graal.phases.common.FloatingReadPhase;
import jdk.compiler.graal.phases.common.FrameStateAssignmentPhase;
import jdk.compiler.graal.phases.common.GuardLoweringPhase;
import jdk.compiler.graal.phases.common.InsertGuardFencesPhase;
import jdk.compiler.graal.phases.common.IterativeConditionalEliminationPhase;
import jdk.compiler.graal.phases.common.LockEliminationPhase;
import jdk.compiler.graal.phases.common.LoopSafepointInsertionPhase;
import jdk.compiler.graal.phases.common.MidTierLoweringPhase;
import jdk.compiler.graal.phases.common.OptimizeDivPhase;
import jdk.compiler.graal.phases.common.ReassociationPhase;
import jdk.compiler.graal.phases.common.RemoveValueProxyPhase;
import jdk.compiler.graal.phases.common.VerifyHeapAtReturnPhase;
import jdk.compiler.graal.phases.common.WriteBarrierAdditionPhase;
import jdk.compiler.graal.phases.tiers.MidTierContext;

public class MidTier extends BaseTier<MidTierContext> {

    @SuppressWarnings("this-escape")
    public MidTier(OptionValues options) {
        CanonicalizerPhase canonicalizer = CanonicalizerPhase.create();

        appendPhase(new LockEliminationPhase());

        if (GraalOptions.OptFloatingReads.getValue(options)) {
            appendPhase(new FloatingReadPhase(canonicalizer));
        }

        if (GraalOptions.ConditionalElimination.getValue(options)) {
            appendPhase(new IterativeConditionalEliminationPhase(canonicalizer, true));
        }

        if (GraalOptions.LoopPredication.getValue(options) && !GraalOptions.SpeculativeGuardMovement.getValue(options)) {
            appendPhase(new LoopPredicationPhase(canonicalizer));
        }

        appendPhase(new LoopSafepointEliminationPhase());

        if (GraalOptions.SpeculativeGuardMovement.getValue(options)) {
            appendPhase(new SpeculativeGuardMovementPhase(canonicalizer));
        }

        appendPhase(new GuardLoweringPhase());

        if (SpectrePHTMitigations.Options.SpectrePHTBarriers.getValue(options) == SpectrePHTMitigations.GuardTargets ||
                        SpectrePHTMitigations.Options.SpectrePHTBarriers.getValue(options) == SpectrePHTMitigations.NonDeoptGuardTargets) {
            appendPhase(new InsertGuardFencesPhase());
        }

        if (GraalOptions.VerifyHeapAtReturn.getValue(options)) {
            appendPhase(new VerifyHeapAtReturnPhase());
        }

        appendPhase(new LoopFullUnrollPhase(canonicalizer, createLoopPolicies(options)));
        appendPhase(new RemoveValueProxyPhase(canonicalizer));

        appendPhase(new LoopSafepointInsertionPhase());

        appendPhase(new MidTierLoweringPhase(canonicalizer));

        if (GraalOptions.ConditionalElimination.getValue(options)) {
            appendPhase(new IterativeConditionalEliminationPhase(canonicalizer, false));
        }

        appendPhase(new OptimizeDivPhase(canonicalizer));

        appendPhase(new FrameStateAssignmentPhase());

        if (GraalOptions.PartialUnroll.getValue(options)) {
            LoopPolicies loopPolicies = createLoopPolicies(options);
            appendPhase(new LoopPartialUnrollPhase(loopPolicies, canonicalizer));
        }

        if (GraalOptions.ReassociateExpressions.getValue(options)) {
            appendPhase(new ReassociationPhase(canonicalizer));
        }

        if (GraalOptions.OptDeoptimizationGrouping.getValue(options)) {
            appendPhase(new DeoptimizationGroupingPhase());
        }

        appendPhase(canonicalizer);

        appendPhase(new WriteBarrierAdditionPhase());
    }

    @Override
    public LoopPolicies createLoopPolicies(OptionValues options) {
        return new DefaultLoopPolicies();
    }
}
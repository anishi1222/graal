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

package org.graalvm.visualizer.view;

import org.graalvm.visualizer.util.GraphTypes;
import org.graalvm.visualizer.util.RangeSlider;
import org.graalvm.visualizer.util.RangeSliderModel;
import org.graalvm.visualizer.view.api.TimelineModel;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.beans.BeanInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author sdedic
 */
public class SliderPanel extends javax.swing.JPanel {
    private static final Logger LOG = Logger.getLogger(SliderPanel.class.getName());

    private final TimelineModel timeline;
    private final Map<String, RangeSlider> sliderUIs = new HashMap<>();
    private final String primaryType;
    private final GraphTypes graphTypes;
    private List<RangeSlider> rowOrder = Collections.emptyList();

    /**
     * Creates new SliderPanel.
     *
     * @param primaryType primary type for this slider.
     * @param timeline    the data model
     */
    public SliderPanel(String primaryType, TimelineModel timeline) {
        this.timeline = timeline;
        this.primaryType = primaryType;
        this.graphTypes = Lookup.getDefault().lookup(GraphTypes.class);

        initComponents();
        timeline.addPropertyChangeListener((pe) -> {
            if (TimelineModel.PROP_PARTITIONS.equals(pe.getPropertyName())) {
                updateSliders();
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sliders = new javax.swing.JPanel();

        sliders.setLayout(new java.awt.GridBagLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(sliders, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(sliders, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel sliders;
    // End of variables declaration//GEN-END:variables

    /**
     * Will possibly add more sliders after the timeline changes.
     */
    private void updateSliders() {
        LOG.log(Level.FINE, "{0}: sliders updating", logId());
        Set<String> types = timeline.getPartitionTypes();
        List<RangeSlider> newOrder = new ArrayList<>();

        List<String> keys = new ArrayList<>(types);
        Collections.sort(keys, graphTypes.typeOrderComparator());

        // reorder the primary type at the top
        if (keys.remove(primaryType)) {
            keys.add(0, primaryType);
        }
        for (String t : types) {
            RangeSliderModel m = timeline.getPartitionRange(t);
            if (m == null) {
                continue;
            }
            RangeSlider sl = sliderUIs.get(t);
            if (sl == null || sl.getModel() != m) {
                sl = new RangeSlider();
                sl.setShowGaps(true);
                sl.setModel(m);
                if (m != timeline.getPrimaryRange()) {
                    sl.setBasicBarColor(RangeSlider.BAR_INACTIVE_COLOR);
                }
                sliderUIs.put(t, sl);
            }
            newOrder.add(sl);
        }
        if (rowOrder.equals(newOrder)) {
            return;
        } else {
            for (Component c : sliders.getComponents()) {
                sliders.remove(c);
            }
        }
        rowOrder = newOrder;
        GridBagConstraints iconConstraints = new GridBagConstraints();
        iconConstraints.anchor = GridBagConstraints.EAST;

        GridBagConstraints sliderConstraints = new GridBagConstraints();
        sliderConstraints.gridwidth = GridBagConstraints.REMAINDER;
        sliderConstraints.weightx = 1.0;
        sliderConstraints.fill = GridBagConstraints.HORIZONTAL;

        for (String k : keys) {
            RangeSlider sl = sliderUIs.get(k);
            JLabel jl = new JLabel();
            Node n = graphTypes.getTypeNode(k);
            Image im = n.getIcon(BeanInfo.ICON_COLOR_16x16);
            jl.setIcon(ImageUtilities.image2Icon(im));
            jl.setToolTipText(n.getDisplayName());
            sliders.add(jl, iconConstraints);
            sliders.add(sl, sliderConstraints);
            sl.setAlignmentX(0);
        }
        JComponent c = this;
        while (true) {
            if (c instanceof JScrollPane) {
                break;
            }
            Component p = c.getParent();
            if (!(p instanceof JComponent)) {
                break;
            }
            c = (JComponent) p;
        }
        LOG.log(Level.FINE, "{0}: sliders updated = {1}", new Object[]{logId(), keys});

        // XXX simplify
        sliders.invalidate();
        invalidate();
        JComponent target = (JComponent) c.getParent();
        SwingUtilities.invokeLater(() -> {
            target.invalidate();
            target.revalidate();
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        updateSliders();
    }

    /**
     * Special customization for JScrollPane layout.
     */
    static class Layout extends ScrollPaneLayout.UIResource {
        final TimelineModel timeline;

        public Layout(TimelineModel timeline) {
            this.timeline = timeline;
        }

        @Override
        public void layoutContainer(Container parent) {
            super.layoutContainer(parent);
            LOG.log(Level.FINER, "{0}: Layouted: {1}, parent: {2}",
                    new Object[]{logId(timeline), parent.getBounds(), parent.getParent().getBounds()});
            if (parent.getSize().width == 0) {
                SwingUtilities.invokeLater(() -> {
                    parent.invalidate();
                    parent.revalidate();
                });
            }
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Dimension extentSize = null;
            JScrollPane scrollPane = (JScrollPane) parent;
            boolean forceScrollbar = false;
            int thsbPolicy = scrollPane.getHorizontalScrollBarPolicy();

            if (viewport != null) {
                extentSize = viewport.getPreferredSize();
            }
            int w = parent.getWidth();
            if (w == 0) {
                // hack: the slider is burried in two BorderLayouts, one
                // of them ought to have NORTH or SOUTH position
                Container p = parent.getParent();
                w = p.getWidth();
                if (w == 0) {
                    SwingUtilities.invokeLater(() -> {
                        p.invalidate();
                        p.revalidate();
                    });
                }
            }
            if (extentSize != null && w > 0) {
                Insets insets = parent.getInsets();
                int prefWidth = insets.left + insets.right;
                prefWidth += extentSize.width;

                Border viewportBorder = scrollPane.getViewportBorder();
                if (viewportBorder != null) {
                    Insets vpbInsets = viewportBorder.getBorderInsets(parent);
                    prefWidth += vpbInsets.left + vpbInsets.right;
                }
                if ((rowHead != null) && rowHead.isVisible()) {
                    prefWidth += rowHead.getPreferredSize().width;
                }

                if (prefWidth > w && (hsb != null) && (thsbPolicy != HORIZONTAL_SCROLLBAR_NEVER)) {
                    forceScrollbar = true;
                }
            }
            Dimension dim;
            try {

                if (forceScrollbar) {
                    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                }
                dim = super.preferredLayoutSize(parent);
            } finally {
                if (forceScrollbar) {
                    scrollPane.setHorizontalScrollBarPolicy(thsbPolicy);
                }
            }
            LOG.log(Level.FINER, "{0}: Preferred layout: {1}, parent was: {2}",
                    new Object[]{logId(timeline), dim, parent.getSize()});
            return dim;
        }
    }

    private static String logId(TimelineModel timeline) {
        return "SliderPanel[" + timeline.getSource().getName() + " / " + timeline.getPrimaryType() + "]"; // NOI18N
    }

    private String logId() {
        return logId(timeline);
    }

    @Override
    public String toString() {
        return "SliderPanel[timeline = " + timeline + ", " + paramString() + "]"; // NOI18N
    }
}

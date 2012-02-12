package com.nikonhacker.gui.component.codeStructure;


import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.nikonhacker.Format;
import com.nikonhacker.dfr.CodeStructure;
import com.nikonhacker.dfr.Function;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class CodeStructureMxGraphComponent extends mxGraphComponent {
    private CodeStructure codeStructure;
    private Function currentlySelectedFunction;

    public CodeStructureMxGraphComponent(final CodeStructureMxGraph graph, final CodeStructureFrame codeStructureFrame) {
        super(graph);
        this.codeStructure = codeStructureFrame.codeStructure;

        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem findUsageMenuItem = new JMenuItem("Find usage");
        findUsageMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                graph.expandFunction(currentlySelectedFunction, codeStructure, true, false);
            }
        });
        popupMenu.add(findUsageMenuItem);

        JMenuItem findCalleesMenuItem = new JMenuItem("Find callees");
        findCalleesMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                graph.expandFunction(currentlySelectedFunction, codeStructure, false, true);
            }
        });
        popupMenu.add(findCalleesMenuItem);

        JMenuItem removeMenuItem = new JMenuItem("Remove");
        removeMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                graph.removeFunction(currentlySelectedFunction);
            }
        });
        popupMenu.add(removeMenuItem);


        // This handles only mouse click events
        getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Object cell = getCellAt(e.getX(), e.getY(), false);
                if (cell != null) {
                    mxCell vertex = (mxCell) cell;
                    try {
                        Object value = vertex.getValue();
                        if (value instanceof Function) {
                            Function function = (Function) value;
                            codeStructureFrame.writeFunction(function);
                        }
                        else if (value instanceof Integer) {
                            codeStructureFrame.writeText("; The function at address 0x" + Format.asHex((Integer) value, 8) + " was not part of a CODE segment and was not disassembled");
                        }
                        else {
                            codeStructureFrame.writeText("; The target for this jump could not be determined in a static way");
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                maybePopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybePopup(e);
            }

            private void maybePopup(MouseEvent e) {
                if (e.isPopupTrigger()) { // platform dependant trigger
                    Object cell = getCellAt(e.getX(), e.getY(), false);
                    if (cell != null) {
                        Object value = ((mxCell) cell).getValue();
                        if (value instanceof Function) {
                            // If right button, also show popup menu
                            currentlySelectedFunction = (Function) value;
                            popupMenu.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void installDoubleClickHandler() {
        graphControl.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (isEnabled()) {
                    if (!e.isConsumed() && isEditEvent(e)) {
                        Object cell = getCellAt(e.getX(), e.getY(), false);

                        if (cell != null) {
                            mxCell vertex = (mxCell) cell;
                            //JOptionPane.showMessageDialog(null, "Dbl Click inside " + vertex.getValue(), "Done", JOptionPane.INFORMATION_MESSAGE);
                            Object value = vertex.getValue();
                            if (value instanceof Function) {
                                ((CodeStructureMxGraph)graph).expandFunction((Function) value, codeStructure, true, true);
                            }
                        }
                    }
                }
            }

        });
    }


}

package com.github.naton1.rl;

import com.elvarg.game.content.combat.hit.HitDamage;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.event.EventDispatcher;
import com.elvarg.game.event.events.HitAppliedEvent;
import com.elvarg.game.event.events.HitCalculatedEvent;
import com.elvarg.game.event.events.PlayerLoggedOutEvent;
import com.elvarg.game.event.events.PlayerPacketsFlushedEvent;
import com.github.naton1.rl.env.AgentEnvironment;
import com.github.naton1.rl.env.EnvironmentDescriptor;
import com.github.naton1.rl.env.nh.NhEnvironmentDescriptor;
import com.github.naton1.rl.env.nh.NhEnvironmentParams;
import com.github.naton1.rl.util.ContractLoader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultCaret;
import lombok.RequiredArgsConstructor;

public class EnvironmentDebugger {

    private static final Map<Player, Context> contexts = new WeakHashMap<>();

    public static void initialize() {
        EventDispatcher.getGlobal().add(PlayerLoggedOutEvent.class, e -> onLogout(e.getPlayer()));
        EventDispatcher.getGlobal().add(PlayerPacketsFlushedEvent.class, e -> onFlush(e.getPlayer()));
        EventDispatcher.getGlobal().add(HitCalculatedEvent.class, e -> {
            if (e.getPendingHit().getAttacker().isPlayer()
                    && e.getPendingHit().getTarget().isPlayer()) {
                onHitCalculated(e.getPendingHit().getAttacker().getAsPlayer(), e.getPendingHit());
                onHitCalculated(e.getPendingHit().getTarget().getAsPlayer(), e.getPendingHit());
            }
        });
        EventDispatcher.getGlobal().add(HitAppliedEvent.class, e -> {
            final HitDamage hit = e.getHitDamage();
            if (hit.getMetadata().getAttacker().isPlayer()
                    && hit.getMetadata().getTarget().isPlayer()
                    && hit.getMetadata().getAssociatedPendingHit() == null) {
                onHitApplied(hit.getMetadata().getAttacker().getAsPlayer(), hit);
                onHitApplied(hit.getMetadata().getTarget().getAsPlayer(), hit);
            }
        });
    }

    private static synchronized void onLogout(Player player) {
        final Context ctx = contexts.remove(player);
        if (ctx != null) {
            ctx.cleanup();
        }
    }

    private static synchronized void onHitCalculated(Player player, PendingHit hit) {
        final Context ctx = contexts.get(player);
        if (ctx != null && ctx.environment != null) {
            ctx.environment.onHitCalculated(hit);
        }
    }

    private static synchronized void onHitApplied(Player player, HitDamage hit) {
        final Context ctx = contexts.get(player);
        if (ctx != null && ctx.environment != null) {
            ctx.environment.onHitApplied(hit);
        }
    }

    public static synchronized <T> void registerParams(Player player, T params) {
        final Context ctx = contexts.get(player);
        if (ctx != null) {
            ctx.params = params;
        }
    }

    private static synchronized void onFlush(Player player) {
        if (player.isPlayerBot() && !(player instanceof RemoteEnvironmentPlayerBot)) {
            return;
        }
        final Context ctx = contexts.computeIfAbsent(player, Context::new);
        if (player.getCombat().getTarget() != null
                && (ctx.environment == null || player.getCombat().getTarget() != ctx.environment.getTarget())) {
            initializeNewEnvironment(player, ctx);
        }
        ctx.syncTick();
        EventQueue.invokeLater(ctx::tickUi);
    }

    private static void initializeNewEnvironment(Player player, Context ctx) {
        // Always use this env for now
        final NhEnvironmentDescriptor nhEnvironmentDescriptor = new NhEnvironmentDescriptor();
        ctx.environmentDescriptor = nhEnvironmentDescriptor;
        if (ctx.params == null) {
            ctx.params = new NhEnvironmentParams();
        }
        ctx.environment = nhEnvironmentDescriptor.createEnvironment(
                player, player.getCombat().getTarget().getAsPlayer(), null, (NhEnvironmentParams) ctx.params);
    }

    @RequiredArgsConstructor
    private static class Context {
        private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

        private final Player player;
        private EnvironmentDescriptor<?> environmentDescriptor;
        private Object params;
        private AgentEnvironment environment;

        private PvpPanel pvpPanel;

        private List<Number> obs;
        private List<List<Boolean>> actionMasks;
        private String meta;

        public void cleanup() {
            if (pvpPanel != null) {
                SwingUtilities.getWindowAncestor(pvpPanel).setVisible(false);
                pvpPanel = null;
            }
        }

        public void syncTick() {
            if (this.environment == null) {
                this.obs = null;
                this.actionMasks = null;
                return;
            }
            this.environment.onTickStart();
            this.environment.onTickProcessed();
            this.obs = this.environment.getObs();
            this.actionMasks = this.environment.getActionMasks();
            if (this.player instanceof RemoteEnvironmentPlayerBot) {
                this.meta = gson.toJson(((RemoteEnvironmentPlayerBot) this.player).getMeta());
            }
            this.environment.onTickEnd();
        }

        public void tickUi() {
            if (this.pvpPanel == null) {
                final JFrame frame = new JFrame("Environment View: " + player.getUsername());
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setSize(400, 600);

                pvpPanel = new PvpPanel();
                frame.add(pvpPanel);

                frame.setVisible(true);
            }
            if (obs == null || actionMasks == null) {
                return;
            }
            pvpPanel.update(
                    obs,
                    actionMasks,
                    environmentDescriptor.getMeta().getObservations().stream()
                            .map(ContractLoader.Observation::getDescription)
                            .collect(Collectors.toList()),
                    environmentDescriptor.getMeta().getActions().stream()
                            .map(ContractLoader.ActionHead::getActions)
                            .flatMap(List::stream)
                            .map(ContractLoader.Action::getDescription)
                            .collect(Collectors.toList()),
                    this.meta);
        }
    }

    private static class PvpPanel extends JPanel {

        static {
            // This gets initialized on the EDT
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private final JTable obsTable;
        private final JTable actionMasksTable;
        private final JTextArea metaTextArea;

        public PvpPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            obsTable = createTable("Observations");
            actionMasksTable = createTable("Action Masks");
            metaTextArea = createMetaPane();
        }

        private JTextArea createMetaPane() {
            final JTextArea textArea = new JTextArea(10, 0);
            final JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Meta", TitledBorder.CENTER, TitledBorder.TOP));
            add(scrollPane);
            textArea.setEditable(false);
            final DefaultCaret caret = (DefaultCaret) textArea.getCaret();
            caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
            textArea.setText("No meta available. Only available for remote environment player bots.");
            scrollPane.setPreferredSize(new Dimension(0, 400));
            return textArea;
        }

        private JTable createTable(String title) {
            final String[] columnNames = {"Index", "Value", "Description"};
            final JTable table = new JTable(new DefaultTableModel(columnNames, 0));
            table.setEnabled(false);
            table.getColumnModel().getColumn(0).setMaxWidth(45); // Index
            table.getColumnModel().getColumn(1).setMaxWidth(45); // Value
            final JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), title, TitledBorder.CENTER, TitledBorder.TOP));
            add(scrollPane);

            final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component cell =
                            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (cell instanceof JComponent) {
                        ((JComponent) cell).setToolTipText(value.toString());
                    }
                    return cell;
                }
            };

            for (int i = 0; i < table.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setCellRenderer(renderer);
            }

            return table;
        }

        public void update(
                List<Number> obs,
                List<List<Boolean>> actionMasks,
                List<String> observationDescriptions,
                List<String> actionMaskDescriptions,
                String meta) {
            EventQueue.invokeLater(() -> {
                updateTable(obsTable, obs, observationDescriptions);
                updateTable(
                        actionMasksTable,
                        actionMasks.stream().flatMap(List::stream).collect(Collectors.toList()),
                        actionMaskDescriptions);
                if (meta != null) {
                    metaTextArea.setText(meta);
                }
            });
        }

        private <T> void updateTable(JTable table, List<T> data, List<String> descriptions) {
            final DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0); // clear existing data
            for (int i = 0; i < data.size(); i++) {
                model.addRow(new Object[] {i, data.get(i), descriptions.get(i)});
            }
        }
    }
}

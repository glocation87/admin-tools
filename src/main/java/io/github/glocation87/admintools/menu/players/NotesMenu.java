package io.github.glocation87.admintools.menu.players;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Text;
import io.github.glocation87.admintools.data.StaffNote;
import io.github.glocation87.admintools.menu.Icons;
import io.github.glocation87.admintools.menu.ListMenu;
import io.github.glocation87.admintools.menu.Menu;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public final class NotesMenu extends ListMenu<StaffNote> {
    private static final int WRAP = 38;

    private final OfflinePlayer target;

    public NotesMenu(AdminToolsPlugin plugin, Player viewer, Menu parent, OfflinePlayer target) {
        super(plugin, viewer, parent, "<yellow><b>Notes: " + target.getName(), 4);
        this.target = target;
    }

    @Override
    protected List<StaffNote> items() {
        return plugin.data().notes(target.getUniqueId());
    }

    @Override
    protected ItemStack icon(StaffNote note) {
        List<String> lore = new ArrayList<>(wrap(Text.escape(note.text())));
        lore.add("");
        lore.add("<dark_gray>Shift + right click to delete");
        return Icons.of(Material.PAPER, "<yellow>" + note.author() + " <gray>" + Text.date(note.time()), lore);
    }

    @Override
    protected void onClick(StaffNote note, ClickType type) {
        if (type.isShiftClick() && type.isRightClick()) {
            plugin.data().removeNote(target.getUniqueId(), note);
            refresh();
        }
    }

    @Override
    protected void extras() {
        button(bottom() + 1, Icons.of(Material.WRITABLE_BOOK, "<green>Add Note", "Asks for the text in chat"), () ->
            plugin.prompts().ask(viewer, "<gray>Note about <white>" + target.getName() + "<gray>?", text -> {
                plugin.data().addNote(target.getUniqueId(), new StaffNote(viewer.getName(), System.currentTimeMillis(), text));
                open();
            }));
    }

    public static List<String> wrap(String text) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            if (line.length() + word.length() > WRAP && line.length() > 0) {
                lines.add("<white>" + line.toString().trim());
                line.setLength(0);
            }
            line.append(word).append(' ');
        }
        if (line.length() > 0) {
            lines.add("<white>" + line.toString().trim());
        }
        return lines;
    }
}

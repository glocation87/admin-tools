package io.github.glocation87.admintools.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.glocation87.admintools.AdminToolsPlugin;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class MenuTest {

    private ServerMock server;
    private AdminToolsPlugin plugin;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(AdminToolsPlugin.class);
        player = server.addPlayer();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private InventoryClickEvent click(Menu menu, int slot, ClickType type) {
        InventoryClickEvent event = new InventoryClickEvent(player.getOpenInventory(), InventoryType.SlotType.CONTAINER, slot, type,
            org.bukkit.event.inventory.InventoryAction.PICKUP_ALL);
        server.getPluginManager().callEvent(event);
        return event;
    }

    @Test
    void opensNextTickAndDispatchesClicks() {
        List<ClickType> clicks = new ArrayList<>();
        Menu menu = new Menu(plugin, player, null, "Test", 3) {
            @Override
            protected void draw() {
                button(13, Icons.of(Material.STONE, "Hit me"), clicks::add);
            }
        };
        menu.open();
        assertNull(plugin.menus().current(player));
        server.getScheduler().performOneTick();
        assertSame(menu, plugin.menus().current(player));

        InventoryClickEvent hit = click(menu, 13, ClickType.SHIFT_RIGHT);
        InventoryClickEvent miss = click(menu, 0, ClickType.LEFT);

        assertTrue(hit.isCancelled());
        assertTrue(miss.isCancelled());
        assertEquals(List.of(ClickType.SHIFT_RIGHT), clicks);
    }

    @Test
    void listMenuPagesAndCountsEntries() {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            numbers.add(i);
        }
        List<Integer> clicked = new ArrayList<>();
        ListMenu<Integer> menu = new ListMenu<>(plugin, player, null, "Numbers", 3) {
            @Override
            protected List<Integer> items() {
                return numbers;
            }

            @Override
            protected ItemStack icon(Integer item) {
                return Icons.of(Material.PAPER, "Number " + item);
            }

            @Override
            protected void onClick(Integer item, ClickType type) {
                clicked.add(item);
            }
        };
        menu.open();
        server.getScheduler().performOneTick();

        click(menu, 10, ClickType.LEFT);
        assertEquals(List.of(0), clicked);

        click(menu, 23, ClickType.LEFT);
        click(menu, 10, ClickType.LEFT);
        assertEquals(List.of(0, 7), clicked);

        click(menu, 23, ClickType.LEFT);
        click(menu, 15, ClickType.LEFT);
        click(menu, 16, ClickType.LEFT);
        assertEquals(List.of(0, 7, 19), clicked);
        assertEquals(Material.BLACK_STAINED_GLASS_PANE, menu.getInventory().getItem(23).getType());
    }

    @Test
    void confirmRunsActionThenReturnsToParent() {
        boolean[] ran = {false};
        Menu parent = new Menu(plugin, player, null, "Parent", 3) {
            @Override
            protected void draw() {
                button(13, Icons.of(Material.TNT, "Boom"), () -> confirm("Sure?", Icons.of(Material.TNT, "Boom"), () -> ran[0] = true));
            }
        };
        parent.open();
        server.getScheduler().performOneTick();
        click(parent, 13, ClickType.LEFT);
        server.getScheduler().performOneTick();

        Menu confirm = plugin.menus().current(player);
        assertTrue(confirm instanceof ConfirmMenu);
        click(confirm, 11, ClickType.LEFT);
        server.getScheduler().performOneTick();

        assertTrue(ran[0]);
        assertSame(parent, plugin.menus().current(player));
    }
}

package voruti.announceplayerserverswitch;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Optional;

@Plugin(
        id = "announceplayerserverswitch",
        name = "AnnouncePlayerServerSwitch",
        version = BuildConstants.VERSION,
        description = "A Velocity plugin that announces players switching servers.",
        url = "https://github.com/voruti/AnnouncePlayerServerSwitch",
        authors = {"voruti"}
)
public class AnnouncePlayerServerSwitch {

    @Inject
    private Logger logger;

    @Inject
    private ProxyServer server;


    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Enabled.");
    }

    @Subscribe
    public EventTask onServerPostConnect(ServerPostConnectEvent event) {
        return EventTask.async(() -> {
            logger.trace("onServerPostConnect {}", event);

            String playerName = event.getPlayer().getGameProfile().getName();
            String serverName = event.getPlayer().getCurrentServer()
                    .map(serverConnection -> serverConnection.getServerInfo().getName())
                    .orElse("unknown");
            String previousServerName = Optional.ofNullable(event.getPreviousServer())
                    .map(registeredServer -> registeredServer.getServerInfo().getName())
                    .orElse(null);

            this.sendMessage(this.generateMessage(playerName, serverName, previousServerName));
        });
    }

    @Subscribe
    public EventTask onDisconnect(DisconnectEvent event) {
        return EventTask.async(() -> {
            logger.trace("onDisconnect {}", event);

            String playerName = event.getPlayer().getGameProfile().getName();
            String previousServerName = event.getPlayer().getCurrentServer()
                    .map(serverConnection -> serverConnection.getServerInfo().getName())
                    .orElse("unknown");

            this.sendMessage(this.generateMessage(playerName, null, previousServerName));
        });
    }


    private String generateMessage(@NotNull String playerName, @Nullable String serverName,
                                   @Nullable String previousServerName) {
        logger.trace("generateMessage playerName: {}, serverName: {}, previousServerName: {}",
                playerName, serverName, previousServerName);

        if (previousServerName == null || previousServerName.isEmpty()) {
            return String.format("%s joined %s", playerName, serverName);
        } else if (serverName == null || serverName.isEmpty()) {
            return String.format("%s left %s", playerName, previousServerName);
        } else {
            return String.format("%s moved %s -> %s", playerName, previousServerName, serverName);
        }
    }

    private void sendMessage(String message) {
        logger.trace("sendMessage \"{}\"", message);

        server.sendMessage(Component.text(message, Style.style(NamedTextColor.YELLOW)));
        logger.trace("Sent broadcast");
    }
}

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

import java.util.Objects;
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

            this.sendMessage(this.generateInwardsMessage(playerName, serverName, previousServerName));
        });
    }

    @Subscribe
    public EventTask onDisconnect(DisconnectEvent event) {
        return EventTask.async(() -> {
            logger.trace("onDisconnect {}", event);

            String playerName = event.getPlayer().getGameProfile().getName();
            String previousServerName = event.getPlayer().getCurrentServer()
                    .map(serverConnection -> serverConnection.getServerInfo().getName())
                    .orElse(null);

            this.sendMessage(this.generateOutwardsMessage(playerName, previousServerName));
        });
    }


    private String generateOutwardsMessage(@NotNull String playerName, @Nullable String previousServerName) {
        logger.trace("generateMessage playerName: {}, previousServerName: {}", playerName, previousServerName);

        if (Objects.isNull(previousServerName) || previousServerName.isBlank()) {
            return this.generateDisconnectMessage(playerName);
        } else {
            return this.generateLeaveMessage(playerName, previousServerName);
        }
    }

    private String generateInwardsMessage(@NotNull String playerName, @NotNull String serverName,
                                          @Nullable String previousServerName) {
        logger.trace("generateMessage playerName: {}, serverName: {}, previousServerName: {}",
                playerName, serverName, previousServerName);

        if (Objects.isNull(previousServerName) || previousServerName.isBlank()) {
            return this.generateJoinMessage(playerName, serverName);
        } else {
            return this.generateMoveMessage(playerName, serverName, previousServerName);
        }
    }

    private String generateMoveMessage(@NotNull String playerName, @NotNull String serverName,
                                       @NotNull String previousServerName) {
        logger.trace("generateMessage playerName: {}, serverName: {}, previousServerName: {}",
                playerName, serverName, previousServerName);

        return String.format("%s moved %s -> %s", playerName, previousServerName, serverName);
    }

    private String generateDisconnectMessage(@NotNull String playerName) {
        logger.trace("generateJoinMessage playerName: {}", playerName);

        return String.format("%s disconnected", playerName);
    }

    private String generateLeaveMessage(@NotNull String playerName, @NotNull String previousServerName) {
        logger.trace("generateJoinMessage playerName: {}, previousServerName: {}", playerName, previousServerName);

        return String.format("%s left %s", playerName, previousServerName);
    }

    private String generateJoinMessage(@NotNull String playerName, @NotNull String serverName) {
        logger.trace("generateJoinMessage playerName: {}, serverName: {}", playerName, serverName);

        return String.format("%s joined %s", playerName, serverName);
    }

    private void sendMessage(String message) {
        logger.trace("sendMessage \"{}\"", message);

        server.sendMessage(Component.text(message, Style.style(NamedTextColor.YELLOW)));
        logger.trace("Sent broadcast");
    }
}

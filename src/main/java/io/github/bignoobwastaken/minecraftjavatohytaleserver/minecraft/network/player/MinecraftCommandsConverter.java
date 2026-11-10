package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.arguments.system.Argument;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import net.kyori.adventure.key.Key;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandNode;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandParser;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandType;
import org.geysermc.mcprotocollib.protocol.data.game.command.properties.CommandProperties;

import java.util.*;

public class MinecraftCommandsConverter {
    private final List<CommandNode> nodes = new ArrayList<>();

    {
        convertCommandsFromHytaleToMinecraft();
    }

    private void convertCommandsFromHytaleToMinecraft() {
        Map<String, AbstractCommand> commandRegistration = HytaleServer.get().getCommandManager().getCommandRegistration();

        List<Integer> childInt = new ArrayList<>();
        commandRegistration.values().forEach(command -> {
            if (command.getName() == null) return;
            Map<AbstractCommand, Integer> cmdIndex = new IdentityHashMap<>();
            int idx = buildCommandNode(command, cmdIndex);
            childInt.add(idx);
            command.getAliases().forEach(alias -> childInt.add(buildAliasNode(alias, idx)));
            command.getRequiredArguments().forEach(arg -> childInt.add(buildArgumentNode(arg, idx)));
            command.getOptionalArguments().values().forEach(arg -> childInt.add(buildArgumentNode(arg, idx)));
        });
        CommandNode commandNode = new CommandNode(
                CommandType.ROOT,
                false,
                false,
                childInt.stream().mapToInt(Integer::intValue).toArray(),
                OptionalInt.empty(),
                null,
                null,
                null,
                null
        );
        nodes.add(commandNode);
    }

    public CommandNode[] getCommandNodes() {
        return nodes.toArray(new CommandNode[0]);
    }

    private int buildCommandNode(AbstractCommand command, Map<AbstractCommand, Integer> cmdIndex) {
        List<Integer> childInt = new ArrayList<>();
        command.getSubCommands().values().forEach(sub -> {
            if (sub.getName() == null) return;
            int subIdx = buildCommandNode(sub, cmdIndex);
            childInt.add(subIdx);
            sub.getAliases().forEach(alias -> childInt.add(buildAliasNode(alias, subIdx)));
            command.getRequiredArguments().forEach(arg -> childInt.add(buildArgumentNode(arg, subIdx)));
            command.getOptionalArguments().values().forEach(arg -> childInt.add(buildArgumentNode(arg, subIdx)));
        });
        CommandNode node = new CommandNode(
                CommandType.LITERAL,
                childInt.isEmpty(),
                false,
                childInt.stream().mapToInt(Integer::intValue).toArray(),
                OptionalInt.empty(),
                command.getName(),
                null,
                null,
                null

        );
        int index = nodes.size();
        nodes.add(node);
        cmdIndex.put(command, index);
        return index;
    }

    private int buildAliasNode(String alias, int redirectTo) {
        CommandNode node = new CommandNode(
                CommandType.LITERAL,
                false,
                true,
                new int[0],
                OptionalInt.of(redirectTo),
                alias,
                null,
                null,
                null

        );
        int index = nodes.size();
        nodes.add(node);
        return index;
    }

    private <T> int buildArgumentNode(Argument<?, T> arg, int redirectTo) {
        CommandParser parser = getParser(arg.getArgumentType());
        String description = arg.getDescription();
        assert parser != null;
        assert description != null;
        CommandNode node = new CommandNode(
                CommandType.ARGUMENT,
                false,
                true,
                new int[0],
                OptionalInt.of(redirectTo),
                arg.getName(),
                parser,
                null,
                Key.key("")
        );
        int index = nodes.size();
        nodes.add(node);
        return index;
    }

    private <T> CommandParser getParser(ArgumentType<T> types) {
        if (types == ArgTypes.BOOLEAN) return CommandParser.BOOL;
        if (types == ArgTypes.FLOAT) return CommandParser.FLOAT;
        if (types == ArgTypes.DOUBLE) return CommandParser.DOUBLE;
        if (types == ArgTypes.INTEGER) return CommandParser.INTEGER;
        if (types == ArgTypes.STRING) return CommandParser.STRING;
        if (types == ArgTypes.GAME_PROFILE_LOOKUP) return CommandParser.GAME_PROFILE;
        if (types == ArgTypes.VECTOR3I) return CommandParser.VEC3;
        if (types == ArgTypes.VECTOR2I) return CommandParser.VEC2;
        //if (types == ArgTypes.COLOR) return CommandParser.COLOR;
        if (types == ArgTypes.INT_RANGE) return CommandParser.INT_RANGE;
        if (types == ArgTypes.GAME_MODE) return CommandParser.GAMEMODE;
        if (types == ArgTypes.UUID) return CommandParser.UUID;
        return null;
    }

}

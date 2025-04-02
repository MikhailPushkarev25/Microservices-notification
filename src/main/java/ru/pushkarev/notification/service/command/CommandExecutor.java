package ru.pushkarev.notification.service.command;

import org.springframework.stereotype.Service;
import ru.pushkarev.notification.annotation.CommandTypeMapping;
import ru.pushkarev.notification.enums.CommandType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommandExecutor {

    private final Map<CommandType, Command<?, ?>> commands = new HashMap<>();

    public CommandExecutor(List<Command<?, ?>> commandList) {
        for (Command<?, ?> command : commandList) {
            CommandType commandType = getCommandType(command);
            commands.put(commandType, command);
        }
    }

    public <T, R> R executeCommand(CommandType commandType, T dto) {
        @SuppressWarnings("unchecked")
        Command<T, R> command = (Command<T, R>) commands.get(commandType);
        if (command != null) {
            return command.execute(dto);
        } else {
            throw new IllegalArgumentException("Unknown command: " + commandType);
        }
    }

    private CommandType getCommandType(Command<?, ?> command) {
        CommandTypeMapping annotation = command.getClass().getAnnotation(CommandTypeMapping.class);
        if (annotation != null) {
            return annotation.value();
        }
        throw new IllegalArgumentException("Unknown command class: " + command.getClass());
    }
}





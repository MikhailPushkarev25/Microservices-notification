package ru.pushkarev.notification.service.command;

public interface Command<T, R> {

    R execute(T dto);
}

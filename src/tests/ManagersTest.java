package tests;

import manager.Managers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    //убедитесь, что утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры менеджеров
    @Test
    void shouldBeNotNullGetDefault() {
        assertNotNull(Managers.getDefault());
    }

    @Test
    void shouldBeNotNullGetDefaultHistory() {
        assertNotNull(Managers.getDefaultHistory());
    }
}
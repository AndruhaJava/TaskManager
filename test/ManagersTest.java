package test;

import manager.Managers;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void shouldBeNotNullGetDefault() {
        assertNotNull(Managers.getDefault());
    }

    @Test
    void shouldBeNotNullGetDefaultHistory() {
        assertNotNull(Managers.getDefaultHistory());
    }
}
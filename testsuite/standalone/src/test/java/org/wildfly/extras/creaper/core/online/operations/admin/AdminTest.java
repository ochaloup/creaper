package org.wildfly.extras.creaper.core.online.operations.admin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ManagementVersion;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.ReadAttributeOption;

@RunWith(Arquillian.class)
public class AdminTest {
    private OnlineManagementClient client;
    private Operations ops;
    private Administration admin;

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
        admin = new Administration(client);
    }

    @After
    public void close() throws IOException {
        client.close();
    }

    @Test
    public void reload() throws IOException, InterruptedException, TimeoutException {
        admin.reload();

        assertFalse(admin.isReloadRequired());
        assertFalse(admin.reloadIfRequired());

        Address jspConfigurationAddress;
        if (client.serverVersion().lessThan(ManagementVersion.VERSION_2_0_0)) { // AS7, JBoss Web
            jspConfigurationAddress = Address.subsystem("web").and("configuration", "jsp-configuration");
        } else { // WildFly, Undertow
            jspConfigurationAddress = Address.subsystem("undertow").and("servlet-container", "default").and("setting", "jsp");
        }

        ModelNodeResult originalValueIgnoreDefaults = null;
        try {
            originalValueIgnoreDefaults = ops.readAttribute(jspConfigurationAddress, "development",
                    ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
            ModelNodeResult originalValue = ops.readAttribute(jspConfigurationAddress, "development",
                    ReadAttributeOption.INCLUDE_DEFAULTS);
            boolean development = originalValue.booleanValue();
            ops.writeAttribute(jspConfigurationAddress, "development", !development);

            assertTrue(admin.isReloadRequired());
            assertTrue(admin.reloadIfRequired());
        } finally {
            if (originalValueIgnoreDefaults != null) {
                ops.writeAttribute(jspConfigurationAddress, "development", originalValueIgnoreDefaults.value());
                admin.reloadIfRequired();
            }
        }
    }

    @Test
    public void notRestartedReload() throws IOException, InterruptedException, TimeoutException {
        // transaction jts will be used
        ops.writeAttribute(Address.subsystem("transactions"), "jts", true);
        assertTrue("Server restart should be required as attribute of jts was changed", admin.isRestartRequired());
        assertFalse("Server reload should not be required as jts change requires only reload", admin.isReloadRequired());

        ops.writeAttribute(Address.subsystem("transactions"), "jts", false);
        assertTrue("Server restart is expected to still be required as some operation before required restarting of server", admin.isRestartRequired());
        assertFalse("Server reload should not be required as jts change requires only reload", admin.isReloadRequired());

        admin.reload();

        assertFalse("After reload reload should not be required", admin.isReloadRequired());
        assertTrue("After reload restart still should be required", admin.isRestartRequired());
    }
}

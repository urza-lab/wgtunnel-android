package com.zaneschepke.wireguardautotunnel.domain.model

import com.zaneschepke.wireguardautotunnel.core.orchestration.isUnknownShortcutTunnel
import com.zaneschepke.wireguardautotunnel.data.entity.TunnelConfig.Companion.GLOBAL_CONFIG_NAME
import com.zaneschepke.wireguardautotunnel.util.extensions.uniqueTunnelName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TunnelNamingTest {

    // Keys are generated per run so no key material lives in the repository
    private fun tunnel(name: String = "office") =
        TunnelConfig.generateDefaultGlobalConfig().renamedTo(name)

    @Test
    fun `unique name uses the first free suffix`() {
        assertEquals("office (1)", uniqueTunnelName("office", setOf("office")))
        assertEquals("office (2)", uniqueTunnelName("office", setOf("office", "office (1)")))
    }

    @Test
    fun `unique name continues an existing suffix`() {
        assertEquals("office (2)", uniqueTunnelName("office (1)", setOf("office", "office (1)")))
    }

    @Test
    fun `unique name keeps long and non ascii names intact`() {
        val long = "ä".repeat(80)
        assertEquals("$long (1)", uniqueTunnelName(long, setOf(long)))
    }

    @Test
    fun `duplicate keeps config and connection settings`() {
        val original =
            tunnel()
                .copy(
                    id = 7,
                    groupId = 3,
                    entryTunnelId = 2,
                    isIpv6Preferred = true,
                    ipv6RestoreEnabled = true,
                    isDDNSTunnel = true,
                    isMetered = true,
                )

        val copy = original.duplicatedAs("office (1)", position = 5)

        assertEquals(0, copy.id)
        assertEquals("office (1)", copy.name)
        assertEquals("office (1)", copy.getConfig().name)
        assertEquals(5, copy.position)
        assertEquals(3, copy.groupId)
        assertEquals(2, copy.entryTunnelId)
        assertTrue(copy.isIpv6Preferred)
        assertTrue(copy.ipv6RestoreEnabled)
        assertTrue(copy.isDDNSTunnel)
        assertTrue(copy.isMetered)
        assertEquals(
            original.getConfig().`interface`.privateKey,
            copy.getConfig().`interface`.privateKey,
        )
        assertEquals(original.getConfig().peers, copy.getConfig().peers)
    }

    @Test
    fun `duplicate drops default flags and auto tunnel mappings`() {
        val copy =
            tunnel()
                .copy(
                    isPrimaryTunnel = true,
                    isMobileDataTunnel = true,
                    isEthernetTunnel = true,
                    tunnelNetworks = listOf("home-wifi"),
                    tunnelBSSIDs = listOf("00:00:5e:00:53:01"),
                    autoTunnelApps = listOf("com.example.app"),
                )
                .duplicatedAs("office (1)", position = 1)

        assertFalse(copy.isPrimaryTunnel)
        assertFalse(copy.isMobileDataTunnel)
        assertFalse(copy.isEthernetTunnel)
        assertTrue(copy.tunnelNetworks.isEmpty())
        assertTrue(copy.tunnelBSSIDs.isEmpty())
        assertTrue(copy.autoTunnelApps.isEmpty())
    }

    @Test
    fun `rename updates both names and keeps identity and mappings`() {
        val original =
            tunnel().copy(id = 7, isPrimaryTunnel = true, tunnelNetworks = listOf("home-wifi"))

        val renamed = original.renamedTo("  split  ")

        assertEquals(7, renamed.id)
        assertEquals("split", renamed.name)
        assertEquals("split", renamed.getConfig().name)
        assertTrue(renamed.isPrimaryTunnel)
        assertEquals(listOf("home-wifi"), renamed.tunnelNetworks)
    }

    @Test
    fun `rename validation`() {
        val others = listOf("taken")
        assertNull(validateTunnelRename(" free ", others, isRunning = false))
        assertEquals(
            TunnelRenameError.EMPTY,
            validateTunnelRename("   ", others, isRunning = false),
        )
        assertEquals(
            TunnelRenameError.TAKEN,
            validateTunnelRename(" taken ", others, isRunning = false),
        )
        assertEquals(
            TunnelRenameError.RESERVED,
            validateTunnelRename(GLOBAL_CONFIG_NAME, others, isRunning = false),
        )
        assertEquals(
            TunnelRenameError.RUNNING,
            validateTunnelRename("free", others, isRunning = true),
        )
    }

    @Test
    fun `start shortcut with a stale name is ignored`() {
        assertTrue(isUnknownShortcutTunnel("old-name", found = false))
        assertFalse(isUnknownShortcutTunnel("office", found = true))
        assertFalse(isUnknownShortcutTunnel(null, found = false))
        assertFalse(isUnknownShortcutTunnel("  ", found = false))
    }
}

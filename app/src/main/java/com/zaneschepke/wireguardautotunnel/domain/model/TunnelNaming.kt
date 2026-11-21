package com.zaneschepke.wireguardautotunnel.domain.model

/** Keeps the id, so default flags and auto-tunnel mappings stay attached to this tunnel. */
fun TunnelConfig.renamedTo(newName: String): TunnelConfig {
    val name = newName.trim()
    return copy(name = name, quickConfig = getConfig().withName(name).asQuickString())
}

/**
 * A new tunnel with the same config (including keys) and connection settings. Default flags,
 * auto-tunnel mappings and runtime state belong to the original and are not carried over.
 */
fun TunnelConfig.duplicatedAs(name: String, position: Int): TunnelConfig =
    renamedTo(name)
        .copy(
            id = 0,
            position = position,
            isPrimaryTunnel = false,
            isMobileDataTunnel = false,
            isEthernetTunnel = false,
            tunnelNetworks = emptyList(),
            tunnelBSSIDs = emptyList(),
            autoTunnelApps = emptyList(),
        )

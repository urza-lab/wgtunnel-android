package com.zaneschepke.wireguardautotunnel.ui.screens.tunnels.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zaneschepke.wireguardautotunnel.R
import com.zaneschepke.wireguardautotunnel.domain.model.TunnelRenameError
import com.zaneschepke.wireguardautotunnel.ui.common.dialog.InfoDialog
import com.zaneschepke.wireguardautotunnel.ui.common.textbox.ConfigurationTextBox

@get:StringRes
val TunnelRenameError.messageRes: Int
    get() =
        when (this) {
            TunnelRenameError.EMPTY -> R.string.tunnel_name_empty
            TunnelRenameError.RESERVED -> R.string.tunnel_name_reserved
            TunnelRenameError.TAKEN -> R.string.tunnel_name_taken
            TunnelRenameError.RUNNING -> R.string.tunnel_running_name_message
        }

@Composable
fun TunnelNameDialog(
    initialName: String,
    validate: (String) -> TunnelRenameError?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var error by remember { mutableStateOf<TunnelRenameError?>(null) }

    InfoDialog(
        onDismiss = onDismiss,
        title = stringResource(R.string.rename_tunnel),
        body = {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                ConfigurationTextBox(
                    value = name,
                    label = stringResource(R.string.name),
                    hint = initialName,
                    onValueChange = {
                        name = it
                        error = null
                    },
                    isError = error != null,
                    supportingText = error?.let { { Text(stringResource(it.messageRes)) } },
                )
            }
        },
        confirmText = stringResource(R.string.okay),
        onAttest = {
            error = validate(name)
            if (error == null) onConfirm(name.trim())
        },
    )
}

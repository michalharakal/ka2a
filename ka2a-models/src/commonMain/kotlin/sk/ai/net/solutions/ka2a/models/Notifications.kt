// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package sk.ai.net.solutions.ka2a.models

import kotlinx.serialization.Serializable

// Push Notifications
@Serializable
data class PushNotificationConfig(
    val url: String,
    val token: String? = null,
    val authentication: Authentication? = null,
)

@Serializable
data class TaskPushNotificationConfig(
    val id: String,
    val pushNotificationConfig: PushNotificationConfig,
)

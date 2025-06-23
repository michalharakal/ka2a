// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package sk.ai.net.solutions.ka2a.client.pat


/**
 * List of Exceptions that can be thrown by the A2A4K client.
 */

sealed class A2AException(message: String, ex: Exception? = null) : Exception(message, ex)
class ServerException(message: String, ex: Exception? = null) : A2AException(message, ex)

package com.hackdefence.scanner.utils

import androidx.compose.ui.graphics.Color
import com.hackdefence.scanner.data.AttendeeDetails

object TicketColorHelper {
    // Color definitions based on ticket type and price
    private val VIP_1899_COLOR = Color(0xFF4CAF50) // Green
    private val VIP_1199_COLOR = Color(0xFFF44336) // Red
    private val STANDARD_COLOR = Color(0xFF2196F3) // Blue
    private val MEDIA_PARTNER_COLOR = Color(0xFF9C27B0) // Purple
    private val HIRING_PARTNER_COLOR = Color(0xFFFF9800) // Orange
    private val COLLABORATOR_COLOR = Color(0xFF00BCD4) // Light Blue
    private val FREE_COLOR = Color(0xFF212121) // Black
    private val DEFAULT_COLOR = Color(0xFF757575) // Grey

    data class TicketColorInfo(
        val backgroundColor: Color,
        val displayText: String,
        val subText: String?
    )

    fun getTicketColor(details: AttendeeDetails?): TicketColorInfo {
        if (details == null) {
            return TicketColorInfo(DEFAULT_COLOR, "UNKNOWN", null)
        }

        val ticketType = details.ticket_type?.lowercase() ?: ""
        val userType = details.user_type?.lowercase() ?: ""
        val price = details.final_price ?: 0.0
        val participationMode = details.participation_mode?.lowercase() ?: ""

        // Determine ticket category and color
        return when {
            // Media Partner - Purple
            userType.contains("media") || ticketType.contains("media") -> {
                TicketColorInfo(
                    MEDIA_PARTNER_COLOR,
                    "MEDIA PARTNER",
                    "Free Entry"
                )
            }

            // Hiring Partner - Orange
            userType.contains("hiring") || ticketType.contains("hiring") -> {
                TicketColorInfo(
                    HIRING_PARTNER_COLOR,
                    "HIRING PARTNER",
                    "Free Entry"
                )
            }

            // Collaborator - Light Blue
            ticketType.contains("collaborator") -> {
                TicketColorInfo(
                    COLLABORATOR_COLOR,
                    "COLLABORATOR",
                    "Free Entry"
                )
            }

            // VIP Tickets with specific prices
            ticketType.contains("vip") -> {
                when {
                    price == 1899.0 -> TicketColorInfo(
                        VIP_1899_COLOR,
                        "VIP TICKET",
                        "₹1899 Premium"
                    )
                    price == 1199.0 -> TicketColorInfo(
                        VIP_1199_COLOR,
                        "VIP TICKET",
                        "₹1199 Early Bird"
                    )
                    price == 0.0 -> TicketColorInfo(
                        FREE_COLOR,
                        "VIP TICKET",
                        "Free Entry"
                    )
                    // Any other VIP price → RED
                    else -> TicketColorInfo(
                        VIP_1199_COLOR,
                        "VIP TICKET",
                        String.format("₹%.0f", price)
                    )
                }
            }

            // Standard Tickets - Blue
            ticketType.contains("standard") -> {
                if (price == 0.0) {
                    TicketColorInfo(
                        FREE_COLOR,
                        "STANDARD TICKET",
                        "Free Entry"
                    )
                } else {
                    TicketColorInfo(
                        STANDARD_COLOR,
                        "STANDARD TICKET",
                        String.format("₹%.0f", price)
                    )
                }
            }

            // Free tickets (not collaborator/standard/special categories) - Black
            price == 0.0 -> {
                TicketColorInfo(
                    FREE_COLOR,
                    "FREE ENTRY",
                    ticketType.uppercase().ifEmpty { "General" }
                )
            }

            // Default case
            else -> {
                TicketColorInfo(
                    DEFAULT_COLOR,
                    ticketType.uppercase().ifEmpty { "TICKET" },
                    if (price > 0) String.format("₹%.0f", price) else null
                )
            }
        }
    }

    fun getStatusColor(isSuccess: Boolean, isWarning: Boolean, isError: Boolean): Color {
        return when {
            isSuccess -> Color(0xFF4CAF50) // Green
            isWarning -> Color(0xFFFF9800) // Orange
            isError -> Color(0xFFF44336) // Red
            else -> DEFAULT_COLOR
        }
    }
}

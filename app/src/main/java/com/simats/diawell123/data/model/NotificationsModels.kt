package com.simats.diawell123.data.model

import com.google.gson.annotations.SerializedName

data class NotificationsResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: NotificationsData? = null
)

data class NotificationsData(
    @SerializedName("user_id")
    val userId: Int = 0,

    @SerializedName("date_header")
    val dateHeader: String = "Today",

    @SerializedName("total")
    val total: Int = 0,

    @SerializedName("unread_count")
    val unreadCount: Int = 0,

    @SerializedName("notifications")
    val notifications: List<NotificationItemData> = emptyList()
)

data class NotificationItemData(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("title")
    val title: String = "",

    @SerializedName("body")
    val body: String = "",

    @SerializedName("type")
    val type: String = "general",

    @SerializedName("time")
    val time: String = "",

    @SerializedName("created_at")
    val createdAt: String = "",

    @SerializedName("is_unread")
    val isUnread: Boolean = false,

    @SerializedName("is_read")
    val isRead: Int = 0
)

data class MarkReadRequest(
    @SerializedName("notification_id")
    val notificationId: Int
)

data class SimpleNotificationResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = ""
)


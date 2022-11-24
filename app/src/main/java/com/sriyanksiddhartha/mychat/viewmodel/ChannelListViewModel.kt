package com.sriyanksiddhartha.mychat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.chat.android.client.ChatClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ChannelListViewModel @Inject constructor(
    private val client: ChatClient
) : ViewModel() {

    private val _createChannelEvent = MutableSharedFlow<CreateChannelEvent>()
    val createChannelEvent = _createChannelEvent.asSharedFlow()

    fun logout() {
        client.disconnect()
    }

    fun createChannel(channelName: String, channelType: String = "messaging") {

        val trimmedChannelName = channelName.trim()
        val channelId = UUID.randomUUID().toString()

        viewModelScope.launch {

            if (trimmedChannelName.isEmpty()) {
                _createChannelEvent.emit(
                    CreateChannelEvent.Error("Название канала не может быть пустым.")
                )
                return@launch
            }

            client.createChannel(
                channelType = channelType,
                channelId = channelId,
                memberIds = emptyList(),
                extraData = mapOf(
                    "name" to trimmedChannelName,
                    "image" to "https://bit.ly/2TIt8NR"
                )
            ).enqueue { result ->

                if (result.isSuccess) {
                    viewModelScope.launch {
                        _createChannelEvent.emit(CreateChannelEvent.Success)
                    }
                } else {
                    viewModelScope.launch {
                        _createChannelEvent.emit(
                            CreateChannelEvent.Error(result.error().message ?: "Ошибка")
                        )
                    }
                }
            }
        }

    }

    sealed class CreateChannelEvent {
        data class Error(val error: String) : CreateChannelEvent()
        object Success : CreateChannelEvent()
    }
}
package com.comp1786.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class EventViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = EventDatabase.get(application).events()
    val events = dao.observeAll()
    private val errorChannel = Channel<Unit>(Channel.BUFFERED)
    val errors = errorChannel.receiveAsFlow()

    fun save(event: Event, onSuccess: () -> Unit) = change(onSuccess) {
        if (event.id == 0L) dao.insert(event) else dao.update(event)
    }
    fun delete(event: Event, onSuccess: () -> Unit) = change(onSuccess) { dao.delete(event) }
    private fun change(onSuccess: () -> Unit, action: suspend () -> Unit) {
        viewModelScope.launch {
            try { action(); onSuccess() }
            catch (cancel: CancellationException) { throw cancel }
            catch (_: Exception) { errorChannel.send(Unit) }
        }
    }
}

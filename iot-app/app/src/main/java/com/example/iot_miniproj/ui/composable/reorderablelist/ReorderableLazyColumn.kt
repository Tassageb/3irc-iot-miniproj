package com.example.iot_miniproj.ui.composable.reorderablelist

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun <T : Any> ReorderableLazyColumn(
    modifier: Modifier = Modifier,
    itemsList: ArrayList<T>,
    onMove: (ArrayList<T>) -> Unit,
    element: @Composable (modifier: Modifier, elt: T) -> Unit
) {
    val stateList = rememberLazyListState()

    val dragDropState =
        rememberDragDropState(
            lazyListState = stateList,
            draggableItemsNum = itemsList.size,
            onMove = { fromIndex, toIndex ->
                Log.d("ITEMS", itemsList.joinToString { it.toString() })
                itemsList.add(toIndex, itemsList.removeAt(fromIndex))
                onMove(itemsList)
            })

    LazyColumn(
        modifier = modifier.dragContainer(dragDropState),
        state = stateList,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false
    ) {
        item {
            Text(text = "Ordre d'affichage", fontSize = 30.sp)
        }

        draggableItems(items = itemsList, dragDropState = dragDropState) { modifier, item ->
            element(modifier, item)
        }

    }
}
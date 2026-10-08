package com.example.productivitylauncher.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.ui.Nav
import com.example.productivitylauncher.ui.components.AppField
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.BtnKind
import com.example.productivitylauncher.ui.components.CButton
import com.example.productivitylauncher.ui.components.CeramicCard
import com.example.productivitylauncher.ui.components.Ic
import com.example.productivitylauncher.ui.components.PageHeader
import com.example.productivitylauncher.ui.components.RadiusMd
import com.example.productivitylauncher.ui.theme.AppColors

/** Capture everything on your mind now. Sort it later: make it the frog, or let it go. */
@Composable
fun BrainDumpScreen(state: LauncherState, nav: Nav) {
    var text by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        PageHeader("Brain dump", onBack = nav.back)
        Row(Modifier.padding(horizontal = 28.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) {
                AppField(text, { text = it }, "What's on your mind?", fill = AppColors.card, onDone = { state.addDump(text); text = "" })
            }
            CButton("Add", { state.addDump(text); text = "" }, kind = BtnKind.Primary, small = true, enabled = text.isNotBlank(), leading = Ic.Plus)
        }
        if (state.dumps.isEmpty()) {
            AppText("Nothing here. Your head is clear.", color = AppColors.muted, modifier = Modifier.padding(28.dp))
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 28.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.dumps, key = { it.id }) { d ->
                CeramicCard(shape = RadiusMd, padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 16.dp)) {
                    AppText(d.text, size = 16.sp, lineHeight = 22.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CButton("Make it my frog", {
                            state.updateFrog(d.text); state.setFrogDone(false); state.removeDump(d.id); nav.back()
                        }, kind = BtnKind.Accent, small = true)
                        CButton("Let it go", { state.removeDump(d.id) }, kind = BtnKind.Soft, small = true, leading = Ic.Trash)
                    }
                }
            }
        }
    }
}

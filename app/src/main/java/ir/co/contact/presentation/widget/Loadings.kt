package ir.co.contact.presentation.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ScreenLoading(
    modifier: Modifier,
    transparent: Boolean = false
) {
    Box(
        modifier = modifier.fillMaxSize()
        .background(if(transparent) Color.Transparent else MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = modifier
                .width(50.dp)
                .height(50.dp)
                .align(Alignment.Center),
            strokeWidth = 2.dp
        )
    }
}



@Preview
@Composable
fun ScreenLoadingPreview(){
    ScreenLoading(Modifier, true)
}


package cm.project.android.projectx.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cm.project.android.projectx.R
import cm.project.android.projectx.ui.AppViewModel
import coil.compose.AsyncImage

data class Level(
    val level: Int,
    val min: Int,
    val max: Int
)

data class Rank(
    val rank: String,
    val min: Int,
    val max: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowUserDetails(
    vm: AppViewModel = viewModel(),
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    vm.user?.let { vm.getUser(it.uid) }

    val ranks = listOf(Rank("Rookie",0,999),
        Rank("Explorer",1000,1999),
        Rank("Adventurer",2000,2999),
        Rank("Discoverer",3000,3999),
        Rank("Expert",4000,4999),
        Rank("Master",5000,5999),
        Rank("Legend",6000,6999),
        Rank("Hero",7000,7999),
        Rank("God",8000,8999),
        Rank("Creator",9000,9999))

    val levels = listOf(Level(1,0,9),
        Level(2,10,19),
        Level(3,20,29),
        Level(4,30,39),
        Level(5,40,49),
        Level(6,50,59),
        Level(7,60,69),
        Level(8,70,79),
        Level(9,80,89),
        Level(10,90,99))

    Column {
        Row(modifier = Modifier.height(250.dp)) {
            AsyncImage(model = vm.userl?.pictureUrl,
                contentDescription = "User Picture",
                modifier = Modifier
                    .padding(20.dp)
                    .clip(RoundedCornerShape(20.dp)))
            Column(modifier = Modifier
                .padding(top = 50.dp)
                ) {
                Text(text = vm.user?.displayName ?: "No name",
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                )
                Text(text = vm.userl?.username ?: "No name",
                    modifier = Modifier)
            }
        }

        Row {
            Row(modifier = Modifier
                .padding(start = 20.dp, end = 20.dp)
                .border(
                    border = BorderStroke(1.dp, color = Color.Black),
                    shape = RoundedCornerShape(20.dp),
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "XP",
                    modifier = Modifier
                        .padding(10.dp)
                        .width(50.dp)
                        .height(50.dp)
                )
                Column(modifier = Modifier.padding(end = 10.dp)) {
                    Text(text = "${vm.userl!!.totalXP}",
                        fontSize = 20.sp,
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .align(Alignment.CenterHorizontally),
                    )
                    Text(text = "Total XP",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 5.dp))
                }
            }
            Column {
                Text(text = "Rank: ${ranks[vm.userl!!.totalXP/1000].rank}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center)
                Text(text = "${ranks[vm.userl!!.totalXP/1000].max + 1  - vm.userl!!.totalXP} XP to next rank",
                    modifier = Modifier.padding(top = 5.dp),
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center)
            }
        }


        Details(vm = vm, icon = Icons.Default.LocationOn, text = "AddedPOIs", number = vm.userl!!.addedPOIs, levels = levels)
    
        Details(vm = vm, icon = Icons.Default.Star, text = "Received Ratings", number = vm.userl!!.receivedRatings, levels = levels)

        Details(vm = vm, icon = Icons.Default.ThumbUp, text = "Given Ratings", number = vm.userl!!.givenRatings, levels = levels)
    }
}

@Composable
fun Details(
    vm: AppViewModel,
    icon: ImageVector,
    text: String,
    number: Int,
    levels: List<Level>,
) {

    var l: Level? = null

    //calculate level of user
    for (level in levels) {
        if (level.min <= number && level.max >= number) {
            l = level
        }
    }

    Row(modifier = Modifier
        .padding(top = 10.dp, start = 20.dp, end = 20.dp)
        .border(
            border = BorderStroke(1.dp, color = Color.Black),
            shape = RoundedCornerShape(20.dp),
        )
        .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, top = 10.dp, bottom = 10.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = "Icon",
                modifier = Modifier
                    .width(50.dp)
                    .height(50.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Text(text = "Level: ${l!!.level}",
                modifier = Modifier,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center)
        }
        Column(modifier = Modifier
            .padding(start = 20.dp)){
            Text(text = text,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp))
            Row(modifier = Modifier.padding(top = 10.dp)) {
                LinearProgressIndicator(
                    progress = number.toFloat()/ l!!.max.toFloat(),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .width(150.dp)
                        .height(30.dp)
                )
                Text(text = "$number/${l?.max!! + 1 ?: 0}",
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .align(Alignment.CenterVertically),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center)
            }
        }
    }
}

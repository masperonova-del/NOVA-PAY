package com.novapay.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.text.style.TextOverflow

private val Ink=Color(0xFF080B12); private val Panel=Color(0xFF111827)
private val Blue=Color(0xFF172238); private val Gold=Color(0xFFD7B56D)
private val Violet=Color(0xFF8274E8); private val Silver=Color(0xFFDCE3EF)
private val Muted=Color(0xFF929DB0); private val Green=Color(0xFF55C99A)

class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
 window.statusBarColor=android.graphics.Color.rgb(8,11,18)
 window.navigationBarColor=android.graphics.Color.rgb(8,11,18)
 setContent { NovaApp() }}
}

@Composable fun NovaApp(){
 var page by rememberSaveable{mutableIntStateOf(0)}
 var hidden by rememberSaveable{mutableStateOf(false)}
 var frozen by rememberSaveable{mutableStateOf(false)}
 var dialog by remember{mutableStateOf(false)}
 MaterialTheme(colorScheme=darkColorScheme(primary=Gold,secondary=Violet,background=Ink,surface=Panel)){
 Scaffold(containerColor=Ink,bottomBar={
 NavigationBar(containerColor=Color(0xFF0D1320)){
 val labels=listOf("Accueil","Cartes","Activité","Profil")
 val icons=listOf(Icons.Rounded.Home,Icons.Rounded.CreditCard,Icons.Rounded.ReceiptLong,Icons.Rounded.Person)
 labels.forEachIndexed{i,s->NavigationBarItem(selected=page==i,onClick={page=i},icon={Icon(icons[i],null)},label={Text(s,fontSize=10.sp)},colors=NavigationBarItemDefaults.colors(selectedIconColor=Gold,selectedTextColor=Gold,indicatorColor=Gold.copy(.12f),unselectedIconColor=Muted,unselectedTextColor=Muted))}
 }}){pad->
 Column(Modifier.fillMaxSize().padding(pad).padding(horizontal=20.dp)){
 when(page){
 0->Home(hidden,{hidden=!hidden},frozen,{frozen=!frozen},{page=1})
 1->Cards(frozen,{frozen=!frozen},{dialog=true})
 2->ActivityPage()
 else->Profile({dialog=true})
 }
 }
 if(dialog) AlertDialog(onDismissRequest={dialog=false},containerColor=Panel,title={Text("NOVA PAY",color=Gold)},text={Text("Cette fonction sera reliée à un service sécurisé dans une prochaine étape. Aucune opération bancaire réelle n'est effectuée dans cette version.",color=Silver)},confirmButton={TextButton(onClick={dialog=false}){Text("Fermer",color=Gold)}})
 }}
}

@Composable fun Brand(){
 Row(verticalAlignment=Alignment.CenterVertically,modifier=Modifier.padding(top=22.dp,bottom=22.dp)){
 Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Brush.linearGradient(listOf(Blue,Panel))).border(1.dp,Gold.copy(.7f),RoundedCornerShape(14.dp)),contentAlignment=Alignment.Center){
 Text("N",fontSize=29.sp,fontWeight=FontWeight.Black,color=Gold)
 Box(Modifier.align(Alignment.TopEnd).padding(7.dp).size(5.dp).clip(CircleShape).background(Violet))
 }
 Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)){
 Text("NOVA PAY",fontSize=19.sp,fontWeight=FontWeight.Black,letterSpacing=2.sp,color=Silver)
 Text("L'argent, sans frontières.",fontSize=11.sp,color=Muted)
 }
 }
}

@Composable fun Home(hidden:Boolean,toggle:()->Unit,frozen:Boolean,freeze:()->Unit,toCards:()->Unit){
 LazyColumn(contentPadding=PaddingValues(bottom=22.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){
 item{Brand()}
 item{Column{
 Text("SOLDE DISPONIBLE",fontSize=10.sp,letterSpacing=2.sp,color=Muted)
 Row(verticalAlignment=Alignment.Bottom){Text(if(hidden)"••••••" else "125 000",fontSize=35.sp,fontWeight=FontWeight.Bold,color=Silver);Spacer(Modifier.width(8.dp));Text("FCFA",fontSize=13.sp,color=Gold,modifier=Modifier.padding(bottom=6.dp));Spacer(Modifier.weight(1f));IconButton(onClick=toggle){Icon(if(hidden)Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,null,tint=Gold)}}
 Text("Solde fictif de démonstration",fontSize=11.sp,color=Muted)
 }}
 item{CardVisual(frozen)}
 item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
 Action(Icons.Rounded.CreditCard,"Mes cartes",toCards,Modifier.weight(1f))
 Action(Icons.Rounded.Lock,if(frozen)"Débloquer" else "Verrouiller",freeze,Modifier.weight(1f))
 Action(Icons.Rounded.ReceiptLong,"Activité",{},Modifier.weight(1f))
 }}
 item{Text("Activité récente",fontSize=17.sp,fontWeight=FontWeight.Bold,color=Silver)}
 item{Tx("Abonnement numérique","Aujourd'hui • Démonstration","− 4 900 FCFA",Icons.Rounded.Subscriptions,false)}
 item{Tx("Achat en ligne","Hier • Démonstration","− 12 500 FCFA",Icons.Rounded.ShoppingBag,false)}
 item{Tx("Recharge fictive","28 oct. • Démonstration","+ 25 000 FCFA",Icons.Rounded.SouthWest,true)}
 item{Notice()}
 }
}

@Composable fun CardVisual(frozen:Boolean){
 Box(Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(listOf(Color(0xFF28334A),Color(0xFF101827),Color(0xFF332A50)))).border(1.dp,Gold.copy(.55f),RoundedCornerShape(24.dp)).padding(20.dp)){
 Column(Modifier.fillMaxSize(),verticalArrangement=Arrangement.SpaceBetween){
 Row(verticalAlignment=Alignment.CenterVertically){Text("NOVA",color=Gold,fontSize=18.sp,fontWeight=FontWeight.Black,letterSpacing=3.sp);Spacer(Modifier.weight(1f));Text("VIRTUAL",color=Silver,fontSize=9.sp,letterSpacing=2.sp)}
 Column(verticalArrangement=Arrangement.spacedBy(7.dp)){Text("••••  ••••  ••••  2048",color=Silver,fontSize=18.sp,letterSpacing=2.sp);Row{Text("TEST CARD",color=Muted,fontSize=9.sp);Spacer(Modifier.weight(1f));Text("DEMO",color=Gold,fontWeight=FontWeight.Bold,fontSize=10.sp)}}
 Row(verticalAlignment=Alignment.CenterVertically){Text("NOVA MEMBER",color=Silver,fontSize=10.sp);Spacer(Modifier.weight(1f));Text("N",color=Gold,fontSize=27.sp,fontWeight=FontWeight.Black)}
 }
 if(frozen) Box(Modifier.fillMaxSize().background(Ink.copy(.75f)),contentAlignment=Alignment.Center){Text("🔒  CARTE VERROUILLÉE",color=Gold,fontWeight=FontWeight.Bold)}
 }
}

@Composable fun Cards(frozen:Boolean,toggle:()->Unit,add:()->Unit){
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top=24.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){
 Heading("Mes cartes","Gérez vos moyens de paiement")
 CardVisual(frozen)
 Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("Carte virtuelle de démonstration",color=Silver,fontWeight=FontWeight.SemiBold);Text("Aucune donnée bancaire réelle",color=Muted,fontSize=11.sp)};Switch(frozen,{toggle()},colors=SwitchDefaults.colors(checkedThumbColor=Gold,checkedTrackColor=Gold.copy(.3f)))}
 Button(onClick=add,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp),colors=ButtonDefaults.buttonColors(containerColor=Gold,contentColor=Ink)){Icon(Icons.Rounded.Add,null);Spacer(Modifier.width(8.dp));Text("Ajouter une carte")}
 Notice()
 }
}

@Composable fun ActivityPage(){
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top=24.dp)){
 Heading("Activité","Vos mouvements en un coup d'œil")
 Spacer(Modifier.height(16.dp))
 Tx("Abonnement numérique","03 nov. 2026 • Démonstration","− 4 900 FCFA",Icons.Rounded.Subscriptions,false)
 Tx("Achat en ligne","02 nov. 2026 • Démonstration","− 12 500 FCFA",Icons.Rounded.ShoppingBag,false)
 Tx("Recharge fictive","28 oct. 2026 • Démonstration","+ 25 000 FCFA",Icons.Rounded.SouthWest,true)
 Notice()
 }
}

@Composable fun Profile(open:()->Unit){
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top=24.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){
 Heading("Profil","Votre espace NOVA")
 listOf("Informations personnelles","Sécurité et confidentialité","Notifications","Centre d'aide","À propos de NOVA PAY").forEach{label->
 Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Panel).clickable{open()}.padding(16.dp),verticalAlignment=Alignment.CenterVertically){
 Icon(Icons.Rounded.AccountCircle,null,tint=Gold);Spacer(Modifier.width(12.dp));Text(label,color=Silver,modifier=Modifier.weight(1f),fontSize=13.sp);Icon(Icons.Rounded.ChevronRight,null,tint=Muted)
 }}
 Notice()
 }
}

@Composable fun Heading(title:String,sub:String){Column(Modifier.padding(bottom=4.dp)){Text(title,fontSize=27.sp,fontWeight=FontWeight.Bold,color=Silver);Text(sub,fontSize=12.sp,color=Muted)}}
@Composable fun Action(icon:androidx.compose.ui.graphics.vector.ImageVector,label:String,click:()->Unit,mod:Modifier){Column(mod.clip(RoundedCornerShape(15.dp)).background(Panel).clickable{click()}.padding(vertical=15.dp,horizontal=5.dp),horizontalAlignment=Alignment.CenterHorizontally){Icon(icon,null,tint=Gold);Spacer(Modifier.height(7.dp));Text(label,fontSize=10.sp,color=Silver,maxLines=1,overflow=TextOverflow.Ellipsis)}}
@Composable fun Tx(title:String,sub:String,amount:String,icon:androidx.compose.ui.graphics.vector.ImageVector,incoming:Boolean){
 Row(Modifier.fillMaxWidth().padding(vertical=11.dp),verticalAlignment=Alignment.CenterVertically){
 Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(Blue),contentAlignment=Alignment.Center){Icon(icon,null,tint=if(incoming)Green else Gold)}
 Spacer(Modifier.width(11.dp));Column(Modifier.weight(1f)){Text(title,color=Silver,fontSize=12.sp,fontWeight=FontWeight.SemiBold);Text(sub,color=Muted,fontSize=10.sp)}
 Text(amount,color=if(incoming)Green else Silver,fontSize=11.sp,fontWeight=FontWeight.SemiBold)
 }
}
@Composable fun Notice(){
 Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(Panel).padding(15.dp)){
 Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Rounded.Shield,null,tint=Gold);Spacer(Modifier.width(8.dp));Text("Version 1.0 • Démonstration",color=Gold,fontWeight=FontWeight.Bold,fontSize=12.sp)}
 Spacer(Modifier.height(6.dp));Text("Les montants, cartes et opérations affichés sont fictifs. NOVA PAY ne détient pas de fonds, n'émet pas de carte bancaire et n'effectue aucun paiement réel dans cette version.",color=Muted,fontSize=11.sp,lineHeight=16.sp)
 }
}

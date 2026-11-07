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
import kotlinx.coroutines.launch
import io.github.jan.supabase.auth.signOut
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*

private val Ink=Color(0xFF080B12); private val Panel=Color(0xFF111827)
private val Blue=Color(0xFF172238); private val Gold=Color(0xFFD7B56D)
private val Violet=Color(0xFF8274E8); private val Silver=Color(0xFFDCE3EF)
private val Muted=Color(0xFF929DB0); private val Green=Color(0xFF55C99A)
private const val PREFS_NAME="nova_pay_demo"
private const val KEY_FROZEN="demo_card_frozen"
private const val KEY_LIMIT="demo_spending_limit"
private const val KEY_BALANCE="demo_balance"
private const val KEY_HISTORY="demo_history"

class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
 window.statusBarColor=android.graphics.Color.rgb(8,11,18)
 window.navigationBarColor=android.graphics.Color.rgb(8,11,18)
 setContent { NovaApp() }}
}

@Composable fun NovaApp(){
 val context=LocalContext.current
 val prefs=remember{context.getSharedPreferences(PREFS_NAME,android.content.Context.MODE_PRIVATE)}
 val authStorage=remember{AuthStorage(context)}
 var currentUser by remember{mutableStateOf(NovaSupabase.currentUser())}
 val scope = rememberCoroutineScope()
 var page by rememberSaveable{mutableIntStateOf(0)}
 var hidden by rememberSaveable{mutableStateOf(false)}
 var frozen by remember{mutableStateOf(prefs.getBoolean(KEY_FROZEN,false))}
 var spendingLimit by remember{mutableIntStateOf(prefs.getInt(KEY_LIMIT,100000))}
 var balance by remember{mutableLongStateOf(prefs.getLong(KEY_BALANCE,125000L))}
 var history by remember{mutableStateOf(prefs.getString(KEY_HISTORY,"") ?: "")}
 var transactionDialog by rememberSaveable{mutableStateOf(false)}
 var txAmount by rememberSaveable{mutableStateOf("")}
 var txLabel by rememberSaveable{mutableStateOf("")}
 var txError by rememberSaveable{mutableStateOf("")}
 var dialog by remember{mutableStateOf(false)}

 MaterialTheme(colorScheme=darkColorScheme(primary=Gold,secondary=Violet,background=Ink,surface=Panel)){
  if(currentUser==null){
   AuthScreen(authStorage=authStorage,onAuthenticated={user->currentUser=user;page=0})
  }else{
   val user=currentUser!!
   val toggleFrozen={
    frozen=!frozen
    prefs.edit().putBoolean(KEY_FROZEN,frozen).apply()
   }
   val updateLimit:(Int)->Unit={value->
    spendingLimit=value
    prefs.edit().putInt(KEY_LIMIT,value).apply()
   }
   Scaffold(containerColor=Ink,bottomBar={
    NavigationBar(containerColor=Color(0xFF0D1320)){
     val labels=listOf("Accueil","Cartes","Activité","Profil")
     val icons=listOf(Icons.Rounded.Home,Icons.Rounded.CreditCard,Icons.Rounded.ReceiptLong,Icons.Rounded.Person)
     labels.forEachIndexed{i,s->NavigationBarItem(selected=page==i,onClick={page=i},icon={Icon(icons[i],null)},label={Text(s,fontSize=10.sp)},colors=NavigationBarItemDefaults.colors(selectedIconColor=Gold,selectedTextColor=Gold,indicatorColor=Gold.copy(.12f),unselectedIconColor=Muted,unselectedTextColor=Muted))}
    }
   }){pad->
    Column(Modifier.fillMaxSize().padding(pad).padding(horizontal=20.dp)){
     when(page){
      0->Home(hidden,{hidden=!hidden},frozen,toggleFrozen,{page=1},{page=2},balance,{transactionDialog=true})
      1->Cards(frozen,toggleFrozen,spendingLimit,updateLimit)
      2->ActivityPage(history)
      else->Profile(user=user,open={dialog=true},onProfileUpdated={updated->authStorage.updateProfile(updated.name,updated.email,updated.phone)?.let{currentUser=it}},onLogout={
       scope.launch {
       try { NovaSupabase.client.auth.signOut() } finally { currentUser=null; page=0 }
      }
      })
     }
    }
    if(transactionDialog) AlertDialog(onDismissRequest={transactionDialog=false},containerColor=Panel,title={Text("Transaction simulée",color=Gold)},text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){Text("Démonstration uniquement — aucun paiement réel.",color=Muted,fontSize=12.sp);OutlinedTextField(value=txLabel,onValueChange={txLabel=it;txError=""},label={Text("Libellé")},singleLine=true);OutlinedTextField(value=txAmount,onValueChange={txAmount=it.filter{c->c.isDigit()};txError=""},label={Text("Montant en FCFA")},singleLine=true);if(txError.isNotBlank())Text(txError,color=Color(0xFFFF8A80),fontSize=12.sp)}},confirmButton={TextButton(onClick={val amount=txAmount.toLongOrNull();when{txLabel.isBlank()->txError="Ajoute un libellé.";amount==null||amount<=0->txError="Montant invalide.";amount>balance->txError="Solde fictif insuffisant.";else->{balance-=amount;prefs.edit().putLong(KEY_BALANCE,balance).apply();val row="${System.currentTimeMillis()}|${txLabel.replace("|"," ")}|-$amount";history=if(history.isBlank())row else "$row\n$history";prefs.edit().putString(KEY_HISTORY,history).apply();txLabel="";txAmount="";txError="";transactionDialog=false}}}){Text("Confirmer la simulation",color=Gold)}},dismissButton={TextButton(onClick={transactionDialog=false}){Text("Annuler",color=Muted)}})
    if(dialog) AlertDialog(onDismissRequest={dialog=false},containerColor=Panel,title={Text("NOVA PAY",color=Gold)},text={Text("Cette rubrique est une maquette. Aucune opération bancaire réelle n'est effectuée.",color=Silver)},confirmButton={TextButton(onClick={dialog=false}){Text("Fermer",color=Gold)}})
   }
  }
 }
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

@Composable fun Home(hidden:Boolean,toggle:()->Unit,frozen:Boolean,freeze:()->Unit,toCards:()->Unit,toActivity:()->Unit,balance:Long,newTransaction:()->Unit){
 LazyColumn(contentPadding=PaddingValues(bottom=22.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){
 item{Brand()}
 item{Column{
 Text("SOLDE DISPONIBLE",fontSize=10.sp,letterSpacing=2.sp,color=Muted)
 Row(verticalAlignment=Alignment.Bottom){Text(if(hidden)"••••••" else "%,d".format(java.util.Locale.FRANCE,balance),fontSize=35.sp,fontWeight=FontWeight.Bold,color=Silver);Spacer(Modifier.width(8.dp));Text("FCFA",fontSize=13.sp,color=Gold,modifier=Modifier.padding(bottom=6.dp));Spacer(Modifier.weight(1f));IconButton(onClick=toggle){Icon(if(hidden)Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,null,tint=Gold)}}
 Text("Solde fictif de démonstration",fontSize=11.sp,color=Muted)
 }}
 item{CardVisual(frozen)}
 item{Button(onClick=newTransaction,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp),colors=ButtonDefaults.buttonColors(containerColor=Gold,contentColor=Ink)){Icon(Icons.Rounded.Add,null);Spacer(Modifier.width(8.dp));Text("Nouvelle transaction simulée",fontWeight=FontWeight.Bold)}}
 item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
 Action(Icons.Rounded.CreditCard,"Mes cartes",toCards,Modifier.weight(1f))
 Action(if(frozen)Icons.Rounded.LockOpen else Icons.Rounded.Lock,if(frozen)"Activer" else "Geler",freeze,Modifier.weight(1f))
 Action(Icons.Rounded.ReceiptLong,"Activité",toActivity,Modifier.weight(1f))
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
 Row(verticalAlignment=Alignment.CenterVertically){Text("NOVA",color=Gold,fontSize=18.sp,fontWeight=FontWeight.Black,letterSpacing=3.sp);Spacer(Modifier.weight(1f));Text("VIRTUAL • DEMO",color=Silver,fontSize=9.sp,letterSpacing=1.sp)}
 Column(verticalArrangement=Arrangement.spacedBy(7.dp)){Text("••••  ••••  ••••  2048",color=Silver,fontSize=18.sp,letterSpacing=2.sp);Row{Text("CARTE DE TEST",color=Muted,fontSize=9.sp);Spacer(Modifier.weight(1f));Text("NON VALIDE",color=Gold,fontWeight=FontWeight.Bold,fontSize=9.sp)}}
 Row(verticalAlignment=Alignment.CenterVertically){Text("NOVA MEMBER",color=Silver,fontSize=10.sp);Spacer(Modifier.weight(1f));Text("N",color=Gold,fontSize=27.sp,fontWeight=FontWeight.Black)}
 }
 if(frozen) Box(Modifier.fillMaxSize().background(Ink.copy(.78f)),contentAlignment=Alignment.Center){Text("CARTE GELÉE • DÉMO",color=Gold,fontWeight=FontWeight.Bold)}
 }
}

@Composable fun Cards(frozen:Boolean,toggle:()->Unit,limit:Int,onLimitChange:(Int)->Unit){
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top=24.dp,bottom=22.dp),verticalArrangement=Arrangement.spacedBy(17.dp)){
 Heading("Mes cartes","Gérez votre carte de démonstration")
 CardVisual(frozen)
 Row(verticalAlignment=Alignment.CenterVertically){
 Column(Modifier.weight(1f)){Text("État de la carte",color=Silver,fontWeight=FontWeight.SemiBold);Text(if(frozen)"Gelée • Les actions de démo sont suspendues" else "Active • Mode démonstration",color=if(frozen)Gold else Green,fontSize=11.sp)}
 Switch(checked=!frozen,onCheckedChange={toggle()},colors=SwitchDefaults.colors(checkedThumbColor=Gold,checkedTrackColor=Gold.copy(.3f),uncheckedThumbColor=Muted))
 }
 Button(onClick=toggle,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp),colors=ButtonDefaults.buttonColors(containerColor=if(frozen)Green else Color(0xFF29364D),contentColor=if(frozen)Ink else Silver)){
 Icon(if(frozen)Icons.Rounded.LockOpen else Icons.Rounded.Lock,null);Spacer(Modifier.width(8.dp));Text(if(frozen)"Activer la carte de démo" else "Geler la carte de démo")
 }
 Divider(color=Panel)
 Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
 Text("Plafond de dépenses simulé",color=Silver,fontWeight=FontWeight.SemiBold)
 Text("%,d FCFA".format(java.util.Locale.FRANCE,limit),fontSize=25.sp,fontWeight=FontWeight.Bold,color=Gold)
 Slider(value=limit.toFloat(),onValueChange={onLimitChange(it.toInt())},valueRange=10000f..500000f,steps=48,colors=SliderDefaults.colors(thumbColor=Gold,activeTrackColor=Gold,inactiveTrackColor=Blue))
 Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("10 000",color=Muted,fontSize=10.sp);Text("500 000 FCFA",color=Muted,fontSize=10.sp)}
 Text("Ce plafond est uniquement local et ne bloque aucun paiement réel.",color=Muted,fontSize=10.sp)
 }
 Divider(color=Panel)
 Column(verticalArrangement=Arrangement.spacedBy(10.dp)){
 Text("Informations de démonstration",color=Silver,fontWeight=FontWeight.SemiBold)
 InfoLine("Numéro affiché","•••• •••• •••• 2048")
 InfoLine("Titulaire","NOVA MEMBER")
 InfoLine("Réseau","Aucun réseau de paiement")
 InfoLine("Sécurité","Aucun CVV ni code bancaire")
 }
 Notice()
 }
}

@Composable fun InfoLine(label:String,value:String){
 Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Panel).padding(13.dp),verticalAlignment=Alignment.CenterVertically){
 Text(label,color=Muted,fontSize=11.sp,modifier=Modifier.weight(1f));Text(value,color=Silver,fontSize=11.sp,fontWeight=FontWeight.SemiBold)
 }
}

@Composable fun ActivityPage(history:String){
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top=24.dp)){
 Heading("Activité","Transactions fictives enregistrées sur cet appareil")
 Spacer(Modifier.height(16.dp))
 if(history.isBlank()) Text("Aucune transaction simulée pour le moment.",color=Muted)
 else history.lines().forEach { line ->
  val parts=line.split("|",limit=3)
  if(parts.size==3){val date=java.text.SimpleDateFormat("dd MMM yyyy • HH:mm",java.util.Locale.FRANCE).format(java.util.Date(parts[0].toLongOrNull()?:0L));Tx(parts[1],"$date • Démonstration",parts[2]+" FCFA",Icons.Rounded.ReceiptLong,false)}
 }
 Spacer(Modifier.height(14.dp));Notice()
 }
}

@Composable fun Profile(user:AuthUser,open:()->Unit,onProfileUpdated:(AuthUser)->Unit,onLogout:()->Unit){
 var editing by remember { mutableStateOf(false) }
 var verifying by remember { mutableStateOf(false) }
 var name by remember(user.name) { mutableStateOf(user.name) }
 var email by remember(user.email) { mutableStateOf(user.email) }
 var phone by remember(user.phone) { mutableStateOf(user.phone) }
 var error by remember { mutableStateOf("") }
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top=24.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){
  Heading("Profil","Votre espace NOVA")
  Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Panel).padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
   Row(verticalAlignment=Alignment.CenterVertically){
    Icon(Icons.Rounded.AccountCircle,null,tint=Gold,modifier=Modifier.size(42.dp))
    Spacer(Modifier.width(12.dp))
    Column(Modifier.weight(1f)){
     Text(user.name,color=Silver,fontWeight=FontWeight.Bold,fontSize=17.sp)
     Text(user.email,color=Muted,fontSize=11.sp)
    }
   }
   Divider(color=Blue)
   InfoLine("Téléphone",user.phone)
   Row(verticalAlignment=Alignment.CenterVertically){
    Column(Modifier.weight(1f)){
     Text("Vérification du téléphone",color=Muted,fontSize=11.sp)
     Text(if(user.phoneVerified)"Vérifié" else "Non vérifié",color=if(user.phoneVerified)Green else Gold,fontSize=12.sp,fontWeight=FontWeight.SemiBold)
    }
    TextButton(onClick={verifying=true},enabled=!user.phoneVerified){Text(if(user.phoneVerified)"Vérifié" else "Vérifier",color=Gold)}
   }
   InfoLine("Type de compte","Compte local de démonstration")
  }
  Button(onClick={editing=true},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp),colors=ButtonDefaults.buttonColors(containerColor=Gold,contentColor=Ink)){
   Icon(Icons.Rounded.Edit,null);Spacer(Modifier.width(8.dp));Text("Modifier mes informations")
  }
  listOf("Sécurité et confidentialité","Notifications","Centre d'aide","À propos de NOVA PAY").forEach{label->
   Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Panel).clickable{open()}.padding(16.dp),verticalAlignment=Alignment.CenterVertically){
    Icon(Icons.Rounded.Settings,null,tint=Gold);Spacer(Modifier.width(12.dp));Text(label,color=Silver,modifier=Modifier.weight(1f),fontSize=13.sp);Icon(Icons.Rounded.ChevronRight,null,tint=Muted)
   }
  }
  Button(onClick=onLogout,modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(14.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF48252B),contentColor=Color.White)){
   Icon(Icons.Rounded.Logout,null);Spacer(Modifier.width(8.dp));Text("Se déconnecter")
  }
  Notice()
 }
 if(editing) AlertDialog(onDismissRequest={editing=false},containerColor=Panel,title={Text("Modifier le profil",color=Gold)},text={
  Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
   OutlinedTextField(value=name,onValueChange={name=it;error=""},label={Text("Nom complet")},singleLine=true)
   OutlinedTextField(value=email,onValueChange={email=it;error=""},label={Text("Adresse e-mail")},singleLine=true)
   OutlinedTextField(value=phone,onValueChange={phone=it;error=""},label={Text("Téléphone")},singleLine=true)
   if(error.isNotBlank()) Text(error,color=Color(0xFFFF8A80),fontSize=12.sp)
  }
 },confirmButton={TextButton(onClick={
  if(name.isBlank()||!email.contains("@")||phone.isBlank()) error="Vérifie les trois champs."
  else {onProfileUpdated(user.copy(name=name.trim(),email=email.trim(),phone=phone.trim()));editing=false}
 }){Text("Enregistrer",color=Gold)}},dismissButton={TextButton(onClick={editing=false}){Text("Annuler",color=Muted)}})
 if(verifying) AlertDialog(onDismissRequest={verifying=false},containerColor=Panel,title={Text("Vérification du téléphone",color=Gold)},text={Text("Le service d'envoi de SMS n'est pas encore connecté. Ton numéro reste donc non vérifié. Nous ne pouvons pas confirmer un numéro sans code envoyé par un prestataire SMS.",color=Silver)},confirmButton={TextButton(onClick={verifying=false}){Text("Compris",color=Gold)}})
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
 Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Rounded.Shield,null,tint=Gold);Spacer(Modifier.width(8.dp));Text("Version 1.1 • Démonstration",color=Gold,fontWeight=FontWeight.Bold,fontSize=12.sp)}
 Spacer(Modifier.height(6.dp));Text("Cette carte est fictive : elle ne possède aucun numéro bancaire valide, aucun CVV et aucun réseau de paiement. Le solde, le plafond et les opérations sont simulés et stockés uniquement sur cet appareil. Aucun paiement réel n'est possible.",color=Muted,fontSize=11.sp,lineHeight=16.sp)
 }
}
